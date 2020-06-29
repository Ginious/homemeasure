package ginious.home.measure.service.mqtt;

import org.apache.commons.lang.StringUtils;
import org.eclipse.paho.client.mqttv3.MqttClient;
import org.eclipse.paho.client.mqttv3.MqttConnectOptions;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Service;

import ginious.home.measure.model.AbstractService;
import ginious.home.measure.model.Measure;
import ginious.home.measure.model.MeasureCache;
import lombok.extern.slf4j.Slf4j;

/**
 * Service publishing measure changes to a MQTT broker.
 */
@Slf4j
@Service
@ConditionalOnProperty(prefix = MqttServiceConfig.CONFIG_PREFIX, name = "enabled", matchIfMissing = false)
public final class MqttService extends AbstractService<MqttServiceConfig> {

  /**
   * MQTT broker client for publishing measures.
   */
  private MqttClient brokerClient;

  /**
   * DI constructor called by Spring.
   * 
   * @param inCache
   *          The cache for measurements.
   * @param inConfig
   *          The service configuration.
   */
  public MqttService(MeasureCache inCache, MqttServiceConfig inConfig) {
    super(inCache, inConfig);
  }

  protected void measureChangedCustom(Measure inChangedMeasure) {

    try {
      MqttClient lClient = getBrokerClient();
      if (lClient != null) {
        lClient.publish(getConfig().getTopic_root() + "/" + inChangedMeasure.getDeviceId() + "/"
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
      lOptions.setConnectionTimeout(0);
      lOptions.setAutomaticReconnect(true);

      // User
      if (StringUtils.isNotBlank(getConfig().getBroker_user())) {
        lOptions.setUserName(getConfig().getBroker_user());
      } // if

      // Password
      if (StringUtils.isNotBlank(getConfig().getBroker_password())) {
        lOptions.setPassword(getConfig().getBroker_password().toCharArray());
      } // if

      if (getConfig().getBroker_url() == null) {
        log.error("Setting [" + MqttServiceConfig.CONFIG_PREFIX + ".broker-url] is missing!");
        return null;
      } // if

      // connect
      try {
        brokerClient = new MqttClient(getConfig().getBroker_url(), getClass().getName());
        brokerClient.connect(lOptions);
      }
      catch (Throwable t) {
        log.error("Failed to connect to MQTT broker [" + getConfig().getBroker_url() + "]!", t);
      } // catch
    } // else

    return brokerClient;
  }

  @Override
  protected void initCustom() {
    initIncludedMeasures(getConfig().getIncluded_measures());

    log.info("Connecting MQTT using [{}/{}] to [{}]",
        getConfig().getBroker_user() != null ? getConfig().getBroker_user() : "<anonymous>",
        getConfig().getBroker_password() != null ? "*******" : "none", getConfig().getBroker_url());
  }
}