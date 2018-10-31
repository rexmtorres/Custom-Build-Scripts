package com.rexmtorres.packagehelper

import com.android.build.gradle.api.ApplicationVariant
import com.android.build.gradle.api.BaseVariant
import com.android.build.gradle.api.LibraryVariant
import org.gradle.api.GradleException
import org.gradle.api.Project
import org.gradle.api.file.ConfigurableFileCollection
import org.gradle.external.javadoc.JavadocMemberLevel

/**
 * Usage:
 * <pre><code>
 *     packageIt {
 *         {@link PackageExtension#app(Closure)}
 *         {@link PackageExtension#lib(Closure)}
 *         {@link PackageExtension#javadoc(Closure)}
 *     }
 * </code></pre>
 * @author Rex M. Torres
 */
class PackageExtension {
    protected final static String extensionName = "packageIt"

    protected static Project project

    protected ApplicationPackage[] appPackages = []
    protected LibraryPackage[] libPackages = []
    protected JavaDocSettings[] javaDocSettings = []

    /**
     * Configures an Android application to be packaged.
     *
     * <p>
     * Usage:
     * <pre><code>
     *     packageIt {
     *         app {
     *             variant = &lt;{@link ApplicationVariant}>
     *             apkFile = &lt;{@link File}>
     *             unsignedApkFile = &lt;{@link File}>
     *             proguardMapDir = &lt;{@link File}>
     *         }
     *     }
     * </code></pre>
     * <ul>
     *     <li>{@code variant} - The build variant of the application to be packaged.  This property
     *         is <i>mandatory</i>.
     *     <li>{@code apkFile} - The file to where the APK will be exported.  Note that this may be
     *         a signed or unsigned APK depending on the signing configuration.  <b>If this points
     *         to an existing file, that file will be overwritten.</b>  This property is optional
     *         <i>if</i> {@code unsignedApkFile} is already defined; that is, at least one of them
     *         must be present.  If this is not specified, then only {@code unsignedApkFile} will be
     *         exported.
     *     <li>{@code unsignedApkFile} - The file to where the unsigned APK will be exported.  <b>If
     *         this points to an existing file, that file will be overwritten.</b>  This property is
     *         optional <i>if</i> {@code apkFile} is already defined; that is, at least one of them
     *         must be present.  If this is not specified, then only {@code apkFile} will be
     *         exported.
     *     <li>{@code proguardMapDir} - The directory where the Proguard map files will be exported.
     *         This property is <i>optional</i>.  If this is not specified, then the Proguard map
     *         files will not be exported.
     * </ul>
     *
     * @param appClosure {@link ApplicationPackage} closure for setting up the build configuration.
     */
    void app(final Closure<ApplicationPackage> appClosure) {
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

    /**
     * Configures an Android library to be packaged.
     *
     * <p>
     * Usage:
     * <pre><code>
     *     packageIt {
     *         lib {
     *             variant = &lt;{@link LibraryVariant}>
     *             aarFile = &lt;{@link File}>
     *             jarFile = &lt;{@link File}>
     *             proguardMapDir = &lt;{@link File}>
     *         }
     *     }
     * </code></pre>
     * <ul>
     *     <li>{@code variant} - The build variant of the library to be packaged.  This property is
     *         <i>mandatory</i>.
     *     <li>{@code aarFile} - The file to where the AAR will be exported.  <b>If this points to
     *         an existing file, that file will be overwritten.</b>  This property is optional
     *         <i>if</i> {@code jarFile} is already defined; that is, at least one of them must be
     *         present.  If this is not specified, then only the JAR file will be exported.
     *     <li>{@code jarFile} - The file to where the JAR will be exported.  <b>If this points to
     *         an existing file, that file will be overwritten.</b>  This property is optional
     *         <i>if</i> {@code aarFile} is already defined; that is, at least one of them must be
     *         present.  If this is not specified, then only the AAR file will be exported.
     *     <li>{@code proguardMapDir} - The directory where the Proguard map files will be exported.
     *         This property is <i>optional</i>.  If this is not specified, then the Proguard map
     *         files will not be exported.
     * </ul>
     *
     * @param appClosure {@link LibraryPackage} closure for setting up the build configuration.
     */
    void lib(final Closure<LibraryPackage> libClosure) {
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

    /**
     * Configures Javadoc.
     *
     * <p>
     * Usage:
     * <pre><code>
     *     packageIt {
     *         javadoc {
     *             variant = &lt;{@link BaseVariant}>
     *             outputZipFile = &lt;{@link File}>
     *             title = &lt;{@link String}>
     *             javadocMemberLevel = &lt;{@link JavadocMemberLevel}>
     *             additionalSourceFiles = &lt;{@link ConfigurableFileCollection}>
     *             additionalClasspathFiles = &lt;{@link ConfigurableFileCollection}>
     *             excludedFiles = &lt;{@link List}&lt;{@link String}>>
     *         }
     *     }
     * </code></pre>
     * <ul>
     *     <li>{@code variant} - The build variant of the module for which Javadoc will be
     *         configured.  This property is <i>mandatory</i>.
     *     <li>{@code optional} - The zip file to where the Javadoc will be stored.  <b>If this
     *         points to an existing file, that file will be overwritten.</b>  This property is
     *         <i>mandatory</i>.
     *     <li>{@code title} - The title of the Javadoc to be generated.  This property is
     *         <i>optional</i>.
     *     <li>{@code javadocMemberLevel} - Specifies the visibility level of the members to be
     *         included in the Javadoc.  This value maps to the {@code -public}, {@code -protected},
     *         {@code -package} and {@code -private} options of the javadoc executable. This
     *         property is <i>optional</i> and defaults to {@link JavadocMemberLevel#PROTECTED}.
     *     <li>{@code additionalSourceFiles} - List of additional source files to be included in the
     *         Javadoc.  This property is <i>optional</i>.  The variant's source files
     *         (variant.javaCompile.source) are already included, so there's no need to add them in
     *         this property.
     *     <li>{@code additionalClasspathFiles} - List of additional class paths used to resolve
     *         type references in the source codes.  This property is <i>optional</i>.  The
     *         variant's classpath as well as the Android and annotation libraries
     *         (variant.javaCompile.classpath.files,
     *         &lt;sdk_directory>/platforms/&lt;platform_directory>/android.jar,
     *         &lt;sdk_directory>/tools/support/annotations.jar) are already included, so there's no
     *         need to add them in this property.
     *     <li>{@code excludedFiles} - Set of patterns for files to be excluded from Javadoc.  This
     *         property is <i>optional</i>.
     * </ul>
     *
     * @param javadocClosure {@link JavaDocSettings} closure for setting up Javadoc.
     */
    void javadoc(final Closure<JavaDocSettings> javadocClosure) {
        def javadoc = new JavaDocSettings()
        project.configure(javadoc, javadocClosure)

        if (javadoc.variant == null) {
            throw new GradleException("javadoc.variant cannot be null!")
        }

        if (javadoc.outputZipFile == null) {
            throw new GradleException("javadoc.outputZipFile cannot be null!")
        }

        javaDocSettings += javadoc
    }
}