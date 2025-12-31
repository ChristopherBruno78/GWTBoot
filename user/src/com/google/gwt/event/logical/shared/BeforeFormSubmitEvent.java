/*
 * Copyright 2008 Google Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 */
package com.google.gwt.event.logical.shared;

import com.google.gwt.event.shared.GwtEvent;

/**
 * Represents a before form submit event.
 */
public class BeforeFormSubmitEvent extends GwtEvent<BeforeFormSubmitHandler> {

  /**
   * Handler type.
   */
  private static Type<BeforeFormSubmitHandler> TYPE;

  /**
   * Fires a before form submit event on all registered handlers in the handler
   * manager. If no such handlers exist, this method will do nothing.
   *
   * @param source the source of the handlers
   * @return the event so that the caller can check if it was canceled, or null
   *         if no handlers of this event type have been registered
   */
  public static BeforeFormSubmitEvent fire(HasBeforeFormSubmitHandlers source) {
    // If no handlers exist, then type can be null.
    if (TYPE != null) {
      BeforeFormSubmitEvent event = new BeforeFormSubmitEvent();
      source.fireEvent(event);
      return event;
    }
    return null;
  }

  /**
   * Gets the type associated with this event.
   *
   * @return returns the handler type
   */
  public static Type<BeforeFormSubmitHandler> getType() {
    if (TYPE == null) {
      TYPE = new Type<BeforeFormSubmitHandler>();
    }
    return TYPE;
  }

  private boolean canceled;

  /**
   * Creates a new before form submit event.
   */
  protected BeforeFormSubmitEvent() {
  }

  /**
   * Cancel the before form submit event.
   *
   * Classes overriding this method should still call super.cancel().
   */
  public void cancel() {
    canceled = true;
  }

  @Override
  public final Type<BeforeFormSubmitHandler> getAssociatedType() {
    return TYPE;
  }

  /**
   * Has the form submit event already been canceled?
   *
   * @return is canceled
   */
  public boolean isCanceled() {
    return canceled;
  }

  @Override
  protected void dispatch(BeforeFormSubmitHandler handler) {
    handler.onBeforeFormSubmit(this);
  }
}
