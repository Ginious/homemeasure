package ginious.home.measure.model;

/**
 * A serializer is capable of providing all device measures in a machine and/ or
 * human readable form like e.g. HTML, JSON, XML, etc.
 */
public interface MeasurementsSerializer {

	/**
	 * Performs the serialization of all measures provided by the given cache.
	 * 
	 * @param inCache The cache containing the measures to serialize.
	 * @return The serialized measures like e.g. HTML, JSON or XML.
	 */
	String serialize(MeasureCache inCache);

	/**
	 * Gets the id of the serializer.
	 * 
	 * @return The id.
	 */
	String getId();
}