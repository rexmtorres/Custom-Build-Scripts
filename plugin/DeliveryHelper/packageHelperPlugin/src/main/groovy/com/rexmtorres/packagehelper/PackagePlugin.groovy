package com.rexmtorres.packagehelper

import groovy.io.FileType
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.Task
import org.gradle.api.tasks.Copy
import org.gradle.api.tasks.JavaExec
import org.gradle.api.tasks.bundling.Zip
import org.gradle.api.tasks.javadoc.Javadoc

/**
 * Plugin class for processing {@link PackageExtension}.
 *
 * @author Rex M. Torres
 */
class PackagePlugin implements Plugin<Project> {
    private static def groupPackageHelperMain = "packageHelper"
    private static def groupPackageHelperOthers = "phOthers"

    private static def cacheLoc = "${System.getProperty("user.home")}/.gradle/rmtcache/${PackagePlugin.canonicalName}"

    private static def resourceStepCounter = "stepCounter_v3.0.4"
    private static def resourceSyntaxHighlighter = "syntaxHighlighter_v3.0.83"

    private static class StepCounterExecInfo {
        static def jar = "stepcounter-3.0.4-jar-with-dependencies.jar"
        static def main = "jp.sf.amateras.stepcounter.Main"
        static def classpath = new File("${cacheLoc}/${resourceStepCounter}/${jar}")
    }

    void apply(Project project) {
        PackageExtension.project = project

        prepareResource(resourceStepCounter, project)
        prepareResource(resourceSyntaxHighlighter, project)

        def delivery = project.extensions.create(PackageExtension.extensionName, PackageExtension)

        project.afterEvaluate {
            def packageTask = project.task("createPackage") {
                group groupPackageHelperMain
            }

            setUpAppTasks(delivery.appPackages, project, packageTask)
            setUpLibTasks(delivery.libPackages, project, packageTask)
            setUpStepCounterTasks(delivery.stepCounterSettings, project, packageTask)
            setUpJavaDocTasks(delivery.javaDocSettings, project, packageTask)
        }
    }

