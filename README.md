Custom-Build-Scripts
======

Helper functions to help set up Gradle tasks for commonly used procedures.
To use this in your Android Studio Gradle projects, import this script into your module by adding:
```gradle
apply from: 'https://rexmtorres.github.io/Custom-Build-Scripts/scripts/rmt.gradle'
```
 
This will make the following functions available:
* **exportAar** - Exports the AAR and extracts the JAR (inside the AAR) into the specified
  location renaming the files to the specified name.
  
  ```gradle
  exportAar(variant, destFolder, baseFileName)
  ```
  * **variant**: *LibraryVariant* - library build variant to be processed
  * **destFolder**: *String* - path to where the AAR and JAR files will be exported
  * **baseFileName**: *String* - file name to be used for the AAR and JAR files
  (i.e. output files => *&lt;baseFileName>.aar* and *&lt;baseFileName>.jar*)
  
  ##### See also [libraryVariants](https://google.github.io/android-gradle-dsl/current/com.android.build.gradle.LibraryExtension.html#com.android.build.gradle.LibraryExtension:libraryVariants), [build variants](https://developer.android.com/studio/build/build-variants)

* **exportApk** - Exports the APK into the specified location.
  
  ```gradle
  exportApk(variant, destFolder, baseFileName, unsignApk)
  ```
  * **variant**: *ApplicationVariant* - application build variant to be processed
  * **destFolder**: *String* - path to where the APK file will be exported
  * **baseFileName**: *String* - file name to be used for the APK file
  (i.e. output file => *&lt;baseFileName>.apk*)
  * **unsignApk**: *boolean* - indicates whether to remove the signing info from the exported file
  (if originally signed)
  
  ##### See also [applicationVariants](https://google.github.io/android-gradle-dsl/current/com.android.build.gradle.AppExtension.html#com.android.build.gradle.AppExtension:applicationVariants), [build variants](https://developer.android.com/studio/build/build-variants)

* **exportProguardMapping** - Copies the Proguard map files into a specified location.
  
  ```gradle
  exportProguardMapping(variant, outputFolder)
  ```
  * **variant**: *BaseVariant* - build variant to be processed
  * **outputFolder**: *String* - path to where the Proguard map files will be placed
  
  ##### See also [BaseExtension](https://google.github.io/android-gradle-dsl/current/com.android.build.gradle.BaseExtension.html), [build variants](https://developer.android.com/studio/build/build-variants), [libraryVariants](https://google.github.io/android-gradle-dsl/current/com.android.build.gradle.LibraryExtension.html#com.android.build.gradle.LibraryExtension:libraryVariants), [applicationVariants](https://google.github.io/android-gradle-dsl/current/com.android.build.gradle.AppExtension.html#com.android.build.gradle.AppExtension:applicationVariants)
  
* **createJavaDoc** - Generates JavaDoc.

  ```gradle
  createJavaDoc(variant, additionalSourceFiles, additionalClasspathFiles, excludedFiles, javaDocTitle, javadocMemberLevel, archiveName, outputPath)
  ```
  * **variant**: *BaseVariant* - build variant to be processed
  * **additionalSourceFiles**: *ConfigurableFileCollection* - additional source files to be included in the Javadoc.  May be null.  The default source files point to: ***variant.javaCompile.source***
  * **additionalClasspathFiles**: *ConfigurableFileCollection* - - additional classes to be referenced in the Javadoc.  May be null.  The default class path points to: ***variant.javaCompile.classpath.files, android.jar, annotations.jar***
  * **excludedFiles**: *List* - list of source files to be excluded from the Javadoc.  May be null.
  * **javaDocTitle**: *String* - Javadoc title
  * **javadocMemberLevel**: *[JavadocMemberLevel](https://docs.gradle.org/current/javadoc/org/gradle/external/javadoc/JavadocMemberLevel.html)* - visibility level of the classes, methods, variables to be included in the Javadoc.  This corresponds to the *-public*, *-protected*, *-package* and *-private* options of the Javadoc executable.
  * **archiveName**: *String* - file name of the zip file to be created that will contain the generated Javadoc.
  * **outputPath**: *String* - path to where the Javadoc zip file will be placed
  
* **calculateLinesOfCode** - Generates step count of the source code using [Amateras StepCounter](http://amateras.osdn.jp/cgi-bin/fswiki/wiki.cgi?page=StepCounter).
  
  ```gradle
  calculateLinesOfCode(variant, outputDir, outputFile)
  ```
  * **variant**: *BaseVariant* - build variant to be processed
  * **outputDir**: *String* - path to where the step count file will be placed
  * **outputFile**: *String* - file name of the step count file
