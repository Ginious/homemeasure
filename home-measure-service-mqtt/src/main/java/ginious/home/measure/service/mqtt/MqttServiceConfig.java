package ginious.home.measure.service.mqtt;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;

import ginious.home.measure.model.ServiceConfiguration;
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
@ConfigurationProperties(MqttServiceConfig.CONFIG_PREFIX)
@PropertySource("classpath:mqtt-service-default.properties")
public class MqttServiceConfig implements ServiceConfiguration {

  public static final String CONFIG_PREFIX = "service.mqtt";
  
  @Getter(AccessLevel.PUBLIC)
  private boolean enabled;
  
  @NonNull
  private String included_measures;
  
  @NonNull
  private String broker_url;

  private String broker_password;
  private String broker_user;
  private String topic_root;
}
