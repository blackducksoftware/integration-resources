import groovy.io.FileType

NEW_LINE = System.getProperty("line.separator")

def workingDirectoryName = System.getProperty('user.dir')
def workingDirectory = new File(workingDirectoryName)
println "working directory: ${workingDirectoryName}"

def allFiles = []
workingDirectory.eachFileRecurse (FileType.FILES) { file ->
  def contents = file.text
  if (contents.contains('import org.junit.Test')) {
    allFiles << file
    println "Adding ${file.name} for cleaning."
  }
}

allFiles.each {
  updateFile(it)

  def contents = it.text
  it.write(contents.replaceAll(NEW_LINE + NEW_LINE + NEW_LINE, NEW_LINE + NEW_LINE))

  println "Done cleaning ${it.name}."
}

private void updateFile(File fileToFix) {
  def foundPackageDeclaration = false

  File tempFile = new File('/tmp/tempDestination')
  tempFile.withWriter { w ->
    def previousLine = null
    fileToFix.eachLine { line ->
      def cleanedLine = line
      if (!cleanedLine.startsWith('import org.junit.Assert') && !cleanedLine.startsWith('import static org.junit.Assert')) {
        if (cleanedLine.startsWith('package ')) {
          foundPackageDeclaration = true
        } else {
          if (foundPackageDeclaration && !line?.trim()) {
            cleanedLine = NEW_LINE + 'import static org.junit.jupiter.api.Assertions.*;' + NEW_LINE
            foundPackageDeclaration = false
          } else {
            cleanedLine = cleanedLine.replace('import org.junit.Test', 'import org.junit.jupiter.api.Test')
            cleanedLine = cleanedLine.replace('Assert.', '')
          }
        }
        w.write(cleanedLine + NEW_LINE)
      }
    }
  }

  tempFile.renameTo(fileToFix)
}
