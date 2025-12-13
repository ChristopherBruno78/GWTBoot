package com.google.gwt.event.dom.client;

import com.google.gwt.event.shared.EventHandler;

/**
 * Handler interface for {@link PointerDownEvent} events.
 */
public interface PointerDownHandler extends EventHandler {
  /**
   * Called when PointerDownEvent is fired.
   *
   * @param event the {@link PointerDownEvent} that was fired
   */
  void onPointerDown(PointerDownEvent event);
}
