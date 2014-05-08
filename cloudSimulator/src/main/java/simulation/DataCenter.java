package simulation;

import algorithms.DataCenterManagement;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import lombok.Data;
import model.PhysicalMachine;
import model.VirtualMachine;
import algorithms.DataCenterManagement;
import cloudSimulator.weather.Location;

@Data
public class DataCenter {

	private HashMap<VirtualMachine, Integer> migrationQueue = new HashMap<VirtualMachine, Integer>();
	
	/**
	 * Gets called on every simulated minute.
	 * Here VM allocation and load updating should be done
	 */
	public void simulate(int minute) {
		handleMigrations(minute);
		for (PhysicalMachine pm : physicalMachines) {
			if (pm.isRunning()) {
				pm.updateLoads();
			}
		}
		algorithm.scaleVirtualMachines(this);
		//System.out.printf("[%s] - Simulated times is %s\n", name, Utils.getCurrentTime(minute));
	}
	
	/**
	 * Called when a VM gets migrated to this dataCenter
	 * @param vm
	 * @param targetTime
	 */
	public void queueAddVirtualMachine(VirtualMachine vm, int targetTime) {
		migrationQueue.put(vm, targetTime);
	}
	
	/**
	 * Handle incoming migrations by looping over the queue and find
	 * the ones that have arrived yet (i.e the target time is reached) 
	 */
	private void handleMigrations(int minute) {
		Iterator<Map.Entry<VirtualMachine, Integer>> iter = migrationQueue.entrySet().iterator();
		while(iter.hasNext()) {
			Map.Entry<VirtualMachine, Integer> entry = iter.next();
			if (entry.getValue() >= minute) {
				VirtualMachine vm = entry.getKey();
				PhysicalMachine pm = algorithm.findPMForMigration(this, vm);
				pm.getVirtualMachines().add(vm);
				vm.setOnline(true);
				iter.remove();
			}
		}
	}
	
	/**
	 * Returns the PowerConsumption of the data center in Watt
	 * 
	 * @return
	 */
	public double getPowerConsumption() {
		double result = 0.;
		for (PhysicalMachine pm : physicalMachines) {
			if (pm.isRunning()) {
				result += pm.getPowerConsumption();
			}
		}
		return result;
	}
	
    private String name;
    private List<PhysicalMachine> physicalMachines;
    private DataCenterManagement algorithm;

    private float energyPriceDay;
    private float energyPriceNight;
    private int timezoneOffset;
    private Location location;

    private HashMap<VirtualMachine, Long> migrationQueue = new HashMap<VirtualMachine, Long>();

    /**
     * Gets called on every simulated minute. Here VM allocation and load
     * updating should be done
     */
    public void simulate(int minute) {
        handleMigrations();
        for (PhysicalMachine pm : physicalMachines) {
            if (pm.isRunning()) {
                pm.updateLoads();
            }
        }
        algorithm.scaleVirtualMachines(this);
        System.out.printf("[%s] - Simulated times is %s\n", name, Utils.getCurrentTime(minute));
    }
    
    public float getCurrentEneryPrice(Date date){
        Calendar cal = Calendar.getInstance(); // creates calendar
        cal.setTime(new Date()); // sets calendar time/date
        cal.add(Calendar.HOUR_OF_DAY, this.timezoneOffset); // adds one hour
        int hour = cal.get(Calendar.HOUR_OF_DAY);
        return hour >= 8 && hour < 20 ? this.energyPriceDay : this.energyPriceNight;
    }
    
    public double getAveragePricePerVM(Date date){
        return this.getPowerConsumption() / (double)this.getPhysicalMachines().size() * this.getCurrentEneryPrice(date);
    }

    /**
     * Called when a VM gets migrated to this dataCenter
     *
     * @param vm
     * @param targetTime
     */
    public void queueAddVirtualMachine(VirtualMachine vm, Long targetTime) {
        migrationQueue.put(vm, targetTime);
    }

    /**
     * Handle incoming migrations by looping over the queue and find the ones
     * that have arrived yet (i.e the target time is reached)
     */
    private void handleMigrations() {
        Iterator<Map.Entry<VirtualMachine, Long>> iter = migrationQueue.entrySet().iterator();
        Long currentTime = System.currentTimeMillis();
        while (iter.hasNext()) {
            Map.Entry<VirtualMachine, Long> entry = iter.next();
            if (entry.getValue() >= currentTime) {
                VirtualMachine vm = entry.getKey();
                PhysicalMachine pm = algorithm.findPMForMigration(this, vm);
                pm.getVirtualMachines().add(vm);
                vm.setOnline(true);
                iter.remove();
            }
        }
    }

    /**
     * Returns the PowerConsumption of the data center in Watt
     *
     * @return
     */
    public double getPowerConsumption() {
        double result = 0.;
        for (PhysicalMachine pm : physicalMachines) {
            if (pm.isRunning()) {
                result += pm.getPowerConsumption();
            }
        }
        return result;
    }

}
