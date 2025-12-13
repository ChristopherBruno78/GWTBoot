package com.google.gwt.event.logical.shared;

import com.google.gwt.event.shared.GwtEvent;

public class ConfirmEvent extends GwtEvent<ConfirmHandler> {
    private static final Type<ConfirmHandler> TYPE = new Type<>();

    @Override
    public Type<ConfirmHandler> getAssociatedType() {
        return TYPE;
    }

    public static Type<ConfirmHandler> getType() {
        return TYPE;
    }

    @Override
    protected void dispatch(ConfirmHandler confirmHandler) {
        confirmHandler.onConfirm(this);
    }
}