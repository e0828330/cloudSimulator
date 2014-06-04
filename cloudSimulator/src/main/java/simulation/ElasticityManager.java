package simulation;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import lombok.Data;
import model.DataPoint;
import model.VirtualMachine;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import utils.Utils;
import algorithms.DataCenterMigration;
import algorithms.SLAViolationAlgorithm;

@Data
@Service
public class ElasticityManager {

	static Logger logger = LoggerFactory.getLogger(ElasticityManager.class);
	
	private DataCenterMigration algorithm;
	private List<DataCenter> dataCenters = new ArrayList<DataCenter>();
	
	/* TODO: Move to config */
	private final double costsPerViolation = 10.;

	private ArrayList<DataPoint> energyCostList = new ArrayList<DataPoint>(8760);
	private ArrayList<DataPoint> slaCostList = new ArrayList<DataPoint>(8760);
	private HashMap<String, ArrayList<DataPoint>> vmCounts = new HashMap<String, ArrayList<DataPoint>>();
	
	private int hour = 0;
	
	@Autowired
	private SLAViolationAlgorithm slaViolations;

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
		slaViolations.updateSLAViolsations(minute, (ArrayList<DataCenter>) dataCenters);

		for (DataCenter dc : dataCenters) {
			dc.simulate(minute);
		}
		
		/* Run the algorithm once per hour */
		if ((minute % 60) == 0) {
			algorithm.manageVirtualMachines(this, minute);

			if (hour == 0) {
				for (DataCenter dc : dataCenters) {
					vmCounts.put(dc.getName(), new ArrayList<DataPoint>(8760));
				}
			}
			
			/* Compute energy costs */
			double energyCosts = 0.;
			for (DataCenter dc : dataCenters) {
				/* Record vm counts per dc */
				vmCounts.get(dc.getName()).add(new DataPoint(hour, dc.getVirtualMachineCount()));
				/* Compute energy costs */
				energyCosts += dc.getCurrentEnergyCosts(minute);
			}
			/* Compute SLA costs based on priority */
			double slaCosts = 0.;
			for (Integer violatedPrio : slaViolations.getPriorityViolationList()) {
				slaCosts += (double)((costsPerViolation * Math.max(1, 10 - (10 / violatedPrio))) / 10) * 0.1;
			}
			energyCostList.add(new DataPoint(hour, energyCosts));
			slaCostList.add(new DataPoint(hour, slaCosts));
			hour++;
			slaViolations.reset();
		}
	}

}
