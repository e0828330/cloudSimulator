package cloudSimulator.algorihms;

import algorithms.DataCenterMigration;
import cloudSimulator.weather.Forecast;
import cloudSimulator.weather.Weather;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import model.PhysicalMachine;
import model.VirtualMachine;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import simulation.DataCenter;
import simulation.ElasticityManager;
import utils.Utils;

@Service(value = "migrationRandom")
public class DataCenterMigrationRandom implements DataCenterMigration {

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

    VirtualMachine vm = findVMToMigrate(currentEnergyPrices);
    if (null != vm) {
      DataCenter dc = findDataCenterToMigrateTo(currentEnergyPrices, vm);
      if (null != dc && !dc.equals(vm.getPm().getDataCenter()) && isMigrationValuable(vm, dc, minute)) {
        logger.trace("From DC: " + vm.getPm().getDataCenter().getName() + " to DC: " + dc.getName());
        em.migrate(vm, vm.getPm().getDataCenter(), dc);
      } else {
        logger.trace("No target dc found for vm on " + vm.getPm().getDataCenter().getName());
      }
    } else {
      logger.trace("Time - " + currentTime + " -  no migration.");
    }
  }

  /**
   * Gets a random VM of a random PM of the most expensive DC
   *
   * @param map
   * @return VirtualMachine
   */
  public VirtualMachine findVMToMigrate(TreeMap<Double, DataCenter> map) {
    VirtualMachine vmRand = null;
    DataCenter dc;
    for (Map.Entry<Double, DataCenter> entry : map.descendingMap().entrySet()) {
      dc = entry.getValue();
      if (dc.getPhysicalMachines().size() > 0) {
        int randomPM = 0 + (int) (Math.random() * (dc.getPhysicalMachines().size() - 1));
        PhysicalMachine pm = dc.getPhysicalMachines().get(randomPM);
        if (pm.getVirtualMachines().size() > 0) {
          int randomVM = 0 + (int) (Math.random() * (pm.getVirtualMachines().size() - 1));
          vmRand = pm.getVirtualMachines().get(randomVM);
          break;
        }

      }
    }
    return vmRand;
  }

  /**
   * Finds a random DC with available Memory for the VM
   *
   * @param map
   * @param vm
   * @return DataCenter
   */
  public DataCenter findDataCenterToMigrateTo(TreeMap<Double, DataCenter> map, VirtualMachine vm) {
    DataCenter dc = null;
    List<Double> keys = new ArrayList<Double>(map.keySet());
    Collections.shuffle(keys);
    
    
    for (Double o : keys) {
      DataCenter randomDC = map.get(o);
      if (!randomDC.equals(vm.getPm().getDataCenter()) && randomDC.getHighestAvailableFreeMemory() > vm.getMemory() * vm.getUsedMemory() && (dc == null || randomDC.getHighestAvailableFreeMemory() < dc.getHighestAvailableFreeMemory())) {
        dc = randomDC;
        break;
      }
    }

    return dc;
  }

  /**
   * Decides wether a migration is valuable
   *
   * @param sourceVM
   * @param targetDC
   * @param minute
   * @return
   */
  public boolean isMigrationValuable(VirtualMachine sourceVM, DataCenter targetDC, int minute) {
    return true; //(0 + (int) (Math.random() * (10))) % 2 == 0;
  }

}
