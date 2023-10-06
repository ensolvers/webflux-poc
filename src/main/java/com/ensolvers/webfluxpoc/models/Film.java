package com.ensolvers.webfluxpoc.models;

import lombok.Getter;
import lombok.Setter;
import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Table;

import java.time.LocalDateTime;

@Getter
@Setter
@Table(name = "film")
public class Film {
  @Id
  private Long filmId;
  private String title;
  private String description;
  private Integer releaseYear;
  private Long languageId;
  private Long originalLanguageId;
  private Integer rentalDuration;
  private Integer rentalRate;
  private Integer length;
  private Double replacementCost;
  private String rating;
  private String specialFeatures;
  private LocalDateTime lastUpdate;

  public Film() {}

}
