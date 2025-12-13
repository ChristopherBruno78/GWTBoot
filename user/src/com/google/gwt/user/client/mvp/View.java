package com.google.gwt.user.client.mvp;

import com.google.gwt.user.client.ui.Composite;

public abstract class View<T extends ViewPresenter> extends Composite {
  protected T presenter;

  protected void setPresenter(T presenter) {
    this.presenter = presenter;
  }

  protected abstract void bind();

  @Override
  protected void onAttach() {
    super.onAttach();
    this.presenter.onViewLoaded();
  }

  @Override
  protected void onDetach() {
    super.onDetach();
    this.presenter.onViewDismissed();
  }
}
