package com.google.gwt.event.dom.client;

import com.google.gwt.dom.client.BrowserEvents;

public class PointerOverEvent extends PointerEvent<PointerOverHandler> {
  /**
   * Event type for PointerOverEvent. Represents the meta-data associated with
   * this event.
   */
  private static final Type<PointerOverHandler> TYPE = new Type<>(
          BrowserEvents.POINTEROVER,
    new PointerOverEvent()
  );

  /**
   * Gets the event type associated with PointerOverEvent.
   *
   * @return the handler type
   */
  public static Type<PointerOverHandler> getType() {
    return TYPE;
  }

  /**
   * Protected constructor, use
   * {@link DomEvent#fireNativeEvent(com.google.gwt.dom.client.NativeEvent, com.google.gwt.event.shared.HasHandlers)}
   * to fire pointer down events.
   */
  protected PointerOverEvent() {}

  @Override
  public final Type<PointerOverHandler> getAssociatedType() {
    return TYPE;
  }

  @Override
  protected void dispatch(PointerOverHandler handler) {
    handler.onPointerOver(this);
  }
}
