def projectDir = new File(request.outputDirectory, request.artifactId)
def process = new ProcessBuilder("mvn", "-B", "-f", new File(projectDir, "pom.xml").absolutePath, "spotless:apply")
        .directory(projectDir)
        .redirectErrorStream(true)
        .start()
println process.inputStream.text
def code = process.waitFor()
if (code != 0) {
    throw new RuntimeException("spotless:apply exited " + code)
}
