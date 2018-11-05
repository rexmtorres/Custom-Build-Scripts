package com.rexmtorres.mylibrary;

import android.util.Log;

/**
 * A class.
 *
 * @author retorres
 */
public class Test {
    /** A public field. */
    public String someField;

    /** A protected field. */
    protected String someOtherField;

    /** A private field. */
    private com.rexmtorres.mylibrary.internal.Test internalTest;

    public Test() {
        internalTest = new com.rexmtorres.mylibrary.internal.Test();
    }

    /**
     * A public method.
     *
     * @param someArg A {@link String} argument.
     */
    public void someMethod(String someArg) {
        internalTest.someMethod(someArg);
        doSomething(someArg);
    }

    /**
     * A protected method.
     *
     * @param someArg A {@link String} argument.
     */
    protected void doSomething(String someArg) {
        int someValue = aPrivateMethod(someArg);
        someField = someArg;

        Log.d("TAG", "someValue = " + someValue);
    }

    /**
     * A private method.
     *
     * @param someArg A {@link String} argument.
     *
     * @return an integer
     */
    private int aPrivateMethod(String someArg) {
        someOtherField = someArg;
        return (someArg == null) ? 0 : 1;
    }
}
