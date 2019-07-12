@Grab(group = 'org.apache.httpcomponents', module = 'httpclient', version = '4.5.2')
@Grab(group = 'org.apache.httpcomponents', module = 'httpcore', version = '4.4.11')

import groovy.json.JsonOutput
import org.apache.http.HttpEntity
import org.apache.http.HttpMessage
import org.apache.http.client.methods.HttpGet
import org.apache.http.client.methods.HttpPatch
import org.apache.http.entity.ContentType
import org.apache.http.entity.StringEntity
import org.apache.http.impl.client.HttpClientBuilder

def client = HttpClientBuilder.create().build()

def toKeep = []

def repoNames = []

repoNames.findAll { repoName ->
    !toKeep.contains(repoName)
}.each { repoName ->
    HttpPatch httpPatch = createPatch(repoName)
    def response = client.execute(httpPatch)
    println repoName + ' - ' + response.getStatusLine().getStatusCode()
    response.close()
}

private HttpPatch createPatch(String repoName) {
    HttpPatch httpPatch = new HttpPatch("https://api.github.com/repos/blackducksoftware/${repoName}")
    String patchJson = JsonOutput.toJson([name: repoName, archived: true])
    HttpEntity httpEntity = new StringEntity(patchJson, ContentType.create("application/json"))
    httpPatch.setEntity(httpEntity)
    addHeaders(httpPatch)

    httpPatch
}

private void addHeaders(HttpMessage httpMessage) {
    httpMessage.addHeader('Accept', 'application/vnd.github.v3+json')
    httpMessage.addHeader('Authorization', "token aadc6c9c2c6661385b84e8ac79924429330c6733")
}
