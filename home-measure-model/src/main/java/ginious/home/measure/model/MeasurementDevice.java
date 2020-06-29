package ginious.home.measure.model;

import java.util.Set;

/**
 * Interface of a common measure device as it is used in a home measure server instance.
 */
public interface MeasurementDevice extends HasIdentifier {

  /**
   * Switch device on.
   */
  void switchOn();

  /**
   * Switch device off.
   */
  void switchOff();

  /**
   * Provides the identifier of all measures that are supported by this device.
   * 
   * @return All measures supported by this device.
   */
  Set<String> getSupportedMeasureIds();
}