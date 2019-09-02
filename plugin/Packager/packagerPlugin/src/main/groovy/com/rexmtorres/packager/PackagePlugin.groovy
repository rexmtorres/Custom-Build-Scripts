/*
 * @author Rex M. Torres
 */

package com.rexmtorres.packager

import groovy.io.FileType
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.Task
import org.gradle.api.tasks.Copy
import org.gradle.api.tasks.JavaExec
import org.gradle.api.tasks.bundling.Zip
import org.gradle.api.tasks.javadoc.Javadoc
import org.gradle.internal.os.OperatingSystem

/**
 * Plugin class for processing the <tt>packager</tt> extension defined by {@link PackageExtension}.
 */
class PackagePlugin implements Plugin<Project> {
    private final static def groupPackagerMain = "packager"
    private final static def groupPackagerOthers = "phOthers"

    private final static
    def cacheLoc = "${System.getProperty("user.home")}/.gradle/rmtcache/${PackagePlugin.canonicalName}"

    private final static def resourceStepCounter = "stepCounter_v3.0.4"
    private final static def resourceSyntaxHighlighter = "syntaxHighlighter_v3.0.83"

    private static class StepCounterExecInfo {
        static def jar = "stepcounter-3.0.4-jar-with-dependencies.jar"
        static def main = "jp.sf.amateras.stepcounter.Main"
        static def classpath = new File("${cacheLoc}/${resourceStepCounter}/${jar}")
    }

    private boolean debug
    private Project targetProject

    void apply(Project project) {
        PackageExtension.project = project
        targetProject = project

        prepareResource(resourceStepCounter, project)
        prepareResource(resourceSyntaxHighlighter, project)

        def delivery = project.extensions.create(PackageExtension.extensionName, PackageExtension)

        project.afterEvaluate {
            debug = delivery.debug

            log("PackagePlugin.apply> Creating task 'createPackage'")
            def packageTask = project.task("createPackage") {
                group groupPackagerMain
            }

            setUpAppTasks(delivery.appPackages, project, packageTask)
            setUpLibTasks(delivery.libPackages, project, packageTask)
            setUpStepCounterTasks(delivery.stepCounterSettings, project, packageTask)
            setUpJavaDocTasks(delivery.javaDocSettings, project, packageTask)
        }
    }

