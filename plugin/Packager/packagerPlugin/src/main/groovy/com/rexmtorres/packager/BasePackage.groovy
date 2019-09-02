/*
 * @author Rex M. Torres
 */

package com.rexmtorres.packager

import com.android.build.gradle.api.BaseVariant

class BasePackage {
    /**
     * The build variant of the module to be packaged.  This property is <i>mandatory</i>.
     */
    BaseVariant variant

    /**
     * The directory where the Proguard map files will be exported. This property is
     * <i>optional</i>.  If this is not specified, then the Proguard map files will not be exported.
     *
     * @deprecated There are now a few obfuscation tools available aside from Proguard (e.g.
     *             Dexguard and Google's own R8).  These have different outputs and it's not worth
     *             it to have specific code for each.  The user is better of handling this on
     *             his/her own.
     */
    @Deprecated
    File proguardMapDir
}
