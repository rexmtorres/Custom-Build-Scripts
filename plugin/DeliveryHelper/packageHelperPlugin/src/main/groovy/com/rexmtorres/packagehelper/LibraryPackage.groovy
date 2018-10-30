package com.rexmtorres.packagehelper

import com.android.build.gradle.api.LibraryVariant

/**
 * @author Rex M. Torres
 */
class LibraryPackage {
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