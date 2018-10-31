package com.rexmtorres.packagehelper

import com.android.build.gradle.api.BaseVariant
import org.gradle.api.file.ConfigurableFileCollection
import org.gradle.external.javadoc.JavadocMemberLevel

/**
 * Class for storing the settings for {@link PackageExtension#javadoc(groovy.lang.Closure)}.
 *
 * @author Rex M. Torres
 */
class JavaDocSettings {
    BaseVariant variant
    File outputZipFile
    String javadocTitle
    String windowTitle
    boolean failOnError
    JavadocMemberLevel javadocMemberLevel
    ConfigurableFileCollection additionalSourceFiles
    ConfigurableFileCollection additionalClasspathFiles
    List<String> excludedFiles
    File optionsFile

    @Override
    String toString() {
        return """|doc${variant.name.capitalize()} {
                  |    variant = $variant
                  |    outputZipFile = $outputZipFile
                  |    javadocTitle = $javadocTitle
                  |    windowTitle = $windowTitle
                  |    failOnError = $failOnError
                  |    javadocMemberLevel = $javadocMemberLevel
                  |    additionalSourceFiles = $additionalSourceFiles
                  |    additionalClasspathFiles = $additionalClasspathFiles
                  |    excludedFiles = $excludedFiles
                  |    optionsFile = $optionsFile
                  |}""".stripMargin()
    }
}