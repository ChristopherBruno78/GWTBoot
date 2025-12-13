package com.google.gwt.user.client.rpc;

import com.google.gwt.user.client.Console;

public interface Callback<T> extends AsyncCallback<T> {
  void onSuccess(T result);
  default void onFailure(Throwable error) {
    Console.error(error.getMessage());
  }
}
