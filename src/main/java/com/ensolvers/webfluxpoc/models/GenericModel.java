package com.ensolvers.webfluxpoc.models;

import org.springframework.data.annotation.Id;

import java.util.Objects;

/**
 * Class defining the core aspects of all entities (persistent objects) to be stored in the DB
 */
public abstract class GenericModel {

  @Id
  private long id;

  public boolean equals(Object o) {
    return (this == o) || o != null && id != 0 && getClass() == o.getClass() && id == getClass().cast(o).getId() && same(o);
  }

  protected boolean same(Object o) {
    return true;
  }

  public int hashCode() {
    return Objects.hash(id);
  }

  // Accessors
  public long getId() {
    return id;
  }

  public void setId(long id) {
    this.id = id;
  }

}