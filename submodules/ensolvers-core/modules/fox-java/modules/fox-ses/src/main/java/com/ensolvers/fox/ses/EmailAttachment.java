package com.ensolvers.fox.ses;

import java.io.File;

public class EmailAttachment {
  private File file;
  private String name;

  public EmailAttachment() {
  }

  public EmailAttachment(File file, String name) {
    this.file = file;
    this.name = name;
  }

  public File getFile() {
    return file;
  }

  public void setFile(File file) {
    this.file = file;
  }

  public String getName() {
    return name;
  }

  public void setName(String name) {
    this.name = name;
  }

  public EmailAttachment withFile(File file) {
    this.file = file;
    return this;
  }

  public EmailAttachment withName(String name) {
    this.name = name;
    return this;
  }

}
