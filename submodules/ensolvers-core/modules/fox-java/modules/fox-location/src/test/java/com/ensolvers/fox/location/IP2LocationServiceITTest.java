/* Copyright (c) 2021 Ensolvers
 * All Rights Reserved
 *
 * The contents of this file is dual-licensed under 2 alternative Open Source/Free licenses: LGPL 2.1 or later and
 * Apache License 2.0. (starting with JNA version 4.0.0).
 *
 * You can freely decide which license you want to apply to the project.
 *
 * You may obtain a copy of the LGPL License at: http://www.gnu.org/licenses/licenses.html
 *
 * A copy is also included in the downloadable source code package
 * containing JNA, in file "LGPL2.1".
 *
 * You may obtain a copy of the Apache License at: http://www.apache.org/licenses/
 *
 * A copy is also included in the downloadable source code package
 * containing JNA, in file "AL2.0".
 */
package com.ensolvers.fox.location;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.junit.platform.commons.logging.Logger;
import org.junit.platform.commons.logging.LoggerFactory;

class IP2LocationServiceITTest {

  @Test
  @Disabled("")
  void testLocation() throws InterruptedException, IPV4NotFoundException, IPV6NotFoundException, InvalidIPException {
    IP2LocationService service = IP2LocationService.getInstance(true);
    Thread.sleep(100);

    long fromTime = System.currentTimeMillis();
    IP2LocationInfo info = service.getInfoFor("190.106.47.14");
    long toTime = System.currentTimeMillis();

    assertNotNull(info.getCountryCode());
    assertNotNull(info.getCountryName());
    assertNotNull(info.getCityName());
    assertNotEquals(0, info.getLat());
    assertNotEquals(0, info.getLng());
    System.out.println("Perf:" + (toTime - fromTime) + " (millis)");

    fromTime = System.currentTimeMillis();
    info = service.getInfoFor("192.91.253.248");
    toTime = System.currentTimeMillis();

    assertNotNull(info.getCountryCode());
    assertNotNull(info.getCountryName());
    assertNotNull(info.getCityName());
    assertNotEquals(0, info.getLat());
    assertNotEquals(0, info.getLng());
    System.out.println("Perf:" + (toTime - fromTime) + " (millis)");
    assertTrue(toTime - fromTime < 5);

    fromTime = System.currentTimeMillis();
    info = service.getInfoFor("136.0.16.217");
    toTime = System.currentTimeMillis();

    assertNotNull(info.getCountryCode());
    assertNotNull(info.getCountryName());
    assertNotNull(info.getCityName());
    assertNotEquals(0, info.getLat());
    assertNotEquals(0, info.getLng());
    System.out.println("Perf:" + (toTime - fromTime) + " (millis)");
    assertTrue(toTime - fromTime < 5);

    fromTime = System.currentTimeMillis();
    for (int i = 0; i < 10000000; i++) {
      info = service.getInfoFor("104.236.195.72");
    }
    toTime = System.currentTimeMillis();

    assertNotNull(info.getCountryCode());
    assertNotNull(info.getCountryName());
    assertNotNull(info.getCityName());
    assertNotEquals(0, info.getLat());
    assertNotEquals(0, info.getLng());
    System.out.println("Perf:" + (toTime - fromTime) + " (millis)");
    assertTrue(toTime - fromTime < 30000); // this may fail depending on the machine
  }

  @Test
  @Disabled("Test gets hang")
  void testIsEU() throws InterruptedException, IPV4NotFoundException, IPV6NotFoundException, InvalidIPException {
    IP2LocationService service = IP2LocationService.getInstance(false);
    Thread.sleep(100);

    String[] europeIPs = new String[] { "217.171.223.0", "217.212.238.0", "217.217.253.0", "217.223.255.0", "221.120.144.0" };
    String[] nonEuropeIPs = new String[] { "217.139.235.0", "217.146.5.0", "222.165.163.0", "223.252.112.0", "223.255.255.0" };

    for (String euIP : europeIPs) {
      IP2LocationInfo info = service.getInfoFor(euIP);
      assertEquals("EU", info.getContinentCode());
      assertTrue(service.isIpFromEU(euIP));
    }

    for (String noEuIP : nonEuropeIPs) {
      IP2LocationInfo info = service.getInfoFor(noEuIP);
      assertNotEquals("EU", info.getContinentCode());
      assertFalse(service.isIpFromEU(noEuIP));
    }
  }

  @Test
  void testValidIp() {
    // IPv4
    assertTrue(NetworkUtils.isValidIPv4Address("69.89.31.226"));
    assertTrue(NetworkUtils.isValidIPv4Address("0.0.0.0"));
    assertTrue(NetworkUtils.isValidIPv4Address("255.255.255.255"));
    assertTrue(NetworkUtils.isValidIPv4Address("192.0.168.1"));
    assertFalse(NetworkUtils.isValidIPv4Address("192.0.168"));
    assertFalse(NetworkUtils.isValidIPv4Address("192.0.168:1"));
    assertFalse(NetworkUtils.isValidIPv4Address("192.0.168.1."));
    assertFalse(NetworkUtils.isValidIPv4Address("192.0.A.1"));
    assertFalse(NetworkUtils.isValidIPv4Address("192.0.256.1"));
    assertFalse(NetworkUtils.isValidIPv4Address("192.0.168.-1"));
    assertFalse(NetworkUtils.isValidIPv4Address("192_0_168_1"));

    // IPv6
    assertTrue(NetworkUtils.isValidIPv6Address("2001:0DB8::1428:57ab"));
    assertTrue(NetworkUtils.isValidIPv6Address("2001:0DB8::CD30"));
    assertTrue(NetworkUtils.isValidIPv6Address("2001:0DB8:0000:0000:0000::1428:57ab"));
    assertFalse(NetworkUtils.isValidIPv6Address("ABCD::6789:ABCD::EF01"));
    assertFalse(NetworkUtils.isValidIPv6Address("2001::0DB8::CD30"));
    assertFalse(NetworkUtils.isValidIPv6Address("2001:DB8Z:2de::e13"));
    assertFalse(NetworkUtils.isValidIPv6Address("2001:DB8:2de::e13:"));
    assertFalse(NetworkUtils.isValidIPv6Address("2001:0DB85::CD30"));
    assertFalse(NetworkUtils.isValidIPv6Address("2001:0DB8:1001:0000:1001::1428:57ab:AC43"));
    assertFalse(NetworkUtils.isValidIPv6Address("2001:0DB8:1428:57ab:AC43"));
    assertFalse(NetworkUtils.isValidIPv6Address("2001:DB8:::1400:57ab:AC43"));
  }

  /**
   * Should turn an IPv6 Address to a number. Fn is not implemented on the IP2LocationService
   */
  @Disabled("Functionality currently out of use")
  @Test
  void shouldTurnIpv6ToNumber() {
    IP2LocationService service = IP2LocationService.getInstance(true);
    Assertions.assertTrue(true);
  }
}
