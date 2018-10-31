package com.rexmtorres.packagehelper

import com.android.build.gradle.api.BaseVariant
import org.gradle.api.file.ConfigurableFileCollection

/**
 * @author Rex M. Torres
 */
class StepCounterSettings {
    BaseVariant variant
    File outputCsvFile
    ConfigurableFileCollection additionalSourceFiles
    List<String> includes
    List<String> excludes

    @Override
    String toString() {
        return """|stepCounter${variant.name.capitalize()} {
                  |    variant = $variant
                  |    outputCsvFile = $outputCsvFile
                  |    additionalSourceFiles = $additionalSourceFiles
                  |    includes = $includes
                  |    excludes = $excludes
                  |}""".stripMargin()
    }
}
