package com.google.gwt.event.dom.client;

import com.google.gwt.event.shared.EventHandler;

/**
 * Handler interface for {@link InputEvent} events.
 */
public interface InputHandler extends EventHandler {
    /**
     * Called when {@link InputEvent} is fired.
     *
     * @param event the {@link InputEvent} that was fired
     */
    void onInput(InputEvent event);
}
