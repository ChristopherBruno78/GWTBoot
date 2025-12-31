/*
 * Copyright 2006 Google Inc.
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
package com.google.gwt.user.client;

import com.google.gwt.core.client.Scheduler;

/**
 * Encapsulates an action for later execution, often from a different context.
 * 
 * <p>
 * The Command interface provides a layer of separation between the code
 * specifying some behavior and the code invoking that behavior. This separation
 * aids in creating reusable code.
 * </p>
 * 
 * <p>
 * The Command interface is often implemented with an anonymous inner class. For
 * example,
 * 
 * <pre>
 * Command sayHello = new Command() {
 *   public void execute() {
 *     Window.alert("Hello");
 *   }
 * };
 * sayHello.execute();
 * </pre>
 * 
 * </p>
 * This type extends ScheduledCommand to help migrate from DeferredCommand API.
 */
public interface Command extends Scheduler.ScheduledCommand {
  /**
   * Causes the Command to perform its encapsulated behavior.
   */
  void execute();
}
