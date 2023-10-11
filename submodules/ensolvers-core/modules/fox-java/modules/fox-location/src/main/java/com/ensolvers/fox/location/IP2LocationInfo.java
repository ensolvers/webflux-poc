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

public class IP2LocationInfo implements Comparable<IP2LocationInfo> {

  private long fromIP;
  private long toIP;
  private String countryCode;
  private String countryName;
  private String regionName;
  private String cityName;
  private String continentCode;
  private double lat;
  private double lng;

  public long getFromIP() {
    return fromIP;
  }

  public void setFromIP(long fromIP) {
    this.fromIP = fromIP;
  }

  public long getToIP() {
    return toIP;
  }

  public void setToIP(long toIP) {
    this.toIP = toIP;
  }

  public String getCountryCode() {
    return countryCode;
  }

  public void setCountryCode(String countryCode) {
    this.countryCode = countryCode;
  }

  public String getCountryName() {
    return countryName;
  }

  public void setCountryName(String countryName) {
    this.countryName = countryName;
  }

  public String getRegionName() {
    return regionName;
  }

  public void setRegionName(String regionName) {
    this.regionName = regionName;
  }

  public String getCityName() {
    return cityName;
  }

  public void setCityName(String cityName) {
    this.cityName = cityName;
  }

  public double getLat() {
    return lat;
  }

  public void setLat(double lat) {
    this.lat = lat;
  }

  public double getLng() {
    return lng;
  }

  public void setLng(double lng) {
    this.lng = lng;
  }

  public String getContinentCode() {
    return continentCode;
  }

  public void setContinentCode(String continentCode) {
    this.continentCode = continentCode;
  }

  public int compareTo(IP2LocationInfo o) {
    if (this.fromIP <= o.fromIP && this.toIP >= o.toIP) {
      return 0;
    }

    if (this.toIP <= o.fromIP) {
      return -1;
    } else {
      return 1;
    }
  }

  /**
   * This method is only for IPv6 processing. Depending on the application's IPv6 traffic, a real location service for
   * IPv6 would be created
   *
   * @return a mocked location
   */
  public static IP2LocationInfo generateMockLocation() {
    IP2LocationInfo mockLocation = new IP2LocationInfo();
    String mockData = "IPv6MOCK";
    mockLocation.setContinentCode(mockData);
    mockLocation.setCountryCode(mockData);
    mockLocation.setCountryName(mockData);
    mockLocation.setCityName(mockData);
    return mockLocation;
  }
}
