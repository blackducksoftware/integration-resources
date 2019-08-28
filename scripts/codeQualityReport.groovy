@Grab(group = 'org.apache.httpcomponents', module = 'httpclient', version = '4.5.2')
@Grab(group = 'org.apache.httpcomponents', module = 'httpcore', version = '4.4.11')

import groovy.json.JsonSlurper
import org.apache.http.HttpMessage
import org.apache.http.HttpResponse
import org.apache.http.client.methods.HttpGet
import org.apache.http.impl.client.HttpClientBuilder

import java.net.URLEncoder
import java.time.Instant
import java.time.ZoneOffset
import java.time.format.DateTimeFormatter
import java.time.temporal.ChronoUnit

def daysToInclude = 14

List<String> repos = new File('repositories.txt').readLines()
Map<String, String> repoNamesToSimpleNames = createRepoNamesToSimpleNames(repos)

List<String> aliases = new File('sonar_alias.txt').readLines()
Map<String, String> repoNamesToAlias = createRepoNamesToAlias(aliases)

def jsonSlurper = new JsonSlurper()
def client = HttpClientBuilder.create().build()

DateTimeFormatter iso8601 = DateTimeFormatter.ofPattern('yyyy-MM-dd\'T\'HH:mm:ssX')
DateTimeFormatter easternClock = DateTimeFormatter.ofPattern('yyyy-MM-dd hh:mm:ss a')
Instant now = Instant.now()
Instant before = now.minus(daysToInclude, ChronoUnit.DAYS).truncatedTo(ChronoUnit.DAYS)
def since = before.atZone(ZoneOffset.UTC).format(iso8601)
def sinceEastern = before.atZone(TimeZone.getTimeZone("America/New_York").toZoneId()).format(easternClock)

def simpleNameToStatus = new HashMap<String, RepoStatus>()

repoNamesToSimpleNames.each { repoName, simpleName ->
    def done = false
    RepoStatus repoStatus = new RepoStatus(repoName)

    while (!done) {
        // Get and process builds
        def buildsResponse = getBuildsForRepo(client.&execute, repoName, repoStatus.totalBuildsForRepo)
        def jsonbuildsResponse = jsonSlurper.parse(buildsResponse.getEntity().getContent())

        if (jsonbuildsResponse.'@type' == 'error' || jsonbuildsResponse.'@pagination'.count == 0) {
            done = true
        } else {
            for ( build in jsonbuildsResponse.builds ) {
                done = (build.started_at != null && Instant.parse(build.started_at).isBefore(before))
                if ( done ) break
                if (build.branch?.name != 'master' || build.state == 'cancelled') continue

                if (!repoStatus.latestBuildStatus) {
                    repoStatus.latestBuildStatus = build.state
                }

                if (build.state == 'passed') {
                    repoStatus.totalBuildsPassed++
                }

                repoStatus.totalBuildsForRepo++
            }
        }
    }
    if (repoStatus.totalBuildsForRepo > 0) {
        // Get Sonar quality gate
        def projectKeyResponse = getSonarProjectKey(client.&execute, repoName, repoNamesToAlias)
        def jsonProjectKeyResponse = jsonSlurper.parse(projectKeyResponse.getEntity().getContent())

        if (jsonProjectKeyResponse.components != null && jsonProjectKeyResponse.components.size() > 0) {
            def projectKey = jsonProjectKeyResponse.components[0].key
            def qualityGateResponse = getSonarQualityGateStatus(client.&execute, projectKey)
            def jsonQualityGateResponse = jsonSlurper.parse(qualityGateResponse.getEntity().getContent())
            if (jsonQualityGateResponse?.projectStatus?.status) {
                repoStatus.sonarStatus = parseStatus(jsonQualityGateResponse.projectStatus.status)
            }
        }

        simpleNameToStatus.put(simpleName, repoStatus)
    }
}

StringBuilder content = new StringBuilder()

