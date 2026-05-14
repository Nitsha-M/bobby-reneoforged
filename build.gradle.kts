plugins {
	id("java")
	id("net.neoforged.moddev") version "2.0.141"
	id("com.github.breadmoirai.github-release") version "2.2.12"
	id("com.matthewprenger.cursegradle") version "1.4.0"
	id("com.modrinth.minotaur") version "2.+"
	id("elect86.gik") version "0.0.4"
}

val modVersion: String by project
val mavenGroup: String by project
group = mavenGroup

val minecraftVersion: String by project
val clothConfigVersion: String by project
val neoForgeVersion: String by project

version = "$modVersion+mc$minecraftVersion"

sourceSets {
	val main = main.get()
	create("sodium06") {
		compileClasspath += main.compileClasspath
		compileClasspath += main.output
		main.runtimeClasspath += output
		tasks.named<Jar>("jar") { from(output) }
	}

	create("embeddium") {
		compileClasspath += main.compileClasspath
		compileClasspath += main.output
		main.runtimeClasspath += output
		tasks.named<Jar>("jar") { from(output) }
	}
}

neoForge {
	version = neoForgeVersion

	mods {
		create("bobby") {
			sourceSet(sourceSets.main.get())
			sourceSet(sourceSets.named("sodium06").get())
			sourceSet(sourceSets.named("embeddium").get())
		}
	}

	runs {
		configureEach {
			systemProperty("forge.logging.markers", "REGISTRIES")
			systemProperty("forge.logging.console.level", "debug")
		}
		create("client") {
			client()
		}
		create("server") {
			server()
		}
	}
}

dependencies {
	val configurateVersion: String by project
	val geantyrefVersion: String by project
	val hoconVersion: String by project
	val sodium06Version: String by project
	val embeddiumVersion: String by project
	val starlightVersion: String by project

	implementation("org.spongepowered:configurate-core:$configurateVersion")
	implementation("org.spongepowered:configurate-hocon:$configurateVersion")
	jarJar("org.spongepowered:configurate-core:[$configurateVersion,)")
	jarJar("org.spongepowered:configurate-hocon:[$configurateVersion,)")
	jarJar("io.leangen.geantyref:geantyref:[$geantyrefVersion,)")
	jarJar("com.typesafe:config:[$hoconVersion,)")
	jarJar("net.kyori:option:[1.1.0,)")

	"additionalRuntimeClasspath"("org.spongepowered:configurate-core:$configurateVersion")
	"additionalRuntimeClasspath"("org.spongepowered:configurate-hocon:$configurateVersion")
	"additionalRuntimeClasspath"("io.leangen.geantyref:geantyref:$geantyrefVersion")
	"additionalRuntimeClasspath"("com.typesafe:config:$hoconVersion")
	"additionalRuntimeClasspath"("net.kyori:option:1.1.0")

	"sodium06CompileOnly"("maven.modrinth:sodium:$sodium06Version")
	compileOnly("maven.modrinth:starlight:$starlightVersion")
	"embeddiumCompileOnly"("maven.modrinth:embeddium:$embeddiumVersion")

	implementation("me.shedaniel.cloth:cloth-config-neoforge:$clothConfigVersion")
}

tasks.processResources {
	inputs.property("version", project.version)
	inputs.property("clothConfigVersion", clothConfigVersion)

	filesMatching("META-INF/neoforge.mods.toml") {
		expand(mutableMapOf(
			"version" to project.version,
			"clothConfigVersion" to clothConfigVersion
		))
	}
}

tasks.withType<JavaCompile> {
	options.encoding = "UTF-8"
	sourceCompatibility = "21"
	targetCompatibility = "21"
}

tasks.withType<AbstractArchiveTask> {
	val archivesBaseName: String by project
	archiveBaseName.set(archivesBaseName)
}

tasks.jar {
	from("LICENSE.md")
}

repositories {
	maven("https://maven.shedaniel.me") {
		content {
			includeGroup("me.shedaniel.cloth")
		}
	}
	maven("https://maven.terraformersmc.com/releases") {
		content {
			includeGroup("com.terraformersmc")
		}
	}
	maven("https://api.modrinth.com/maven") {
		content {
			includeGroup("maven.modrinth")
		}
	}
}
