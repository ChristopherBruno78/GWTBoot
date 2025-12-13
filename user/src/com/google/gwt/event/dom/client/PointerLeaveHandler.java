package com.google.gwt.event.dom.client;

import com.google.gwt.event.shared.EventHandler;

/**
 * Handler interface for {@link PointerLeaveEvent} events.
 */
public interface PointerLeaveHandler extends EventHandler {
  /**
   * Called when PointerLeaveEvent is fired.
   *
   * @param event the {@link PointerLeaveEvent} that was fired
   */
  void onPointerLeave(PointerLeaveEvent event);
}
