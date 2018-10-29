package com.rexmtorres.deliveryhelper

import com.android.build.gradle.api.ApplicationVariant
import com.android.build.gradle.api.BaseVariant
import com.android.build.gradle.api.LibraryVariant
import org.gradle.api.GradleException
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.file.ConfigurableFileCollection
import org.gradle.external.javadoc.JavadocMemberLevel

class JavaDocSettings {
    BaseVariant variant

    String title
    JavadocMemberLevel javadocMemberLevel
    ConfigurableFileCollection additionalSourceFiles
    ConfigurableFileCollection additionalClasspathFiles
    List<String> excludedFiles
    File outputFile

    @Override
    String toString() {
        return """|doc${variant.name.capitalize()} {
                  |    variant = $variant
                  |    title = $title
                  |    javadocMemberLevel = $javadocMemberLevel
                  |    additionalSourceFiles = $additionalSourceFiles
                  |    additionalClasspathFiles = $additionalClasspathFiles
                  |    excludedFiles = $excludedFiles
                  |    outputFile = $outputFile
                  |}""".stripMargin()
    }
}

class ApplicationDelivery {
    ApplicationVariant variant
    File apkFile
    File unsignedApkFile
    File proguardMapDir

    @Override
    String toString() {
        return """|app${variant.name.capitalize()} {
                  |    variant = $variant
                  |    apkFile = $apkFile
                  |    unsignedApkFile = $unsignedApkFile
                  |    proguardMapDir = $proguardMapDir
                  |}""".stripMargin()
    }
}

class LibraryDelivery {
    LibraryVariant variant
    File aarFile
    File jarFile
    File proguardMapDir

    @Override
    String toString() {
        return """|lib${variant.name.capitalize()} {
                  |    variant = $variant
                  |    aarFile = $aarFile
                  |    jarFile = $jarFile
                  |    proguardMapDir = $proguardMapDir
                  |}""".stripMargin()
    }
}

class DeliveryExtension {
    static Project project

    private BaseVariant[] variants = []

    private ApplicationDelivery[] appDeliveries = []
    private LibraryDelivery[] libDeliveries = []
    private JavaDocSettings[] javaDocSettings = []

    void app(final Closure appClosure) {
        def app = new ApplicationDelivery()
        project.configure(app, appClosure)

        if (app.variant == null) {
            throw new GradleException("app.variant cannot be null!")
        }

        if (app.apkFile == null) {
            throw new GradleException("app.apkFile cannot be null!")
        }

        appDeliveries += app

        if (!variants.contains(app.variant)) {
            variants += app.variant
        }
    }

    void lib(final Closure libClosure) {
        def lib = new LibraryDelivery()
        project.configure(lib, libClosure)

        if (lib.variant == null) {
            throw new GradleException("lib.variant cannot be null!")
        }

        if (lib.aarFile == null) {
            throw new GradleException("lib.aarFile cannot be null!")
        }

        libDeliveries += lib

        if (!variants.contains(lib.variant)) {
            variants += lib.variant
        }
    }

    void javadoc(final Closure javadocClosure) {
        def javadoc = new JavaDocSettings()
        project.configure(javadoc, javadocClosure)

        if (javadoc.variant == null) {
            throw new GradleException("javadoc.variant cannot be null!")
        }

        if (javadoc.outputFile == null) {
            throw new GradleException("javadoc.outputFile cannot be null!")
        }

        javaDocSettings += javadoc

        if (!variants.contains(lib.variant)) {
            variants += lib.variant
        }
    }

    @Override
    String toString() {
        appDeliveries.each {
            println "app: $it"
        }
        libDeliveries.each {
            println "lib: $it"
        }
        javaDocSettings.each {
            println "doc: $it"
        }
        return super.toString()
    }
}

class DeliveryPlugin implements Plugin<Project> {
    private def resVersion = "v1"
    private def binZip = "/res/bin_${resVersion}.zip"
    private
    def cacheLoc = "${System.getProperty("user.home")}/.gradle/rmtcache/com.rexmtorres.deliveryhelper/${resVersion}"

    void apply(Project project) {
        DeliveryExtension.project = project

        def delivery = project.extensions.create("delivery", DeliveryExtension)

        project.task("createDelivery") {
            doLast {
            }
        }
    }
}
