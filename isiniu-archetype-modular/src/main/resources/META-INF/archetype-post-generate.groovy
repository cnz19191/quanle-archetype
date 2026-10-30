def projectDir = new File(request.outputDirectory, request.artifactId)
def mavenHome = System.getProperty("maven.home")
def mvn = mavenHome ? new File(mavenHome, "bin/mvn").absolutePath : "mvn"
def command = [mvn, "-B", "-f", new File(projectDir, "pom.xml").absolutePath]
def repo = System.getProperty("maven.repo.local")
if (repo) {
    command.add("-Dmaven.repo.local=" + repo)
}
command.add("spotless:apply")
def process = new ProcessBuilder(command)
        .directory(projectDir)
        .redirectErrorStream(true)
        .start()
println process.inputStream.text
def code = process.waitFor()
if (code != 0) {
    throw new RuntimeException("spotless:apply exited " + code)
}
