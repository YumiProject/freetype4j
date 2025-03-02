plugins {
	`java-library`
	alias(libs.plugins.licenser)
}

group = "dev.yumi.bindings"
version = "1.0.0-alpha.1-SNAPSHOT"
val javaVersion = 23

repositories {
	mavenCentral()
}

dependencies {
	api(libs.jetbrains.annotations)

	testImplementation(platform(libs.junit.bom))
	testImplementation(libs.junit.jupiter)
	testRuntimeOnly(libs.junit.launcher)
}

java {
	toolchain {
		languageVersion.set(JavaLanguageVersion.of(javaVersion))
	}

	withSourcesJar()
	withJavadocJar()

	testResultsDir.set(layout.buildDirectory.dir("junit-xml"))
}

tasks.withType<JavaCompile>().configureEach {
	options.encoding = "UTF-8"
	options.isDeprecation = true
	options.release.set(javaVersion)
}

tasks.withType<Javadoc>().configureEach {
	options {
		this as StandardJavadocDocletOptions

		addStringOption("Xdoclint:all,-missing", "-quiet")
	}
}

license {
	rule(file("codeformat/HEADER"))
}

tasks.withType<Test>().configureEach {
	// Using JUnitPlatform for running tests
	useJUnitPlatform()
	jvmArgs(
		"--enable-native-access=${project.group}.${project.name}",
	)

	testLogging {
		events("passed")
	}
}
