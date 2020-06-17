package ginious.home.measure.device.sma;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import org.slf4j.MarkerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

import de.re.easymodbus.exceptions.ModbusException;
import de.re.easymodbus.modbusclient.ModbusClient;
import de.re.easymodbus.modbusclient.ModbusClient.RegisterOrder;
import ginious.home.measure.model.AbstractMeasurementDevice;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
@ConditionalOnProperty(prefix = SmaConverterConfig.CONFIG_PREFIX, name = "enabled", matchIfMissing = false)
public class SmaConverterMeasurementDevice extends AbstractMeasurementDevice {

	/**
	 * Modbus data types.
	 */
	public enum DataType {
		S32, //
		U32, //
		U64;
	}

	/**
	 * Existing measures including addressing information and refresh interval
	 * in milliseconds.
	 */
	public enum Measure {

		CURRENT_W("Current_W", 30775, 2, DataType.S32, 10_000), // 10 Sekunden
		DAY_WH("Day_Wh", 30535, 2, DataType.U32, 60_000), // 1 Minute
		DAY_SALE("Day_Sale"), //
		TOTAL_KWH("Total_MWh", 30531, 4, DataType.U32, 300_000), // 5 Minuten
		TOTAL_SALE("Total_Sale");

		public String id;
		private int start;
		private int length;
		private DataType type;
		private int refreshAfter;

		private Measure(String inId) {
			id = inId;
		}

		private Measure(String inId, int inStart, int inLength, DataType inType, int inRefreshAfter) {
			id = inId;
			start = inStart;
			length = inLength;
			type = inType;
			refreshAfter = inRefreshAfter;
		}
	}

	@Autowired
	private SmaConverterConfig config;

	private int REQUEST_INTERVAL_MS = 10_000; // 10 seconds

	/**
	 * Registry holding the timestamp when a measurement was last requested from
	 * the converter. It is required to preserve access timeouts.
	 */
	private Map<Measure, Long> lastMeasurementTS = new HashMap<>();

	/**
	 * Client for testing purposes only.
	 */
	private ModbusClient testClient;

	/**
	 * Default device constructor.
	 */
	public SmaConverterMeasurementDevice() {
		super();
	}

	/**
	 * Connects to the SMA converter.
	 * 
	 * @return The client used to communicate with the SMA converter.
	 */
	private ModbusClient connect() {

		ModbusClient outClient = createModbusClient();

		String lIpAddress = config.getIp_address();
		Integer lPort = config.getPort();
		int lModbusID = config.getConverter_id();

		try {
			outClient.Connect(lIpAddress, lPort);
		} catch (Throwable t) {
			log.error(MarkerFactory.getMarker(getId()),
					"SMA converter [modbus id {}] could not be connected: {}:{} - reason: {}", lModbusID, lIpAddress,
					config.getPort(), t.getMessage());
			return null;
		} // catch

		outClient.setConnectionTimeout(config.getTimeout());
		outClient.setUnitIdentifier((byte) Byte.valueOf((byte) lModbusID));

		if (outClient.isConnected()) {
			log.debug(MarkerFactory.getMarker(getId()), "SMA converter [modbus id {}] was successfully connected: {}:{}",
					lModbusID, lIpAddress, lPort);
		} else {
			log.warn(MarkerFactory.getMarker(getId()),
					"SMA converter [modbus id {}] could not be connected: {}:{} - unknown reason", lModbusID,
					lIpAddress, lPort);
			outClient = null;
		} // else

		return outClient;
	}

	/**
	 * Creates a new Modbus Client or returns the mocked test client.
	 * 
	 * @return A new Modbus Client or the mocked test client.
	 */
	private ModbusClient createModbusClient() {

		ModbusClient outClient;

		if (testClient == null) {
			outClient = new ModbusClient();
		} else {
			outClient = testClient;
		} // else

		return outClient;
	}

