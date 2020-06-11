package ginious.home.measure.service.influxdb;

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
@ConfigurationProperties(InfluxDbServiceConfig.CONFIG_PREFIX)
@PropertySource("classpath:influxdbservice-default.properties")
public class InfluxDbServiceConfig implements ginious.home.measure.model.Configuration {

  public static final String CONFIG_PREFIX = "service.influxdb";
  
  @NonNull 
  private String included_measures;
  
  @NonNull
  private String url;
  
  @NonNull
  private String dbname;

  @NonNull
  private String user;

  @NonNull
  private String password;

  @NonNull
  private Integer flush_duration;
  
  @Getter(AccessLevel.PUBLIC)
  private boolean enabled;
}