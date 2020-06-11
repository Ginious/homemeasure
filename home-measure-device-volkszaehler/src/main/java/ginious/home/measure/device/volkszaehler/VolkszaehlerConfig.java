package ginious.home.measure.device.volkszaehler;

import java.util.Date;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;

import lombok.AccessLevel;
import lombok.Data;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.NonNull;
import lombok.Setter;

// Lombok
@Data
@NoArgsConstructor
@Getter(AccessLevel.PROTECTED)
@Setter(AccessLevel.PUBLIC)
// Spring
@Configuration
@ConfigurationProperties(VolkszaehlerConfig.CONFIG_PREFIX)
@PropertySource("classpath:volkszaehler-default.properties")
public class VolkszaehlerConfig implements ginious.home.measure.model.Configuration {
  
  public static final String CONFIG_PREFIX = "device.volkszaehler";
  
  private String meter_solar;
  
  private float costperkwh_ht;
  private float costperkwh_nt;
  private float salesperkwh_solar;

  @NonNull
  private String meter_total;

  @NonNull
  private String meter_nt;
  

  @NonNull
  private String meter_ht;

  @NonNull
  private String meter_actual;

  @NonNull
  private Date interval_start;

  @NonNull
  private String device;

  @NonNull
  private Integer baudrate;

  @Getter(AccessLevel.PUBLIC)
  private boolean enabled;  
}