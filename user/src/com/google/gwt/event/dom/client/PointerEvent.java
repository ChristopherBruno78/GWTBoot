package com.google.gwt.event.dom.client;

import com.google.gwt.dom.client.NativeEvent;

import com.google.gwt.event.dom.client.MouseEvent;
import com.google.gwt.event.shared.EventHandler;

/**
 * Abstract class representing Pointer events.
 *
 * @param <H> handler type
 */
public abstract class PointerEvent<H extends EventHandler>
  extends MouseEvent<H> {

  public final int getPointerId() {
    return getPointerId(getNativeEvent());
  }

  private static native int getPointerId(NativeEvent e) /*-{
            return e.pointerId;
    }-*/;
}
