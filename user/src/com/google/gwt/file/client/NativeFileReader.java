package com.google.gwt.file.client;

import jsinterop.annotations.JsPackage;
import jsinterop.annotations.JsType;

@JsType(isNative = true, namespace = JsPackage.GLOBAL, name = "FileReader")
class NativeFileReader {

  public native void readAsDataURL(File file);

  public native void readAsText(File file);

  public native void addEventListener(String event, EventListener listener);
}
