package algorithms;

import model.PhysicalMachine;
import model.VirtualMachine;
import simulation.DataCenter;

public interface DataCenterManagement {
	/**
	 * Sets virtual machine parameters like CPU, memory etc. based on SLA and
	 * load If it does not fit it can do migration between physical machines
	 * inside the dataCenter.
	 * 
	 * @param dc
	 */
	public void scaleVirtualMachines(DataCenter dc);

	/**
	 * Finds a PM for the incoming VM that got migrated
	 * 
	 * @param dc
	 * @param vm
	 * 
	 * @return {PhysicalMachine}
	 */
	public PhysicalMachine findPMForMigration(DataCenter dc, VirtualMachine vm);
}
