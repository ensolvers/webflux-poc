package com.ensolvers.webfluxpoc.models;

import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Table;

import java.time.LocalDateTime;

@Data
@Table(name = "film")
public class Film {
  @Id
  public Long filmId;
  public String title;
  public String description;
  public Integer releaseYear;
  public Long languageId;
  public Long originalLanguageId;
  public Integer rentalDuration;
  public Integer rentalRate;
  public Integer length;
  public Double replacementCost;
  public String rating;
  public String specialFeatures;
  public LocalDateTime lastUpdate;

}
