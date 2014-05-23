package cloudSimulator;

import model.PhysicalMachine;
import model.ServiceLevelAgreement;
import model.VirtualMachine;

import org.junit.Test;

import simulation.DataCenter;
import simulation.ElasticityManager;
import cloudSimulator.algorihms.DataCenterMigrationBestFit;
import cloudSimulator.algorihms.DataCentertBestFit;

public class SLAViolationsTest {

	@Test
	public void testDownTimeViolation() {
		ElasticityManager em = new ElasticityManager();
		DataCenter dc = new DataCenter();
		PhysicalMachine pm1 = new PhysicalMachine();
		VirtualMachine vm1 = new VirtualMachine();
		VirtualMachine vm2 = new VirtualMachine();
		
		em.setAlgorithm(new DataCenterMigrationBestFit());
		dc.setAlgorithm(new DataCentertBestFit());
		
		em.getDataCenters().add(dc);
		dc.getPhysicalMachines().add(pm1);
		pm1.getVirtualMachines().add(vm1);
		pm1.getVirtualMachines().add(vm2);
		

		ServiceLevelAgreement sla = new ServiceLevelAgreement();
		System.out.println("BEFORE = " + sla.getVms().size());
		sla.getVms().add(vm1);
		sla.getVms().add(vm2);
		
		System.out.println("SIZE = " + sla.getVms().size());
		
		sla.setBandwidth(50);
		sla.setSize(40);
		sla.setCpus(4);
		sla.setMemory(4);
		sla.setMaxDowntime(101.0);
		
		pm1.setRunning(false);
		
		vm1.setSla(sla);
		vm2.setSla(sla);
		
		vm1.setBandwidth(200);
		vm1.setCpus(10);
		vm1.setMemory(10);
		vm1.setSize(50);
		
		vm2.setBandwidth(200);
		vm2.setCpus(10);
		vm2.setMemory(10);
		vm2.setSize(50);
		
		em.simulate(1);
		
		System.out.println(em.getCurrentSLAViolsations(1));
		
	}

}
