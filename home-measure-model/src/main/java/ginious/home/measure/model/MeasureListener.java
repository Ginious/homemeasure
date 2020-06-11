package ginious.home.measure.model;

/**
 * Listener for measures.
 */
public interface MeasureListener {

	/**
	 * Called when a measure changed.
	 * 
	 * @param inChangedMeasure The measure that has changed.
	 */
	void measureChanged(Measure inChangedMeasure);
}