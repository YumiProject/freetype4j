import de.undercouch.gradle.tasks.download.Download

plugins {
	`java-library`
	`maven-publish`
	alias(libs.plugins.licenser)
	alias(libs.plugins.download)
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

tasks.jar {
	val archivesName = base.archivesName.get()
	from(file("LICENSE")) {
		rename { "${it}_${archivesName}" }
	}
}

license {
	rule(file("codeformat/HEADER"))
}

val firaCodeZipFile = "Fira_Code_v6.2.zip"
val downloadFiraCodeTask = tasks.register<Download>("downloadFiraCode") {
	src("https://github.com/tonsky/FiraCode/releases/download/6.2/${firaCodeZipFile}")
	dest(layout.buildDirectory.dir("test"))
	overwrite(false)
}

val extractFiraCodeTask = tasks.register<Copy>("extractFiraCode") {
	dependsOn(downloadFiraCodeTask)
	from(zipTree(layout.buildDirectory.file("test/${firaCodeZipFile}")).matching { include("**/*.ttf") })
	into(layout.buildDirectory.dir("test"))
}

tasks.withType<Test>().configureEach {
	dependsOn(extractFiraCodeTask)

	// Using JUnitPlatform for running tests
	useJUnitPlatform()
	jvmArgs(
		"--enable-native-access=${project.group}.${project.name}",
	)

	testLogging {
		events("passed")
	}
}

// Setup publishing of artifacts.
publishing {
	repositories {
		mavenLocal()
	}

	publications {
		create<MavenPublication>("mavenJava") {
			from(components["java"])

			pom {
				url = "https://github.com/YumiProject/freetype4j"

				organization {
					name = "Yumi Project"
					url = "https://yumi.dev/"
				}

				developers {
					developer {
						name = "Yumi Project Development Team"
						email = "infra@yumi.dev"
					}
				}

				licenses {
					license {
						name = "Mozilla Public License Version 2.0"
						url = "https://www.mozilla.org/en-US/MPL/2.0/"
					}
				}

				scm {
					url = "https://github.com/YumiProject/freetype4j"
					connection = "scm:git:git://github.com/YumiProject/freetype4j"
					developerConnection = "scm:git:ssh://github.com:YumiProject/freetype4j"
				}
			}

			afterEvaluate {
				artifactId = base.archivesName.get()

				pom {
					description = "Modern Java Bindings for the FreeType library."
				}
			}
		}
	}
}
