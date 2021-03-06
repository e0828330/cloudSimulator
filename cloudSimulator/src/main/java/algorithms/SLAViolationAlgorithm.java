package algorithms;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;

import lombok.Data;

import org.springframework.stereotype.Service;

import model.PhysicalMachine;
import model.ServiceLevelAgreement;
import model.VirtualMachine;
import simulation.DataCenter;
import utils.Utils;


@Data
@Service
public class SLAViolationAlgorithm {
	/**
	 * This parameter [0.0-1.0] is the percentage where we set a buffer for the violations.
	 * Normally only if we reach 100% of the resources we will get a violation. By setting this
	 * parameter to something < 1. we can set a "buffer" by saying: we reached @threshold % of 
	 * the resources, so rise a violation.
	 */
	private double threshold = 1.0;

	/**
	 * Returns the current SLA violations
	 * If this function is called with only 1 datacenter in @datacenterList and we have violations,
	 * an action like migrations of VMs should be done
	 * @minute The Current minute for violation-calculation related to downtime
	 * @datacenterList The list of datacenters where the violations may occur
	 * @return Returns the number of total SLA violations in all datacenters given by @datacenterList
	 * 		   If > 0, we have violations
	 */
		
	private ArrayList<Integer> priorityViolationList = new ArrayList<Integer>();

	public int getViolations() {
		return this.priorityViolationList.size();
	}
	
	public void reset() {
		this.priorityViolationList.clear();
	}
	
