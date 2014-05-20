package cloudSimulator.algorihms;

import algorithms.DataCenterMigration;
import cloudSimulator.weather.Forecast;
import cloudSimulator.weather.Weather;
import java.util.Date;
import java.util.TreeMap;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import simulation.DataCenter;
import simulation.ElasticityManager;
import utils.Utils;

@Service(value = "migrationDoNothing")
public class DataCenterMigrationDoNothing implements DataCenterMigration {

  static Logger logger = LoggerFactory.getLogger(DataCenterMigrationRandom.class);

  private TreeMap<Double, DataCenter> currentEnergyPrices;

  @Autowired
  private Forecast forecastService;

  public void manageVirtualMachines(ElasticityManager em, int minute) {
    currentEnergyPrices = new TreeMap<Double, DataCenter>();
    Date currentTime = Utils.getCurrentTime(minute);
    double costs = 0.;
    logger.trace("--- " + currentTime + " ----");
    for (DataCenter dc : em.getDataCenters()) {
      Weather currentWeather = forecastService.getForecast(currentTime, dc.getLocation(), true);
      Double cPrice = Utils.getCoolingEnergyFactor(currentWeather.getCurrentTemperature()) * dc.getCurrentEneryPrice(Utils.getCurrentTime(minute));
      currentEnergyPrices.put(cPrice, dc);
      costs += cPrice;
    }

    logger.trace("Total energy costs: " + costs);

    logger.trace("Time - " + currentTime + " -  no migration.");

  }

}
