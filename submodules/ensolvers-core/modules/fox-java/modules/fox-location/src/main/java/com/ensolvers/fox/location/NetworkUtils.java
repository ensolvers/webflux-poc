package com.ensolvers.fox.location;

public class NetworkUtils {
  private NetworkUtils() {
  }

  public static boolean isValidIPv4Address(String ip) {
    return (ip.matches("^(([0-9]|[1-9][0-9]|1[0-9]{2}|2[0-4][0-9]|25[0-5])\\.){3}([0-9]|[1-9][0-9]|1[0-9]{2}|2[0-4][0-9]|25[0-5])$"));
  }

  public static Boolean isValidIPv6Address(String ip) {
    int simplification = 0;
    int partCounter = 0;
    StringBuilder hexadecimalValue = new StringBuilder();

    // Check if the first and last characters are ::, otherwise the ip is invalid
    if ((ip.charAt(0) == ':' && ip.charAt(1) != ':') || (ip.charAt(ip.length() - 1) == ':' && ip.charAt(ip.length() - 2) != ':')) {
      return false;
    }

    // Check that every hexadecimal value of the ip is valid, when reaching a ':'
    // will check if the value is a valid one.
    // If valid, it will also check if the following value is another ':'
    for (int i = 0; i < ip.length(); i++) {
      if (ip.charAt(i) == ':') {
        // check ip ip part is valid
        if (hexadecimalValue.length() > 0) {
          if (Boolean.FALSE.equals(checkIfHexadecimalValueIsValid(hexadecimalValue.toString()))) {
            return false;
          } else {
            partCounter = partCounter + 1;
            hexadecimalValue.setLength(0);
          }
        }
        if (ip.length() > (i + 1) && (ip.charAt(i + 1) == ':')) {
          simplification = simplification + 1;
        }
      } else {
        hexadecimalValue.append(ip.charAt(i));
      }
      // If there are more than one simplification (::) the ip address is not valid
      if (simplification > 1) {
        return false;
      }
    } // end of for cycle

    // Check last value
    if (Boolean.FALSE.equals(checkIfHexadecimalValueIsValid(hexadecimalValue.toString()))) {
      return false;
    } else {
      partCounter = partCounter + 1;
    }
    // PartCounter should be 8 without simplification
    // Or less than 8 with 1 simplification
    return ((simplification == 1 && partCounter < 8) || (simplification == 0 && partCounter == 8));
  }

  private static Boolean checkIfHexadecimalValueIsValid(String hexadecimal) {
    try {
      if (hexadecimal.length() > 4) {
        return false;
      }
      Long.valueOf(hexadecimal, 16);
      return true;
    } catch (Exception e) {
      return false;
    }
  }
}
