package ginious.home.measure.service.mqtt;

import org.apache.commons.lang.StringUtils;
import org.eclipse.paho.client.mqttv3.MqttClient;
import org.eclipse.paho.client.mqttv3.MqttConnectOptions;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Service;

import ginious.home.measure.model.AbstractService;
import ginious.home.measure.model.Measure;
import lombok.extern.slf4j.Slf4j;

/**
 * Service publishing measure changes to a MQTT broker.
 */
@Slf4j
@Service
@ConditionalOnProperty(prefix = MqttServiceConfig.CONFIG_PREFIX, name = "enabled", matchIfMissing = false)
public final class MqttService extends AbstractService {

  @Autowired
  private MqttServiceConfig config;

  /**
   * MQTT broker client for publishing measures.
   */
  private MqttClient brokerClient;

  /**
   * Default constructor.
   */
  public MqttService() {
    super();
  }

  protected void measureChangedCustom(Measure inChangedMeasure) {

    try {
      MqttClient lClient = getBrokerClient();
      if (lClient != null) {
        lClient.publish(config.getTopic_root() + "/" + inChangedMeasure.getDeviceId() + "/"
            + inChangedMeasure.getId(), inChangedMeasure.getValue().getBytes(), 0, false);
        log.debug("Published measure [{}.{}={}]", inChangedMeasure.getDeviceId(),
            inChangedMeasure.getId(), inChangedMeasure.getValue());
      }
      else {
        log.warn("Skipped publishing of measure for measure [{}.{}={}] due to a previous problem!",
            inChangedMeasure.getDeviceId(), inChangedMeasure.getId(), inChangedMeasure.getValue());
      } // else
    }
    catch (MqttException e) {
      log.error("Failed to publish changed measure [{}.{}={}] to MQTT broker - Reason: {}",
          inChangedMeasure.getDeviceId(), inChangedMeasure.getId(), inChangedMeasure.getValue(),
          e.getMessage());
    } // catch
  }

  /**
   * Creates and gets the client used for accessing the MQTT broker. Creation will
   * only take place if this is the first call or the connection is not open
   * anymore.
   * 
   * @return The client or <code>null</code> in case that no connection could be
   *         established.
   */
  private MqttClient getBrokerClient() {

    if (brokerClient == null || !brokerClient.isConnected()) {

      MqttConnectOptions lOptions = new MqttConnectOptions();
      lOptions.setConnectionTimeout(60);
      lOptions.setAutomaticReconnect(false);

      // User
      if (StringUtils.isNotBlank(config.getBroker_user())) {
        lOptions.setUserName(config.getBroker_user());
      } // if

      // Password
      if (StringUtils.isNotBlank(config.getBroker_password())) {
        lOptions.setPassword(config.getBroker_password().toCharArray());
      } // if

      log.info("Connecting MQTT using [{}/{}] to [{}]",
          config.getBroker_user() != null ? config.getBroker_user() : "<anonymous>",
          config.getBroker_password() != null ? "*******" : "none", config.getBroker_url());

      if (config.getBroker_url() == null) {
        log.error("Setting [" + MqttServiceConfig.CONFIG_PREFIX + ".broker-url] is missing!");
        return null;
      } // if

      // connect
      try {
        brokerClient = new MqttClient(config.getBroker_url(), getClass().getName());
        brokerClient.connect(lOptions);
      }
      catch (Throwable t) {
        log.error("Failed to connect to MQTT broker [" + config.getBroker_url() + "]!", t);
      } // catch
    } // else

    return brokerClient;
  }

  @Override
  protected void initCustom() {
    initIncludedMeasures(config.getIncluded_measures());
  }
}