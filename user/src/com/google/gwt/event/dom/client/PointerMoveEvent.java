package com.google.gwt.event.dom.client;

import com.google.gwt.dom.client.BrowserEvents;

/**
 * Represents a native PointerMoveEvent event.
 */
public class PointerMoveEvent extends PointerEvent<PointerMoveHandler> {
  /**
   * Event type for PointerMoveEvent. Represents the meta-data associated with
   * this event.
   */
  private static final Type<PointerMoveHandler> TYPE = new Type<PointerMoveHandler>(
          BrowserEvents.POINTERMOVE,
    new PointerMoveEvent()
  );

  /**
   * Gets the event type associated with PointerMoveEvent.
   *
   * @return the handler type
   */
  public static Type<PointerMoveHandler> getType() {
    return TYPE;
  }

  /**
   * Protected constructor, use
   * {@link DomEvent#fireNativeEvent(com.google.gwt.dom.client.NativeEvent, com.google.gwt.event.shared.HasHandlers)}
   * to fire pointer down events.
   */
  protected PointerMoveEvent() {}

  @Override
  public final Type<PointerMoveHandler> getAssociatedType() {
    return TYPE;
  }

  @Override
  protected void dispatch(PointerMoveHandler handler) {
    handler.onPointerMove(this);
  }
}
