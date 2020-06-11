package ginious.home.measure.cache.serializer;

import java.util.Collection;

import org.springframework.stereotype.Service;

import ginious.home.measure.model.Measure;
import ginious.home.measure.model.MeasureCache;
import ginious.home.measure.model.MeasurementsSerializer;

/**
 * Simple serializer that generates measures as XML.
 */
@Service
final class XMLMeasuresSerializer implements MeasurementsSerializer {

  /**
   * The unique ID of this serializer.
   */
  private static final String ID = "xml";

  /**
	 * Standard constructor.
	 */
	public XMLMeasuresSerializer() {
		super();
	}
  
	@Override
  public final String getId() {
    return ID;
  }

	@Override
	public String serialize(MeasureCache inCache) {

		StringBuilder lBuilder = new StringBuilder("<devices>\n");
		for (String lCurrDeviceId : inCache.getDeviceIds()) {

			lBuilder.append("\t<device id=\"");
			lBuilder.append(lCurrDeviceId);
			lBuilder.append("\">\n");
			lBuilder.append(serializeMeasures(inCache.getMeasures(lCurrDeviceId)));
			lBuilder.append("\t</device>\n");
		} // for
		lBuilder.append("</devices>");

		return lBuilder.toString();
	}

	/**
	 * Serializes the given collection of measures.
	 * 
	 * @param inMeasures
	 *            The measures to serialize.
	 * @return The resulting XML.
	 */
	private String serializeMeasures(Collection<Measure> inMeasures) {

		StringBuilder lBuilder = new StringBuilder("\t\t<measures>\n");
		for (Measure lCurrMeasure : inMeasures) {

			lBuilder.append("\t\t\t<measure>\n");
			lBuilder.append("\t\t\t\t<id>");
			lBuilder.append(lCurrMeasure.getId());
			lBuilder.append("</id>\n");
			lBuilder.append("\t\t\t\t<value>");
			lBuilder.append(lCurrMeasure.getValue());
			lBuilder.append("</value>\n");
			lBuilder.append("\t\t\t</measure>\n");
		} // for
		lBuilder.append("\t\t<measures>\n");

		return lBuilder.toString();
	}
}