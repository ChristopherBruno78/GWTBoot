package com.google.gwt.event.logical.shared;

import com.google.gwt.event.shared.GwtEvent;

public class CancelEvent extends GwtEvent<CancelHandler> {
  private static final Type<CancelHandler> TYPE = new Type<>();

  @Override
  public Type<CancelHandler> getAssociatedType() {
    return TYPE;
  }

  public static Type<CancelHandler> getType() {
    return TYPE;
  }

  @Override
  protected void dispatch(CancelHandler cancelHandler) {
    cancelHandler.onCancel(this);
  }
}
