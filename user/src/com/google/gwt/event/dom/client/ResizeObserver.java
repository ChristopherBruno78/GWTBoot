package com.edusoftwerks.gwt.mosaic.widgets.client.events.resize;

import com.google.gwt.dom.client.Element;
import jsinterop.annotations.JsConstructor;
import jsinterop.annotations.JsFunction;
import jsinterop.annotations.JsPackage;
import jsinterop.annotations.JsType;
import com.google.gwt.dom.client.DOMRect;

/**
 * JS interop class for native ResizeObserver.
 */
@JsType(isNative = true, name = "ResizeObserver", namespace = JsPackage.GLOBAL)
public class ResizeObserver {

    @JsFunction
    @FunctionalInterface
    public interface Callback {
        void onResize(ResizeObserverEntry[] entries, ResizeObserver observer);
    }

    public ResizeObserver(Callback callback) {
    }

    public native void observe(Element element);

    public native void unobserve(Element element);

    public native void disconnect();

    /**
     * JS interop class for native ResizeObserverEntry.
     */
    @JsType(isNative = true, name = "ResizeObserverEntry", namespace = JsPackage.GLOBAL)
    public static class ResizeObserverEntry {
        public Element target;
        public native DOMRect contentRect();
    }
}
