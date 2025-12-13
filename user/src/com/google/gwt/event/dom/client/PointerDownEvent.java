package com.google.gwt.event.dom.client;

import com.google.gwt.dom.client.BrowserEvents;

/**
 * Represents a native PointerDownEvent.
 */
public class PointerDownEvent extends PointerEvent<PointerDownHandler> {
  /**
   * Event type for PointerDownEvent. Represents the meta-data associated with
   * this event.
   */
  private static final Type<PointerDownHandler> TYPE = new Type<PointerDownHandler>(
          BrowserEvents.POINTERDOWN,
    new PointerDownEvent()
  );

  /**
   * Gets the event type associated with PointerDownEvent events.
   *
   * @return the handler type
   */
  public static Type<PointerDownHandler> getType() {
    return TYPE;
  }

  /**
   * Protected constructor, use
   * {@link DomEvent#fireNativeEvent(com.google.gwt.dom.client.NativeEvent, com.google.gwt.event.shared.HasHandlers)}
   * to fire pointer down events.
   */
  protected PointerDownEvent() {}

  @Override
  public final Type<PointerDownHandler> getAssociatedType() {
    return TYPE;
  }

  @Override
  protected void dispatch(PointerDownHandler handler) {
    handler.onPointerDown(this);
  }
}
