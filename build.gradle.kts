plugins {
    id("java")
}

group = "org.example"
version = "1.0-SNAPSHOT"

repositories {
    mavenCentral()
}

dependencies {
    testImplementation(platform("org.junit:junit-bom:5.10.0"))
    testImplementation("org.junit.jupiter:junit-jupiter")
    // Tyrus als Client-Implementierung (liefert auch die API mit)
    implementation("org.glassfish.tyrus.bundles:tyrus-standalone-client:2.1.3")
    // Jakarta WebSocket API (nur API)
    implementation("jakarta.websocket:jakarta.websocket-api:2.1.1")
      // Jakarta JSON Binding (Jsonb) für JSON-Handling
    implementation("jakarta.json.bind:jakarta.json.bind-api:3.0.0")
    implementation("org.eclipse:yasson:3.0.3") // Yasson = Jsonb-Implementierung
}

tasks.test {
    useJUnitPlatform()
}