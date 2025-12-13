package com.google.gwt.dom.client;

import jsinterop.annotations.JsPackage;
import jsinterop.annotations.JsType;

/**
 * JS interop class for native DOMRectReadOnly (contentRect).
 */
@JsType(isNative = true, name = "DOMRectReadOnly", namespace = JsPackage.GLOBAL)
public class DOMRect {
    public int width;
    public int height;
    public int top;
    public int left;
    public int bottom;
    public int right;
}