	public void updateSLAViolsations(int minute, ArrayList<DataCenter> datacenterList) {
		// Stores if a PM has violations
		HashMap<String, Boolean> violationMapMemory = new HashMap<String, Boolean>();
		HashMap<String, Boolean> violationMapCPUs = new HashMap<String, Boolean>();
		HashMap<String, Boolean> violationMapNetwork = new HashMap<String, Boolean>();
		
		ArrayList<ServiceLevelAgreement> slaList = new ArrayList<ServiceLevelAgreement>();
		
		// Get all slas in our system and store if pms are violated
		for (DataCenter dc : datacenterList) {
			slaList = dc.getSLAs();
			for (PhysicalMachine pm : dc.getPhysicalMachines()) {
				violationMapMemory.put(pm.getId(), Utils.PMMemoryIsViolated(pm, threshold));
				violationMapCPUs.put(pm.getId(), Utils.PMCPUsIsViolated(pm, threshold));
				violationMapNetwork.put(pm.getId(), Utils.PMBandwidthIsViolated(pm, threshold));
			}
		}

		for (ServiceLevelAgreement sla : slaList) {
			double downtime = sla.getDownTimeInPercent(minute);
			// Downtime is violated
			if (downtime > sla.getMaxDowntime()) {
				priorityViolationList.add(sla.getPriority());
				continue;
			}

			// Now iterate through the SLAs and check if they are violated
			
			
			// Memory	
			double assignedUsedMemory = 0.;
			boolean allVMsHaveFullMemoryLoad = true;
			
			ArrayList<VirtualMachine> tmpVMList = sla.getVms();
			
			for (VirtualMachine vm : tmpVMList) {
				if (vm.isOnline() == false) continue;
				assignedUsedMemory += vm.getUsedMemory() * vm.getMemory();
				allVMsHaveFullMemoryLoad = allVMsHaveFullMemoryLoad && (vm.getUsedMemory() >= 1.); // if this vm has full memory load
			}
	
			if (allVMsHaveFullMemoryLoad && assignedUsedMemory < sla.getMemory()) {
				// Memory of sla is violated because the load of all vms is 100% and the memory of the vms assigned < SLA
				priorityViolationList.add(sla.getPriority());
				continue;
			}
			else if (allVMsHaveFullMemoryLoad) {
				// All vms of this sla have full load, but the SLA is not yet violated.
				// Now check if one PM where the vms are running is overloaded, if yes, we have a violation (swapping)
				boolean isViolated = false;
				for (VirtualMachine vm : tmpVMList) {
					isViolated |= violationMapMemory.get(vm.getPm().getId());
				}
				// This SLA is violated in memory
				if (isViolated) {
					priorityViolationList.add(sla.getPriority());
					continue;
				}
			}
			else {
				// all vms running on pms which are  overloaded
				boolean allPMsOverloaded = true;
				for (VirtualMachine vm : tmpVMList) {
					allPMsOverloaded = allPMsOverloaded && violationMapMemory.get(vm.getPm().getId());
				}
				if (allPMsOverloaded) {
					priorityViolationList.add(sla.getPriority());
					continue;
				}
			}

			// CPUs
			double assignedUsedCPUs = 0.;
			boolean allVMsHaveFullCPULoad = true;
			
			for (VirtualMachine vm : tmpVMList) {
				if (vm.isOnline() == false) continue;
				assignedUsedCPUs += vm.getUsedCPUs() * vm.getCpus();
				allVMsHaveFullCPULoad = allVMsHaveFullCPULoad && (vm.getUsedCPUs() >= 1.); // if this vm has full cpu load
			}
	
			if (allVMsHaveFullCPULoad && assignedUsedCPUs < sla.getCpus()) {
				// CPUs of sla is violated because the load of all vms is 100% and the cpus of the vms assigned < SLA
				priorityViolationList.add(sla.getPriority());
				continue;
			}
			else if (allVMsHaveFullCPULoad) {
				// All vms of this sla have full load, but the SLA is not yet violated.
				// Now check if one PM where the vms are running is overloaded, if yes, we have a violation
				boolean isViolated = false;
				for (VirtualMachine vm : tmpVMList) {
					isViolated |= violationMapCPUs.get(vm.getPm().getId());
				}
				// This SLA is violated in cpus
				if (isViolated) {
					priorityViolationList.add(sla.getPriority());
					continue;
				}
			}
			else {
				// all vms running on pms which are  overloaded
				boolean allPMsOverloaded = true;
				for (VirtualMachine vm : tmpVMList) {
					allPMsOverloaded = allPMsOverloaded && violationMapCPUs.get(vm.getPm().getId());
				}
				if (allPMsOverloaded) {
					priorityViolationList.add(sla.getPriority());
					continue;
				}
			}			
			
			// Bandwidth
			double assignedUsedBandwidth = 0.;
			boolean allVMsHaveFullBandwidthLoad = true;
			
			for (VirtualMachine vm : tmpVMList) {
				if (vm.isOnline() == false) continue;
				assignedUsedBandwidth += vm.getUsedBandwidth() * vm.getBandwidth();
				allVMsHaveFullBandwidthLoad = allVMsHaveFullBandwidthLoad && (vm.getUsedBandwidth() >= 1.); // if this vm has full bandwidth load
			}
	
			if (allVMsHaveFullBandwidthLoad && assignedUsedBandwidth < sla.getBandwidth()) {
				// CPUs of sla is violated because the load of all vms is 100% and the bandwidth of the vms assigned < SLA
				priorityViolationList.add(sla.getPriority());
				continue;
			}
			else if (allVMsHaveFullBandwidthLoad) {
				// All vms of this sla have full load, but the SLA is not yet violated.
				// Now check if one PM where the vms are running is overloaded, if yes, we have a violation
				boolean isViolated = false;
				for (VirtualMachine vm : tmpVMList) {
					isViolated |= violationMapNetwork.get(vm.getPm().getId());
				}
				// This SLA is violated in networkbandwidth
				if (isViolated) {
					priorityViolationList.add(sla.getPriority());
					continue;
				}
			}
			else {
				// all vms running on pms which are  overloaded
				boolean allPMsOverloaded = true;
				for (VirtualMachine vm : tmpVMList) {
					allPMsOverloaded = allPMsOverloaded && violationMapNetwork.get(vm.getPm().getId());
				}
				if (allPMsOverloaded) {
					priorityViolationList.add(sla.getPriority());
					continue;
				}
			}			
		}
	}
}
