package ginious.home.measure.model;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;

/**
 * A common measure created filled
 */
@AllArgsConstructor
@EqualsAndHashCode
@Getter(AccessLevel.PUBLIC)
@Setter(AccessLevel.PUBLIC)
public class Measure {

  private String deviceId;
  private String id;
  private String value;
}