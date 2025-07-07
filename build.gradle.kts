plugins {
    id("java")
}

group = "org.example"
version = "1.0-SNAPSHOT"

repositories {
    mavenCentral()
}

dependencies {
    // Jakarta WebSocket API (explicit dependency)
    implementation("jakarta.websocket:jakarta.websocket-api:2.1.0")
    
    // WebSocket-Client: Tyrus (enthält Implementation)
    implementation("org.glassfish.tyrus.bundles:tyrus-standalone-client:2.1.3")

    // JSON-Binding (Yasson + API)
    implementation("jakarta.json.bind:jakarta.json.bind-api:3.0.0")
    implementation("org.eclipse:yasson:3.0.3")

    // Test
    testImplementation(platform("org.junit:junit-bom:5.10.0"))
    testImplementation("org.junit.jupiter:junit-jupiter")
}

tasks.test {
    useJUnitPlatform()
}
