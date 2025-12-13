package com.google.gwt.user.client;

import jsinterop.annotations.JsPackage;
import jsinterop.annotations.JsType;

@JsType(isNative = true, namespace = JsPackage.GLOBAL, name="console")
public class Console {

    public static native void log(String message);
    public static native void debug(String message);
    public static native void info(String message);
    public static native void error(String message);
    public static native void warn(String message);

    public static native void clear();

    public static native void count(String label);
    public static native void countReset(String label);

    public static native void time(String label);
    public static native void timeEnd(String label);
    public static native void timeLog(String label);


}