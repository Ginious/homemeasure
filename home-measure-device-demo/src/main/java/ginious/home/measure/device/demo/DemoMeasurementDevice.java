package ginious.home.measure.device.demo;

import org.apache.commons.lang.math.RandomUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

import ginious.home.measure.model.AbstractMeasurementDevice;

@Component
@ConditionalOnProperty(prefix = DemoConfig.CONFIG_PREFIX, name = "enabled", matchIfMissing = false)
public class DemoMeasurementDevice extends AbstractMeasurementDevice {
  
  enum Measure {
    MEASURE_1, //
    MEASURE_2, //
    MEASURE_3, //
    MEASURE_4, //
    MEASURE_5;
  }

  @Autowired
  private DemoConfig config;

  public DemoMeasurementDevice() {
    super();
  }

  @Override
  protected void initCustom() {
    registerMeasureId(Measure.MEASURE_1.name());
    registerMeasureId(Measure.MEASURE_2.name());
    registerMeasureId(Measure.MEASURE_3.name());
    registerMeasureId(Measure.MEASURE_4.name());
    registerMeasureId(Measure.MEASURE_5.name());
  }

  /**
   * Default constructor.
   * 
   * @param inDeviceId
   *          The id of the device.
   */
  public DemoMeasurementDevice(String inDeviceId) {
    super();
  }

  @Override
  protected void switchOffCustom() {

  }

  @Override
  protected void switchOnCustom() {

    boolean firstRun = true;

    for (;;) {
      if (firstRun) {
        setMeasureValue(Measure.MEASURE_1.name(), config.getMesswert1());
        sleep(1000);
        setMeasureValue(Measure.MEASURE_2.name(), config.getMesswert2());
        sleep(1000);
        setMeasureValue(Measure.MEASURE_3.name(), config.getMesswert3());
        sleep(1000);
        setMeasureValue(Measure.MEASURE_4.name(), config.getMesswert4());
        sleep(1000);
        setMeasureValue(Measure.MEASURE_5.name(), config.getMesswert5());
        firstRun = false;
      }
      sleep(5000);
      setMeasureValue(Measure.MEASURE_1.name(), String.valueOf(RandomUtils.nextInt(100)));
      sleep(1000);
      setMeasureValue(Measure.MEASURE_2.name(), String.valueOf(RandomUtils.nextInt(100)));
      sleep(1000);
      setMeasureValue(Measure.MEASURE_3.name(), String.valueOf(RandomUtils.nextInt(100)));
      sleep(1000);
      setMeasureValue(Measure.MEASURE_4.name(), String.valueOf(RandomUtils.nextInt(100)));
      sleep(1000);
      setMeasureValue(Measure.MEASURE_5.name(), String.valueOf(RandomUtils.nextInt(100)));
    } // for
  }
}