import groovy.transform.Field
import org.artifactory.fs.ItemInfo
import org.artifactory.repo.RepoPath
import org.artifactory.repo.RepoPathFactory
import org.artifactory.request.Request

executions {
    /**
     * This will attempt retrieve a property from a repoPath and return it as plain text
     *
     * This can be triggered with the following curl command:
     * curl -X GET -u admin:password "http://ARTIFACTORY_SERVER/artifactory/api/plugins/execute/getProperty?params=repoPath={repoPath}|propertyKey={propertyKey}}"
     * 
     * An example would be getting the version of detect to use:
     * curl -X GET "http://test-repo.blackducksoftware.com/artifactory/api/plugins/execute/getProperty?params=repoPath=bds-integrations-release/com/blackducksoftware/integration/hub-detect|propertyKey=DETECT_LATEST"
     **/
    getProperty(httpMethod: 'GET', users:["anonymous"]) { params ->
        String propertyKey = null
        String providedRepoPath = null
        params.each { key, values -> 
            if (key.equals("propertyKey") && values.size() > 0) {
                propertyKey = values.get(0)
            } else if (key.equals("repoPath") && values.size() > 0) {
                providedRepoPath = values.get(0)
            }
        }

        if (providedRepoPath != null && propertyKey != null) {
            final RepoPath repoPath = RepoPathFactory.create(providedRepoPath)
            final String propertyValue = repositories.getProperty(repoPath, propertyKey);
            message = propertyValue
        } else {
            message = "Invalid Parameters"
        }
    }
}