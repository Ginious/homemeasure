package ginious.home.measure.device.demo;

import org.apache.commons.lang.math.RandomUtils;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

import ginious.home.measure.model.AbstractMeasurementDevice;
import ginious.home.measure.model.MeasureCache;

@Component
@ConditionalOnProperty(prefix = DemoConfig.CONFIG_PREFIX, name = "enabled", matchIfMissing = false)
public class DemoMeasurementDevice extends AbstractMeasurementDevice<DemoConfig> {

  enum Measure {
    MEASURE_1, //
    MEASURE_2, //
    MEASURE_3, //
    MEASURE_4, //
    MEASURE_5;
  }

  /**
   * DI constructor called by Spring.
   * 
   * @param inCache
   *          The cache for measurements.
   * @param inConfig
   *          The device configuration.
   */
  public DemoMeasurementDevice(MeasureCache inCache, DemoConfig inConfig) {
    super(inCache, inConfig);
  }

  @Override
  protected void initCustom() {
    registerMeasureId(Measure.MEASURE_1.name());
    registerMeasureId(Measure.MEASURE_2.name());
    registerMeasureId(Measure.MEASURE_3.name());
    registerMeasureId(Measure.MEASURE_4.name());
    registerMeasureId(Measure.MEASURE_5.name());
  }

  @Override
  protected void switchOffCustom() {

  }

  @Override
  protected void switchOnCustom() {

    boolean firstRun = true;

    for (;;) {
      if (firstRun) {
        setMeasureValue(Measure.MEASURE_1.name(), getConfig().getMesswert1());
        sleep(1000);
        setMeasureValue(Measure.MEASURE_2.name(), getConfig().getMesswert2());
        sleep(1000);
        setMeasureValue(Measure.MEASURE_3.name(), getConfig().getMesswert3());
        sleep(1000);
        setMeasureValue(Measure.MEASURE_4.name(), getConfig().getMesswert4());
        sleep(1000);
        setMeasureValue(Measure.MEASURE_5.name(), getConfig().getMesswert5());
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