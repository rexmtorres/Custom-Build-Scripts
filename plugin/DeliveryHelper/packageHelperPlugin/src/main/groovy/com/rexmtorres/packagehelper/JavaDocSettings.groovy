package com.rexmtorres.packagehelper

import com.android.build.gradle.api.BaseVariant
import org.gradle.api.file.ConfigurableFileCollection
import org.gradle.external.javadoc.JavadocMemberLevel

/**
 * @author Rex M. Torres
 */
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