package com.google.gwt.event.dom.client;

import com.google.gwt.dom.client.Element;

public class PointerEvents {

  public static native void setPointerCapture(
    Element element,
    int pointerId
  ) /*-{
        element.setPointerCapture(pointerId);
    }-*/;

  public static native void releasePointerCapture(
    Element element,
    int pointerId
  ) /*-{
        element.releasePointerCapture(pointerId);
    }-*/;

  public static native boolean hasPointerCapture(
    Element element,
    int pointerId
  ) /*-{
        return element.hasPointerCapture(pointerId);
    }-*/;
}
