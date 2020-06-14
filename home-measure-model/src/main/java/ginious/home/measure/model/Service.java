package ginious.home.measure.model;

/**
 * Tagging interface for services as those are self contained and thus need a public API.
 */
public interface Service {

	/**
	 * Gets the id of the service which is the class name without the suffix 'Service'.
	 * .
	 * @return The service Id.
	 */
	String getId();
}