package simulation;

import algorithms.DataCenterMigration;

import java.util.ArrayList;
import java.util.List;

import lombok.Data;
import model.PhysicalMachine;
import model.ServiceLevelAgreement;
import model.VirtualMachine;
import utils.Utils;

@Data
public class ElasticityManager {

	private DataCenterMigration algorithm;
	private List<DataCenter> dataCenters = new ArrayList<DataCenter>();

	/* TODO: Implement */

	/**
	 * TODO: LIVE VM Migration?
	 * 
	 * Migrates a VM from the dataCenter source to target This works as follows:
	 * 1) VM is set to off line 2) VM gets removed from source dataCenter 3) VM
	 * migration time gets computed 4)
	 * targetDatacenter.queueAddVirtualMachine(virtualMachine, targetTime) gets
	 * called 5) The dataCenter adds the VM to its list 6) DataCenter turn the
	 * VM on once targetTime is reached
	 * 
	 * @param vm
	 * @param source
	 * @param target
	 */
	public void migrate(VirtualMachine vm, DataCenter source, DataCenter target) {
		vm.setOnline(false); // 1)
		vm.getPm().getVirtualMachines().remove(vm); // 2)
		int targetTime = Utils.getMigrationTime(vm.getPm().getBandwidth()
				* (1 - vm.getPm().getBandwidthUtilization()), vm.getMemory()
				* vm.getUsedMemory() + vm.getSize()); // 3)

		target.queueAddVirtualMachine(vm, targetTime); // 4)

	}

	public void simulate(int minute) {
		// TODO: Implement rest
		for (DataCenter dc : dataCenters) {
			dc.simulate(minute);
		}

		/* Run the algorithm once per hour */
		if ((minute % 60) == 0) {
			algorithm.manageVirtualMachines(this, minute);
		}
	}

	/**
	 * TODO: Downtime??
	 * Returns the current SLA violations for this PM
	 * 
	 * @return
	 */
	public int getCurrentSLAViolsations(int minute) {
		int violations = 0;
		ArrayList<ServiceLevelAgreement> slaList = new ArrayList<ServiceLevelAgreement>();
		// Get all slas in our system
		for (DataCenter dc : dataCenters) {
			slaList = dc.getSLAs();
		}
		
		System.out.println("SLAList = " + slaList.size());
		
		for (ServiceLevelAgreement sla : slaList) {
			int cpus = 0;
			int bandwidth = 0;
			int memory = 0;
			int size = 0;
			double downtime = sla.getDownTimeInPercent(minute);
			
			System.out.println("VMS SIZE = " + sla.getVms().size());
			
			// Get all VMs for each SLA
			for (VirtualMachine vm : sla.getVms()) {
				if (vm.isOnline()) {
					cpus += vm.getCpus();
					bandwidth += vm.getBandwidth();
					memory += vm.getMemory();
					size += vm.getSize();
				}
			}
			System.out.println(downtime + " > " + sla.getMaxDowntime());
			System.out.println(cpus + " < " + sla.getCpus());
			System.out.println(bandwidth + " < " + sla.getBandwidth());
			System.out.println(memory + " < " + sla.getMemory());
			System.out.println(size + " < " + sla.getSize());
			
			if (downtime > sla.getMaxDowntime() ||
				cpus < sla.getCpus() ||
				bandwidth < sla.getBandwidth() || 
				memory < sla.getMemory() ||
				size < sla.getSize()) {
				violations++;
			}
		}
		return violations;
	}

}
