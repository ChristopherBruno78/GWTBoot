package com.google.gwt.resources.client;

import com.google.gwt.resources.ext.DefaultExtensions;
import com.google.gwt.resources.ext.ResourceGeneratorType;
import com.google.gwt.resources.rg.StyleResourceGenerator;

/**
 * A resource that contains plain css that should be incorporated into the compiled
 * output.
 */
@DefaultExtensions(value = {".css"})
@ResourceGeneratorType(StyleResourceGenerator.class)
public interface StyleResource extends ResourcePrototype {
    String getText();
    boolean ensureInjected();
    boolean ensureInjectedAtStart();
    boolean ensureInjectedAtEnd();
}