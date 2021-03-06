package cloudSimulator.algorihms;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Random;
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
import algorithms.DataCenterMigration;
import cloudSimulator.ConfigParser;
import cloudSimulator.weather.Forecast;

@Service(value = "migrationRandom")
public class DataCenterMigrationRandom implements DataCenterMigration {

  static Logger logger = LoggerFactory.getLogger(DataCenterMigrationRandom.class);

  private TreeMap<Double, DataCenter> currentEnergyPrices;

  @Autowired
  private Forecast forecastService;
  
  @Autowired
  private ConfigParser cp;
  
  private Random generator;

  public void manageVirtualMachines(ElasticityManager em, int minute) {
    generator = new Random(cp.getRandomSeed());
    
    currentEnergyPrices = new TreeMap<Double, DataCenter>();
    Date currentTime = Utils.getCurrentTime(minute);
    logger.trace("--- " + currentTime + " ----");
    for (DataCenter dc : em.getDataCenters()) {
      currentEnergyPrices.put(dc.getCurrentEnergyCosts(minute), dc);
    }

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
        int randomPM = generator.nextInt(dc.getPhysicalMachines().size())-1;
        PhysicalMachine pm = dc.getPhysicalMachines().get(randomPM >= 0 ? randomPM : 0);
        if (pm.getVirtualMachines().size() > 0) {
          int randomVM = generator.nextInt(pm.getVirtualMachines().size())-1;
          vmRand = pm.getVirtualMachines().get(randomVM >= 0 ? randomVM : 0);
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
    Collections.shuffle(keys, generator);
    
    
    for (Double o : keys) {
      DataCenter randomDC = map.get(o);
      if (!randomDC.equals(vm.getPm().getDataCenter()) && !randomDC.isOverloaded() && randomDC.getHighestAvailableFreeMemory() > vm.getMemory() * vm.getUsedMemory() && (dc == null || randomDC.getHighestAvailableFreeMemory() < dc.getHighestAvailableFreeMemory())) {
        dc = randomDC;
        break;
      }
    }

    return dc;
  }

  /**
   * Decides whether a migration is valuable
   *
   * @param sourceVM
   * @param targetDC
   * @param minute
   * @return
   */
  public boolean isMigrationValuable(VirtualMachine sourceVM, DataCenter targetDC, int minute) {
    return !targetDC.isOverloaded();
  }

}
