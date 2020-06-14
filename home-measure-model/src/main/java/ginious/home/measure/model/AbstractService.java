package ginious.home.measure.model;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.annotation.PostConstruct;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.Validate;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * Base type for conrete service implementations providing basic functionality.
 */
public abstract class AbstractService implements Service {

	/**
	 * Central cache for measures.
	 */
	@Autowired
	private MeasureCache cache;

	/**
	 * All measures that are included to be handled by this service.
	 */
	private Map<String, List<String>> measuresToInclude = new HashMap<>();

	/**
	 * Default constructor.
	 */
	protected AbstractService() {
		super();
	}

	@PostConstruct
	private void init() {
		cache.addMeasureListener(m -> measureChanged(m));
		initCustom();
	}

	protected abstract void initCustom();

	/**
	 * Performs the initialization of included measures for this service based
	 * on the underlying property <code>service.NAME.included-measures</code>.
	 * Measures in the form <code>DEVICE_ID.MEASURE_ID</code> (or
	 * <code>DEVICE_ID.*</code> for all measures of a device) must be provided
	 * in comma separated form. When the property is empty all devices and all
	 * measures are included.
	 * 
	 * @param inMeasureIdsCsv
	 *            The comma separated measures to be included for this service.
	 */
	protected void initIncludedMeasures(String inMeasureIdsCsv) {

		String[] lDeviceMeasuresToInclude = StringUtils.split(inMeasureIdsCsv, ",;");
		if (lDeviceMeasuresToInclude != null) {

			for (String lCurrDeviceMeasure : lDeviceMeasuresToInclude) {
				Validate.isTrue(lCurrDeviceMeasure.contains("."), "The device measure [" + lCurrDeviceMeasure
						+ "] is invalid - please prefix measures with the device id like [my_device_id.my_measure_id]!");
				String lDeviceId = StringUtils.substringBefore(lCurrDeviceMeasure, ".");
				String lMeasureId = StringUtils.substringAfter(lCurrDeviceMeasure, ".");
				List<String> lDeviceMeasures = measuresToInclude.get(lDeviceId);
				if (lDeviceMeasures == null) {
					lDeviceMeasures = new ArrayList<>();
					measuresToInclude.put(lDeviceId, lDeviceMeasures);
				} // if
				lDeviceMeasures.add(lMeasureId);
			} // for
		} // if
	}

	/**
	 * This callback will be called for each measure of which the value changed.
	 * The call will be delegated to the subclass implementation
	 * <code>measureChangedCustom</code> as long as the measure was not
	 * explicitly excluded.
	 */
	private void measureChanged(Measure inChangedMeasure) {

		// skip empty measures from recording
		if (inChangedMeasure.getValue() == null || "null".equalsIgnoreCase(inChangedMeasure.getValue())) {
			return;
		} // if

		// include measure when ...
		// (1) no measure is specifically included
		// (2) measure is specifically included
		// (3) all measures of a device are included
		List<String> lMeasureIds = measuresToInclude.get(inChangedMeasure.getDeviceId());
		if (lMeasureIds == null || //
				lMeasureIds.isEmpty() || // (1)
				lMeasureIds.contains(inChangedMeasure.getId()) || // (2)
				lMeasureIds.contains("*")) { // (3)
			measureChangedCustom(inChangedMeasure);
		} // if
	}

	/**
	 * This callback will be called for measures that are explicitly not
	 * excluded.
	 * 
	 * @param inChangedMeasure
	 *            The measure that has changed.
	 */
	protected abstract void measureChangedCustom(Measure inChangedMeasure);

	public final String getId() {
		return StringUtils.remove(getClass().getSimpleName(), Service.class.getSimpleName()).toLowerCase();
	}
}