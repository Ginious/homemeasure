package ginious.home.measure.device.demo;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.PropertySource;
import org.springframework.stereotype.Component;

import ginious.home.measure.model.Configuration;
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
@Component
@ConfigurationProperties(DemoConfig.CONFIG_PREFIX)
@PropertySource("classpath:demo-device-default.properties")
public class DemoConfig implements DeviceConfiguration {

  public static final String CONFIG_PREFIX = "device.demo";

  private String messwert1;

  private String messwert2;

  private String messwert3;

  private String messwert4;
  
  private String messwert5;

  @Getter(AccessLevel.PUBLIC)
  private boolean enabled;
}
