package simulation;

import algorithms.DataCenterMigration;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
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
	 * Returns the current SLA violations for this all datacenters
	 * 
	 * @return
	 */
	public int getCurrentSLAViolsations(int minute) {
		int violations = 0;
		// Stores if a PM has violations
		HashMap<PhysicalMachine, Boolean> violationMapMemory = new HashMap<PhysicalMachine, Boolean>();
		HashMap<PhysicalMachine, Boolean> violationMapCPUs = new HashMap<PhysicalMachine, Boolean>();
		HashMap<PhysicalMachine, Boolean> violationMapNetwork = new HashMap<PhysicalMachine, Boolean>();
		
		ArrayList<ServiceLevelAgreement> slaList = new ArrayList<ServiceLevelAgreement>();
		
		// Get all slas in our system and store if pms are violated
		for (DataCenter dc : dataCenters) {
			slaList = dc.getSLAs();
			for (PhysicalMachine pm : dc.getPhysicalMachines()) {
				if (!violationMapMemory.containsKey(pm)) {
					violationMapMemory.put(pm, Utils.PMMemoryIsViolated(pm));
				}
				if (!violationMapCPUs.containsKey(pm)) {
					violationMapCPUs.put(pm, Utils.PMCPUsIsViolated(pm));
				}
				if (!violationMapNetwork.containsKey(pm)) {
					violationMapNetwork.put(pm, Utils.PMBandwidthIsViolated(pm));
				}				
			}
		}
		
		for (ServiceLevelAgreement sla : slaList) {
			double downtime = sla.getDownTimeInPercent(minute);
			
			// Downtime is violated
			if (downtime > sla.getMaxDowntime()) {
				violations++;
				continue;
			}

			// Now iterate through the SLAs and check if they are violated
			
			// Memory	
			double assignedUsedMemory = 0.;
			boolean allVMsHaveFullMemoryLoad = false;
			
			for (VirtualMachine vm : sla.getVms()) {
				if (vm.isOnline() == false) continue;
				assignedUsedMemory += vm.getUsedMemory() * vm.getMemory();
				allVMsHaveFullMemoryLoad = allVMsHaveFullMemoryLoad && (vm.getUsedMemory() >= 1.); // if this vm has full memory load
			}
	
			if (allVMsHaveFullMemoryLoad && assignedUsedMemory < sla.getMemory()) {
				// Memory of sla is violated because the load of all vms is 100% and the memory of the vms assigned < SLA
				violations++;
			}
			else if (allVMsHaveFullMemoryLoad) {
				// All vms of this sla have full load, but the SLA is not yet violated.
				// Now check if one PM where the vms are running is overloaded, if yes, we have a violation (swapping)
				boolean isViolated = false;
				for (VirtualMachine vm : sla.getVms()) {
					isViolated |= violationMapMemory.get(vm.getPm());
				}
				// This SLA is violated in memory
				if (isViolated) {
					violations++;
				}
			}
			
			
			// CPUs
			double assignedUsedCPUs = 0.;
			boolean allVMsHaveFullCPULoad = false;
			
			for (VirtualMachine vm : sla.getVms()) {
				if (vm.isOnline() == false) continue;
				assignedUsedCPUs += vm.getUsedCPUs() * vm.getCpus();
				allVMsHaveFullCPULoad = allVMsHaveFullCPULoad && (vm.getUsedCPUs() >= 1.); // if this vm has full cpu load
			}
	
			if (allVMsHaveFullCPULoad && assignedUsedCPUs < sla.getCpus()) {
				// CPUs of sla is violated because the load of all vms is 100% and the cpus of the vms assigned < SLA
				violations++;
			}
			else if (allVMsHaveFullCPULoad) {
				// All vms of this sla have full load, but the SLA is not yet violated.
				// Now check if one PM where the vms are running is overloaded, if yes, we have a violation
				boolean isViolated = false;
				for (VirtualMachine vm : sla.getVms()) {
					isViolated |= violationMapCPUs.get(vm.getPm());
				}
				// This SLA is violated in memory
				if (isViolated) {
					violations++;
				}
			}			
			
			// Bandwidth
			double assignedUsedBandwidth = 0.;
			boolean allVMsHaveFullBandwidthLoad = false;
			
			for (VirtualMachine vm : sla.getVms()) {
				if (vm.isOnline() == false) continue;
				assignedUsedBandwidth += vm.getUsedBandwidth() * vm.getBandwidth();
				allVMsHaveFullBandwidthLoad = allVMsHaveFullBandwidthLoad && (vm.getUsedBandwidth() >= 1.); // if this vm has full bandwidth load
			}
	
			if (allVMsHaveFullBandwidthLoad && assignedUsedBandwidth < sla.getBandwidth()) {
				// CPUs of sla is violated because the load of all vms is 100% and the bandwidth of the vms assigned < SLA
				violations++;
			}
			else if (allVMsHaveFullBandwidthLoad) {
				// All vms of this sla have full load, but the SLA is not yet violated.
				// Now check if one PM where the vms are running is overloaded, if yes, we have a violation
				boolean isViolated = false;
				for (VirtualMachine vm : sla.getVms()) {
					isViolated |= violationMapNetwork.get(vm.getPm());
				}
				// This SLA is violated in memory
				if (isViolated) {
					violations++;
				}
			}			
		}
		return violations;
	}

}
