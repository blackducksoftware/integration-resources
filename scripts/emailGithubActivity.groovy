def githubActivity = new GithubActivity()
def emailer = new Emailer()

String githubActivityContent = githubActivity.computeGithubActivity(14)

String to = System.getenv('GITHUB_ACTIVITY_TO')
String from = System.getenv('GITHUB_ACTIVITY_FROM')
String subject = 'github activity'
String host = System.getenv('BD_EMAIL_HOST')

emailer.sendEmail(to, from, subject, host, githubActivityContent)