    private void setUpAppTasks(final ApplicationPackage[] appPackages, final Project project, final Task dependent) {
        if (appPackages.size() < 1) {
            return
        }

        def appTask = project.task("phExportApp") {
            group groupPackageHelperMain
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
                appTask.dependsOn project.task("phExport${varNameCap}Apk") {
                    dependsOn project.tasks[taskNameAssemble]
                    group groupPackageHelperOthers
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
                def unsignTask = project.task("phUnsign${varNameCap}Apk", type: Zip) {
                    dependsOn project.tasks[taskNameAssemble]

                    archiveName "${variant.dirName}/${srcApk.name}"
                    from project.zipTree(srcApk)
                    exclude "/META-INF/**"
                }

                appTask.dependsOn project.task("phExport${varNameCap}UnsignedApk") {
                    dependsOn unsignTask
                    group groupPackageHelperOthers
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

        dependent.dependsOn appTask
    }

    private void setUpLibTasks(final LibraryPackage[] libPackages, final Project project, final Task dependent) {
        if (libPackages.size() < 1) {
            return
        }

        def libTask = project.task("phExportLib") {
            group groupPackageHelperMain
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
                libTask.dependsOn project.task("phExport${varNameCap}Aar") {
                    dependsOn project.tasks[taskNameAssemble]
                    group groupPackageHelperOthers
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
                libTask.dependsOn project.task("phExport${varNameCap}Jar") {
                    dependsOn project.tasks[taskNameAssemble]
                    group groupPackageHelperOthers
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

        dependent.dependsOn libTask
    }

    private void setUpStepCounterTasks(final StepCounterSettings[] settings, final Project project, final Task dependent) {
        if (settings.size() < 1) {
            return
        }

        def scTask = project.task("phGenerateStepCounter") {
            group groupPackageHelperMain
            description "Generates Amateras StepCounter profile for the specified build."
        }

        println "settings = $settings"

        settings.each { setting ->
            def variant = setting.variant
            def varNameCap = variant.name.capitalize()

            def inputFiles = variant.getJavaCompiler().inputs.files
            def outputFile = setting.outputCsvFile

            def stepCounterBuild = new File("${project.buildDir}/stepCounter/${variant.dirName}/files")

            def taskDelete = project.task("phDeleteSc${varNameCap}Files") {
                dependsOn project.tasks["assemble${varNameCap}"]

                inputs.files(inputFiles)

                doLast {
                    stepCounterBuild.deleteDir()
                }
            }

            def taskCopy = project.task("phCopySc${varNameCap}Files") {
                dependsOn taskDelete

                def sources = project.files(inputFiles)

                if (setting.additionalSourceFiles != null) {
                    sources += setting.additionalSourceFiles
                }

                inputs.files(sources)
                outputs.dir(stepCounterBuild)

                doLast {
                    sources.each { file ->
                        if (file.exists()) {
                            project.copy {
                                from file
                                into new File(stepCounterBuild, file.name)

                                include setting.includes
                                exclude setting.excludes
                            }
                        }
                    }
                }
            }

            def taskStepCounter = project.task("phExecuteScFor${varNameCap}", type: JavaExec) {
                dependsOn taskCopy

                def tempCsv = new File(stepCounterBuild.parentFile, outputFile.name)

                inputs.dir(stepCounterBuild)
                outputs.file(tempCsv)

                classpath = project.files(StepCounterExecInfo.classpath)
                main = StepCounterExecInfo.main
                args = [
                        "-format=csv",
                        "-output=\"${tempCsv.absolutePath}\"",
                        "-encoding=UTF-8",
                        "\"${stepCounterBuild.absolutePath}\""
                ]
            }

            scTask.dependsOn project.task("phGenerateStepCounterFor${varNameCap}") {
                dependsOn taskStepCounter
                group groupPackageHelperOthers

                def tempCsv = taskStepCounter.outputs.files.first()

                inputs.file(tempCsv)
                outputs.file(outputFile)

                doLast {
                    def csvContent = tempCsv.getText("UTF-8")
                    csvContent = csvContent.replaceAll("\r\n", "\n")
                    csvContent = csvContent.replaceAll("\n", "\r\n")

                    def lines = csvContent.split("\n").length + 1

                    outputFile.withWriter { writer ->
                        writer.write("\ufeff" +                                     // Force BOM header to display Japanese chars correctly in Excel
                                "\"\u30d5\u30a1\u30a4\u30eb\r\n(File)\"," +         // Header: ファイル (File)
                                "\"\u7a2e\u985e\r\n(Type)\"," +                     // Header: 種類 (Type)
                                "\"\u30ab\u30c6\u30b4\u30ea\r\n(Category)\"," +     // Header: カテゴリ (Category)
                                "\"\u5b9f\u884c\r\n(Executable)\"," +               // Header: 実行 (Run)
                                "\"\u7a7a\u884c\r\n(Blank Lines)\"," +              // Header: 空行 (Blank Lines)
                                "\"\u30b3\u30e1\u30f3\u30c8\r\n(Comment)\"," +      // Header: コメント (Comment)
                                "\"\u5408\u8a08\r\n(Total)\"\r\n" +                 // Header: 合計 (Total)
                                "${csvContent}\r\n" +                               // Step Count Data (starts from row 2)
                                ",,\u5408\u8a08 (Total),=sum(D2:D${lines}),=sum(E2:E${lines}),=sum(F2:F${lines}),=sum(G2:G${lines})")  // Summation
                    }
                }
            }
        }

        dependent.dependsOn scTask
    }

    private void setUpJavaDocTasks(final JavaDocSettings[] settings, final Project project, final Task dependent) {
        if (settings.size() < 1) {
            return
        }

        def javadocTask = project.task("phGenerateJavadoc") {
            group groupPackageHelperMain
            description "Generates Javadoc for the specified build."
        }

        println "settings = $settings"

        def androidApiRef = "http://d.android.com/reference"
        def androidBoothClasspath = project.android.getBootClasspath()

        settings.each { setting ->
            def variant = setting.variant
            def varNameCap = variant.name.capitalize()

            def additionalSourceFiles = setting.additionalSourceFiles
            def sourceFiles = variant.getJavaCompiler().inputs.files.filter {
                it.name.endsWith(".java") ||
                        it.name.endsWith(".kt") ||
                        it.name.endsWith(".groovy")
            }

            if (additionalSourceFiles != null) {
                sourceFiles += additionalSourceFiles
            }

            def additionalClasspathFiles = setting.additionalClasspathFiles
            def classpathFiles = androidBoothClasspath + variant.getJavaCompiler().outputs.files

            if (additionalClasspathFiles != null) {
                classpathFiles += additionalClasspathFiles
            }

            def taskJavadoc = project.task("phGenerateJavadocFilesFor${varNameCap}", type: Javadoc) {
                dependsOn project.tasks["assemble${varNameCap}"]

                def tempJavadocDir = new File("${project.buildDir}/phjavadoc/${variant.dirName}")

                inputs.files(sourceFiles, classpathFiles)
                outputs.dir(tempJavadocDir)

                classpath = project.files(classpathFiles)

                failOnError setting.failOnError
                source sourceFiles
                destinationDir tempJavadocDir

                sourceFiles.each {
                    println "source: $it"
                }

                classpathFiles.each {
                    println "class: $it"
                }

                if (setting.javadocTitle != null) {
                    title setting.javadocTitle
                }

                if (setting.excludedFiles != null) {
                    exclude setting.excludedFiles
                }

                options {
                    if (setting.javadocMemberLevel != null) {
                        memberLevel = setting.javadocMemberLevel
                    }

                    if (setting.windowTitle != null) {
                        windowTitle = setting.windowTitle
                    }

                    if (setting.optionsFile != null) {
                        optionFiles << setting.optionsFile
                    }
                }

                doLast {
                    // Force Android API reference links to be opened in a new window/tab by adding
                    // a "target="_blank" attribute to the <a> html tags.
                    // Without the attribute, the Android API webpage will load inside the Javadoc
                    // frame but will fail (not allowed by browsers?  or the Android website itself
                    // does not allow to be opened inside a frame?).
                    destinationDir.eachFileRecurse(FileType.FILES) {
                        if (it.name.matches(/.+\.html?$/)) {
                            def html = it.text.replaceAll(/<a(\s+href\s*=\s*["']$androidApiRef[^"']*["'])/,
                                    /<a target="_blank" $1/)
                            it.withWriter { writer -> writer << html }
                        }
                    }
                }
            }

            def syntaxHighlighterRes = new File("${cacheLoc}/${resourceSyntaxHighlighter}")

            def taskSyntaxHighlighter = project.task("phApplySyntaxHighlighterFor${varNameCap}") {
                dependsOn taskJavadoc

                def tempJavadocDir = taskJavadoc.destinationDir

                def syntaxTheme = "Eclipse"
                def styleSheetFile = new File("${tempJavadocDir}/stylesheet.css")

                inputs.dir(tempJavadocDir)
                outputs.dir(tempJavadocDir)

                doLast {
                    // Copy the SyntaxHighlighter resources into the Javadoc dir.
                    project.copy {
                        from syntaxHighlighterRes
                        into tempJavadocDir
                    }

                    // Apply SyntaxHighlighter to the HTML files.
                    tempJavadocDir.eachFileRecurse(FileType.FILES) {
                        if (it.name.matches(/.+\.html?$/)) {
                            def html = it.text.replaceAll("<(link.+\\s)(href=\")(.+)(/stylesheet\\.css\".+)>",
                                    "<\$1\$2\$3\$4>\r\n<script type=\"text/javascript\" src=\"\$3/js/shCore.js\"></script>\r\n<script type=\"text/javascript\" src=\"\$3/js/shBrushJava.js\"></script>")
                                .replaceAll("</html>", "<script type=\"text/javascript\">SyntaxHighlighter.all()</script>\r\n</html>")

                            it.withWriter { writer -> writer << html }
                        }
                    }

                    // Apply SyntaxHighlighter to the stylesheet.
                    if(styleSheetFile.exists()) {
                        def styleSheet = styleSheetFile.getText("UTF-8")
                        styleSheetFile.write("@import url(\"css/shCore.css\");\r\n" +
                                "@import url(\"css/shTheme${syntaxTheme}.css\");\r\n\r\n${styleSheet}")
                    }
                }
            }

            def outputFile = setting.outputZipFile

            javadocTask.dependsOn project.task("phGenerateJavadocFor${varNameCap}", type: Zip) {
                dependsOn taskSyntaxHighlighter
                group groupPackageHelperOthers
                description "Generates Javadoc for ${varNameCap}."

                def tempJavadocDir = taskJavadoc.destinationDir

                inputs.dir(tempJavadocDir)
                outputs.file(outputFile)

                baseName "javadoc${varNameCap}"
                from tempJavadocDir
                into "API Guide"

                doLast {
                    project.copy {
                        from archivePath
                        into outputFile.parentFile
                        rename "${baseName}.zip", outputFile.name
                    }
                }
            }
        }

        dependent.dependsOn javadocTask
    }

    private void createProguardMapTask(final BasePackage basePackage, final Project project, final Task dependent) {
        def destProguardMapDir = basePackage.proguardMapDir

        if (destProguardMapDir != null) {
            def variant = basePackage.variant
            def varNameCap = variant.name.capitalize()

            def srcProguardMapDir = new File("${project.buildDir}/outputs/mapping/${variant.dirName}")

            if (srcProguardMapDir.exists()) {
                dependent.dependsOn project.task("phExport${varNameCap}ProguardMap", type: Copy) {
                    dependsOn project.tasks["assemble${varNameCap}"]
                    group groupPackageHelperOthers
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

    private void prepareResource(final String resource, final Project project) {
        def cacheRoot = new File(cacheLoc)
        def cachedResource = new File(cacheRoot, resource)

        if (!cachedResource.exists()) {
            if (!cacheRoot.exists()) {
                cacheRoot.mkdirs()
            }

            def resourceUri = getClass().getResource("/res/${resource}.zip")
            def cachedResourceZip = new File(cacheRoot, "${resource}.zip")

            if (!cachedResourceZip.exists()) {
                resourceUri.withInputStream {
                    cachedResourceZip << it
                }
            }

            project.copy {
                from project.zipTree(cachedResourceZip)
                into cacheRoot
            }

            cachedResourceZip.delete()
        }
    }
}
