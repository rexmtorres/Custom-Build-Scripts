package com.rexmtorres.deliveryhelper

import com.android.build.gradle.api.BaseVariant
import org.gradle.api.NamedDomainObjectContainer
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.file.ConfigurableFileCollection
import org.gradle.external.javadoc.JavadocMemberLevel

class JavaDocSettings {
    String title
    JavadocMemberLevel javadocMemberLevel
    ConfigurableFileCollection additionalSourceFiles
    ConfigurableFileCollection additionalClasspathFiles
    List<String> excludedFiles
    File outputFile

    @Override
    String toString() {
        return title
    }
}

class Delivery {
    static Project project

    String name

    BaseVariant variant
    File outputFile
    File unsignedApkFile
    File proguardMapDir

    private JavaDocSettings javaDocSettings

    Delivery(final String name) {
        this.name = name
    }

    void javaDoc(final Closure javaDocClosure) {
        javaDocSettings = new JavaDocSettings()

        project.configure(javaDocSettings, javaDocClosure)
        if (javaDocClosure.hasProperty("title")) {
            println "  title: " + javaDocClosure.getProperty("title")
        }

    }
}

class DeliveryPlugin implements Plugin<Project> {
    private def resVersion = "v1"
    private def binZip = "/res/bin_${resVersion}.zip"
    private def cacheLoc = "${System.getProperty("user.home")}/.gradle/rmtcache/com.rexmtorres.deliveryhelper/${resVersion}"

    void apply(Project project) {
        Delivery.project = project

        NamedDomainObjectContainer<Delivery> deliveryContainer = project.container(Delivery)

        project.extensions.add("deliveries", deliveryContainer)

        project.task('hello') {
            doLast {
                def deliveries = project.extensions.getByName("deliveries")

                deliveries.all { del ->
                    println "${del.name} {"

                    if (del.variant != null) {
                        println "    variant = ${del.variant.name}"
                    }

                    if (del.outputFile != null) {
                        println "    outputFile = ${del.outputFile}"
                    }

                    if (del.unsignedApkFile != null) {
                        println "    unsignedApkFile = ${del.unsignedApkFile}"
                    }

                    if (del.proguardMapDir != null) {
                        println "    proguardMapDir = ${del.proguardMapDir}"
                    }

                    if (del.javaDocSettings != null) {
                        println "    javaDoc = ${del.javaDocSettings}"
                    }

                    println "}"
                }

//                println 'Hello from GreetingPlugin'
//
//                def resUri = getClass().getResource(binZip)
//                println "URL: $resUri"
//
//                def cacheRoot = new File("${cacheLoc}")
//
//                if (!cacheRoot.exists()) {
//                    cacheRoot.mkdirs()
//                }
//
//                def cachedBin = new File(cacheRoot, "bin.zip")
//
//                if (!cachedBin.exists()) {
//                    println "$cachedBin does not exist."
//
//                    resUri.withInputStream {
//                        cachedBin << it
//                    }
//                } else {
//                    println "$cachedBin already exists."
//                }
//
//                project.copy {
//                    from project.zipTree { cachedBin }
//                    into cacheRoot
//                }
            }
        }
    }
}
