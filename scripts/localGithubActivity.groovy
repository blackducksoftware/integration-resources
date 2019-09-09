@Grab(group = 'org.apache.httpcomponents', module = 'httpclient', version = '4.5.2')
@Grab(group = 'org.apache.httpcomponents', module = 'httpcore', version = '4.4.11')

def githubActivity = new GithubActivity()

String githubActivityContent = githubActivity.computeGithubActivity()

println githubActivityContent
String teamActivityFilename = "C:\\Users\\ekerwin\\Documents\\team_activity_${System.currentTimeMillis()}.html"
new File(teamActivityFilename) << githubActivityContent
"C:\\Program Files (x86)\\Google\\Chrome\\Application\\chrome.exe ${teamActivityFilename}".execute()

println content
String teamActivityFilename = "~/atom_workspace/swipedir/code_quality_${System.currentTimeMillis()}.html"
new File(teamActivityFilename) << content
"/Applications/Vivaldi.app/Contents/MacOS/Vivaldi ${teamActivityFilename}".execute()

