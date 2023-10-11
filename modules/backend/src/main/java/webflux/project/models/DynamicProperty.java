package webflux.project.models;

import java.sql.Timestamp;
import lombok.Getter;
import lombok.Setter;
import org.springframework.data.relational.core.mapping.Column;
import org.springframework.data.relational.core.mapping.Table;

/**
 * Represents a Dynamic Application Property in the Platform
 *
 * @author Sebastian Javier Guzman
 */
@Getter
@Setter
@Table("core_dynamic_property")
public class DynamicProperty extends GenericModel {

  @Column("prop_key")
  private String key;

  private String content;

  private Long sinceVersion;

  private Timestamp createdAt;

  private Timestamp updatedAt;

  public DynamicProperty() {
  }

  /**
   * Generates a Dynamic Property instance
   *
   * @param key          Unique Property key
   * @param content      Dynamic property content/value
   * @param sinceVersion Minimum version since this key will be available
   */
  public DynamicProperty(String key, String content, Long sinceVersion) {
    this.key = key;
    this.content = content;
    this.sinceVersion = sinceVersion;
  }

  protected boolean same(Object o) {
    return key.equals(((DynamicProperty) o).key);
  }

  public String toString() {
    return "DynamicProperty{" + "key='" + key + '\'' + ", content='" + content + '\'' + ", sinceVersion=" + sinceVersion + ", createdAt=" + createdAt
        + ", updatedAt=" + updatedAt + '}';
  }
}