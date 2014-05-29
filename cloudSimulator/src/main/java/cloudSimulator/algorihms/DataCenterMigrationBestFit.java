package cloudSimulator.algorihms;

import java.util.Date;
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
import algorithms.DataCenterMigration;
import cloudSimulator.weather.Forecast;
import cloudSimulator.weather.Weather;

@Service(value = "migrationBestFit")
public class DataCenterMigrationBestFit implements DataCenterMigration {

	static Logger logger = LoggerFactory.getLogger(DataCenterMigrationBestFit.class);
	
    private TreeMap<Double, DataCenter> currentEnergyPrices;

    @Autowired
    private Forecast forecastService;

    public void manageVirtualMachines(ElasticityManager em, int minute) {
        currentEnergyPrices = new TreeMap<Double, DataCenter>();
        Date currentTime = Utils.getCurrentTime(minute);
        logger.trace("--- " + currentTime + " ----");
        for (DataCenter dc : em.getDataCenters()) {
            currentEnergyPrices.put(dc.getCurrentEnergyCosts(minute), dc);
        }
     
        VirtualMachine vm = findVMToMigrate(currentEnergyPrices);
        if (null != vm) {
             DataCenter dc = findDataCenterToMigrateTo(currentEnergyPrices, vm);
             if(null != dc && !dc.equals(vm.getPm().getDataCenter()) && isMigrationValuable(vm, dc, minute)){
            	 logger.trace("From DC: " + vm.getPm().getDataCenter().getName() + " to DC: " + dc.getName());
                em.migrate(vm, vm.getPm().getDataCenter(), dc);
             }
             else {
            	 logger.trace("No target dc found for vm on " + vm.getPm().getDataCenter().getName());
             }
        }
        else {
        	logger.trace("Time - " + currentTime + " -  no migration.");
        }
    }

    /**
     * Gets the VM with the lowest migration time on the most expensive DC
     * 
     * @param map
     * @return VirtualMachine
     */
    public VirtualMachine findVMToMigrate(TreeMap<Double, DataCenter> map) {
        VirtualMachine vmMin = null;
        DataCenter dc = null;
        double currentMigrationSize = 0.;
        for (Map.Entry<Double, DataCenter> entry : map.descendingMap().entrySet()) {
            dc = entry.getValue();
            for (PhysicalMachine pm : dc.getPhysicalMachines()) {
                for (VirtualMachine vm : pm.getOnlineVMs()) {
                    double bandwidth = pm.getBandwidth() * (1 - pm.getBandwidthUtilization());
                    if (vmMin == null || Utils.getMigrationTime(bandwidth, (vm.getMemory()) * vm.getUsedMemory() + vm.getSize()) < Utils.getMigrationTime(bandwidth, currentMigrationSize)) {
                        vmMin = vm;
                        currentMigrationSize = vmMin.getMemory() * vmMin.getUsedMemory() + vmMin.getSize();
                    }
                }
                if (null != vmMin) {
                    break;
                }
            }
        }
        return vmMin;
    }
    
    /**
     * Finds the cheapest DC with available Memory for the VM
     * 
     * @param map
     * @param vm
     * @return DataCenter
     */
    public DataCenter findDataCenterToMigrateTo(TreeMap<Double, DataCenter> map, VirtualMachine vm){
        DataCenter dc = null;
        
        for (Map.Entry<Double, DataCenter> entry : map.entrySet()) {
            if(!entry.getValue().equals(vm.getPm().getDataCenter()) &&  entry.getValue().getHighestAvailableFreeMemory() > vm.getMemory() * vm.getUsedMemory() && (dc == null || entry.getValue().getHighestAvailableFreeMemory() < dc.getHighestAvailableFreeMemory())){
                dc = entry.getValue();
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
     * @return 
     */
    public boolean isMigrationValuable(VirtualMachine sourceVM, DataCenter targetDC, int minute){
        Weather sourceWeather = forecastService.getForecast(Utils.getCurrentTime(minute), sourceVM.getPm().getDataCenter().getLocation(), true);
        Weather targetWeather = forecastService.getForecast(Utils.getCurrentTime(minute), targetDC.getLocation(), true);
        DataCenter sourceDC = sourceVM.getPm().getDataCenter();
        double targetForecastPrice = targetDC.getCurrentEneryPrice(Utils.getCurrentTime(minute)) * Utils.getCoolingEnergyFactor(targetWeather.getForecast() * targetWeather.getCurrentTemperature());
        double sourceForecastPrice = sourceDC.getCurrentEneryPrice(Utils.getCurrentTime(minute)) * Utils.getCoolingEnergyFactor(sourceWeather.getForecast() * sourceWeather.getCurrentTemperature());
        return targetForecastPrice < sourceForecastPrice;
    }
    
    
}
