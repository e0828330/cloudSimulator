package simulation;

import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import utils.Utils;
import lombok.Data;
import model.PhysicalMachine;
import model.VirtualMachine;
import algorithms.DataCenterManagement;

@Data
public class DataCenter {
		
	private String name;
	private List<PhysicalMachine> physicalMachines;
	private DataCenterManagement algorithm;
	
	private int minute;

	private ConcurrentHashMap<VirtualMachine, Long> migrationQueue = new ConcurrentHashMap<VirtualMachine, Long>();
	
	/**
	 * Gets called on every simulated minute.
	 * Here VM allocation and load updating should be done
	 */
	public void simulate() {
		minute++;
		handleMigrations();
		System.out.printf("[%s] - Simluated times is %s\n", name, Utils.getCurrentTime(minute));
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
				// TODO: Do the migration
				iter.remove();
			}
		}
	}
	
}