content.append '<html>\n'
content.append("""<head>
\t<style type="text/css">
\t\tth {
\t\t\tfont-family: courier;
\t\t\tsize: 10pt;
\t\t\tpadding: 0 15px 0 15px;
\t\t}
\t\ttd,span {
\t\t\tfont-family: courier;
\t\t\tsize: 10pt;
\t\t\tpadding: 0 15px 0 15px;
\t\t}
\t</style>
</head>""")
content.append '<body>\n'
content.append "<span>In the last ${daysToInclude} days</span><br />\n"
content.append '<br />\n'
content.append '<table>\n'
simpleNameToStatus.toSorted{ it.key }.each { simpleName, repoStatus ->
    content.append "<tr><th colspan=2 style='text-align: left;'>${simpleName}</th></tr>\n"
    content.append "<tr><td style='text-align: right;'>Quality gate:</td><td>${repoStatus.sonarStatus}</td></tr>\n"
    content.append "<tr><td style='text-align: right;'>Latest build:</td><td>${repoStatus.latestBuildStatus.toUpperCase()}</td></tr>\n"
    content.append "<tr><td style='text-align: right;'>History:</td><td>${repoStatus.getPassPercentage()}% of ${repoStatus.totalBuildsForRepo} builds have passed</td></tr>\n"
    content.append '<tr><td colspan=2>&nbsp;</td></tr>\n'
}
content.append '</table>\n'

content.append '<table>\n'
simpleNameToStatus.toSorted{ it.key }.each { simpleName, repoStatus ->
    content.append '<tr>\n'
    content.append "<td>${simpleName}</td><td><a href=${repoStatus.gitHubUrl}>${repoStatus.gitHubUrl}</a></td>\n"
    content.append '</tr>\n'
}
content.append '</table>\n'

content.append '<br />\n'
content.append '<br />\n'

content.append "<span>Since: ${since} (${sinceEastern})</span><br />\n"

content.append '</body></html>\n'

println content
String teamActivityFilename = "~/atom_workspace/swipedir/code_quality_${System.currentTimeMillis()}.html"
new File(teamActivityFilename) << content
"/Applications/Vivaldi.app/Contents/MacOS/Vivaldi ${teamActivityFilename}".execute()

private Map<String, String> createRepoNamesToSimpleNames(List<String> repos) {
    repos.collectEntries { repoLine ->
        String[] tokens = repoLine.split("\\|")
        String name = tokens[0]
        String value = name.substring(name.lastIndexOf('/') + 1)
        if (tokens.size() > 1) {
            value = tokens[1]
        }

        [name, value]
    }
}

private Map<String, String> createRepoNamesToAlias(List<String> aliases) {
    aliases.collectEntries { repoLine ->
        String[] tokens = repoLine.split("\\|")
        [tokens[0], tokens[1]]
    }
}

private HttpResponse getBuildsForRepo(Closure clientExecutor, String repo, int offset) {
    def get = new HttpGet("https://api.travis-ci.org/repo/${URLEncoder.encode(repo)}/builds?offset=${offset}")
    get.addHeader('Travis-API-Version', '3')
    get.addHeader('Authorization', "token ${System.getenv('TRAVIS_AUTH_TOKEN')}")

    clientExecutor.call(get)
}

private HttpResponse getSonarProjectKey(Closure clientExecutor, String repo, Map<String, String> repoNamesToAlias) {
    def repoNameWithoutOrg = repo.substring(repo.lastIndexOf('/') + 1)
    def projectName = repoNameWithoutOrg
    if (repoNamesToAlias.get(repoNameWithoutOrg)) {
        projectName = repoNamesToAlias.get(repoNameWithoutOrg)
    }
    def get = new HttpGet("https://sonarcloud.io/api/components/search?qualifiers=TRK&organization=black-duck-software&q=${URLEncoder.encode(projectName)}")

    clientExecutor.call(get)
}

private HttpResponse getSonarQualityGateStatus(Closure clientExecutor, String projectKey) {
    def get = new HttpGet("https://sonarcloud.io/api/qualitygates/project_status?projectKey=${URLEncoder.encode(projectKey)}");

    clientExecutor.call(get)
}

private String parseStatus(String status) {
    if ('OK'.equals(status)) {
        return 'PASSING'
    }
    if ('ERROR'.equals(status)) {
        return 'FAILING'
    }
    return status
}

class RepoStatus {
    String gitHubUrl
    String sonarStatus = 'NOT FOUND'
    String latestBuildStatus
    int totalBuildsForRepo = 0
    int totalBuildsPassed = 0

    public RepoStatus(String repoName) {
        gitHubUrl = "https://github.com/${repoName}"
    }

    def getPassPercentage() {
        ((totalBuildsPassed / totalBuildsForRepo)*100).round(0)
    }
}
