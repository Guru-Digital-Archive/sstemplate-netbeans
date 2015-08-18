/**
 * Silverstripe Template for Netbeans
 *
 * Copyright (c) 2015 Corey Sewell
 *
 * For warranty and licensing information, view the LICENSE file.
 */
package org.netbeans.modules.php.sstemplate;

import java.util.HashSet;
import java.util.Set;

public class Debugger {

    private static Debugger instance = null;

    protected Debugger() {
        // Exists only to defeat instantiation.
    }

    public static Debugger getInstance() {
        if (instance == null) {
            instance = new Debugger();
        }
        return instance;
    }

    public static void message(Object msg) {
        Debugger.getInstance().log(msg);
    }

    public static void oneLine(Object msg) {
        Debugger.getInstance().log(msg.toString().replace("\n", "").replace("\r", ""));
    }

    public void log(Object o) {
        StackTraceElement caller = getCaller();
        System.out.println(caller.getClassName() + "." + caller.getMethodName() + "::" + caller.getLineNumber() + " : " + o.toString());

    }

    public StackTraceElement getCaller() {
        StackTraceElement[] stackTraceElements = Thread.currentThread().getStackTrace();
        StackTraceElement caller = stackTraceElements[0];
        Set<String> classesToSkip = new HashSet<String>();
        classesToSkip.add(StackTraceElement.class.getName());
        classesToSkip.add(Debugger.class.getName());
        classesToSkip.add(java.lang.Thread.class.getName());

        for (StackTraceElement callerTest : stackTraceElements) {
            if (!classesToSkip.contains(callerTest.getClassName())) {
                caller = callerTest;
                break;
            }

        }
        return caller;
    }
}
