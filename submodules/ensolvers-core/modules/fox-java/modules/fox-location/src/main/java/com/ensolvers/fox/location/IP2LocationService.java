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

import java.io.*;
import java.math.BigInteger;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.concurrent.locks.ReentrantLock;
import org.apache.commons.compress.archivers.ArchiveEntry;
import org.apache.commons.compress.archivers.ArchiveInputStream;
import org.apache.commons.compress.archivers.tar.TarArchiveInputStream;
import org.apache.commons.compress.compressors.gzip.GzipCompressorInputStream;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVRecord;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.junit.jupiter.api.Disabled;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The IP 2 Location services provides a convenient way of obtaining geo location information from an IP. We are
 * levering the CSV service from: https://lite.ip2location.com/
 * <p>
 * Internally, data is loaded into memory: About 50MB and then the search is done by binary search over the sorted
 * array. Performance is less than 1ms per call.
 *
 * @author Esteban Robles Luna
 */
public class IP2LocationService {

  private static final String TEST_FILE = "IP2LOCATION-test.csv";
  private static final Boolean EUROPEAN_IP_DEFAULT_VALUE = false;
  private static final Logger logger = LoggerFactory.getLogger(IP2LocationService.class);

  private static IP2LocationService instance;
  private Map<String, String> countryToContinentMap;
  private static String resourceFile = "IP2LOCATION-LITE-DB5.tar.gz";

  /**
   * Creates a new service and sets its input stream
   *
   * @return a new IP2LocationService service instance
   */
  private static IP2LocationService build() {
    InputStream io = IP2LocationService.class.getClassLoader().getResourceAsStream(resourceFile);
    return new IP2LocationService(io);
  }

  /**
   * @param ip2LocationServiceEnabled true to read the ip2location csv, false to read the test csv (smaller, faster)
   * @return an IP2LocationService service instance
   */
  public static IP2LocationService getInstance(Boolean ip2LocationServiceEnabled) {

    if (instance == null) {

      if (Boolean.FALSE.equals(ip2LocationServiceEnabled)) {
        useTestResource();
      }

      synchronized (IP2LocationService.class) {

        instance = IP2LocationService.build();

        new Thread() {
          @Override
          public void run() {
            instance.read();
          }
        }.start();

      }
    }

    return instance;
  }

  /** Use the test csv. */
  public static void useTestResource() {
    resourceFile = TEST_FILE;
  }

  /**
   * Returns true if the IP is from Europe, false otherwise
   *
   * @param ip the ip
   * @return the boolean result
   */
  public boolean isIpFromEU(String ip) {
    try {
      IP2LocationInfo info = this.getInfoFor(ip);

      if (StringUtils.isEmpty(info.getContinentCode())) {
        logger.warn("[IP2LOCATION] Failed to find continent for info: {}, {}", info.getCountryName(), info.getCityName());
        return EUROPEAN_IP_DEFAULT_VALUE;
      }

      return info.getContinentCode().equals("EU");
    } catch (IPV4NotFoundException e) {
      logger.warn(FoxStringUtils.concat("[IP2LOCATION] Error trying to get location info for ip, ipv4 not found: ", ip), e);
      return EUROPEAN_IP_DEFAULT_VALUE;
    } catch (IPV6NotFoundException e) {
      logger.warn(FoxStringUtils.concat("[IP2LOCATION] Error trying to get location info for ip, ipv6 not found: ", ip), e);
      return EUROPEAN_IP_DEFAULT_VALUE;
    } catch (InvalidIPException e) {
      logger.warn(FoxStringUtils.concat("[IP2LOCATION] Error trying to get location info for ip, invalid ip: ", ip), e);
      return EUROPEAN_IP_DEFAULT_VALUE;
    } catch (Exception e) {
      logger.warn(FoxStringUtils.concat("[IP2LOCATION] Something went wrong for when getting info for ip: ", ip), e);
      return EUROPEAN_IP_DEFAULT_VALUE;
    }
  }

  /**
   * Gets the continent for the country code provided. Will load lazily and just once.
   *
   * @param countryCode the country code
   * @return the string
   * @throws IOException the io exception
   */
  public String country2Continent(String countryCode) throws IOException {
    if (countryToContinentMap == null) {
      this.countryToContinentMap = this.createMapFromFile();
    }
    return countryToContinentMap.get(countryCode);
  }

