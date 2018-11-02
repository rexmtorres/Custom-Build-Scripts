package com.rexmtorres.packagehelper

import com.android.build.gradle.api.ApplicationVariant

/**
 * Class for storing the settings for the
 * <a href="PackageExtension.html#app(Closure<ApplicationPackage>)">app</a> closure.
 *
 * @author Rex M. Torres
 */
class ApplicationPackage extends BasePackage {
    /**
     * The build variant of the application to be packaged.  This property is <i>mandatory</i>.
     */
    ApplicationVariant variant

    /**
     * The file to where the APK will be exported.  Note that this may be a signed or unsigned APK
     * depending on the signing configuration.  <b>If this points to an existing file, that file
     * will be overwritten.</b>  This property is optional <i>if</i> {@link #unsignedApkFile} is
     * already defined; that is, at least one of them must be present.  If this is not specified,
     * then only {@link #unsignedApkFile} will be exported.
     */
    File apkFile

    /**
     * The file to where the <i>unsigned</i> APK will be exported.  <b>If this points to an existing
     * file, that file will be overwritten.</b>  This property is optional <i>if</i>
     * {@link #apkFile} is already defined; that is, at least one of them must be present.  If this
     * is not specified, then only {@link #apkFile} will be exported.
     */
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