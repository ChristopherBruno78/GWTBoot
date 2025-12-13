package com.google.gwt.file.client;

import com.google.gwt.user.client.Event;
import jsinterop.annotations.JsFunction;

@JsFunction
interface EventListener {
  void handleEvent(Event evt);
}
