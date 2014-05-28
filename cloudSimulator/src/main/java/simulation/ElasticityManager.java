package simulation;

import java.util.ArrayList;
import java.util.List;

import lombok.Data;
import model.DataPoint;
import model.ServiceLevelAgreement;
import model.VirtualMachine;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import utils.Utils;
import algorithms.DataCenterMigration;

@Data
@Service
public class ElasticityManager {

	static Logger logger = LoggerFactory.getLogger(ElasticityManager.class);
	
	private DataCenterMigration algorithm;
	private List<DataCenter> dataCenters = new ArrayList<DataCenter>();
	
	/* TODO: Move to config */
	private final int costsPerViolation = 10;

	private ArrayList<DataPoint> energyCostList = new ArrayList<DataPoint>(8760);
	private ArrayList<DataPoint> slaCostList = new ArrayList<DataPoint>(8760);
	
	private int hour = 0;

	/**
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
		for (DataCenter dc : dataCenters) {
			dc.simulate(minute);
		}

		/* Run the algorithm once per hour */
		if ((minute % 60) == 0) {
			algorithm.manageVirtualMachines(this, minute);
			double energyCosts = 0.;
			for (DataCenter dc : dataCenters) {
				energyCosts += dc.getCurrentEnergyCosts(minute);
			}
			double slaCosts = getCurrentSLAViolsations(minute) * costsPerViolation;
			energyCostList.add(new DataPoint(hour, energyCosts));
			slaCostList.add(new DataPoint(hour, slaCosts));
			hour++;
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
		
		logger.trace("SLAList = " + slaList.size());
		
		for (ServiceLevelAgreement sla : slaList) {
			int cpus = 0;
			int bandwidth = 0;
			int memory = 0;
			int size = 0;
			double downtime = sla.getDownTimeInPercent(minute);
			
			logger.trace("VMS SIZE = " + sla.getVms().size());
			
			// Get all VMs for each SLA
			for (VirtualMachine vm : sla.getVms()) {
				if (vm.isOnline()) {
					cpus += vm.getCpus();
					bandwidth += vm.getBandwidth();
					memory += vm.getMemory();
					size += vm.getSize();
				}
			}

			logger.trace(downtime + " > " + sla.getMaxDowntime());
			logger.trace(cpus + " < " + sla.getCpus());
			logger.trace(bandwidth + " < " + sla.getBandwidth());
			logger.trace(memory + " < " + sla.getMemory());
			logger.trace(size + " < " + sla.getSize());
			
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
