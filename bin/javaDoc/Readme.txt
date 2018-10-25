yWorks doclet (yDoc) only works with JDK 4-7.  It will not work with JDK 8 and up.
Since JDK 7 (and below) are not maintained anymore, most build machines will use
JDK 8 or newer.  In order for yWorks to work, we will force the JavaDoc Gradle
task to use our bundled javadoc tool, which is from JDK 7 (update 80).

Hopefully, yWorks doclet will be updated to support the newer JDKs.  If not, we
may need to find a replacement doclet that also generates UML diagrams.

TODO:  Trim the bundled JDK.
We only need the javadoc tool from the bundled JDK.  We can probably remove the
unneeded files in the bundled JDK.