/*
 * @author Rex M. Torres
 */

package com.rexmtorres.packagehelper

import com.android.build.gradle.api.BaseVariant

class BasePackage {
    /**
     * The build variant of the module to be packaged.  This property is <i>mandatory</i>.
     */
    BaseVariant variant

    /**
     * The directory where the Proguard map files will be exported. This property is
     * <i>optional</i>.  If this is not specified, then the Proguard map files will not be exported.
     */
    File proguardMapDir
}
