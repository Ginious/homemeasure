package ginious.home.measure.cache.serializer;

import java.util.Collection;
import java.util.Iterator;

import org.springframework.stereotype.Service;

import ginious.home.measure.model.Measure;
import ginious.home.measure.model.MeasureCache;
import ginious.home.measure.model.MeasurementsSerializer;

/**
 * Simple serializer that generates measures as Json.
 */
@Service
final class JsonMeasuresSerializer implements MeasurementsSerializer {

  /**
   * The unique ID of this serializer.
   */
  private static final String ID = "json";

  /**
   * Standard constructor.
   */
  public JsonMeasuresSerializer() {
    super();
  }

  @Override
  public String serialize(MeasureCache inCache) {

    StringBuilder lBuilder = new StringBuilder("{\n");
    Iterator<String> lDeviceIds = inCache.getDeviceIds().iterator();
    while (lDeviceIds.hasNext()) {
      String lCurrDeviceId = lDeviceIds.next();

      lBuilder.append("  \"");
      lBuilder.append(lCurrDeviceId);
      lBuilder.append("\":\n  {");
      lBuilder.append(serializeMeasures(inCache.getMeasures(lCurrDeviceId)));
      lBuilder.append("  }");

      if (lDeviceIds.hasNext()) {
        lBuilder.append(",\n");
      }
      else {
        lBuilder.append("\n");
      } // else
    } // for
    lBuilder.append("}");

    return lBuilder.toString();
  }

  /**
   * Serializes the given collection of measures.
   * 
   * @param inMeasures
   *          The measures to serialize.
   * @return The resulting XML.
   */
  public String serializeMeasures(Collection<Measure> inMeasures) {

    StringBuilder lBuilder = new StringBuilder("\n");
    Iterator<Measure> lMeasures = inMeasures.iterator();
    while (lMeasures.hasNext()) {

      Measure lCurrMeasure = lMeasures.next();
      lBuilder.append("    \"");
      lBuilder.append(lCurrMeasure.getId());
      lBuilder.append("\":\t\"");
      lBuilder.append(lCurrMeasure.getValue());
      lBuilder.append("\"");
      if (lMeasures.hasNext()) {
        lBuilder.append(",\n");
      }
      else {
        lBuilder.append("\n");
      }
    } // for

    return lBuilder.toString();
  }

  @Override
  public String getId() {
    return ID;
  }
}