  /**
   * Instantiates a Map with country code => continent code Used by other methods to quickly determine a country's
   * continent.
   *
   * @return
   * @throws IOException
   */
  private Map<String, String> createMapFromFile() throws IOException {
    String fileToMap = "country-continent-mapping.txt";
    InputStream io = IP2LocationService.class.getClassLoader().getResourceAsStream(fileToMap);
    String[] lines = IOUtils.toString(io, StandardCharsets.UTF_8).split("\\r?\\n");

    Map<String, String> map = new HashMap<>();

    for (String each : lines) {
      String[] data = each.split("\\s*,\\s*");
      map.put(data[0], data[1]);
    }

    return map;
  }

  // The Input stream of the CSV source
  private InputStream csvIO;
  // The list of sorted IPs obtained from the CSV
  private List<IP2LocationInfo> infos;
  // loading lock to avoid blocking when obtaining the instance
  private ReentrantLock loadingLock;

  private volatile boolean hasLoaded;

  public IP2LocationService(String csvLocation) throws FileNotFoundException {
    this.csvIO = new FileInputStream(csvLocation);
  }

  public IP2LocationService(InputStream csvIO) {
    this.csvIO = csvIO;
    this.loadingLock = new ReentrantLock();
    this.hasLoaded = false;
  }

  public void read() {
    this.loadingLock.lock();
    this.infos = new ArrayList<>(4000000);
    this.readFromTarGZ(this.csvIO);
  }

  private void basicRead(InputStream io) throws IOException {
    Iterator<CSVRecord> iterator;
    Reader reader = new InputStreamReader(io);
    CSVParser parser = new CSVParser(reader, CSVFormat.DEFAULT);

    try {
      iterator = parser.iterator();

      int i = 0;
      while (iterator.hasNext()) {
        IP2LocationInfo info = this.parse(iterator.next());
        /*
         * IMPROVEMENT (see getIpAsNumber() comments for the reason) make this.infos a MAP indexed by info.getFromIP() to search
         * faster
         */
        this.infos.add(info);
        i++;

        if (i % 10000 == 0) {
          logger.info("Read {} rows", i);
        }
      }

      parser.close();
    } catch (IOException e) {
      logger.error("Error working with stream", e);
    } finally {
      IOUtils.close(reader);
      IOUtils.close(this.csvIO);
      this.hasLoaded = true;
      this.loadingLock.unlock();
    }
  }

  private void readFromTarGZ(InputStream tarGz) {
    try (InputStream bi = new BufferedInputStream(tarGz);
        InputStream gzi = new GzipCompressorInputStream(bi);
        ArchiveInputStream o = new TarArchiveInputStream(gzi)) {

      ArchiveEntry entry = null;

      while ((entry = o.getNextEntry()) != null) {
        if (!o.canReadEntryData(entry)) {
          // log something?
          continue;
        }
        this.basicRead(o);
        return;
      }
    } catch (IOException e) {
      logger.error("Error fetching data from tar gz csv file", e);
    }
  }

  private IP2LocationInfo parse(CSVRecord csvRecord) throws IOException {
    long fromIP = Long.parseLong(csvRecord.get(0));
    long toIP = Long.parseLong(csvRecord.get(1));
    String countryCode = csvRecord.get(2);
    String countryName = csvRecord.get(3);
    String regionName = csvRecord.get(4);
    String cityName = csvRecord.get(5);
    double lat = Double.parseDouble(csvRecord.get(6));
    double lng = Double.parseDouble(csvRecord.get(7));

    IP2LocationInfo info = new IP2LocationInfo();
    info.setFromIP(fromIP);
    info.setToIP(toIP);
    info.setCountryCode(countryCode);
    info.setContinentCode(this.country2Continent(countryCode));
    info.setCountryName(countryName);
    info.setRegionName(regionName);
    info.setCityName(cityName);
    info.setLat(lat);
    info.setLng(lng);

    return info;
  }

