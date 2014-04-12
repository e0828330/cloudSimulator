package algorithms;

import simulation.DataCenter;

public interface DataCenterManagement {
	/**
	 * Sets virtual machine parameters like CPU, memory etc. based on SLA and load
	 * If it does not fit it can do migration between physical machines inside the
	 * dataCenter.
	 * 
	 * @param dc
	 */
	public void scaleVirtualMachines(DataCenter dc);
}
