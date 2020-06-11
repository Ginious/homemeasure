package ginious.home.measure.device.volkszaehler;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

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

    ACTUAL("Actual_Wh"), //
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
    super("volkszaehler");
  }

  private SerialReceiver getReceiver() {

    if (receiver == null) {

      try {
        SerialPort lPort = SerialPortBuilder.newBuilder( //
            config.getDevice()) //
            .setBaudRate(config.getBaudrate()) //
            .setDataBits(DataBits.DATABITS_8) //
            .setParity(Parity.NONE) //
            .setStopBits(StopBits.STOPBITS_1) //
            .setFlowControl(FlowControl.RTS_CTS) //
            .build();

        receiver = new SerialReceiver(lPort);
      }
      catch (UnsatisfiedLinkError e) {
        log.error(MarkerFactory.getMarker(getId()), "Failed loading device driver - reason: {0}",
            e.getMessage());
      }
      catch (Throwable t) {
        log.error(MarkerFactory.getMarker(getId()),
            "Failed to initiate communication with USB dongle - reason: {0}", t.getMessage());
      } // catch
    } // if

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
   *          The test receiver mock.
   */
  protected void setReceiver(SerialReceiver inReceiver) {
    receiver = inReceiver;
  }

  @Override
  protected void switchOffCustom() {

  }

  @Override
  protected void switchOnCustom() {

    Map<String, Measure> lMeasuresByObjName = new HashMap<>();
    lMeasuresByObjName.put(config.getMeter_ht(), Measure.HT);
    lMeasuresByObjName.put(config.getMeter_nt(), Measure.NT);
    lMeasuresByObjName.put(config.getMeter_solar(), Measure.SOLAR_OUT);
    lMeasuresByObjName.put(config.getMeter_total(), Measure.TOTAL);
    lMeasuresByObjName.put(config.getMeter_actual(), Measure.ACTUAL);

    for (;;) {

      SerialReceiver lReceiver = getReceiver();

      // quit when device was switched off
      if (wasSwitchedOff()) {
        try {
          lReceiver.close();
        }
        catch (IOException e) {
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
      }
      catch (IOException e) {
        continue;
      } // catch

      if (lSmlFile != null) {

        List<SmlMessage> lMessages = lSmlFile.getMessages();
        for (SmlMessage lCurrMessage : lMessages) {

          if (lCurrMessage.getMessageBody().getTag() == EMessageBody.GET_LIST_RESPONSE) {
            
            SmlGetListRes lResponse = (SmlGetListRes)lCurrMessage.getMessageBody().getChoice();
            SmlList lValueList = lResponse.getValList();
            SmlListEntry[] lListEntries = lValueList.getValListEntry();
            for (SmlListEntry lCurrEntry : lListEntries) {

              Measure lMeasure = lMeasuresByObjName.get(lCurrEntry.getObjName().toString());
              if (lMeasure != null) {
                setMeasureValue(lMeasure.id, lCurrEntry.getValue().toString());
              } // if
            } // for
          } // if
        } // for
      } // if

      sleep(1000);
    } // for
  }
}
