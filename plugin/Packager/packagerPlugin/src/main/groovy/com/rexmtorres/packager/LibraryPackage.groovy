/*
 * @author Rex M. Torres
 */

package com.rexmtorres.packager

import com.android.build.gradle.api.LibraryVariant

/**
 * Class for storing the settings for the
 * <a href="PackageExtension.html#lib(Closure<LibraryPackage>)">lib</a> closure.
 */
class LibraryPackage extends BasePackage {
    /**
     * The build variant of the library to be packaged.  This property is <i>mandatory</i>.
     */
    LibraryVariant variant

    /**
     * The file to where the AAR will be exported.  <b>If this points to an existing file, that file
     * will be overwritten.</b>  This property is optional <i>if</i> {@link #jarFile} is already
     * defined; that is, at least one of them must be present.  If this is not specified, then only
     * the JAR file will be exported.
     */
    File aarFile

    /**
     * The file to where the JAR will be exported.  <b>If this points to an existing file, that file
     * will be overwritten.</b>  This property is optional <i>if</i> {@link #aarFile} is already
     * defined; that is, at least one of them must be present.  If this is not specified, then only
     * the AAR file will be exported.
     */
    File jarFile

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