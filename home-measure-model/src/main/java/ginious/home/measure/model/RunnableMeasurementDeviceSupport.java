package ginious.home.measure.model;

public class RunnableMeasurementDeviceSupport implements Runnable {

	private MeasurementDevice device;

	public RunnableMeasurementDeviceSupport(MeasurementDevice inDevice) {
		super();

		device = inDevice;
	}

	@Override
	public void run() {

		device.switchOn();
	}
}