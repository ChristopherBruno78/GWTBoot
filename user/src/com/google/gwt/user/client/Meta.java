package com.google.gwt.user.client;

public class Meta {

  public static native String getContent(String name) /*-{
        var metaTag  = $doc.querySelector("meta[name=\"" + name + "\"]");
        if( metaTag ){
            return metaTag.content;
        }
        return null;
    }-*/;
}
