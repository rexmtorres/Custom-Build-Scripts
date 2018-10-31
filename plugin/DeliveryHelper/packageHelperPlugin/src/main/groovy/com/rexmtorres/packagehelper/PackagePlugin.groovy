package com.rexmtorres.packagehelper

import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.Task
import org.gradle.api.tasks.Copy
import org.gradle.api.tasks.bundling.Zip

/**
 * @author Rex M. Torres
 */
class PackagePlugin implements Plugin<Project> {
    private static def groupPackageHelper = "packageHelper"

    private static def resVersion = "v1"
    private static def binZip = "/res/bin_${resVersion}.zip"
    private static
    def cacheLoc = "${System.getProperty("user.home")}/.gradle/rmtcache/com.rexmtorres.deliveryhelper/${resVersion}"

    void apply(Project project) {
        PackageExtension.project = project

        def delivery = project.extensions.create(PackageExtension.extensionName, PackageExtension)

        project.afterEvaluate {
            setUpAppTasks(delivery.appPackages, project)
            setUpLibTasks(delivery.libPackages, project)
        }

        project.task("createDelivery") {
            doLast {
            }
        }
    }

    private void setUpAppTasks(final ApplicationPackage[] appPackages, final Project project) {
        if (appPackages.size() < 1) {
            return
        }

        def appTask = project.task("phExportApp") {
            group groupPackageHelper
            description "Exports the generated APK(s) into the specified location."
        }

        println "appPackages = $appPackages"

        appPackages.each { app ->
            def variant = app.variant

            def srcApk = variant.outputs.first().outputFile
            def destApk = app.apkFile
            def destUnsignedApk = app.unsignedApkFile

            def varNameCap = variant.name.capitalize()

            def taskNameAssemble = "assemble${varNameCap}"

            //region Task for exporting the APK from <buildDir>/outputs/apk/<build> to the specified location
            if (destApk != null) {
                def taskNameApk = "rmtExport${varNameCap}Apk"

                appTask.dependsOn project.task(taskNameApk) {
                    dependsOn project.tasks[taskNameAssemble]
                    description "Exports ${srcApk} to ${destApk}"

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
            //endregion

            //region Task for usigning then exporting the APK from <buildDir>/outputs/apk/<build> to the specified location
            if (destUnsignedApk != null) {
                def taskNameUnsignApk = "rmtUnsign${varNameCap}Apk"

                def unsignTask = project.task(taskNameUnsignApk, type: Zip) {
                    dependsOn project.tasks[taskNameAssemble]

                    archiveName "${variant.dirName}/${srcApk.name}"
                    from project.zipTree(srcApk)
                    exclude "/META-INF/**"
                }

                def taskNameUnsignedApk = "rmtExport${varNameCap}UnsignedApk"

                appTask.dependsOn project.task(taskNameUnsignedApk) {
                    dependsOn unsignTask
                    description "Unsigns ${srcApk} and exports it to ${destUnsignedApk}"

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
            //endregion

            createProguardMapTask(app, project, appTask)
        }
    }

    private void setUpLibTasks(final LibraryPackage[] libPackages, final Project project) {
        if (libPackages.size() < 1) {
            return
        }

        def libTask = project.task("phExportLib") {
            group groupPackageHelper
            description "Exports the generated AAR(s) and/or JAR(s) into the specified location."
        }

        println "libPackages = $libPackages"

        libPackages.each { lib ->
            def variant = lib.variant

            def srcAar = variant.outputs.first().outputFile
            def destAar = lib.aarFile
            def destJar = lib.jarFile

            def varNameCap = variant.name.capitalize()

            def taskNameAssemble = "assemble${varNameCap}"

            if (destAar != null) {
                def taskNameAar = "rmtExport${varNameCap}Aar"

                libTask.dependsOn project.task(taskNameAar) {
                    dependsOn project.tasks[taskNameAssemble]
                    description "Exports ${srcAar} to ${destAar}"

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

            if (destJar != null) {
                def taskNameJar = "rmtExport${varNameCap}Jar"

                libTask.dependsOn project.task(taskNameJar) {
                    dependsOn project.tasks[taskNameAssemble]
                    description "Extracts the JAR file inside ${srcAar} and exports it to ${destJar}"

                    inputs.file(srcAar)
                    outputs.file(destJar)

                    doLast {
                        project.copy {
                            from project.zipTree(srcAar)
                            into(destJar.parentFile)
                            include "classes.jar"
                            rename "classes.jar", destJar.name
                        }
                    }
                }
            }

            createProguardMapTask(lib, project, libTask)
        }
    }

    private void createProguardMapTask(final BasePackage basePackage, final Project project, final Task dependent) {
        def destProguardMapDir = basePackage.proguardMapDir

        if (destProguardMapDir != null) {
            def variant = basePackage.variant
            def varNameCap = variant.name.capitalize()

            def srcProguardMapDir = new File("${project.buildDir}/outputs/mapping/${variant.dirName}")

            if (srcProguardMapDir.exists()) {
                def taskNameExportProguard = "rmtExport${varNameCap}ProguardMap"

                dependent.dependsOn project.task(taskNameExportProguard, type: Copy) {
                    dependsOn project.tasks["assemble${varNameCap}"]
                    description "Exports ${srcProguardMapDir} into ${destProguardMapDir}."

                    doFirst {
                        if (destProguardMapDir.exists()) {
                            destProguardMapDir.deleteDir()
                        }
                    }

                    from(srcProguardMapDir)
                    into(destProguardMapDir)
                }
            }
        }
    }
}
