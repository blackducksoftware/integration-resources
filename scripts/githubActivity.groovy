@Grab(group = 'org.apache.httpcomponents', module = 'httpclient', version = '4.5.2')
@Grab(group = 'org.apache.httpcomponents', module = 'httpcore', version = '4.4.11')

import groovy.json.JsonSlurper
import org.apache.http.HttpMessage
import org.apache.http.client.methods.HttpGet
import org.apache.http.impl.client.HttpClientBuilder

import java.time.Instant
import java.time.ZoneOffset
import java.time.format.DateTimeFormatter
import java.time.temporal.ChronoUnit

def daysToInclude = 7

List<String> knownUsers = new File('known_users.txt').readLines()
Map<String, String> emailsToUsers = createEmailsToUsers(knownUsers)

List<String> repos = new File('repositories.txt').readLines()
Map<String, String> repoNamesToSimpleNames = createRepoNamesToSimpleNames(repos)

def jsonSlurper = new JsonSlurper()
def client = HttpClientBuilder.create().build()

DateTimeFormatter iso8601 = DateTimeFormatter.ofPattern('yyyy-MM-dd\'T\'HH:mm:ssX')
DateTimeFormatter isoShort = DateTimeFormatter.ofPattern('MM/dd')
DateTimeFormatter easternClock = DateTimeFormatter.ofPattern('yyyy-MM-dd hh:mm:ss a')
Instant now = Instant.now()
Instant before = now.minus(daysToInclude, ChronoUnit.DAYS).truncatedTo(ChronoUnit.DAYS)
def since = before.atZone(ZoneOffset.UTC).format(iso8601)
def until = now.atZone(ZoneOffset.UTC).format(iso8601)
def shortSince = before.atZone(ZoneOffset.UTC).format(isoShort)
def shortUntil = now.atZone(ZoneOffset.UTC).format(isoShort)
def sinceEastern = before.atZone(TimeZone.getTimeZone("America/New_York").toZoneId()).format(easternClock)
def untilEastern = now.atZone(TimeZone.getTimeZone("America/New_York").toZoneId()).format(easternClock)

def repoToEmails = new HashMap<String, Set<String>>()

repoNamesToSimpleNames.each { repoName, simpleName ->
    def done = false
    def page = 1
    while (!done) {
        def get = getCommitsForRepo(repoName, page, since, until)
        def response = client.execute(get)
        def jsonResponse = jsonSlurper.parse(response.getEntity().getContent())
        if (0 == jsonResponse.size()) {
            done = true
        } else {
            Set<String> emails = getEmails(jsonResponse, emailsToUsers)
            if (!emails.empty) {
                if (!repoToEmails.containsKey(repoName)) {
                    repoToEmails.put(repoName, new HashSet<>())
                }
                repoToEmails.get(repoName).addAll(emails)
            }
            page++
        }
    }
}

def reposWithCommits = new HashSet<String>()
def emailToRepos = new HashMap<String, Set<String>>()
repoToEmails.each { repo, emails ->
    reposWithCommits.add(repo)
    emails.each { email ->
        if (!emailToRepos.containsKey(email)) {
            emailToRepos.put(email, new HashSet<>())
        }
        emailToRepos.get(email).add(repo)
    }
}

StringBuilder content = new StringBuilder()

content.append '<html>\n'
content.append("""<head>
\t<style type="text/css">
\t\ttd,span {
\t\t\tfont-family: courier;
\t\t\tsize: 10pt;
\t\t}
\t\t.paddingBetweenCols {
\t\t\tpadding:0 15px 0 15px;
\t\t}
\t</style>
</head>""")
content.append '<body>\n'
content.append "<span>From ${shortSince} to ${shortUntil}</span><br />\n"
content.append '<br />\n'
content.append '<table>\n'
emailToRepos.keySet().toSorted { a, b ->
    a <=> b
}.each { name ->
    content.append "<tr><td class=\"paddingBetweenCols\"><b>${name}</b></td>\n"
    content.append '<td class=\"paddingBetweenCols\">' + emailToRepos.get(name).collect { repoNamesToSimpleNames.get(it) }.toSorted { a, b -> a <=> b}.join(', ') + '</td></tr>\n'
    content.append '<tr><td>&nbsp</td></tr>\n'
}
content.append '</table>\n'

content.append '<table>\n'
reposWithCommits.toSorted { a, b ->
    repoNamesToSimpleNames.get(a) <=> repoNamesToSimpleNames.get(b)
}.each { repo ->
    content.append '<tr>\n'
    content.append "<td>${repoNamesToSimpleNames.get(repo)}</td><td><a href=\"https://github.com/${repo}\">${repo}</a></td>\n"
    content.append '</tr>\n'
}
content.append '</table>\n'

content.append '<br />\n'
content.append '<br />\n'

content.append "<span>Since: ${since} (${sinceEastern})</span><br />\n"
content.append "<span>Until: ${until} (${untilEastern})</span><br />\n"

content.append '</body></html>\n'

println content
String teamActivityFilename = "C:\\Users\\ekerwin\\Documents\\team_activity_${System.currentTimeMillis()}.html"
new File(teamActivityFilename) << content
"C:\\Program Files (x86)\\Google\\Chrome\\Application\\chrome.exe ${teamActivityFilename}".execute()

private Map<String, String> createEmailsToUsers(List<String> knownUsers) {
    Map<String, String> map = [:]

    knownUsers.each { line ->
        String[] tokens = line.split("\\|")
        String name = tokens[0]
        tokens[1..-1].each { token ->
            map.put(token, name)
        }
    }

    map
}

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

private HttpGet getCommitsForRepo(String repo, int page, String since, String until) {
    HttpGet get = new HttpGet("https://api.github.com/repos/${repo}/commits?page=${page}&since=${since}&until=${until}")
    addHeaders(get)

    get
}

private void addHeaders(HttpMessage httpMessage) {
    httpMessage.addHeader('Accept', 'application/vnd.github.v3+json')
    httpMessage.addHeader('Authorization', "token ${System.getenv('GITHUB_AUTH_TOKEN')}")
}

private Set<String> getEmails(def jsonResponse, Map<String, String> emailToUser) {
    Set<String> emails = new HashSet<String>()
    jsonResponse.each {
        def email = it['commit']['author']['email']
        if (email && !email.startsWith('serv-builder')) {
            if (emailToUser.containsKey(email)) {
                emails.add(emailToUser.get(email))
            } else {
                emails.add(email)
            }
        }
    }

    return emails
}
