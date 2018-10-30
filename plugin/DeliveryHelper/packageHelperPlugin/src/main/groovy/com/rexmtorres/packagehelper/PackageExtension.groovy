package com.rexmtorres.packagehelper

import org.gradle.api.GradleException
import org.gradle.api.Project

/**
 * @author Rex M. Torres
 */
class PackageExtension {
    protected final static String extensionName = "packageIt"

    protected static Project project

    protected ApplicationPackage[] appPackages = []
    protected LibraryPackage[] libPackages = []
    protected JavaDocSettings[] javaDocSettings = []

    void app(final Closure appClosure) {
        def app = new ApplicationPackage()
        project.configure(app, appClosure)

        if (app.variant == null) {
            throw new GradleException("app.variant cannot be null!")
        }

        if ((app.apkFile == null) && (app.unsignedApkFile == null)) {
            throw new GradleException("app.apkFile and app.unsignedApkFile cannot be both null!  At least one of them must be defined")
        }

        appPackages += app
    }

    void lib(final Closure libClosure) {
        def lib = new LibraryPackage()
        project.configure(lib, libClosure)

        if (lib.variant == null) {
            throw new GradleException("lib.variant cannot be null!")
        }

        if ((lib.aarFile == null) && (lib.jarFile == null)) {
            throw new GradleException("lib.aarFile and lib.jarFile cannot be both null!  At least one of them must be defined")
        }

        libPackages += lib
    }

    void javadoc(final Closure javadocClosure) {
        def javadoc = new JavaDocSettings()
        project.configure(javadoc, javadocClosure)

        if (javadoc.variant == null) {
            throw new GradleException("javadoc.variant cannot be null!")
        }

        if (javadoc.outputFile == null) {
            throw new GradleException("javadoc.outputFile cannot be null!")
        }

        javaDocSettings += javadoc
    }
}