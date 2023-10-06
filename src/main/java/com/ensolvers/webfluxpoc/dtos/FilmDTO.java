package com.ensolvers.webfluxpoc.dtos;

import com.ensolvers.webfluxpoc.models.Film;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class FilmDTO {
  private String title;
  private String description;
  private Integer releaseYear;
  private Integer rentalDuration;
  private Integer rentalRate;
  private Integer length;
  private Double replacementCost;
  private String rating;
  private String specialFeatures;

  public static FilmDTO toDto(Film film) {
    FilmDTO dto = new FilmDTO();
    dto.setTitle(film.getTitle());
    dto.setDescription(film.getDescription());
    dto.setLength(film.getLength());
    dto.setRating(film.getRating());
    dto.setReleaseYear(film.getReleaseYear());
    dto.setRentalDuration(film.getRentalDuration());
    dto.setRentalRate(film.getRentalRate());
    dto.setReplacementCost(film.getReplacementCost());
    dto.setSpecialFeatures(film.getSpecialFeatures());
    return dto;
  }

}
