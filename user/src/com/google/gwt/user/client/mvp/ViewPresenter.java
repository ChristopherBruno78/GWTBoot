package com.google.gwt.user.client.mvp;

import com.google.gwt.core.client.EntryPoint;
import com.google.gwt.user.client.ui.RootPanel;

public abstract class ViewPresenter<T extends View> implements EntryPoint {
  protected T view;

  @SuppressWarnings("unchecked")
  public ViewPresenter(T view) {
    this.view = view;
    this.view.setPresenter(this);
    this.view.bind();
  }

  public void presentView(String id) {
    var rootPanel = RootPanel.get(id);
    if (rootPanel != null) {
      rootPanel.clear();
      rootPanel.add(view);
    }
  }

  public void onModuleLoad() {
    RootPanel.get().clear();
    RootPanel.get().add(view);
  }

  protected void onViewLoaded() {}

  protected void onViewDismissed() {}
}
