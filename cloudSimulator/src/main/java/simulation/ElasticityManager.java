package simulation;

import algorithms.DataCenterMigration;
import java.util.List;
import lombok.Data;
import model.VirtualMachine;
import utils.Utils;

@Data
public class ElasticityManager {
	
	private DataCenterMigration algorithm;
	private List<DataCenter> dataCenters;
	
	/* TODO: Implement */
	
	/**
     * TODO: LIVE VM Migration?
     * 
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
		vm.setOnline(false);                                                    // 1)
        vm.getPm().getVirtualMachines().remove(vm);                                // 2)
        int targetTime = Utils.getMigrationTime(vm.getPm().getBandwidth() * 
                (1-vm.getPm().getBandwidthUtilization()), 
                vm.getMemory()*vm.getUsedMemory() + vm.getSize());                             // 3)
        
        target.queueAddVirtualMachine(vm, targetTime);                          // 4)
        
	}
	
	public void simulate(int minute) {
		//  TODO: Implement rest
		for(DataCenter dc : dataCenters) {
			dc.simulate(minute);
		}
		
		/* Run the algorithm once per hour */
		if ((minute % 60) == 0) {
			algorithm.manageVirtualMachines(this, minute);
		}
	}
	
}
