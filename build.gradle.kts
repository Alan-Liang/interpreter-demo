plugins {
    kotlin("jvm") version "1.7.10"
    antlr
    application
}

repositories {
    mavenCentral()
}

dependencies {
    implementation(platform("org.jetbrains.kotlin:kotlin-bom"))
    implementation("org.jetbrains.kotlin:kotlin-stdlib-jdk8")
    implementation("org.jetbrains.kotlin:kotlin-reflect:1.7.10")
    testImplementation("org.jetbrains.kotlin:kotlin-test")
    testImplementation("org.jetbrains.kotlin:kotlin-test-junit")
    antlr("org.antlr:antlr4:4.10.1")
}

application {
    mainClass.set("org.anotherkit.interpreter.AppKt")
}

kotlin.target.compilations.create("antlrBase") {
    defaultSourceSet {
        kotlin.srcDir("src/antlr-base/kotlin")
    }
}

sourceSets.create("antlr") {
    java.srcDir("src/antlr/java")
    compileClasspath = compileClasspath
            .plus(sourceSets.main.get().compileClasspath)
            .plus(tasks.getByName("compileAntlrBaseKotlin").outputs.files)
//    println(compileClasspath.asPath)
}
sourceSets.getByName("antlrBase") {
    compileClasspath = compileClasspath
            .plus(sourceSets.main.get().compileClasspath)
}
sourceSets.main {
    val additionalClasspath = tasks.getByName("compileAntlrJava").outputs.files
        .plus(tasks.getByName("compileAntlrBaseJava").outputs.files)
        .plus(tasks.getByName("compileAntlrBaseKotlin").outputs.files)
    compileClasspath = compileClasspath.plus(additionalClasspath)
    runtimeClasspath = runtimeClasspath.plus(additionalClasspath)
}

tasks.compileKotlin {
     dependsOn("compileAntlrJava")
}

tasks.getByName("compileAntlrJava") {
    dependsOn("compileAntlrBaseKotlin", "generateAntlrGrammarSource")
}

tasks.getByName("generateAntlrGrammarSource") {
    this as AntlrTask
    arguments = arguments + listOf("-visitor", "-package", "org.anotherkit.interpreter")
}
