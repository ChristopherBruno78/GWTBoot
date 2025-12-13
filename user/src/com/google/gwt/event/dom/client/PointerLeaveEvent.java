package com.google.gwt.event.dom.client;

import com.google.gwt.dom.client.BrowserEvents;

/**
 * Represents a native PointerLeaveEvent.
 */
public class PointerLeaveEvent extends PointerEvent<PointerLeaveHandler> {
  /**
   * Event type for PointerLeaveEvent. Represents the meta-data associated with
   * this event.
   */
  private static final Type<PointerLeaveHandler> TYPE = new Type<PointerLeaveHandler>(
          BrowserEvents.POINTERLEAVE,
    new PointerLeaveEvent()
  );

  /**
   * Gets the event type associated with PointerLeaveEvent events.
   *
   * @return the handler type
   */
  public static Type<PointerLeaveHandler> getType() {
    return TYPE;
  }

  /**
   * Protected constructor, use
   * {@link DomEvent#fireNativeEvent(com.google.gwt.dom.client.NativeEvent, com.google.gwt.event.shared.HasHandlers)}
   * to fire pointer down events.
   */
  protected PointerLeaveEvent() {}

  @Override
  public final Type<PointerLeaveHandler> getAssociatedType() {
    return TYPE;
  }

  @Override
  protected void dispatch(PointerLeaveHandler handler) {
    handler.onPointerLeave(this);
  }
}
