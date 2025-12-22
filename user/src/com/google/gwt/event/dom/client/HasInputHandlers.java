package com.google.gwt.event.dom.client;

import com.google.gwt.event.shared.HandlerRegistration;
import com.google.gwt.event.shared.HasHandlers;

/**
 * A widget that implements this interface provides registration for
 * {@link InputHandler} instances.
 */
public interface HasInputHandlers extends HasHandlers {
    /**
     * Adds a {@link InputEvent} handler.
     *
     * @param handler the key down handler
     * @return {@link HandlerRegistration} used to remove this handler
     */
    HandlerRegistration addInputHandler(InputHandler handler);
}