package ginious.home.measure.server;

import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

import ginious.home.measure.model.Configuration;
import ginious.home.measure.model.DeviceConfiguration;
import ginious.home.measure.model.MeasurementDevice;
import ginious.home.measure.model.RunnableMeasurementDeviceSupport;
import ginious.home.measure.model.ServiceConfiguration;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@SpringBootApplication
@ComponentScan({"ginious.home.measure"})
public class HomeMeasureApplication implements ApplicationRunner {

  @Autowired
  private ApplicationContext context;

  @Autowired
  private ThreadPoolTaskExecutor executor;

  @Autowired(required = false)
  private List<MeasurementDevice> devices;

  public static void main(String[] args) {

    SpringApplication.run(HomeMeasureApplication.class, args);
  }

  @Override
  public void run(ApplicationArguments inArgs) throws Exception {

    Set<String> lIdsOfActiveDevices = new HashSet<>();
    if (devices != null && !devices.isEmpty()) {
      devices.forEach(d -> {
        executor.execute(new RunnableMeasurementDeviceSupport(d));
        lIdsOfActiveDevices.add(d.getId());
      });
    }
    else {
      log.error(
          "***** No devices found in classpath. They can be enabled in application.properties e.g. 'device.demo.enabled=true'");
      System.exit(1);
    } // else

    log.info("*");
    
    logInfos(DeviceConfiguration.class, "Device", lIdsOfActiveDevices);
    logInfos(ServiceConfiguration.class, "Service", new HashSet<>());

    log.info("**************************************************");
    log.info("*");
    log.info(
        "* add 'device|service.NAME.enabled=true|false' to application.properties to activate|deactivate device|service");
  }

  private void logInfos(Class<? extends Configuration> inTypeOfConfiguration, String inDisplayName,
      Set<String> inActiveEntities) {

    log.info("**************************************************");
    log.info("* {}s:", inDisplayName.toLowerCase());
    Arrays.asList(context.getBeanNamesForType(inTypeOfConfiguration)).forEach(d -> {

      String lEntityId = StringUtils.remove(StringUtils.remove(d, "Config"), "Service");
      log.info("* \t - {}", lEntityId + (inActiveEntities.contains(lEntityId) ? " -> active" : ""));
    });
  }
}