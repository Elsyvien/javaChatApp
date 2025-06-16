plugins {
    id("java")
}

group = "org.example"
version = "1.0-SNAPSHOT"

repositories {
    mavenCentral()
}

dependencies {
    // WebSocket-Client: Tyrus (enthält API!)
    implementation("org.glassfish.tyrus.bundles:tyrus-standalone-client:2.1.3")

    // JSON-Binding (Yasson + API)
    implementation("jakarta.json.bind:jakarta.json.bind-api:3.0.0")
    implementation("org.eclipse:yasson:3.0.3")

    // Test
    testImplementation("org.junit:junit-bom:5.10.0")
    testImplementation("org.junit.jupiter:junit-jupiter")
}

tasks.test {
    useJUnitPlatform()
}
