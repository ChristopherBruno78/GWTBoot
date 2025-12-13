package com.google.gwt.dom.builder.client;

public class ClassNames {
  private final StringBuilder sb = new StringBuilder();

  private ClassNames() {}

  public static ClassNames start(String... classNames) {
    ClassNames cN = new ClassNames();
    int i = 0, len = classNames.length;
    for (; i < len; i++) {
      if (classNames[i] != null) {
        cN.add(classNames[i].trim());
      }
    }
    return cN;
  }

  public ClassNames add(String className) {
    return add(className, true);
  }

  public ClassNames add(String className, boolean condition) {
    if (condition) {
      sb.append(className);
      sb.append(" ");
    }
    return this;
  }

  public String build() {
    return sb.toString().trim();
  }
}
