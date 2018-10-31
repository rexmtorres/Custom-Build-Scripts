package com.rexmtorres.packagehelper

import com.android.build.gradle.api.ApplicationVariant

/**
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