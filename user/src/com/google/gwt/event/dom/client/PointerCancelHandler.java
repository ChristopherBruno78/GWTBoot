package com.google.gwt.event.dom.client;

import com.google.gwt.event.shared.EventHandler;

/**
 * Handler interface for {@link PointerCancelEvent} events.
 */
public interface PointerCancelHandler extends EventHandler {
  /**
   * Called when PointerCancelEvent is fired.
   *
   * @param event the {@link PointerCancelEvent} that was fired
   */
  void onPointerCancel(PointerCancelEvent event);
}
