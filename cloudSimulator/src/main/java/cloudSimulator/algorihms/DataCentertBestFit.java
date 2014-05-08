package cloudSimulator.algorihms;

import model.PhysicalMachine;
import model.VirtualMachine;

import org.springframework.stereotype.Service;

import simulation.DataCenter;
import utils.Utils;
import algorithms.DataCenterManagement;

@Service(value="managementBestFit")
public class DataCentertBestFit implements DataCenterManagement {
	
	public void scaleVirtualMachines(DataCenter dc) {
		// TODO Auto-generated method stub
	}

	/**
	 * Finds the PM where the VM fits and has the lowest power consumption
	 * 
	 */
	public PhysicalMachine findPMForMigration(DataCenter dc, VirtualMachine vm) {
		PhysicalMachine result = null;

		for(PhysicalMachine pm : dc.getPhysicalMachines()) {
			if (result == null && Utils.VMfitsOnPM(pm, vm)) {
				result = pm;
				continue;
			}
			if (Utils.VMfitsOnPM(pm, vm) && pm.getPowerConsumption() < result.getPowerConsumption()) {
				result = pm;
			}
		}

		if (result == null) {
			// TODO: Shut down one VM with lower prio
		}
		
		return result;
	}

}
