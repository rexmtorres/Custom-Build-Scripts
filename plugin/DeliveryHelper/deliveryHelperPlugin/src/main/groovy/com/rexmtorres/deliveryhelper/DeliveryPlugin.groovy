package com.rexmtorres.deliveryhelper

import com.android.build.gradle.api.ApplicationVariant
import com.android.build.gradle.api.BaseVariant
import com.android.build.gradle.api.LibraryVariant
import org.gradle.api.GradleException
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.file.ConfigurableFileCollection
import org.gradle.api.tasks.bundling.Zip
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
    protected static Project project

    protected ApplicationDelivery[] appDeliveries = []
    protected LibraryDelivery[] libDeliveries = []
    protected JavaDocSettings[] javaDocSettings = []

    void app(final Closure appClosure) {
        def app = new ApplicationDelivery()
        project.configure(app, appClosure)

        if (app.variant == null) {
            throw new GradleException("app.variant cannot be null!")
        }

        if ((app.apkFile == null) && (app.unsignedApkFile == null)) {
            throw new GradleException("app.apkFile and app.unsignedApkFile cannot be both null!  At least one of them must be defined")
        }

        appDeliveries += app
    }

    void lib(final Closure libClosure) {
        def lib = new LibraryDelivery()
        project.configure(lib, libClosure)

        if (lib.variant == null) {
            throw new GradleException("lib.variant cannot be null!")
        }

        if ((lib.aarFile == null) && (lib.jarFile == null)) {
            throw new GradleException("lib.aarFile and lib.jarFile cannot be both null!  At least one of them must be defined")
        }

        libDeliveries += lib
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
    private def groupRmt = "rmt"
    private def groupRmtDelivery = "rmtDelivery"

    private def resVersion = "v1"
    private def binZip = "/res/bin_${resVersion}.zip"
    private def cacheLoc = "${System.getProperty("user.home")}/.gradle/rmtcache/com.rexmtorres.deliveryhelper/${resVersion}"

    void apply(Project project) {
        DeliveryExtension.project = project

        def delivery = project.extensions.create("delivery", DeliveryExtension)

        project.afterEvaluate {
            setUpAppTasks(delivery.appDeliveries, project)
            setUpLibTasks(delivery.libDeliveries, project)
        }

        project.task("createDelivery") {
            doLast {
            }
        }
    }

    private void setUpAppTasks(final ApplicationDelivery[] appDeliveries, final Project project) {
        def apkTask = project.task("rmtExportApk") {
            group groupRmt
        }

        println "appDeliveries = $appDeliveries"

        appDeliveries.each { app ->
            def variant = app.variant

            def srcApk = variant.outputs.first().outputFile
            def destApk = app.apkFile
            def destUnsignedApk = app.unsignedApkFile

            def varNameCap = variant.name.capitalize()

            def taskNameAssemble = "assemble${varNameCap}"

            if (destApk != null) {
                def taskNameApk = "rmtExport${varNameCap}Apk"

                apkTask.dependsOn project.task(taskNameApk) {
                    dependsOn project.tasks[taskNameAssemble]
                    group groupRmt

                    inputs.file(srcApk)
                    outputs.file(destApk)

                    doLast {
                        project.copy {
                            from(srcApk)
                            into(destApk.parentFile)
                            rename srcApk.name, destApk.name
                        }
                    }
                }
            }

            if (destUnsignedApk != null) {
                def taskNameUnsignApk = "rmtUnsign${varNameCap}Apk"

                def unsignTask = project.task(taskNameUnsignApk, type: Zip) {
                    dependsOn project.tasks[taskNameAssemble]

                    archiveName "${variant.dirName}/${srcApk.name}"
                    from project.zipTree(srcApk)
                    exclude "/META-INF/**"
                }

                def taskNameUnsignedApk = "rmtExport${varNameCap}UnsignedApk"

                apkTask.dependsOn project.task(taskNameUnsignedApk) {
                    dependsOn unsignTask
                    group groupRmt

                    inputs.file(srcApk)
                    outputs.file(destUnsignedApk)

                    doLast {
                        project.copy {
                            from(unsignTask.outputs.files.first())
                            into(destUnsignedApk.parentFile)
                            rename srcApk.name, destUnsignedApk.name
                        }
                    }
                }
            }
        }
    }

    private void setUpLibTasks(final LibraryDelivery[] libDeliveries, final Project project) {
        def aarTask = project.task("rmtExportAar") {
            group groupRmt
        }

        def jarTask = project.task("rmtExportJar") {
            group groupRmt
        }

        libDeliveries.each { lib ->
            def variant = lib.variant

            def srcAar = variant.outputs.first().outputFile
            def destAar = lib.aarFile
            def destJar = lib.jarFile

            def varName = variant.name

            def taskNameAssemble = "assemble${varName.capitalize()}"
            def taskNameAar = "rmtExport${varName.capitalize()}Aar"
            def taskNameJar = "rmtExport${varName.capitalize()}Jar"

            if (destAar != null) {
                aarTask.dependsOn project.task(taskNameAar) {
                    dependsOn project.tasks[taskNameAssemble]
                    group groupRmt

                    inputs.file(srcAar)
                    outputs.file(destAar)

                    doLast {
                        project.copy {
                            from(srcAar)
                            into(destAar.parentFile)
                            rename srcAar.name, destAar.name
                        }
                    }
                }
            }
        }
    }
}
