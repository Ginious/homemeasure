package ginious.home.measure.service.jdbc;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;

import lombok.AccessLevel;
import lombok.Data;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.NonNull;
import lombok.Setter;

//Lombok
@Data
@NoArgsConstructor
@Getter(AccessLevel.PROTECTED)
@Setter(AccessLevel.PUBLIC)
// Spring
@Configuration
@ConfigurationProperties(JdbcServiceConfig.CONFIG_PREFIX)
@PropertySource("classpath:jdbc-service-default.properties")
public class JdbcServiceConfig implements ginious.home.measure.model.Configuration {

  public static final String CONFIG_PREFIX = "service.jdbc";

  @Getter(AccessLevel.PUBLIC)
  private boolean enabled;

  @NonNull
  private String included_measures;
  @NonNull
  private String tablename;
  @NonNull
  private String column_name_data;
  @NonNull
  private String column_name_device;
  @NonNull
  private String column_name_timestamp;
  @NonNull
  private String column_name_value;
}
