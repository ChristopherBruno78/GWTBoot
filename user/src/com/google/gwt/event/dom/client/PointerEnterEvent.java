package com.google.gwt.event.dom.client;

import com.google.gwt.dom.client.BrowserEvents;

/**
 * Represents a native PointerEnterEvent.
 */
public class PointerEnterEvent extends PointerEvent<PointerEnterHandler> {
  /**
   * Event type for PointerEnterEvent. Represents the meta-data associated with
   * this event.
   */
  private static final Type<PointerEnterHandler> TYPE = new Type<PointerEnterHandler>(
          BrowserEvents.POINTERENTER,
    new PointerEnterEvent()
  );

  /**
   * Gets the event type associated with PointerEnterEvent events.
   *
   * @return the handler type
   */
  public static Type<PointerEnterHandler> getType() {
    return TYPE;
  }

  /**
   * Protected constructor, use
   * {@link DomEvent#fireNativeEvent(com.google.gwt.dom.client.NativeEvent, com.google.gwt.event.shared.HasHandlers)}
   * to fire pointer down events.
   */
  protected PointerEnterEvent() {}

  @Override
  public final Type<PointerEnterHandler> getAssociatedType() {
    return TYPE;
  }

  @Override
  protected void dispatch(PointerEnterHandler handler) {
    handler.onPointerEnter(this);
  }
}
