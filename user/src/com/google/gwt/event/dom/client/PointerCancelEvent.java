package com.google.gwt.event.dom.client;

import com.google.gwt.dom.client.BrowserEvents;

/**
 * Represents a native PointerCancelEvent.
 */
public class PointerCancelEvent extends PointerEvent<PointerCancelHandler> {
  /**
   * Event type for PointerCancelEvent. Represents the meta-data associated with
   * this event.
   */
  private static final Type<PointerCancelHandler> TYPE = new Type<PointerCancelHandler>(
          BrowserEvents.POINTERCANCEL,
    new PointerCancelEvent()
  );

  /**
   * Gets the event type associated with pointer cancel events.
   *
   * @return the handler type
   */
  public static Type<PointerCancelHandler> getType() {
    return TYPE;
  }

  /**
   * Protected constructor, use
   * {@link DomEvent#fireNativeEvent(com.google.gwt.dom.client.NativeEvent, com.google.gwt.event.shared.HasHandlers)}
   * to fire pointer up events.
   */
  protected PointerCancelEvent() {}

  @Override
  public final Type<PointerCancelHandler> getAssociatedType() {
    return TYPE;
  }

  @Override
  protected void dispatch(PointerCancelHandler handler) {
    handler.onPointerCancel(this);
  }
}
