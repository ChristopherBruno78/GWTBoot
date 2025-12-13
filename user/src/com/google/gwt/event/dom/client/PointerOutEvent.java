package com.google.gwt.event.dom.client;

import com.google.gwt.dom.client.BrowserEvents;

public class PointerOutEvent extends PointerEvent<PointerOutHandler> {
  /**
   * Event type for PointerOutEvent. Represents the meta-data associated with
   * this event.
   */
  private static final Type<PointerOutHandler> TYPE = new Type<>(
          BrowserEvents.POINTEROUT,
    new PointerOutEvent()
  );

  /**
   * Gets the event type associated with PointerOutEvent.
   *
   * @return the handler type
   */
  public static Type<PointerOutHandler> getType() {
    return TYPE;
  }

  /**
   * Protected constructor, use
   * {@link DomEvent#fireNativeEvent(com.google.gwt.dom.client.NativeEvent, com.google.gwt.event.shared.HasHandlers)}
   * to fire pointer down events.
   */
  protected PointerOutEvent() {}

  @Override
  public final Type<PointerOutHandler> getAssociatedType() {
    return TYPE;
  }

  @Override
  protected void dispatch(PointerOutHandler handler) {
    handler.onPointerOut(this);
  }
}
