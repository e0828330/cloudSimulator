package cloudSimulator.algorihms;

import algorithms.DataCenterMigration;
import cloudSimulator.weather.Forecast;
import cloudSimulator.weather.Weather;
import java.util.Map;
import java.util.TreeMap;
import model.PhysicalMachine;
import model.VirtualMachine;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import simulation.DataCenter;
import simulation.ElasticityManager;
import utils.Utils;

@Service(value = "migrationBestFit")
public class DataCenterMigrationBestFit implements DataCenterMigration {

    private TreeMap<Double, DataCenter> currentEnergyPrices;

    @Autowired
    private Forecast f;

    public void manageVirtualMachines(ElasticityManager em, int minute) {
        // TODO Auto-generated method stub
        currentEnergyPrices = new TreeMap<Double, DataCenter>();
        for (DataCenter dc : em.getDataCenters()) {
            Weather currentWeather = f.getForecast(Utils.getCurrentTime(minute), dc.getLocation(), true);
            Double cPrice = Utils.getCoolingEnergyFactor(currentWeather.getCurrentTemperature()) * dc.getCurrentEneryPrice(Utils.getCurrentTime(minute));
            currentEnergyPrices.put(cPrice, dc);

        }
        
        VirtualMachine vm = findVMToMigrate(currentEnergyPrices);
        if (null != vm) {
             DataCenter dc = findDataCenterToMigrateTo(currentEnergyPrices, vm);
             if(null != dc && !dc.equals(vm.getPm().getDataCenter()) && isMigrationValuable(vm, dc, minute)){
                System.out.print("From DC: " + vm.getPm().getDataCenter().getName());
                System.out.println(" To DC: " + dc.getName());
                 em.migrate(vm, vm.getPm().getDataCenter(), dc);
             }
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
            for (PhysicalMachine pm : entry.getValue().getPhysicalMachines()) {
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
            if(entry.getValue().getHighestAvailableFreeMemory() > vm.getMemory() * vm.getUsedMemory() && (dc == null || entry.getValue().getHighestAvailableFreeMemory() < dc.getHighestAvailableFreeMemory())){
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
        // TODO look at forecast
        Weather sourceWeather = f.getForecast(Utils.getCurrentTime(minute), sourceVM.getPm().getDataCenter().getLocation(), true);
        Weather targetWeather = f.getForecast(Utils.getCurrentTime(minute), targetDC.getLocation(), true);
        DataCenter sourceDC = sourceVM.getPm().getDataCenter();
        double targetForecastPrice = targetDC.getCurrentEneryPrice(Utils.getCurrentTime(minute)) * Utils.getCoolingEnergyFactor(targetWeather.getForecast() * targetWeather.getCurrentTemperature());
        double sourceForecastPrice = sourceDC.getCurrentEneryPrice(Utils.getCurrentTime(minute)) * Utils.getCoolingEnergyFactor(sourceWeather.getForecast() * sourceWeather.getCurrentTemperature());
        //System.out.println(targetForecastPrice + " < " + sourceForecastPrice);
        return targetForecastPrice < sourceForecastPrice;
    }
    
    
}
