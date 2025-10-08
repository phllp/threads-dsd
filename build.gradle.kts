plugins {
    application
    id("org.openjfx.javafxplugin") version "0.1.0"
}

group = "org.example"
version = "1.0-SNAPSHOT"

repositories {
    mavenCentral()
}

java {
    // use o JDK que você tiver (17, 19, 21…)
    toolchain { languageVersion.set(JavaLanguageVersion.of(17)) }
}

tasks.withType<JavaCompile>().configureEach {
    options.encoding = "UTF-8"
}
tasks.withType<Javadoc>().configureEach {
    options.encoding = "UTF-8"
}
tasks.withType<Test>().configureEach {
    // Para testes JUnit verem UTF-8
    jvmArgs("-Dfile.encoding=UTF-8")
}

dependencies {
    testImplementation(platform("org.junit:junit-bom:5.9.1"))
    testImplementation("org.junit.jupiter:junit-jupiter")
}

tasks.test {
    useJUnitPlatform()
}

application {
    mainClass.set("app.Main")
    applicationDefaultJvmArgs = listOf("-Dfile.encoding=UTF-8")

}

javafx {
    version = "21.0.5"
    modules = listOf("javafx.controls", "javafx.fxml")
}
