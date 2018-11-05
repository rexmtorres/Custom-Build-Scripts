package com.rexmtorres.mylibrary.internal;

import android.util.Log;

/**
 * An internal class.
 * 
 * @author retorres
 */
public class Test {
    /** A public field. */
    public String someField;

    /** A protected field. */
    protected String someOtherField;

    /** A private field. */
    protected int aPrivateField;

    /**
     * A public method.
     *
     * @param someArg A {@link String} argument.
     */
    public void someMethod(String someArg) {
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

        aPrivateField = someValue;

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
