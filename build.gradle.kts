  plugins {
	java
	id("org.springframework.boot") version "3.3.5"
	id("io.spring.dependency-management") version "1.1.6"
}

group = "com.gradingsystem"
version = "0.0.1-SNAPSHOT"

java {
	toolchain {
		languageVersion = JavaLanguageVersion.of(21)
	}
}

repositories {
	mavenCentral()
}

dependencies {
        //  Parsing Jackson
        implementation ("com.fasterxml.jackson.core:jackson-databind:2.15.1")

        implementation ("com.theokanning.openai-gpt3-java:client:0.17.0")     // OpenAI API for grading
        implementation ("com.theokanning.openai-gpt3-java:service:0.17.0")     // OpenAI API for grading
        implementation ("com.google.cloud:google-cloud-document-ai:2.57.0")   // Google Document AI
        implementation ("org.apache.pdfbox:pdfbox:2.0.29")                   // PDF Processing
        implementation ("org.apache.poi:poi-ooxml:5.2.3")                    // Word Document Processing
        
        // Plagiarism
        implementation ("org.apache.lucene:lucene-core:8.11.2")
        implementation ("org.apache.lucene:lucene-analyzers-common:8.11.2")

        // Natural Language Processing
        implementation ("org.apache.opennlp:opennlp-tools:2.5.0")

        //  Lombok Dependency
        implementation ("org.projectlombok:lombok:1.18.36")
        compileOnly ("org.projectlombok:lombok:1.18.36")
        annotationProcessor ("org.projectlombok:lombok:1.18.36")

	implementation("org.springframework.boot:spring-boot-starter-data-jpa")
	implementation("org.springframework.boot:spring-boot-starter-thymeleaf")
	implementation("org.springframework.boot:spring-boot-starter-web")
        implementation("org.springframework.boot:spring-boot-starter-tomcat")
	runtimeOnly("com.mysql:mysql-connector-j")
	testImplementation("org.springframework.boot:spring-boot-starter-test")
	testRuntimeOnly("org.junit.platform:junit-platform-launcher")
}

tasks.withType<Test> {
	useJUnitPlatform()
}
