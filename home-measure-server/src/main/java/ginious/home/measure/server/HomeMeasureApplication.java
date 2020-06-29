package ginious.home.measure.server;

import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.apache.commons.lang.StringUtils;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

import ginious.home.measure.model.HasIdentifier;
import ginious.home.measure.model.MeasurementDevice;
import ginious.home.measure.model.RunnableMeasurementDeviceSupport;
import ginious.home.measure.model.Service;
import lombok.extern.slf4j.Slf4j;

/**
 * Spring Boot application entry point logging available devices and services
 * and their state. Active devices will be started.
 */
@Slf4j
@SpringBootApplication
@ComponentScan({"ginious.home.measure"})
public class HomeMeasureApplication implements ApplicationRunner {

  private ApplicationContext context;
  private ThreadPoolTaskExecutor executor;
  private List<Service> services;
  private List<MeasurementDevice> devices;

  /**
   * DI constructor called by Spring.
   * 
   * @param inContext
   *          The context of the application used for searching available bean implementations.
   * @param inServices
   *          The services that are available below package ginious.home.measure.
   * @param inDevices
   *          Devices that are available below package ginious.home.measure.
   * @param inTaskExec
   *          The executor used for executing devices.
   */
  public HomeMeasureApplication(ApplicationContext inContext, List<Service> inServices,
      List<MeasurementDevice> inDevices, ThreadPoolTaskExecutor inTaskExec) {

    context = inContext;
    executor = inTaskExec;
    services = inServices;
    devices = inDevices;
  }

  public static void main(String[] args) {

    SpringApplication.run(HomeMeasureApplication.class, args);
  }

  @Override
  public void run(ApplicationArguments inArgs) throws Exception {

    // identify available service ids
    Set<String> lActiveServiceIds = new HashSet<>();
    if (services != null && !services.isEmpty()) {
      services.forEach(s -> lActiveServiceIds.add(s.getId()));
    }
    else {
      log.error(
          "***** No services found in classpath. Enable in application.properties: e.g. 'service.jdbc.enabled=true'");
    } // else

    // identify available devices and start each
    Set<String> lActiveDeviceIds = new HashSet<>();
    if (devices != null && !devices.isEmpty()) {
      devices.forEach(d -> {
        executor.execute(new RunnableMeasurementDeviceSupport(d));
        lActiveDeviceIds.add(d.getId());
      });
    }
    else {

      // exit when no devices are active
      log.error(
          "***** No devices found in classpath - exiting. Enable in application.properties: e.g. 'device.demo.enabled=true'");
      System.exit(1);
    } // else

    // log devices and services

    log.info("*");

    logInfos(MeasurementDevice.class, lActiveDeviceIds);
    logInfos(Service.class, lActiveServiceIds);

    log.info("*************************************************************************");
    log.info("* activate|deactivate device|service in application.properties:");
    log.info("* 'device|service.NAME.enabled=true|false' ");
    log.info("*************************************************************************");
    log.info("*************************************************************************");
    log.info("*");
  }

  private void logInfos(Class<? extends HasIdentifier> inHasId, Set<String> inActiveEntityIds) {

    log.info("*************************************************************************");
    log.info("* {}s:", inHasId.getSimpleName().toLowerCase());
    Arrays.asList(context.getBeanNamesForType(inHasId)).forEach(d -> {

      String lActiveEntityId = StringUtils
          .remove(StringUtils.remove(d, "MeasurementDevice"), "Service").toLowerCase();
      log.info("* \t - {}",
          lActiveEntityId + (inActiveEntityIds.contains(lActiveEntityId) ? " -> active" : ""));
    });
  }
}