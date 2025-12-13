package com.google.gwt.user.client;

public class MetaData {

  public static native String meta(String name) /*-{
        var metaTag  = $doc.querySelector("meta[name=\"" + name + "\"]");
        if( metaTag ){
            return metaTag.content;
        }
        return null;
    }-*/;
}
