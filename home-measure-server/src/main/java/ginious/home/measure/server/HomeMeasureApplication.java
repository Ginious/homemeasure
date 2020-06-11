package ginious.home.measure.server;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

import ginious.home.measure.model.MeasurementDevice;
import ginious.home.measure.model.RunnableMeasurementDeviceSupport;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@SpringBootApplication
@ComponentScan({"ginious.home.measure"})
public class HomeMeasureApplication implements ApplicationRunner {

  @Autowired
  private ThreadPoolTaskExecutor executor;

  @Autowired(required = false)
  private List<MeasurementDevice> devices;
  
  public static void main(String[] args) {
    
    SpringApplication.run(HomeMeasureApplication.class, args);
  }
  
  @Override
  public void run(ApplicationArguments inArgs) throws Exception {

    if (devices != null && !devices.isEmpty()) {
      devices.forEach(d -> executor.execute(new RunnableMeasurementDeviceSupport(d)));
    }
    else {
      log.error("***** No devices found in classpath - exiting!");
      System.exit(1);
    } // else
  }
}