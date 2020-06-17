package ginious.home.measure.device.volkszaehler;

import java.awt.font.TextMeasurer;
import java.io.IOException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang.StringUtils;
import org.openmuc.jrxtx.DataBits;
import org.openmuc.jrxtx.FlowControl;
import org.openmuc.jrxtx.Parity;
import org.openmuc.jrxtx.SerialPort;
import org.openmuc.jrxtx.SerialPortBuilder;
import org.openmuc.jrxtx.StopBits;
import org.openmuc.jsml.structures.EMessageBody;
import org.openmuc.jsml.structures.SmlFile;
import org.openmuc.jsml.structures.SmlList;
import org.openmuc.jsml.structures.SmlListEntry;
import org.openmuc.jsml.structures.SmlMessage;
import org.openmuc.jsml.structures.responses.SmlGetListRes;
import org.openmuc.jsml.transport.SerialReceiver;
import org.slf4j.MarkerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

import ginious.home.measure.model.AbstractMeasurementDevice;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
@ConditionalOnProperty(prefix = VolkszaehlerConfig.CONFIG_PREFIX, name = "enabled", matchIfMissing = false)
public class VolkszaehlerMeasurementDevice extends AbstractMeasurementDevice {

	private enum Measure {

		ACTUAL("Actual_W"), //
		ACTUAL_SOLAR_FEED("ActualSolarFeed_W"), //
		HT("HT_Wh"), //
		NT("NT_Wh"), //
		SOLAR_OUT("Solar_Out_Wh"), //
		TOTAL("Total_Wh");

		private String id;

		private Measure(String inId) {
			id = inId;
		}
	}

	@Autowired
	private VolkszaehlerConfig config;

	/**
	 * The serial receiver for receiving data from the USB device.
	 */
	private SerialReceiver receiver;

	public VolkszaehlerMeasurementDevice() {
		super();
	}

	private SerialReceiver getReceiver() {

		if (receiver == null) {

			try {
				SerialPort lPort = SerialPortBuilder
						.newBuilder( //
								config.getDevice()) //
						.setBaudRate(config.getBaudrate()) //
						.setDataBits(DataBits.DATABITS_8) //
						.setParity(Parity.NONE) //
						.setStopBits(StopBits.STOPBITS_1) //
						.setFlowControl(FlowControl.RTS_CTS) //
						.build();

				receiver = new SerialReceiver(lPort);
			} catch (UnsatisfiedLinkError e) {
				log.error(MarkerFactory.getMarker(getId()), "Failed loading device driver - reason: {}",
						e.getMessage());
			} catch (Throwable t) {
				log.error(MarkerFactory.getMarker(getId()),
						"Failed to initiate communication with USB dongle - reason: {}", t.getMessage());
			} // catch
		} // if

		log.info(MarkerFactory.getMarker(getId()), "Connected to device [{}]", config.getDevice());

		return receiver;
	}

	protected void initCustom() {

		// register all mesaures provided by SMA converter
		for (Measure lCurrMeasure : Measure.values()) {
			registerMeasureId(lCurrMeasure.id);
		} // for
	}

	/**
	 * Setter for test purpose.
	 * 
	 * @param inReceiver
	 *            The test receiver mock.
	 */
	protected void setReceiver(SerialReceiver inReceiver) {
		receiver = inReceiver;
	}

	@Override
	protected void switchOffCustom() {

	}

	@Override
	protected void switchOnCustom() {

		for (;;) {

			SerialReceiver lReceiver = getReceiver();

			// quit when device was switched off
			if (wasSwitchedOff()) {
				try {
					lReceiver.close();
				} catch (IOException e) {
					// ignore
				} // catch
				break;
			} // if

			// read next data bucket
			SmlFile lSmlFile = null;
			try {
				if (lReceiver != null) {
					lSmlFile = lReceiver.getSMLFile();
				} // if
			} catch (IOException e) {
				log.error(MarkerFactory.getMarker(getId()), "Failed to read data from device!", e);
				continue;
			} // catch

			if (lSmlFile != null) {
				processSmlFile(lSmlFile);
			} // if

			sleep(1000);
		} // for
	}

	/**
	 * Processes the data that was read from the serial USB device and updates
	 * the corresponding measures.
	 * 
	 * @param inSmlFile
	 *            The data file.
	 */
	private void processSmlFile(SmlFile inSmlFile) {

		List<SmlMessage> lMessages = inSmlFile.getMessages();
		for (SmlMessage lCurrMessage : lMessages) {

			if (lCurrMessage.getMessageBody().getTag() == EMessageBody.GET_LIST_RESPONSE) {

				SmlGetListRes lResponse = (SmlGetListRes) lCurrMessage.getMessageBody().getChoice();
				SmlList lValueList = lResponse.getValList();
				SmlListEntry[] lListEntries = lValueList.getValListEntry();
				for (SmlListEntry lCurrEntry : lListEntries) {

					String lEntryVal = lCurrEntry.getValue().toString();
					Measure lMeasureToChange = null;

					String objName = lCurrEntry.getObjName().toString();
					if (StringUtils.equals(objName, config.getMeter_ht())) {
						lMeasureToChange = Measure.HT;
					} else if (StringUtils.equals(objName, config.getMeter_nt())) {
						lMeasureToChange = Measure.NT;
					} else if (StringUtils.equals(objName, config.getMeter_solar())) {
						lMeasureToChange = Measure.SOLAR_OUT;
					} else if (StringUtils.equals(objName, config.getMeter_total())) {
						lMeasureToChange = Measure.TOTAL;
					} else if (StringUtils.equals(objName, config.getMeter_actual())) {
						if (StringUtils.startsWith(lEntryVal, "-")) {

							// feeding solar power to energy supplier
							lMeasureToChange = Measure.ACTUAL_SOLAR_FEED;
							lEntryVal = StringUtils.remove(lEntryVal, "-");
							setMeasureValue(Measure.ACTUAL.id, "0");
						} else {
							
							// receiving power from energy supplier
							lMeasureToChange = Measure.ACTUAL;
							setMeasureValue(Measure.ACTUAL_SOLAR_FEED.id, "0");
						} // else
					}

					if (lMeasureToChange != null) {
						setMeasureValue(lMeasureToChange.id, lEntryVal);
					} // if
				} // for
			} // if
		} // for
	}
}
