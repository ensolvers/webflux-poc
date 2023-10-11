package com.ensolvers.fox.location;

public class FoxStringUtils {

  private FoxStringUtils() {
  }

  public static String concat(String... strings) {
    if (strings == null || strings.length == 0) {
      return "";
    }

    StringBuilder builder = new StringBuilder();

    for (String s : strings) {
      if (s != null) {
        builder.append(s);
      }
    }

    return builder.toString();
  }
}
