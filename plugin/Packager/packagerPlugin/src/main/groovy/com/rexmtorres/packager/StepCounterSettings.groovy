/*
 * @author Rex M. Torres
 */

package com.rexmtorres.packager

import com.android.build.gradle.api.BaseVariant
import org.gradle.api.file.ConfigurableFileCollection

/**
 * Class for storing the settings for the
 * <a href="PackageExtension.html#stepCounter(Closure<StepCounterSettings>)">stepCounter</a> closure.
 */
class StepCounterSettings {
    /**
     * The build variant of the module to be processed.  This property is <i>mandatory</i>.
     */
    BaseVariant variant

    /**
     * The CSV report file to be generated.  <b>If this points to an existing file, that file will
     * be overwritten.</b>  This property is <i>mandatory</i>.
     */
    File outputCsvFile

    /**
     * List of additional source files to be included in the report.  This property is
     * <i>optional</i>.  The variant's source files are already included, so there's no need to add
     * them in this property.
     */
    ConfigurableFileCollection additionalSourceFiles

    /**
     * Set of patterns for files to be included in the report.  This property is <i>optional</i>.
     */
    List<String> includes

    /**
     * Set of patterns for files to be excluded in the report.  This property is <i>optional</i>.
     */
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
