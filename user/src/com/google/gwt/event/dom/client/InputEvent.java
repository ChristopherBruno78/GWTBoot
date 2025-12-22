package com.google.gwt.event.dom.client;

import com.google.gwt.dom.client.BrowserEvents;

/**
 * Represents a native key down event.
 */
public class InputEvent extends DomEvent<InputHandler> {

    /**
     * Event type for key down events. Represents the meta-data associated with
     * this event.
     */
    private static final Type<InputHandler> TYPE = new Type<InputHandler>(
            BrowserEvents.INPUT, new InputEvent());

    /**
     * Gets the event type associated with key down events.
     *
     * @return the handler type
     */
    public static Type<InputHandler> getType() {
        return TYPE;
    }

    /**
     * Protected constructor, use
     * {@link DomEvent#fireNativeEvent(com.google.gwt.dom.client.NativeEvent, com.google.gwt.event.shared.HasHandlers)}
     * to fire key down events.
     */
    protected InputEvent() {
    }

    @Override
    public final Type<InputHandler> getAssociatedType() {
        return TYPE;
    }

    @Override
    protected void dispatch(InputHandler handler) {
        handler.onInput(this);
    }

}