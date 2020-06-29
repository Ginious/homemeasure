package ginious.home.measure.model;

import java.util.HashSet;
import java.util.Set;

import javax.annotation.PostConstruct;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.Validate;

/**
 * Base type for concrete device implementations providing basic functionality.
 */
public abstract class AbstractMeasurementDevice<T extends DeviceConfiguration>
    implements MeasurementDevice {

  /**
   * Cache for measures.
   */
  private MeasureCache cache;

  private T config;
  /**
   * Each device must have a unique identifier.
   */
  private String id;

  /**
   * Signals the state of the device.
   */
  private boolean switchedOff;

  /**
   * Set of measures a device is able to provide.
   */
  private Set<String> measureIds = new HashSet<>();

  /**
   * Set of device identifiers used for validation that an identifier is unique.
   */
  private static Set<String> deviceIds = new HashSet<>();

  /**
   * Default constructor.
   * 
   * @param inCache
   *          The underlying cache for measurements.
   */
  protected AbstractMeasurementDevice(MeasureCache inCache, T inConfig) {
    super();

    cache = inCache;
    config = inConfig;

    id = StringUtils.remove(getClass().getSimpleName(), MeasurementDevice.class.getSimpleName())
        .toLowerCase();
    Validate.isTrue(!deviceIds.contains(id), "Device with id [" + id + "] was already registered!");
    deviceIds.add(id);
  }

  /**
   * Device-specific initialization of measures and device settings.
   */
  @PostConstruct
  private void init() {

    initCustom();
    cache.registerDevice(this);
  }

  /**
   * Overwrite method in specific device for custom initialization purposes.
   */
  protected void initCustom() {}

  /**
   * Gets the underlying configuration.
   * 
   * @return The configuration.
   */
  protected final T getConfig() {
    return config;
  }

  @Override
  public final String getId() {
    return id;
  }

  @Override
  public final Set<String> getSupportedMeasureIds() {
    return measureIds;
  }

  /**
   * Registers the given id as measure id. It will later be used when building a
   * cache.
   * 
   * @param inId
   *          The id of the measurement.
   */
  protected final void registerMeasure(String inId) {
    measureIds.add(inId);
  }

  /**
   * Sets the given value of the given measure. The change will be delegated to the singleton {@link MeasureCache}
   * directly so that all attached listeners (e.g. those of services) will be notified correspondingly.
   * 
   * @param inMeasureId
   *          The id of the measurement.
   * @param inValue
   *          The value.
   */
  protected final void setMeasureValue(String inMeasureId, String inValue) {
    cache.setValue(getId(), inMeasureId, inValue);
  }

  /**
   * Helper method for letting the current Thread sleep for the given amount of milliseconds.
   * 
   * @param inMilliseconds
   *          The amount of milliseconds to sleep.
   */
  protected final void sleep(int inMilliseconds) {

    try {
      Thread.sleep(inMilliseconds);
    }
    catch (InterruptedException e) {} // catch
  }

  @Override
  public final void switchOff() {
    switchedOff = true;
    switchOffCustom();
  }

  @Override
  public final void switchOn() {

    switchOnCustom();
  }

  /**
   * Switch OFF the device. Implementation Must be provided device-specific.
   */
  protected abstract void switchOffCustom();

  /**
   * Switch ON the device. Implementation Must be provided device-specific.
   */
  protected abstract void switchOnCustom();

  /**
   * Indicates whether the device was switched off by the server. This information
   * should be used by a specific device to e.g. stop its measuring thread.
   * 
   * @return <code>true</code> when the device has been switched off by the
   *         server, <code>false</code> otherwise.
   */
  protected final boolean wasSwitchedOff() {
    return switchedOff;
  }

  /**
   * Registers the given id as measure id.
   * 
   * @param inId
   *          The id of the measurement.
   */
  protected final void registerMeasureId(String inId) {

    Validate.isTrue(StringUtils.isNotBlank(inId), "inId is required!");
    Validate.isTrue(!measureIds.contains(inId),
        "Measure with id [" + inId + "] was already registered!");

    measureIds.add(inId);
  }
}