  void waitForLoaded() {
    if (this.hasLoaded) {
      return;
    }

    this.loadingLock.lock();
    this.loadingLock.unlock();
  }

  /**
   * Returns location information about the IP.
   *
   * @param ip The IP in V4 or V6 format. Examples: 124.45.21.5 or 2001:db8:85a3::8a2e:370:7334
   * @return The information about the ip
   * @throws IPV4NotFoundException when the IPv4 has not been found.
   * @throws IPV6NotFoundException when the IPv6 has not been found.
   * @throws InvalidIPException    when the ip parameter is not an IP address
   */
  public IP2LocationInfo getInfoFor(String ip) throws IPV4NotFoundException, IPV6NotFoundException, InvalidIPException {
    if (StringUtils.isEmpty(ip)) {
      throw new InvalidIPException();
    }

    this.waitForLoaded();

    // Ipv4 logic
    if (NetworkUtils.isValidIPv4Address(ip)) {
      return getInfoForIpv4(ip);
    }

    // Ipv6 logic
    if (Boolean.TRUE.equals(NetworkUtils.isValidIPv6Address(ip))) {
      return getInfoForIpv6(ip);
    }

    throw new InvalidIPException();
  }

  private IP2LocationInfo getInfoForIpv4(String ip) throws IPV4NotFoundException {
    IP2LocationInfo toFind = new IP2LocationInfo();
    long number = this.getIpv4AsNumber(ip);
    toFind.setFromIP(number);
    toFind.setToIP(number);
    /*
     * see getIpAsNumber() comments If the data is stored in a MAP, x21 faster access is possible
     */
    int index = Collections.binarySearch(this.infos, toFind);
    if (index >= 0) {
      return this.infos.get(index);
    } else {
      throw new IPV4NotFoundException();
    }
  }

  private long getIpv4AsNumber(String ip) {

    String[] parts = ip.split("\\.");

    /*
     * IMPROVEMENTS: this is the worst case scenario: a class C address a) compute only the network part (first byte for
     * class A, two for class B, three for class C b) use the network for x21 faster search (if the data is stored in a
     * hashmap)
     */
    return (Long.valueOf(parts[0]) * 256 * 256 * 256) + (Long.valueOf(parts[1]) * 256 * 256) + (Long.valueOf(parts[2]) * 256) + (Long.valueOf(parts[3]));
  }

  private IP2LocationInfo getInfoForIpv6(String ip) throws IPV6NotFoundException {
    if (ip != null) {
      return IP2LocationInfo.generateMockLocation();
    } else {
      throw new IPV6NotFoundException();
    }
  }

  /**
   * This method returns the ipv6 address as a BigInteger due to a long not being big enough to store the value, it's not
   * in use yet because of the mock approach we are taking, in case we change the approach it might be relevant again
   *
   * @param addr the ipv6 address
   * @return ipv6 in BigInteger format
   */
  @Disabled("Currently out of use because of Mock approach")
  private BigInteger ipv6ToNumber(String addr) {
    int startIndex = addr.indexOf("::");
    if (startIndex != -1) {
      String firstStr = addr.substring(0, startIndex);
      String secondStr = addr.substring(startIndex + 2, addr.length());
      BigInteger first = BigInteger.valueOf(0);
      BigInteger second = BigInteger.valueOf(0);
      if (!firstStr.equals("")) {
        int x = this.countChar(addr, ':');
        first = ipv6ToNumber(firstStr).shiftLeft(16 * (7 - x));
      }
      if (!secondStr.equals("")) {
        second = ipv6ToNumber(secondStr);
      }
      first = first.add(second);
      return first;
    }

    String[] strArr = addr.split(":");
    BigInteger retValue = BigInteger.valueOf(0);
    for (String s : strArr) {
      BigInteger bi = new BigInteger(s, 16);
      retValue = retValue.shiftLeft(16).add(bi);
    }
    return retValue;
  }

  private int countChar(String str, char reg) {
    char[] ch = str.toCharArray();
    int count = 0;
    for (int i = 0; i < ch.length; ++i) {
      if (ch[i] == reg) {
        if (ch[i + 1] == reg) {
          break;
        }
        ++count;
      }
    }
    return count;
  }
}
