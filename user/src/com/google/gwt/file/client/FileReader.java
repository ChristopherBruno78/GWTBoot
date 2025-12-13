package com.google.gwt.file.client;

import com.google.gwt.dom.client.InputElement;
import com.google.gwt.dom.client.Style;
import com.google.gwt.user.client.DOM;
import com.google.gwt.user.client.Event;
import com.google.gwt.user.client.rpc.AsyncCallback;

public class FileReader {
  private static FileReader INSTANCE;
  private final InputElement input;
  private AsyncCallback<String> callback;
  private FileReaderMode mode;

  public static FileReader get() {
    if (INSTANCE == null) {
      INSTANCE = new FileReader();
    }
    return INSTANCE;
  }

  private FileReader() {
    input = InputElement.as(DOM.createElement("input"));
    input.getStyle().setDisplay(Style.Display.NONE);
    input.setAttribute("type", "file");
    DOM.sinkEvents(input, Event.ONCHANGE);
    DOM.setEventListener(
      input,
      event -> {
        File file = getFile(event);
        if (file == null) {
          FileReader.this.callback = null;
          return;
        }
        final NativeFileReader reader = new NativeFileReader();
        reader.addEventListener(
          "load",
          fileLoadEvt -> onContentRead(getContent((Event) fileLoadEvt))
        );

        if (mode == FileReaderMode.DATA_URL) {
          reader.readAsDataURL(file);
        } else if (mode == FileReaderMode.TEXT) {
          reader.readAsText(file);
        }
      }
    );
  }

  private native String getContent(Event event) /*-{
        return event.target.result;
    }-*/;

  private native File getFile(Event event) /*-{
        return event.target.files[0];
    }-*/;

  private void onContentRead(String content) {
    if (callback != null) {
      callback.onSuccess(content);
    }
    input.setValue("");
    callback = null;
  }

  public void prompt(FileReaderMode mode, AsyncCallback<String> callback) {
    this.mode = mode;
    this.callback = callback;
    input.click();
  }
}
