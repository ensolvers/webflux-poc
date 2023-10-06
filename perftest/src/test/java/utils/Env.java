package utils;

public enum Env {
  DEV {
    @Override
    public String getAPIUrl() {
      return "http://localhost:8080";
    }
  };

  public abstract String getAPIUrl();
}