	/**
	 * Gets the value of the given measure from the SMA converter including
	 * register conversion.
	 * 
	 * @param inMeasure
	 *            The measure to get from the converter.
	 * @return The value.
	 */
	private String getValueFromSMA(Measure inMeasure) {

		String outValue = "0";

		ModbusClient lClient = null;
		int[] lRegisters = null;
		try {
			lClient = connect();
			if (lClient != null) {
				lRegisters = lClient.ReadHoldingRegisters(inMeasure.start, inMeasure.length);
			}
		} catch (ModbusException | IOException e) {
			log.warn(MarkerFactory.getMarker(getId()), "Could not read [{} at {}/{} ({})] from modbus: {}",
					inMeasure.name(), inMeasure.start, inMeasure.length, inMeasure.type.name(), e.getMessage());
			return outValue;
		} finally {
			if (lClient != null) {
				try {
					lClient.Disconnect();
				} catch (IOException e) {
					log.warn(MarkerFactory.getMarker(getId()), "Could not properly disconnect modbus: {}",
							e.getMessage());
				} // catch
			} // if
		} // finally

		if (lRegisters != null) {

			// decode register bytes
			if (inMeasure.type == DataType.S32) {
				outValue = String.valueOf(ModbusClient.ConvertRegistersToDouble(lRegisters, RegisterOrder.HighLow));
			} else if (inMeasure.type == DataType.U32) {
				outValue = String.valueOf(ModbusClient.ConvertRegistersToDouble(lRegisters, RegisterOrder.HighLow));
			} else if (inMeasure.type == DataType.U64) {
				outValue = String.valueOf(ModbusClient.ConvertRegistersToLong(lRegisters, RegisterOrder.HighLow));
			} // else if
		} // if

		return outValue;
	}

	protected void initCustom() {

		// register all measures provided by SMA converter
		for (Measure lCurrMeasure : Measure.values()) {
			registerMeasureId(lCurrMeasure.id);
		} // for
	}

	/**
	 * Setter for test purpose.
	 * 
	 * @param inMockClient
	 *            The modbus client mock for testing.
	 */
	protected void setModbusClient(ModbusClient inMockClient) {
		testClient = inMockClient;
	}

	@Override
	protected void switchOffCustom() {

	}

	@Override
	protected void switchOnCustom() {

		// determine sales per KWh
		Float lSalesPerKWh = Float.valueOf(config.getSalesper_kwh());

		// initialize map with measurement timestamps
		for (Measure lCurrMeasure : Measure.values()) {
			lastMeasurementTS.put(lCurrMeasure, null);
		} // for

		for (;;) {

			// quit when device was switched off
			if (wasSwitchedOff()) {
				break;
			} // if

			// ////////////////////////////
			// //// Day Wh + Sales
			// ////////////////////////////
			Measure lCurrMeasure = Measure.DAY_WH;
			Long lNow = System.currentTimeMillis();
			Long lLastMeasureTS = lastMeasurementTS.get(lCurrMeasure);
			if (lLastMeasureTS == null || (lNow - lLastMeasureTS) > lCurrMeasure.refreshAfter) {

				Integer lDayWh = Integer.valueOf(getValueFromSMA(lCurrMeasure));
				setMeasureValue(lCurrMeasure.id, String.valueOf(lDayWh));
				lastMeasurementTS.put(lCurrMeasure, lNow);

				Float lSalesPerDay = (float) lDayWh / 1000 * lSalesPerKWh;
				setMeasureValue(Measure.DAY_SALE.id, String.valueOf(lSalesPerDay));
				lastMeasurementTS.put(Measure.DAY_SALE, lNow);
			} // if

			// ////////////////////////////
			// //// Total MWh + Sales
			// ////////////////////////////
			lCurrMeasure = Measure.TOTAL_KWH;
			lNow = System.currentTimeMillis();
			lLastMeasureTS = lastMeasurementTS.get(lCurrMeasure);
			if (lLastMeasureTS == null || (lNow - lLastMeasureTS) > lCurrMeasure.refreshAfter) {

				Integer lTotalKWh = Integer.valueOf(getValueFromSMA(lCurrMeasure));
				setMeasureValue(lCurrMeasure.id, String.valueOf((float) lTotalKWh / 1000));
				lastMeasurementTS.put(lCurrMeasure, lNow);

				Float lSalesTotal = (float) lTotalKWh * lSalesPerKWh;
				setMeasureValue(Measure.TOTAL_SALE.id, String.valueOf(lSalesTotal));
				lastMeasurementTS.put(Measure.TOTAL_SALE, lNow);
			} // if

			// ////////////////////////////
			// //// Current W
			// ////////////////////////////
			lCurrMeasure = Measure.CURRENT_W;
			lNow = System.currentTimeMillis();
			lLastMeasureTS = lastMeasurementTS.get(lCurrMeasure);
			if (lLastMeasureTS == null || (lNow - lLastMeasureTS) > lCurrMeasure.refreshAfter) {

				Integer lCurrentW = Integer.valueOf(getValueFromSMA(lCurrMeasure));
				if (lCurrentW < 0) {
					lCurrentW = 0;
				} // if
				setMeasureValue(lCurrMeasure.id, String.valueOf(lCurrentW));
				lastMeasurementTS.put(lCurrMeasure, lNow);
			} // if

			// obey minimum waiting time between requests
			sleep(REQUEST_INTERVAL_MS);
		} // for
	}
}