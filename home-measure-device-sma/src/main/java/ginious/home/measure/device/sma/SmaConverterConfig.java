package ginious.home.measure.device.sma;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;

import ginious.home.measure.model.DeviceConfiguration;
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
@ConfigurationProperties(SmaConverterConfig.CONFIG_PREFIX)
@PropertySource("classpath:smaconverter-device-default.properties")
public class SmaConverterConfig implements DeviceConfiguration {

  public static final String CONFIG_PREFIX = "device.smaconverter";

  @NonNull
  private Integer converter_id;

  @NonNull
  private String ip_address;

  @NonNull
  private Integer port;

  @NonNull
  private Integer timeout;

  private Float salesper_kwh;

  @Getter(AccessLevel.PUBLIC)
  private boolean enabled;
}