package ginious.home.measure.service.influxdb;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang.math.NumberUtils;
import org.influxdb.BatchOptions;
import org.influxdb.InfluxDB;
import org.influxdb.InfluxDBFactory;
import org.influxdb.dto.Point;
import org.influxdb.dto.Query;
import org.influxdb.dto.QueryResult;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Service;

import ginious.home.measure.model.AbstractService;
import ginious.home.measure.model.Measure;
import lombok.extern.slf4j.Slf4j;

/**
 * Service for writing measures into an InfluxDB. One InfluxDB measure will be
 * created per device containing the HMServer measure id and its value.
 */
@Slf4j
@Service
@ConditionalOnProperty(prefix = InfluxDbServiceConfig.CONFIG_PREFIX, name = "enabled", havingValue = "true")
public final class InfluxDbService extends AbstractService {

	@Autowired
	private InfluxDbServiceConfig config;

	private static final String POINT_FIELD_VALUE = "value";
	private static final String POINT_TAG_MEASURE_ID = "measure_id";
	private static final String QUERY_CREATE_DATABASE = "CREATE DATABASE %s";
	private static final String QUERY_SHOW_DATABASES = "SHOW DATABASES";

	private InfluxDB influxDB;

	/**
	 * Default constructor.
	 * 
	 * @param inProperties
	 *            The overall application properties.
	 */
	public InfluxDbService() {
		super();
	}

	@Override
	protected void measureChangedCustom(Measure inChangedMeasure) {

		Number valueAsNumber = null;
		try {
			valueAsNumber = NumberUtils.createNumber(inChangedMeasure.getValue().trim());
		} catch (NumberFormatException e) {
			// ignore and do not write measure
		} // catch

		if (valueAsNumber != null) {

			getInfluxDB().write(Point//
					.measurement(inChangedMeasure.getDeviceId()) //
					.tag(POINT_TAG_MEASURE_ID, inChangedMeasure.getId()) //
					.addField(POINT_FIELD_VALUE, valueAsNumber).build());

			log.debug("Persisted measure [{}.{}={}]", inChangedMeasure.getDeviceId(), inChangedMeasure.getId(),
					inChangedMeasure.getValue());

		} // if
	}

	/**
	 * Gets a connection to the InfluxDB.
	 * 
	 * @return The connection to the InfluxDB.
	 */
	private InfluxDB getInfluxDB() {

		if (influxDB == null) {

			log.info("Connecting InfluxDB as [{}/{}] to [{}]",
					config.getUser() != null ? config.getUser() : "<anonymous>",
					config.getPassword() != null ? "*******" : "none", config.getUrl());

			influxDB = InfluxDBFactory.connect(config.getUrl(), config.getUser(), config.getPassword());

			// gather existing database names
			final List<String> existingDbs = new ArrayList<String>();
			QueryResult dbList = influxDB.query(new Query(QUERY_SHOW_DATABASES));
			dbList.getResults().forEach( //
					result -> result.getSeries().forEach( //
							serie -> serie.getValues().forEach( //
									value -> value.forEach(//
											dbname -> existingDbs.add(String.valueOf(dbname))))));

			// create database if not yet existing
			if (!existingDbs.contains(config.getDbname())) {
				influxDB.query(new Query(String.format(QUERY_CREATE_DATABASE, config.getDbname())));
				log.info("Created database [{}]");
			} // if

			influxDB.setDatabase(config.getDbname());
			influxDB.enableBatch(BatchOptions.DEFAULTS.flushDuration(config.getFlush_duration()));
			influxDB.enableGzip();
		} // if

		return influxDB;
	}

	@Override
	protected void initCustom() {
		initIncludedMeasures(config.getIncluded_measures());
	}
}