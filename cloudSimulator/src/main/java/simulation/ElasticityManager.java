package simulation;

import java.util.List;

import algorithms.DataCenterMigration;
import lombok.Data;
import model.VirtualMachine;

@Data
public class ElasticityManager {
	
	private DataCenterMigration algorithm;
	private List<DataCenter> dataCenters;
	
	/* TODO: Implement */
	
	/**
	 * Migrates a VM from the dataCenter source to target
	 * This works as follows:
	 * 1) VM is set to off line
	 * 2) VM gets removed from source dataCenter
	 * 3) VM migration time gets computed
	 * 4) targetDatacenter.queueAddVirtualMachine(virtualMachine, targetTime) gets called
	 * 5) The dataCenter adds the VM to its list
	 * 6) DataCenter turn the VM on once targetTime is reached
	 * 
	 * @param vm
	 * @param source
	 * @param target
	 */
	public void migrate(VirtualMachine vm, DataCenter source, DataCenter target) {
		
	}
	
}
