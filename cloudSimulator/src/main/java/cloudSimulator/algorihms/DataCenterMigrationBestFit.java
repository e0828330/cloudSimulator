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
            Double cPrice = dc.getCurrentEneryPrice(Utils.getCurrentTime(minute)) * (Utils.getCoolingEnergyFactor(currentWeather.getCurrentTemperature()) * dc.getCurrentEneryPrice(Utils.getCurrentTime(minute)));
            currentEnergyPrices.put(cPrice, dc);

        }
        
        VirtualMachine vm = findVMToMigrate(currentEnergyPrices);
        if (null != vm) {
             DataCenter dc = findDataCenterToMigrateTo(currentEnergyPrices, vm);
             if(null != dc && !dc.equals(vm.getPm().getDataCenter()) && isMigrationValuable(vm, dc)){
                System.out.print("From DC: " + vm.getPm().getDataCenter().getName());
                System.out.println(" To DC: " + dc.getName());
                 em.migrate(vm, vm.getPm().getDataCenter(), dc);
             }
        }
    }

    public VirtualMachine findVMToMigrate(TreeMap<Double, DataCenter> map) {
        VirtualMachine vmMin = null;
        DataCenter dc = null;
        for (Map.Entry<Double, DataCenter> entry : map.descendingMap().entrySet()) {
            dc = entry.getValue();
            for (PhysicalMachine pm : entry.getValue().getPhysicalMachines()) {
                for (VirtualMachine vm : pm.getOnlineVMs()) {
                    double bandwidth = pm.getBandwidth() * (1 - pm.getBandwidthUtilization());
                    if (vmMin == null || Utils.getMigrationTime(bandwidth, ((double) vm.getMemory()) * vm.getUsedMemory()) < Utils.getMigrationTime(bandwidth, vmMin.getMemory() * vmMin.getUsedMemory())) {
                        vmMin = vm;
                    }
                }
                if (null != vmMin) {
                    break;
                }
            }
        }
        return vmMin;
    }
    
    public DataCenter findDataCenterToMigrateTo(TreeMap<Double, DataCenter> map, VirtualMachine vm){
        DataCenter dc = null;
        
        for (Map.Entry<Double, DataCenter> entry : map.entrySet()) {
            if(entry.getValue().getHighestAvailableFreeMemory() > vm.getMemory() * vm.getUsedMemory() && (dc == null || entry.getValue().getHighestAvailableFreeMemory() < dc.getHighestAvailableFreeMemory())){
                dc = entry.getValue();
            }
        }
        
        return dc;
    }
    
    public boolean isMigrationValuable(VirtualMachine sourceVM, DataCenter targetDC){
        // TODO look at forecast
        return true;
    }
    
    
}
