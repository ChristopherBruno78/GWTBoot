package com.google.gwt.event.dom.client;

import com.google.gwt.event.shared.EventHandler;

/**
 * Handler interface for {@link PointerEnterEvent} events.
 */
public interface PointerEnterHandler extends EventHandler {
  /**
   * Called when PointerEnterEvent is fired.
   *
   * @param event the {@link PointerEnterEvent} that was fired
   */
  void onPointerEnter(PointerEnterEvent event);
}
