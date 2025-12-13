package com.google.gwt.event.dom.client;

import com.google.gwt.event.shared.EventHandler;

/**
 * Handler interface for {@link PointerMoveEvent} events.
 */
public interface PointerMoveHandler extends EventHandler {
  /**
   * Called when PointerMoveEvent is fired.
   *
   * @param event the {@link PointerMoveEvent} that was fired
   */
  void onPointerMove(PointerMoveEvent event);
}
