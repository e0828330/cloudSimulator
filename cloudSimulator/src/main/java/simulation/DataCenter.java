package simulation;

import algorithms.DataCenterManagement;

import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import cloudSimulator.weather.Location;
import lombok.Data;
import model.PhysicalMachine;
import model.VirtualMachine;
import utils.Utils;

@Data
public class DataCenter {
		
	private String name;
	private List<PhysicalMachine> physicalMachines;
	private DataCenterManagement algorithm;
  
  private float energyPriceDay;
  private float energyPriceNight;
  private int timezoneOffset;
  private Location location;

	private HashMap<VirtualMachine, Long> migrationQueue = new HashMap<VirtualMachine, Long>();
	
	/**
	 * Gets called on every simulated minute.
	 * Here VM allocation and load updating should be done
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
	
	/**
	 * Called when a VM gets migrated to this dataCenter
	 * @param vm
	 * @param targetTime
	 */
	public void queueAddVirtualMachine(VirtualMachine vm, Long targetTime) {
		migrationQueue.put(vm, targetTime);
	}
	
	/**
	 * Handle incoming migrations by looping over the queue and find
	 * the ones that have arrived yet (i.e the target time is reached) 
	 */
	private void handleMigrations() {
		Iterator<Map.Entry<VirtualMachine, Long>> iter = migrationQueue.entrySet().iterator();
		Long currentTime = System.currentTimeMillis();
		while(iter.hasNext()) {
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
