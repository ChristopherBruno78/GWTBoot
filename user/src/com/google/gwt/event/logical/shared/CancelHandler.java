package com.google.gwt.event.logical.shared;

import com.google.gwt.event.shared.EventHandler;

public interface CancelHandler extends EventHandler {
  void onCancel(CancelEvent cancelEvent);
}
