package ginious.home.measure.device.radio;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;

import ginious.home.measure.model.DeviceConfiguration;
import lombok.AccessLevel;
import lombok.Data;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

//Lombok
@Data
@NoArgsConstructor
@Getter(AccessLevel.PROTECTED)
@Setter(AccessLevel.PUBLIC)
// Spring
@Configuration
@ConfigurationProperties(RadioConfig.CONFIG_PREFIX)
@PropertySource("classpath:radio-device-default.properties")
public class RadioConfig implements DeviceConfiguration {

  public static final String CONFIG_PREFIX = "device.radio";
  
  private String protocols;

  @Getter(AccessLevel.PUBLIC)
  private boolean enabled;
}
