package ginious.home.measure.server;

import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import ginious.home.measure.model.MeasureCache;
import ginious.home.measure.model.MeasurementsSerializer;
import lombok.extern.slf4j.Slf4j;

/**
 * Provides the Rest service endpoint <code>measures</code> for listing the measure values of all devices.
 * Using the parameter <code>serializer</code> the serializer to be used can be specified e.g. <code>xml</code>,
 * <code>json</code> or a custom implementation.
 */
@Slf4j
@RestController
public class HomeMeasureRestService {

  @Autowired
  private MeasureCache cache;

  /**
   * All serializer that are available.
   */
  @Autowired(required = false)
  private List<MeasurementsSerializer> serializers;

  // TODO content type must be dynamic
  // TODO define custom path
  @GetMapping(path = "/measures", produces = {"application/json", "application/xml", "text/html", "text/plain"})
  public String measures(
      @RequestParam(name = "serializer", required = false) String inSerializerId) {

    String outSerializedMeasures = null;

    if (serializers != null) {

      Optional<MeasurementsSerializer> lSerializerOpt = serializers.stream()
          .filter(s -> s.getId().equalsIgnoreCase(inSerializerId)).findAny();
      if (lSerializerOpt.isPresent()) {
        outSerializedMeasures = lSerializerOpt.get().serialize(cache);
      }
      else if (!serializers.isEmpty()) {

        MeasurementsSerializer serializer = serializers.iterator().next();
        log.warn("No Serializer found for ID {} - using {} as fallback", inSerializerId,
            serializer.getId());
        outSerializedMeasures = serializer.serialize(cache);
      } // else if
    } // if

    if (outSerializedMeasures == null) {
      String message = "No Serializers found in classpath!";
      log.error(message);
      outSerializedMeasures = "<html><h1>" + message + "</h1></html>";
    } // else

    return outSerializedMeasures;
  }
}