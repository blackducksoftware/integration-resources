@Grab(group = 'org.apache.httpcomponents', module = 'httpclient', version = '4.5.2')
@Grab(group = 'org.apache.httpcomponents', module = 'httpcore', version = '4.4.11')

import groovy.json.JsonSlurper
import org.apache.http.client.methods.HttpGet
import org.apache.http.client.methods.HttpUriRequest
import org.apache.http.impl.client.HttpClientBuilder

jsonSlurper = new JsonSlurper()
client = HttpClientBuilder.create().build()

def allRepoNames = new HashSet<String>()
def done = false
def page = 1
while (!done) {
    def get = getPublicRepos(page)
    def response = client.execute(get)
    def jsonResponse = jsonSlurper.parse(response.getEntity().getContent())
    if (0 == jsonResponse.size) {
        done = true
    } else {
        jsonResponse.each {
            def repoName = it['full_name']
            allRepoNames.add(repoName)
        }
        page++
    }
}

def reposWithNoRecentCommits = allRepoNames.findAll { repoName ->
    0 == getCommitCountForRepo(repoName)
}

reposWithNoRecentCommits.sort().each { println it}

private int getCommitCountForRepo(String repoName) {
    def recentCommitsRequest = getRecentCommitsForRepo(repoName)
    def commitsJson = getJsonResponse(recentCommitsRequest)

    if (commitsJson instanceof Map && commitsJson.containsKey('message')) {
        return 0
    }

    commitsJson.size
}

private HttpGet getPublicRepos(int page) {
    new HttpGet("https://api.github.com/orgs/blackducksoftware/repos?page=${page}&type=public&sort=updated&direction=desc")
}

private HttpGet getRecentCommitsForRepo(String repo) {
    new HttpGet("https://api.github.com/repos/${repo}/commits?since=2018-01-01T00:00:00Z")
}

private Object getJsonResponse(HttpUriRequest httpUriRequest) {
    httpUriRequest.addHeader('Accept', 'application/vnd.github.v3+json')
    httpUriRequest.addHeader('Authorization', "token ${System.getenv('GITHUB_AUTH_TOKEN')}")

    def response = client.execute(httpUriRequest)
    def json = jsonSlurper.parse(response.getEntity().getContent())

    json
}
