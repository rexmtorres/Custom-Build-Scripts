package com.rexmtorres.packagehelper

import com.android.build.gradle.api.ApplicationVariant

/**
 * Class for storing the settings for {@link PackageExtension#app(groovy.lang.Closure)}
 *
 * @author Rex M. Torres
 */
class ApplicationPackage extends BasePackage {
    ApplicationVariant variant
    File apkFile
    File unsignedApkFile

    @Override
    String toString() {
        return """|app${variant.name.capitalize()} {
                  |    variant = $variant
                  |    apkFile = $apkFile
                  |    unsignedApkFile = $unsignedApkFile
                  |    proguardMapDir = $proguardMapDir
                  |}""".stripMargin()
    }
}