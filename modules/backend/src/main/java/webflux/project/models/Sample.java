package webflux.project.models;

import lombok.Getter;
import lombok.Setter;
import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Table;

@Setter
@Getter
@Table(name = "sample_table")
public class Sample {
  @Id
  private Long id;
  private String externalId;
  private String title;
  private String description;
}
