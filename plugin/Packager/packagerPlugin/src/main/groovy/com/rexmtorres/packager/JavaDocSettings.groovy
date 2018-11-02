/*
 * @author Rex M. Torres
 */

package com.rexmtorres.packager

import com.android.build.gradle.api.BaseVariant
import org.gradle.api.file.ConfigurableFileCollection
import org.gradle.external.javadoc.JavadocMemberLevel

/**
 * Class for storing the settings for the
 * <a href="PackageExtension.html#javadoc(Closure<JavaDocSettings>)">javadoc</a> closure.
 */
class JavaDocSettings {
    /**
     * The build variant of the module for which Javadoc will be configured.  This property is
     * <i>mandatory</i>.
     */
    BaseVariant variant

    /**
     * The zip file to where the Javadoc will be stored.  <b>If this points to an existing file,
     * that file will be overwritten.</b>  This property is <i>mandatory</i>.
     */
    File outputZipFile

    /**
     * The javadocTitle of the Javadoc to be generated.  This property is <i>optional</i>.
     */
    String javadocTitle

    /**
     * The title to be displayed on the browser window.  This property is <i>optional</i>.
     */
    String windowTitle

    /**
     * If set to {@code true}, aborts the Javadoc generation if there are errors in the Javadoc
     * comments.  Otherwise, attempt to continue.  This property is <i>optional</i> and is
     * {@code false} by default.
     */
    boolean failOnError

    /**
     * Specifies which members are included in the Javadoc based on their visibility level.  This
     * value maps to the {@code -public}, {@code -protected}, {@code -package} and {@code -private}
     * options of the {@code javadoc} executable. This property is <i>optional</i> and defaults to
     * {@link JavadocMemberLevel#PROTECTED}.
     */
    JavadocMemberLevel javadocMemberLevel

    /**
     * List of additional source files to be included in the Javadoc.  This property is
     * <i>optional</i>.  The variant's source files are already included, so there's no need to add
     * them in this property.
     */
    ConfigurableFileCollection additionalSourceFiles

    /**
     * List of additional class paths used to resolve type references in the source codes.  This
     * property is <i>optional</i>.  The variant's classpath as well as the Android library are
     * already included, so there's no need to add them in this property.
     */
    ConfigurableFileCollection additionalClasspathFiles

    /**
     * Set of patterns for files to be excluded from Javadoc.  This property is <i>optional</i>.
     */
    List<String> excludes

    /**
     * {@link File} containing a list of additional Javadoc tool options.  This property is
     * <i>optional</i>.
     */
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
                  |    excludes = $excludes
                  |    optionsFile = $optionsFile
                  |}""".stripMargin()
    }
}