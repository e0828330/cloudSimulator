package cloudSimulator;

import static org.junit.Assert.assertEquals;
import model.PhysicalMachine;
import model.ServiceLevelAgreement;
import model.VirtualMachine;

import org.junit.Before;
import org.junit.Test;

import simulation.DataCenter;
import simulation.ElasticityManager;
import algorithms.DataCenterManagement;
import algorithms.DataCenterMigration;

public class SLAViolationsTest {
/*
	private static ElasticityManager em;
	private static DataCenter dc;
	private static PhysicalMachine pm1;
	private static VirtualMachine vm1;
	private static VirtualMachine vm2;
	private static ServiceLevelAgreement sla1;
	private static ServiceLevelAgreement sla2;
	
	@Before
	public void initialize() {
		em = new ElasticityManager();
		dc = new DataCenter();
		pm1 = new PhysicalMachine();
		vm1 = new VirtualMachine();
		vm2 = new VirtualMachine();
		
		em.setAlgorithm(new DataCenterMigration() {
			
			public void manageVirtualMachines(ElasticityManager em, int minute) {
				// TODO Auto-generated method stub
				
			}
		});
		dc.setAlgorithm(new DataCenterManagement() {
			
			public void scaleVirtualMachines(DataCenter dc) {
				// TODO Auto-generated method stub
				
			}
			
			public PhysicalMachine findPMForMigration(DataCenter dc, VirtualMachine vm) {
				// TODO Auto-generated method stub
				return null;
			}
		});
		
		em.getDataCenters().add(dc);
		dc.getPhysicalMachines().add(pm1);
		pm1.getVirtualMachines().add(vm1);
		pm1.getVirtualMachines().add(vm2);
		
		pm1.setBandwidth(1000);
		pm1.setCpus(100);
		pm1.setMemory(100);
		pm1.setSize(1000);

		sla1 = new ServiceLevelAgreement();
		sla1.getVms().add(vm1);
		sla1.getVms().add(vm2);
		
		sla1.setBandwidth(50);
		sla1.setSize(40);
		sla1.setCpus(4);
		sla1.setMemory(4);
		sla1.setMaxDowntime(0.1);
		
		sla2 = new ServiceLevelAgreement();
		sla2.getVms().add(vm1);
		
		sla2.setBandwidth(10);
		sla2.setSize(10);
		sla2.setCpus(1);
		sla2.setMemory(10);
		sla2.setMaxDowntime(0.01);		
		
		vm1.setSla(sla1);
		vm2.setSla(sla2);
		
		vm1.setBandwidth(200);
		vm1.setCpus(10);
		vm1.setMemory(10);
		vm1.setSize(50);
		
		vm2.setBandwidth(200);
		vm2.setCpus(10);
		vm2.setMemory(10);
		vm2.setSize(50);		
	}
	
	@Test
	public void testDownTimeViolation() {
		pm1.setRunning(false);
		em.simulate(1);
		assertEquals(2, em.getCurrentSLAViolsations(1));
	}
	
	@Test
	public void testDownTimeViolation2() {
		vm1.setOnline(true);
		vm2.setOnline(true);
		em.simulate(1);
		assertEquals(0, em.getCurrentSLAViolsations(1));
		
		vm1.setOnline(false);
		em.simulate(2);
		assertEquals(1, em.getCurrentSLAViolsations(2));
		
		vm2.setOnline(false);
		em.simulate(3);
		assertEquals(2, em.getCurrentSLAViolsations(3));
	}	
	
	@Test
	public void testDownTimeViolation3() {
		vm1.setOnline(true);
		pm1.getVirtualMachines().remove(vm2);
		sla1.getVms().remove(vm2);
		
		em.simulate(1);
		assertEquals(0, em.getCurrentSLAViolsations(1));
		
		vm1.setOnline(false);
		em.simulate(2);
		assertEquals(1, em.getCurrentSLAViolsations(2));
	}
	
	@Test
	public void testDownTimeViolation4() {
		vm1.setOnline(true);
		// Remove 2nd vm
		pm1.getVirtualMachines().remove(vm2);
		sla1.getVms().remove(vm2);
		
		em.simulate(1);
		assertEquals(0, em.getCurrentSLAViolsations(1));
		
		em.simulate(2);
		assertEquals(0, em.getCurrentSLAViolsations(2));
		em.simulate(3);
		em.simulate(4);
		em.simulate(5);
		em.simulate(6);
		em.simulate(7);
		em.simulate(8);
		em.simulate(9);
		vm1.setOnline(false);
		em.simulate(10);
		
		// 10 minutes, 1min offline = 10%
		sla1.setMaxDowntime(0.1);
		assertEquals(1, em.getCurrentSLAViolsations(10));
		assertEquals(0.1, sla1.getDownTimeInPercent(10), 0.0);
	}
	
	@Test
	public void testDownTimeViolation5_okay() {
		vm1.setOnline(true);
		// Remove 2nd vm
		pm1.getVirtualMachines().remove(vm2);
		sla1.getVms().remove(vm2);
		
		em.simulate(1);
		assertEquals(0, em.getCurrentSLAViolsations(1));
		em.simulate(2);
		assertEquals(0, em.getCurrentSLAViolsations(2));
		em.simulate(3);
		em.simulate(4);
		vm1.setOnline(false);
		em.simulate(5);
		vm1.setOnline(true);
		em.simulate(6);
		em.simulate(7);
		em.simulate(8);
		em.simulate(9);
		em.simulate(10);

		// 10 minutes, 1min offline = 10%, but downtime is 5%
		sla1.setMaxDowntime(0.05);
		assertEquals(1, em.getCurrentSLAViolsations(10));
		assertEquals(0.1, sla1.getDownTimeInPercent(10), 0.0);
	}		
	
	@Test
	public void testCPUViolation() {
		vm1.setOnline(true);
		vm2.setOnline(true);
		em.simulate(1);
		assertEquals(0, em.getCurrentSLAViolsations(1));
		
		sla1.setCpus(vm1.getCpus() + vm2.getCpus());
		em.simulate(2);
		assertEquals(0, em.getCurrentSLAViolsations(2));
		
		sla1.setCpus(vm1.getCpus() + vm2.getCpus() + 1); // more cpus than available
		em.simulate(2);
		assertEquals(1, em.getCurrentSLAViolsations(2));
	}
	
	@Test
	public void testMemoryViolation() {
		vm1.setOnline(true);
		vm2.setOnline(true);
		em.simulate(1);
		assertEquals(0, em.getCurrentSLAViolsations(1));
		
		sla1.setMemory(vm1.getMemory() + vm2.getMemory());
		em.simulate(2);
		assertEquals(0, em.getCurrentSLAViolsations(2));
		
		sla1.setMemory(vm1.getMemory() + vm2.getMemory() + 1);
		em.simulate(2);
		assertEquals(1, em.getCurrentSLAViolsations(2));
	}
	
	@Test
	public void testMemoryViolation2() {
		vm1.setOnline(true);
		vm2.setOnline(true);
		em.simulate(1);
		assertEquals(0, em.getCurrentSLAViolsations(1));
		
		sla1.setMemory(vm1.getMemory() + vm2.getMemory() + 1);
		em.simulate(2);
		assertEquals(1, em.getCurrentSLAViolsations(2));
		
		sla2.setMemory(vm1.getMemory() + vm2.getMemory() + 1);
		em.simulate(2);
		assertEquals(2, em.getCurrentSLAViolsations(2));
	}
	
	@Test
	public void testSizeViolation() {
		vm1.setOnline(true);
		vm2.setOnline(true);
		em.simulate(1);
		assertEquals(0, em.getCurrentSLAViolsations(1));
		
		sla1.setSize(vm1.getSize() + vm2.getSize() + 1);
		em.simulate(2);
		assertEquals(1, em.getCurrentSLAViolsations(2));
		
		sla2.setSize(vm1.getSize() + vm2.getSize() + 1);
		em.simulate(2);
		assertEquals(2, em.getCurrentSLAViolsations(2));
	}
	
	@Test
	public void testBandwidthViolation() {
		vm1.setOnline(true);
		vm2.setOnline(true);
		em.simulate(1);
		assertEquals(0, em.getCurrentSLAViolsations(1));
		
		sla1.setBandwidth(vm1.getBandwidth() + vm2.getBandwidth() + 1);
		em.simulate(2);
		assertEquals(1, em.getCurrentSLAViolsations(2));
		
		sla2.setBandwidth(vm1.getBandwidth() + vm2.getBandwidth() + 1);
		em.simulate(2);
		assertEquals(2, em.getCurrentSLAViolsations(2));
	}	
*/
}
