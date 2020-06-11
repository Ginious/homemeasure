package ginious.home.measure.model;

/**
 * Base interface for Configurations.
 */
public interface Configuration {

  /**
   * Indicates whether a service or a device using this configuration is enabled or not.
   * 
   * @return <code>true</code> when the service or device is enabled, <code>false</code> otherwwise.
   */
  boolean isEnabled();
}