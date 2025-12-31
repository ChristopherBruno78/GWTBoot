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
 * Represents a logical form submit event.
 */
public class FormSubmitEvent extends GwtEvent<FormSubmitHandler> {

  /**
   * Handler type.
   */
  private static Type<FormSubmitHandler> TYPE;

  /**
   * Fires a form submit event on all registered handlers in the handler
   * manager. If no such handlers exist, this method will do nothing.
   *
   * @param source the source of the handlers
   */
  public static void fire(HasFormSubmitHandlers source) {
    if (TYPE != null) {
      FormSubmitEvent event = new FormSubmitEvent();
      source.fireEvent(event);
    }
  }

  /**
   * Gets the type associated with this event.
   *
   * @return returns the handler type
   */
  public static Type<FormSubmitHandler> getType() {
    if (TYPE == null) {
      TYPE = new Type<FormSubmitHandler>();
    }
    return TYPE;
  }

  /**
   * Creates a new form submit event.
   */
  protected FormSubmitEvent() {
  }

  @Override
  public final Type<FormSubmitHandler> getAssociatedType() {
    return TYPE;
  }

  @Override
  protected void dispatch(FormSubmitHandler handler) {
    handler.onFormSubmit(this);
  }
}