    private void setUpAppTasks(
            final ApplicationPackage[] appPackages, final Project project, final Task dependent) {
        if (appPackages.size() < 1) {
            log("PackagePlugin.setUpAppTasks> No ApplicationPackages found.")
            return
        }

        log("PackagePlugin.setUpAppTasks> appPackages = $appPackages")

        log("PackagePlugin.setUpAppTasks> Creating task 'phExportApp'")
        def appTask = project.task("phExportApp") {
            group groupPackagerMain
            description "Exports the generated APK(s) into the specified location."
        }

        appPackages.each { app ->
            def variant = app.variant

            def apkFileName = variant.outputs.first().outputFileName
            def apkFolder = variant.getPackageApplicationProvider().get().outputs.files[1]

            def srcApk = new File(apkFolder, apkFileName)

            def destApk = app.apkFile
            def destUnsignedApk = app.unsignedApkFile

            def varNameCap = variant.name.capitalize()

            def taskNameAssemble = "assemble${varNameCap}"

            //region Task for exporting the APK from <buildDir>/outputs/apk/<build> to the specified location
            if (destApk != null) {
                log("PackagePlugin.setUpAppTasks> Creating task 'phExport${varNameCap}Apk'")
                appTask.dependsOn project.task("phExport${varNameCap}Apk") {
                    def taskName = name

                    dependsOn project.tasks[taskNameAssemble]
                    group groupPackagerOthers
                    description "Exports ${srcApk} to ${destApk}"

                    inputs.file(srcApk)
                    outputs.file(destApk)

                    doLast {
                        project.copy {
                            log("$taskName> Copying $srcApk to $destApk")

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
                log("PackagePlugin.setUpAppTasks> Creating task 'phUnsign${varNameCap}Apk'")
                def unsignTask = project.task("phUnsign${varNameCap}Apk", type: Zip) {
                    def taskName = name

                    dependsOn project.tasks[taskNameAssemble]

                    archiveName "${variant.dirName}/${srcApk.name}"
                    from project.zipTree(srcApk)
                    exclude "/META-INF/MANIFEST.MF"
                    exclude "/META-INF/**.RSA"
                    exclude "/META-INF/**.SF"

                    doLast {
                        log("$taskName> Deleted META-INF/** $archiveName")
                    }
                }

                log("PackagePlugin.setUpAppTasks> Creating task 'phExport${varNameCap}UnsignedApk'")
                appTask.dependsOn project.task("phExport${varNameCap}UnsignedApk") {
                    def taskName = name

                    dependsOn unsignTask
                    group groupPackagerOthers
                    description "Unsigns ${srcApk} and exports it to ${destUnsignedApk}"

                    inputs.file(srcApk)
                    outputs.file(destUnsignedApk)

                    doLast {
                        project.copy {
                            log("$taskName> Copying ${unsignTask.outputs.files.first()} to $destUnsignedApk")

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

    private void setUpLibTasks(
            final LibraryPackage[] libPackages, final Project project, final Task dependent) {
        if (libPackages.size() < 1) {
            log("PackagePlugin.setUpLibTasks> No LibraryPackages found.")
            return
        }

        log("PackagePlugin.setUpLibTasks> libPackages = $libPackages")

        log("PackagePlugin.setUpLibTasks> Creating task 'phExportLib'")
        def libTask = project.task("phExportLib") {
            group groupPackagerMain
            description "Exports the generated AAR(s) and/or JAR(s) into the specified location."
        }

        libPackages.each { lib ->
            def variant = lib.variant

            def srcAar = variant.getPackageLibraryProvider().get().getArchivePath()

            def destAar = lib.aarFile
            def destJar = lib.jarFile

            def varNameCap = variant.name.capitalize()

            def taskNameAssemble = "assemble${varNameCap}"

            if (destAar != null) {
                log("PackagePlugin.setUpLibTasks> Creating task 'phExport${varNameCap}Aar'")
                libTask.dependsOn project.task("phExport${varNameCap}Aar") {
                    def taskName = name

                    dependsOn project.tasks[taskNameAssemble]
                    group groupPackagerOthers
                    description "Exports ${srcAar} to ${destAar}"

                    inputs.file(srcAar)
                    outputs.file(destAar)

                    doLast {
                        project.copy {
                            log("$taskName> Copying $srcAar to $destAar")

                            from(srcAar)
                            into(destAar.parentFile)
                            rename srcAar.name, destAar.name
                        }
                    }
                }
            }

            if (destJar != null) {
                log("PackagePlugin.setUpLibTasks> Creating task 'phExport${varNameCap}Jar'")
                libTask.dependsOn project.task("phExport${varNameCap}Jar") {
                    def taskName = name

                    dependsOn project.tasks[taskNameAssemble]
                    group groupPackagerOthers
                    description "Extracts the JAR file inside ${srcAar} and exports it to ${destJar}"

                    inputs.file(srcAar)
                    outputs.file(destJar)

                    doLast {
                        project.copy {
                            log("$taskName> Extracting $srcAar/classes.jar as $destJar")

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

    private void setUpStepCounterTasks(
            final StepCounterSettings[] settings, final Project project, final Task dependent) {
        if (settings.size() < 1) {
            log("PackagePlugin.setUpStepCounterTasks> No StepCounterSettings found.")
            return
        }

        log("PackagePlugin.setUpStepCounterTasks> settings = $settings")

        log("PackagePlugin.setUpStepCounterTasks> Creating task 'phGenerateStepCounter'")
        def scTask = project.task("phGenerateStepCounter") {
            group groupPackagerMain
            description "Generates Amateras StepCounter profile for the specified build."
        }

        settings.each { setting ->
            def variant = setting.variant
            def varNameCap = variant.name.capitalize()

            def inputFiles = variant.getJavaCompileProvider().get().inputs.files.filter {
                !it.name.endsWith(".jar")
            }
            inputFiles.each {
                log("PackagePlugin.setUpStepCounterTasks> **** inputFile: $it")
            }

            def outputFile = setting.outputCsvFile

            def stepCounterBuild = new File("${project.buildDir}/stepCounter/${variant.dirName}/files")

            log("PackagePlugin.setUpStepCounterTasks> Creating task 'phDeleteSc${varNameCap}Files'")
            def taskDelete = project.task("phDeleteSc${varNameCap}Files") {
                def taskName = name
                def tempCsv = new File(stepCounterBuild.parentFile, outputFile.name)

                dependsOn project.tasks["assemble${varNameCap}"]

                inputs.files(inputFiles)
                outputs.file(tempCsv)
                outputs.dir(stepCounterBuild)

                doLast {
                    log("$taskName> Deleting $stepCounterBuild")

                    stepCounterBuild.deleteDir()
                    stepCounterBuild.mkdirs()

                    tempCsv.createNewFile()
                }
            }

            log("PackagePlugin.setUpStepCounterTasks> Creating task 'phCopySc${varNameCap}Files'")
            def taskCopy = project.task("phCopySc${varNameCap}Files") {
                def taskName = name

                dependsOn taskDelete

                def sources = project.files(inputFiles)

                if (setting.additionalSourceFiles != null) {
                    sources += setting.additionalSourceFiles
                }

                inputs.files(sources)
                outputs.dir(stepCounterBuild)

                doLast {
                    sources.eachWithIndex { file, index ->
                        if (file.exists()) {
                            project.copy {
                                log("$taskName> Copying $file to $stepCounterBuild")

                                from file
                                into new File(stepCounterBuild, "_$index")

                                include setting.includes
                                exclude setting.excludes
                            }
                        }
                    }
                }
            }

            log("PackagePlugin.setUpStepCounterTasks> Creating task 'phExecuteScFor${varNameCap}'")
            def taskStepCounter = project.task("phExecuteScFor${varNameCap}", type: JavaExec) {
                def taskName = name

                dependsOn taskCopy

                def tempCsv = new File(stepCounterBuild.parentFile, outputFile.name)

                inputs.dir(stepCounterBuild)
                outputs.file(tempCsv)

                classpath = project.files(StepCounterExecInfo.classpath)
                main = StepCounterExecInfo.main
                args = [
                        "-format=csv",
                        "-output=${normalizePath(tempCsv.absolutePath)}",
                        "-encoding=UTF-8",
                        "${normalizePath(stepCounterBuild.absolutePath)}"
                ]

                doFirst {
                    log("$taskName> input = $stepCounterBuild")
                    log("$taskName> output = $tempCsv")
                    log("$taskName> classpath = $classpath")
                    log("$taskName> main = $main")
                    log("$taskName> args = $args")
                }

                doLast {
                    log("$taskName> Generated StepCounter report $tempCsv")
                }
            }

            log("PackagePlugin.setUpStepCounterTasks> Creating task 'phGenerateStepCounterFor${varNameCap}'")
            scTask.dependsOn project.task("phGenerateStepCounterFor${varNameCap}") {
                def taskName = name

                dependsOn taskStepCounter
                group groupPackagerOthers

                def tempCsv = taskStepCounter.outputs.files.first()

                inputs.file(tempCsv)
                outputs.file(outputFile)

                doLast {
                    def csvContent = tempCsv.getText("UTF-8")
                    csvContent = csvContent.replaceAll("\r\n", "\n")
                    csvContent = csvContent.replaceAll("\n", "\r\n")

                    def lines = csvContent.split("\n").length + 1

                    log("$taskName> Inserting headers to $outputFile")

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

    private void setUpJavaDocTasks(
            final JavaDocSettings[] settings, final Project project, final Task dependent) {
        if (settings.size() < 1) {
            log("PackagePlugin.setUpJavaDocTasks> No JavaDocSettings found.")
            return
        }

        log("PackagePlugin.setUpJavaDocTasks> settings = $settings")

        log("PackagePlugin.setUpJavaDocTasks> Creating task 'phGenerateJavadoc'")
        def javadocTask = project.task("phGenerateJavadoc") {
            group groupPackagerMain
            description "Generates Javadoc for the specified build."
        }

        def androidApiRef = "http://d.android.com/reference"
        def androidBoothClasspath = project.android.getBootClasspath()

        settings.each { setting ->
            def variant = setting.variant
            def varNameCap = variant.name.capitalize()

            def additionalSourceFiles = setting.additionalSourceFiles
            def sourceFiles = variant.getJavaCompileProvider().get().inputs.files.filter {
                it.name.endsWith(".java") ||
                        it.name.endsWith(".kt") ||
                        it.name.endsWith(".groovy")
            }
            sourceFiles.each {
                log("PackagePlugin.setUpJavaDocTasks> **** sourceFile: $it")
            }

            if (additionalSourceFiles != null) {
                sourceFiles += additionalSourceFiles
            }

            def additionalClasspathFiles = setting.additionalClasspathFiles
            def classpathFiles = androidBoothClasspath + variant.getJavaCompileProvider().get().outputs.files
            variant.getJavaCompileProvider().get().outputs.files.each {
                log("PackagePlugin.setUpJavaDocTasks> **** classpathFile: $it")
            }

            if (additionalClasspathFiles != null) {
                classpathFiles += additionalClasspathFiles
            }

            def tempJavadocDir = new File("${project.buildDir}/phjavadoc/${variant.dirName}")

            // Task: Delete any old Javadoc files in the build directory...
            log("PackagePlugin.setUpJavaDocTasks> Creating task 'phDeleteJavadocFilesFor${varNameCap}'")
            def taskDeleteJavadoc = project.task("phDeleteJavadocFilesFor${varNameCap}") {
                def taskName = name

                dependsOn project.tasks["assemble${varNameCap}"]

                if (tempJavadocDir.exists()) {
                    inputs.dir(tempJavadocDir)
                }

                doLast {
                    if (tempJavadocDir.exists()) {
                        log("$taskName> Deleting: $tempJavadocDir")
                        tempJavadocDir.deleteDir()
                    }
                }
            }

            // Task: Generate Javadocs in the build directory...
            log("PackagePlugin.setUpJavaDocTasks> Creating task 'phGenerateJavadocFilesFor${varNameCap}'")
            def taskJavadoc = project.task("phGenerateJavadocFilesFor${varNameCap}", type: Javadoc) {
                def taskName = name

                dependsOn taskDeleteJavadoc

                inputs.files(sourceFiles, classpathFiles)
                outputs.dir(tempJavadocDir)

                // FIX: https://github.com/rexmtorres/Packager/issues/3
                // It seems that the "exclude" property of Gradle's Javadoc task does not behave
                // in the same way as the "exclude" option of the javadoc executable.  So
                // specifying "**/subpackage/**.java" will not work.
                // https://discuss.gradle.org/t/javadoc-exclusion-question/11875/3
                if (setting.excludes != null) {
                    excludes = setting.excludes
                }

                classpath = project.files(classpathFiles)

                failOnError = setting.failOnError

                // FIX: https://github.com/rexmtorres/Packager/issues/3
                // As a work-around to Gradle's Javadoc task issue
                // (https://discuss.gradle.org/t/javadoc-exclusion-question/11875/3), we will
                // "manually" examine each file in the specified sources and check if the file
                // matches any of the "exclude" patterns.  If it matches, we will filter it out.
                source = sourceFiles.findAll { src ->
                    def normalized = src.absolutePath.replace("\\", "/")

                    def matchCount = setting.excludes.count { ex ->
                        normalized.matches(wildcardToRegex(ex))
                    }

                    if (matchCount > 0) {
                        log("$taskName> exclude: $normalized ($matchCount)")
                    }

                    matchCount < 1
                }

                destinationDir = tempJavadocDir

                source.each {
                    log("$taskName> source: $it")
                }

                classpath.each {
                    log("$taskName> class: $it")
                }

                if (setting.javadocTitle != null) {
                    title = setting.javadocTitle
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

                    linksOffline androidApiRef, "${project.android.sdkDirectory}/docs/reference"
                }

                doLast {
                    // Force Android API reference links to be opened in a new window/tab by adding
                    // a "target="_blank" attribute to the <a> html tags.
                    // Without the attribute, the Android API webpage will load inside the Javadoc
                    // frame but will fail (not allowed by browsers?  or the Android website itself
                    // does not allow to be opened inside a frame?).
                    destinationDir.eachFileRecurse(FileType.FILES) {
                        if (it.name.matches(/.+\.html?$/)) {
                            log("$taskName> Updating Android API links for $it")

                            def html = it.text.replaceAll(/<a(\s+href\s*=\s*["']$androidApiRef[^"']*["'])/,
                                    /<a target="_blank" $1/)
                            it.withWriter { writer -> writer << html }
                        }
                    }
                }
            }

            def syntaxHighlighterRes = new File("${cacheLoc}/${resourceSyntaxHighlighter}")

            // Task: Beautify the generated Javadocs in the build directory using SyntaxHighlighter...
            log("PackagePlugin.setUpJavaDocTasks> Creating task 'phApplySyntaxHighlighterFor${varNameCap}'")
            def taskSyntaxHighlighter = project.task("phApplySyntaxHighlighterFor${varNameCap}") {
                def taskName = name

                dependsOn taskJavadoc

                def syntaxTheme = "Eclipse"
                def styleSheetFile = new File("${tempJavadocDir}/stylesheet.css")

                inputs.dir(tempJavadocDir)
                outputs.dir(tempJavadocDir)

                doLast {
                    // Copy the SyntaxHighlighter resources into the Javadoc dir.
                    project.copy {
                        log("$taskName> Copying SyntaxHighlighter resources to $tempJavadocDir")

                        from syntaxHighlighterRes
                        into tempJavadocDir
                    }

                    // Apply SyntaxHighlighter to the HTML files.
                    tempJavadocDir.eachFileRecurse(FileType.FILES) {
                        if (it.name.matches(/.+\.html?$/)) {
                            log("$taskName> Applying SyntaxHighlighter to $it")

                            def html = it.text.replaceAll("<(link.+\\s)(href=\")(.+)(/stylesheet\\.css\".+)>",
                                    "<\$1\$2\$3\$4>\r\n<script type=\"text/javascript\" src=\"\$3/js/shCore.js\"></script>\r\n<script type=\"text/javascript\" src=\"\$3/js/shBrushJava.js\"></script><script type=\"text/javascript\" src=\"\$3/js/shBrushXml.js\"></script>")
                                    .replaceAll("</html>", "<script type=\"text/javascript\">SyntaxHighlighter.all()</script>\r\n</html>")

                            it.withWriter { writer -> writer << html }
                        }
                    }

                    // Apply SyntaxHighlighter to the stylesheet.
                    if (styleSheetFile.exists()) {
                        log("$taskName> Applying SyntaxHighlighter to $styleSheetFile")

                        def styleSheet = styleSheetFile.getText("UTF-8")
                        styleSheetFile.write("@import url(\"css/shCore.css\");\r\n" +
                                "@import url(\"css/shTheme${syntaxTheme}.css\");\r\n\r\n${styleSheet}")
                    }
                }
            }

            def outputLocation = setting.output

            if (setting.zip) {
                log("PackagePlugin.setUpJavaDocTasks> Creating task 'phGenerateJavadocZipFor${varNameCap}'")
                javadocTask.dependsOn project.task("phGenerateJavadocZipFor${varNameCap}", type: Zip) {
                    def taskName = name

                    dependsOn taskSyntaxHighlighter
                    group groupPackagerOthers
                    description "Generates Javadoc for ${varNameCap}."

                    inputs.dir(tempJavadocDir)
                    outputs.file(outputLocation)

                    baseName "javadoc${varNameCap}"
                    from tempJavadocDir
                    into "API Guide"

                    doLast {
                        log("$taskName> Copying $archivePath to $outputLocation")

                        project.copy {
                            from archivePath
                            into outputLocation.parentFile
                            rename "${baseName}.zip", outputLocation.name
                        }
                    }
                }
            } else {
                log("PackagePlugin.setUpJavaDocTasks> Creating task 'phGenerateJavadocFolderFor${varNameCap}'")
                javadocTask.dependsOn project.task("phGenerateJavadocFolderFor${varNameCap}", type: Copy) {
                    def taskName = name

                    dependsOn taskSyntaxHighlighter
                    group groupPackagerOthers
                    description "Generates Javadoc for ${varNameCap}."

                    inputs.dir(tempJavadocDir)
                    outputs.dir(outputLocation)

                    doFirst {
                        if (outputLocation.exists()) {
                            log("$taskName> Deleting $outputLocation")
                            outputLocation.deleteDir()
                        }
                    }

                    from(tempJavadocDir)
                    into(outputLocation)

                    doLast {
                        log("$taskName> Copied $tempJavadocDir to $outputLocation")
                    }
                }
            }
        }

        dependent.dependsOn javadocTask
    }

    private void createProguardMapTask(
            final BasePackage basePackage, final Project project, final Task dependent) {
        def destProguardMapDir = basePackage.proguardMapDir

        if (destProguardMapDir != null) {
            def variant = basePackage.variant
            def varNameCap = variant.name.capitalize()

            def srcProguardMapDir = new File("${project.buildDir}/outputs/mapping/${variant.dirName}")

            log("PackagePlugin.setUpJavaDocTasks> Creating task 'phExport${varNameCap}ProguardMap'")
            dependent.dependsOn project.task("phExport${varNameCap}ProguardMap", type: Copy) {
                def taskName = name

                dependsOn project.tasks["assemble${varNameCap}"]
                group groupPackagerOthers
                description "Exports ${srcProguardMapDir} into ${destProguardMapDir}."

                doFirst {
                    if (destProguardMapDir.exists()) {
                        log("$taskName> Deleting $destProguardMapDir")
                        destProguardMapDir.deleteDir()
                    }
                }

                from(srcProguardMapDir)
                into(destProguardMapDir)

                doLast {
                    log("$taskName> Copied $srcProguardMapDir to $destProguardMapDir")
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

    private void log(final String message) {
        if (debug) {
            println ":${targetProject.name}:$message"
        }
    }

    /**
     * <ol>
     *     <li>If path contains space(s), return a path that has been modified according to the rules below:
     *          <ol>
     *              <li>If on Windows, enclose path in double quotes.<br>
     *                  Example:<br>
     *                  &nbsp;&nbsp;&nbsp;&nbsp;Original: <b><tt>C:\Path with\spaces\in the\file name.txt</tt></b><br>
     *                  &nbsp;&nbsp;&nbsp;&nbsp;Normalized: <b><tt>"C:\Path with\spaces\in the\file name.txt"</tt></b>
     *              <li>Else (if on Linux/Max OS), escape the space(s).<br>
     *                  Example:<br>
     *                  &nbsp;&nbsp;&nbsp;&nbsp;Original: <b><tt>/Path with/spaces/in the/file name.txt</tt></b><br>
     *                  &nbsp;&nbsp;&nbsp;&nbsp;Normalized: <b><tt>/Path\ with/spaces/in\ the/file\ name.txt</tt></b>
     *          </ol>
     *     <li>Else, return the path.
     * </ol>
     *
     * @param path The path to be processed.
     * @return The path as described above.
     */
    private static String normalizePath(final String path) {
        if (!path.contains(" ")) {
            return path
        }

        if (OperatingSystem.current() == OperatingSystem.WINDOWS) {
            return "\"$path\""
        } else {
            return path.replaceAll(" ", "\\ ")
        }
    }

    // Copied from: https://www.rgagnon.com/javadetails/java-0515.html
    private static String wildcardToRegex(String wildcard) {
        final StringBuffer s = new StringBuffer(wildcard.length())
        s.append('^')

        for (int i = 0; i < wildcard.length(); i++) {
            char c = wildcard.charAt(i)

            switch (c) {
                case '*':
                    s.append(".*")
                    break
                case '?':
                    s.append(".")
                    break;
                // escape special regexp-characters
                case '(': case ')': case '[': case ']': case '$':
                case '^': case '.': case '{': case '}': case '|':
                case '\\':
                    s.append("\\")
                    s.append(c)
                    break
                default:
                    s.append(c)
                    break
            }
        }

        s.append('$')
        return (s.toString())
    }
}
