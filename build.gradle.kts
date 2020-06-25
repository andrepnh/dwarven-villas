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
    testImplementation("org.junit.jupiter:junit-jupiter-engine:5.6.2")
}

configure<JavaPluginConvention> {
    sourceCompatibility = JavaVersion.VERSION_14
}

tasks.withType<JavaCompile>().forEach {
    task -> task.options.compilerArgs.add("--enable-preview")
}

tasks.test {
    jvmArgs = listOf("--enable-preview")
}

application {
    applicationDefaultJvmArgs = listOf("--enable-preview")
    mainClass.set("andrepnh.dwarven.villas.Main")
}
