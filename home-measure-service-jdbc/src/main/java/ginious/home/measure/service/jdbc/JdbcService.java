package ginious.home.measure.service.jdbc;

import java.sql.Timestamp;
import java.util.Date;

import javax.sql.DataSource;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

import ginious.home.measure.model.AbstractService;
import ginious.home.measure.model.Measure;
import lombok.extern.slf4j.Slf4j;

/**
 * A recorder that is capable of writing measures into a relational database.
 */
@Slf4j
@Service
@ConditionalOnProperty(prefix = JdbcServiceConfig.CONFIG_PREFIX, name = "enabled", matchIfMissing = false)
public final class JdbcService extends AbstractService {

  @Autowired
  private JdbcServiceConfig config;

  @Autowired
  private DataSource dataSource;

  private static final String INSERT_STMT = "INSERT INTO %s (%s,%s,%s,%s) VALUES (?,?,?,?)";

  /**
   * Default constructor.
   */
  public JdbcService() {
    super();
  }

  @Override
  protected void measureChangedCustom(Measure inChangedMeasure) {

    String lInsertQuery = String.format(INSERT_STMT, config.getTablename(),
        config.getColumn_name_device(), config.getColumn_name_data(), config.getColumn_name_value(),
        config.getColumn_name_timestamp());
    new JdbcTemplate(dataSource).update(lInsertQuery,
        inChangedMeasure.getDeviceId(), inChangedMeasure.getId(), inChangedMeasure.getValue(),
        new Timestamp(new Date().getTime()));

    log.debug("Persisted measure [{}.{}={}]", inChangedMeasure.getDeviceId(),
        inChangedMeasure.getId(), inChangedMeasure.getValue());
  }

  @Override
  protected void initCustom() {
    initIncludedMeasures(config.getIncluded_measures());
  }
}