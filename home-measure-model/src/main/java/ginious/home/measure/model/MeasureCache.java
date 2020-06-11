package ginious.home.measure.model;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

import org.apache.commons.lang.ObjectUtils;
import org.apache.commons.lang.Validate;

/**
 * A cache for all measures of all devices. Listeners can attach to the cache for being notified about changed
 * measure values. The cache is a singleton instance in the scope of a home measure server instance.
 */
@org.springframework.stereotype.Service
public class MeasureCache {

  /**
   * All measures of all devices that are currently working in the current home measure server instance.
   */
  private Map<String, Set<Measure>> measuresByDevice = new HashMap<>();

  /**
   * All listeners that are registered. They will be notified for each change of a measure.
   */
  private List<MeasureListener> listeners = new ArrayList<>();

  /**
   * Standard constructor.
   */
  public MeasureCache() {
    super();
  }

  /**
   * Removes the given measure listener.
   * 
   * @param inListener The listener to remove.
   */
  public void removeMeasureListener(MeasureListener inListener) {
    listeners.remove(inListener);
  }

  /**
   * Adds a measure listener to be notified when the value one of the measures
   * changed.
   * 
   * @param inListener
   *          The measure listener.
   */
  public void addMeasureListener(MeasureListener inListener) {
    listeners.add(inListener);
  }

  /**
   * Gets the id of the underlying device.
   * 
   * @return The id of the underlying device.
   */
  public Set<String> getDeviceIds() {
    return measuresByDevice.keySet();
  }

  /**
   * Gets all meaasures of the given device from the cache in sorted order.
   * 
   * @param inDeviceId
   *          The id of the device.
   * @return The collection of measures sorted by measure Id.
   */
  public Collection<Measure> getMeasures(String inDeviceId) {

    List<Measure> outMeasures = new ArrayList<>(measuresByDevice.get(inDeviceId));
    Collections.sort(outMeasures, Comparator.comparing(Measure::getId));

    return outMeasures;
  }

  /**
   * Registers the given device by extracting all measures and creating a cache
   * adapter for the device.
   * 
   * @param inDevice
   *          The device for the registration of measures to cache.
   */
  public void registerDevice(MeasurementDevice inDevice) {

    for (String lCurrMeasureId : inDevice.getSupportedMeasureIds()) {

      Set<Measure> lDeviceMeasures = measuresByDevice.get(inDevice.getId());
      if (lDeviceMeasures == null) {
        lDeviceMeasures = new HashSet<>();
        measuresByDevice.put(inDevice.getId(), lDeviceMeasures);
      } // if
      lDeviceMeasures.add(new Measure(inDevice.getId(), lCurrMeasureId, null));
    } // for
  }

  /**
   * Sets the value of the measure given by id. Only when the new value differs from the old value all registered
   * listeners will be notified.
   * 
   * @param inDeviceId
   *          The device id.
   * @param inId
   *          The measure id.
   * @param inValue
   *          The value.
   */
  void setValue(String inDeviceId, String inId, String inValue) {

    Validate.isTrue(measuresByDevice.get(inDeviceId) != null,
        "No measures were registered for device with id [" + inDeviceId + "]!");

    Optional<Measure> measureOpt = measuresByDevice.get(inDeviceId).stream()
        .filter(m -> inId.equals(m.getId())).findAny();
    if (measureOpt.isPresent()) {

      // remember old value and update new
      String oldValue = measureOpt.get().getValue();
      measureOpt.get().setValue(inValue);

      // notify listeners when value has changed
      if (listeners != null && ObjectUtils.notEqual(inValue, oldValue)) {
        listeners.forEach(l -> l.measureChanged(measureOpt.get()));
      } // if
    } // if
  }
}