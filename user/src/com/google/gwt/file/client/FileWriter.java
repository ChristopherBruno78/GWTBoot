package com.google.gwt.file.client;

import com.google.gwt.dom.client.AnchorElement;
import com.google.gwt.dom.client.Element;
import com.google.gwt.dom.client.Style;
import com.google.gwt.http.client.URL;
import com.google.gwt.user.client.DOM;
import com.google.gwt.user.client.rpc.AsyncCallback;

public class FileWriter {
  private static FileWriter INSTANCE;
  private final AnchorElement input;
  private AsyncCallback<Void> callback;

  public static FileWriter get() {
    if (INSTANCE == null) {
      INSTANCE = new FileWriter();
    }
    return INSTANCE;
  }

  public FileWriter() {
    input = AnchorElement.as(DOM.createElement("a"));
    input.getStyle().setDisplay(Style.Display.NONE);
  }

  private void onContentWritten() {
    if (callback != null) {
      callback.onSuccess(null);
    }
    callback = null;
  }

  private native void clickElement(Element a) /*-{
        a.click();
    }-*/;

  public void run(String text, String name, AsyncCallback<Void> callback) {
    this.callback = callback;
    input.setAttribute(
      "href",
      "data:text/plain;charset=utf-8," + URL.encode(text)
    );
    input.setAttribute("download", name);
    clickElement(input);
    onContentWritten();
  }
}
