package com.google.gwt.event.logical.shared;

import com.google.gwt.event.shared.EventHandler;

public interface ConfirmHandler extends EventHandler {
    void onConfirm(ConfirmEvent confirmEvent);
}