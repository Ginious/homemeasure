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
import ginious.home.measure.model.Service;
import ginious.home.measure.model.ServiceConfiguration;
import lombok.extern.slf4j.Slf4j;

/**
 * Spring Boot application entry point logging available devices and services
 * and their state. Active devices will be started.
 */
@Slf4j
@SpringBootApplication
@ComponentScan({ "ginious.home.measure" })
public class HomeMeasureApplication implements ApplicationRunner {

	@Autowired
	private ApplicationContext context;

	@Autowired
	private ThreadPoolTaskExecutor executor;

	@Autowired(required = false)
	private List<Service> services;

	@Autowired(required = false)
	private List<MeasurementDevice> devices;

	public static void main(String[] args) {

		SpringApplication.run(HomeMeasureApplication.class, args);
	}

	@Override
	public void run(ApplicationArguments inArgs) throws Exception {

		// identify available service ids
		Set<String> lActiveServiceIds = new HashSet<>();
		if (services != null) {
			services.forEach(s -> lActiveServiceIds.add(s.getId()));
		} // if

		// identify available devices and start each
		Set<String> lActiveDeviceIds = new HashSet<>();
		if (devices != null) {
			devices.forEach(d -> {
				executor.execute(new RunnableMeasurementDeviceSupport(d));
				lActiveDeviceIds.add(d.getId());
			});
		} else {

			// exit when no devices are active
			log.error(
					"***** No devices found in classpath. They can be enabled in application.properties e.g. 'device.demo.enabled=true'");
			System.exit(1);
		} // else

		// log devices and services

		log.info("*");

		logInfos(DeviceConfiguration.class, "Device", lActiveDeviceIds);
		logInfos(ServiceConfiguration.class, "Service", lActiveServiceIds);

		log.info("*************************************************************************");
		log.info("* activate|deactivate device|service in application.properties:");
		log.info("* 'device|service.NAME.enabled=true|false' ");
		log.info("*************************************************************************");
		log.info("*************************************************************************");
		log.info("*");
	}

	private void logInfos(Class<? extends Configuration> inTypeOfConfiguration, String inDisplayName,
			Set<String> inActiveEntityIds) {

		log.info("*************************************************************************");
		log.info("* {}s:", inDisplayName.toLowerCase());
		Arrays.asList(context.getBeanNamesForType(inTypeOfConfiguration)).forEach(d -> {

			String lActiveEntityId = StringUtils.remove(StringUtils.remove(d, "Config"), "Service");
			log.info("* \t - {}", lActiveEntityId + (inActiveEntityIds.contains(lActiveEntityId) ? " -> active" : ""));
		});
	}
}