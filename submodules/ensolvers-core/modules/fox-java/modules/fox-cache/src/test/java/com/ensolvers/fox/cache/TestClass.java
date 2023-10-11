package com.ensolvers.fox.cache;

public class TestClass {
  Long id;
  String stringValue;
  Integer integerValue;
  Long longValue;

  public TestClass(Long id, String stringValue, Integer integerValue, Long longValue) {
    this.id = id;
    this.stringValue = stringValue;
    this.integerValue = integerValue;
    this.longValue = longValue;
  }

  public TestClass() {
  }

  public TestClass(String stringValue, Integer integerValue) {
    this.stringValue = stringValue;
    this.integerValue = integerValue;
  }

  public Long getId() {
    return id;
  }

  public void setId(Long id) {
    this.id = id;
  }

  public String getStringValue() {
    return stringValue;
  }

  public void setStringValue(String stringValue) {
    this.stringValue = stringValue;
  }

  public Integer getIntegerValue() {
    return integerValue;
  }

  public void setIntegerValue(Integer integerValue) {
    this.integerValue = integerValue;
  }

  public Long getLongValue() {
    return longValue;
  }

  public void setLongValue(Long longValue) {
    this.longValue = longValue;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }

    TestClass testClass = (TestClass) o;

    if (!id.equals(testClass.id)) {
      return false;
    }
    if (!stringValue.equals(testClass.stringValue)) {
      return false;
    }
    if (!integerValue.equals(testClass.integerValue)) {
      return false;
    }
    return longValue.equals(testClass.longValue);
  }

  @Override
  public int hashCode() {
    int result = id.hashCode();
    result = 31 * result + stringValue.hashCode();
    result = 31 * result + integerValue.hashCode();
    result = 31 * result + longValue.hashCode();
    return result;
  }

  @Override
  public String toString() {
    return "TestClass{" + "id=" + id + ", stringValue='" + stringValue + '\'' + ", integerValue=" + integerValue + ", longValue=" + longValue + '}';
  }
}
