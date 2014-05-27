package algorithms;

import java.util.ArrayList;
import java.util.HashMap;

import model.PhysicalMachine;
import model.ServiceLevelAgreement;
import model.VirtualMachine;
import simulation.DataCenter;
import utils.Utils;

public class SLAViolationAlgorithm {

	/**
	 * Returns the current SLA violations
	 * If this function is called with only 1 datacenter in @datacenterList and we have violations,
	 * an action like migrations of VMs should be done
	 * @return Returns the number of total SLA violations in all datacenters given by @datacenterList
	 * 		   If > 0, we have violations
	 */
	public int getCurrentSLAViolsations(int minute, ArrayList<DataCenter> datacenterList) {
		int violations = 0;
		// Stores if a PM has violations
		HashMap<PhysicalMachine, Boolean> violationMapMemory = new HashMap<PhysicalMachine, Boolean>();
		HashMap<PhysicalMachine, Boolean> violationMapCPUs = new HashMap<PhysicalMachine, Boolean>();
		HashMap<PhysicalMachine, Boolean> violationMapNetwork = new HashMap<PhysicalMachine, Boolean>();
		
		ArrayList<ServiceLevelAgreement> slaList = new ArrayList<ServiceLevelAgreement>();
		
		// Get all slas in our system and store if pms are violated
		for (DataCenter dc : datacenterList) {
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
				// This SLA is violated in cpus
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
				// This SLA is violated in networkbandwidth
				if (isViolated) {
					violations++;
				}
			}			
		}
		return violations;
	}
}
