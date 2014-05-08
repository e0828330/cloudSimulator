package cloudSimulator.algorihms;

import model.PhysicalMachine;
import model.VirtualMachine;

import org.springframework.stereotype.Service;

import simulation.DataCenter;
import algorithms.DataCenterManagement;

@Service(value="managementRoundRobin")
public class DataCenterRoundRobin implements DataCenterManagement {
	
	public void scaleVirtualMachines(DataCenter dc) {
		// TODO Auto-generated method stub
	}

	public PhysicalMachine findPMForMigration(DataCenter dc, VirtualMachine vm) {
		// TODO Auto-generated method stub
		return null;
	}

}
