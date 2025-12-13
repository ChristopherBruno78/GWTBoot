package com.google.gwt.event.logical.shared;

import com.google.gwt.event.shared.HandlerRegistration;

public interface HasCancelHandlers {
  HandlerRegistration addCancelHandler(CancelHandler handler);
}
