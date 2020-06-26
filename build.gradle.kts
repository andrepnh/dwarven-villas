plugins {
    java
    application
}

group = "andrepnh"
version = "0.1"

repositories {
    mavenCentral()
}

dependencies {
    implementation("com.google.guava:guava:29.0-jre")
    implementation("io.vavr:vavr:0.10.3")

    testImplementation("org.assertj:assertj-core:3.16.1")
    testImplementation("org.junit.jupiter:junit-jupiter-params:5.6.2")
    testRuntimeOnly("org.junit.jupiter:junit-jupiter-engine:5.6.2")
}

configure<JavaPluginConvention> {
    sourceCompatibility = JavaVersion.VERSION_14
}

tasks.withType<JavaCompile>().forEach {
    task -> task.options.compilerArgs.add("--enable-preview")
}

tasks.test {
    useJUnitPlatform()
    jvmArgs = listOf("--enable-preview")
}

application {
    applicationDefaultJvmArgs = listOf("--enable-preview")
    mainClass.set("andrepnh.dwarven.villas.Main")
}
