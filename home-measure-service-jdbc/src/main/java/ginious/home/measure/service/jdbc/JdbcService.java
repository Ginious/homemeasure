package ginious.home.measure.service.jdbc;

import java.sql.Timestamp;
import java.util.Date;

import javax.sql.DataSource;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

import ginious.home.measure.model.AbstractService;
import ginious.home.measure.model.Measure;
import ginious.home.measure.model.MeasureCache;
import lombok.extern.slf4j.Slf4j;

/**
 * A recorder that is capable of writing measures into a relational database.
 */
@Slf4j
@Service
@ConditionalOnProperty(prefix = JdbcServiceConfig.CONFIG_PREFIX, name = "enabled", matchIfMissing = false)
public final class JdbcService extends AbstractService<JdbcServiceConfig> {

  private DataSource dataSource;

  private static final String INSERT_STMT = "INSERT INTO %s (%s,%s,%s,%s) VALUES (?,?,?,?)";

  /**
   * DI constructor called by Spring.
   * 
   * @param inCache
   *          The cache for measurements.
   * @param inConfig
   *          The service configuration.
   */
  public JdbcService(MeasureCache inCache, JdbcServiceConfig inConfig, DataSource inDataSource) {
    super(inCache, inConfig);
    
    dataSource = inDataSource;
  }

  @Override
  protected void measureChangedCustom(Measure inChangedMeasure) {

    String lInsertQuery = String.format(INSERT_STMT, getConfig().getTablename(),
        getConfig().getColumn_name_device(), getConfig().getColumn_name_data(), getConfig().getColumn_name_value(),
        getConfig().getColumn_name_timestamp());
    new JdbcTemplate(dataSource).update(lInsertQuery,
        inChangedMeasure.getDeviceId(), inChangedMeasure.getId(), inChangedMeasure.getValue(),
        new Timestamp(new Date().getTime()));

    log.debug("Persisted measure [{}.{}={}]", inChangedMeasure.getDeviceId(),
        inChangedMeasure.getId(), inChangedMeasure.getValue());
  }

  @Override
  protected void initCustom() {
    initIncludedMeasures(getConfig().getIncluded_measures());
  }
}