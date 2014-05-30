package cloudSimulator;

import static org.junit.Assert.assertEquals;

import java.util.ArrayList;

import model.PhysicalMachine;
import model.ServiceLevelAgreement;
import model.VirtualMachine;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.support.AnnotationConfigContextLoader;

import simulation.DataCenter;
import simulation.ElasticityManager;
import algorithms.DataCenterManagement;
import algorithms.DataCenterMigration;
import algorithms.SLAViolationAlgorithm;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(loader=AnnotationConfigContextLoader.class, classes={Simulator.class})
public class SLAViolationsTest {

	@Autowired
	private ElasticityManager em;
	private DataCenter dc;
	private PhysicalMachine pm1;
	private VirtualMachine vm1;
	private VirtualMachine vm2;
	private ServiceLevelAgreement sla1;
	private ServiceLevelAgreement sla2;
	private SLAViolationAlgorithm algorithm;
	private ArrayList<DataCenter> tmp;
	
	@Before
	public void initialize() {
		dc = new DataCenter();
		pm1 = new PhysicalMachine();
		vm1 = new VirtualMachine();
		vm2 = new VirtualMachine();
		algorithm = new SLAViolationAlgorithm();
		algorithm.setThreshold(1.);
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
		
		tmp = new ArrayList<DataCenter>();
		tmp.add(dc);
		
		em.getDataCenters().add(dc);
		dc.getPhysicalMachines().add(pm1);
		pm1.getVirtualMachines().add(vm1);
		pm1.getVirtualMachines().add(vm2);
		
		pm1.setId("1");
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
		
		vm1.setPm(pm1);
		vm2.setPm(pm1);
	}

	
	@Test
	public void testDownTimeViolation() {
		pm1.setRunning(false);
		em.simulate(1);
		algorithm.reset();
		algorithm.updateSLAViolsations(1, tmp);
		assertEquals(2, algorithm.getViolations());
	}
	
	@Test
	public void testDownTimeViolation2() {
		vm1.setOnline(true);
		vm2.setOnline(true);
		em.simulate(1);
		algorithm.reset();
		algorithm.updateSLAViolsations(1, tmp);
		assertEquals(0, algorithm.getViolations());
		
		vm1.setOnline(false);
		em.simulate(2);
		algorithm.reset();
		algorithm.updateSLAViolsations(2, tmp);
		assertEquals(1, algorithm.getViolations());
		
		vm2.setOnline(false);
		em.simulate(3);
		
		algorithm.reset();
		algorithm.updateSLAViolsations(3, tmp);
		assertEquals(2, algorithm.getViolations());
	}	
	
	@Test
	public void testDownTimeViolation3() {
		vm1.setOnline(true);
		pm1.getVirtualMachines().remove(vm2);
		sla1.getVms().remove(vm2);
		em.simulate(1);
		algorithm.reset();
		algorithm.updateSLAViolsations(1, tmp);
		assertEquals(0, algorithm.getViolations());
		
		vm1.setOnline(false);
		em.simulate(2);
		algorithm.reset();
		algorithm.updateSLAViolsations(2, tmp);
		assertEquals(1, algorithm.getViolations());
	}
	
	
	@Test
	public void testDownTimeViolation4() {
		vm1.setOnline(true);
		// Remove 2nd vm
		pm1.getVirtualMachines().remove(vm2);
		sla1.getVms().remove(vm2);
		
		// 10 minutes, 1min offline = 10%
		sla1.setMaxDowntime(0.1);
		
		em.simulate(1);
		algorithm.reset();
		algorithm.updateSLAViolsations(1, tmp);
		assertEquals(0, algorithm.getViolations());
		
		em.simulate(2);
		algorithm.reset();
		algorithm.updateSLAViolsations(2, tmp);
		assertEquals(0, algorithm.getViolations());
		em.simulate(3);
		em.simulate(4);
		em.simulate(5);
		em.simulate(6);
		em.simulate(7);
		em.simulate(8);
		em.simulate(9);
		vm1.setOnline(false);
		em.simulate(10);
		em.simulate(11);
		em.simulate(12);
		
		algorithm.reset();
		algorithm.updateSLAViolsations(12, tmp);
		assertEquals(1, algorithm.getViolations());
		
		assertEquals(0.25, sla1.getDownTimeInPercent(12), 0.0);
	}
	
	@Test
	public void testDownTimeViolation5_okay() {
		vm1.setOnline(true);
		// Remove 2nd vm
		pm1.getVirtualMachines().remove(vm2);
		sla1.getVms().remove(vm2);
		
		em.simulate(1);
		algorithm.reset();
		algorithm.updateSLAViolsations(1, tmp);
		assertEquals(0, algorithm.getViolations());
		em.simulate(2);
		algorithm.reset();
		algorithm.updateSLAViolsations(2, tmp);
		assertEquals(0, algorithm.getViolations());
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
		sla1.setMaxDowntime(0.15);
		algorithm.reset();
		algorithm.updateSLAViolsations(10, tmp);
		assertEquals(0, algorithm.getViolations());
		assertEquals(0.1, sla1.getDownTimeInPercent(10), 0.0);
	}		
	
	@Test
	public void testCPUViolation() {
		vm1.setOnline(true);
		vm2.setOnline(true);
		vm1.setUsedCPUs(1.);
		vm2.setUsedCPUs(1.);

		em.simulate(1);
		algorithm.reset();
		algorithm.updateSLAViolsations(1, tmp);
		assertEquals(0, algorithm.getViolations());
		
		sla1.setCpus(vm1.getCpus() + vm2.getCpus());
		em.simulate(2);
		algorithm.reset();
		algorithm.updateSLAViolsations(2, tmp);
		assertEquals(0, algorithm.getViolations());
		

		
		sla1.setCpus(vm1.getCpus() + vm2.getCpus() + 1); // more cpus than available
		em.simulate(3);
		algorithm.reset();
		algorithm.updateSLAViolsations(3, tmp);
		assertEquals(1, algorithm.getViolations());
	}
	
	
	
	@Test
	public void testMemoryViolation() {
		vm1.setOnline(true);
		vm2.setOnline(true);
		vm1.setUsedMemory(1.);
		vm2.setUsedMemory(1.);
		em.simulate(1);
		algorithm.reset();
		algorithm.updateSLAViolsations(1, tmp);
		assertEquals(0, algorithm.getViolations());
		
		sla1.setMemory(vm1.getMemory() + vm2.getMemory());
		em.simulate(2);
		algorithm.reset();
		algorithm.updateSLAViolsations(2, tmp);
		assertEquals(0, algorithm.getViolations());
		
		sla1.setMemory(vm1.getMemory() + vm2.getMemory() + 1);
		em.simulate(3);
		algorithm.reset();
		algorithm.updateSLAViolsations(3, tmp);
		assertEquals(1, algorithm.getViolations());
	}
	
	@Test
	public void testMemoryViolation2() {
		vm1.setOnline(true);
		vm2.setOnline(true);
		vm1.setUsedMemory(1.);
		vm2.setUsedMemory(1.);
		//em.simulate(1);
		algorithm.reset();
		algorithm.updateSLAViolsations(1, tmp);
		assertEquals(0, algorithm.getViolations());
		
		sla1.setMemory(vm1.getMemory() + vm2.getMemory() + 1);
		em.simulate(2);
		algorithm.reset();
		algorithm.updateSLAViolsations(2, tmp);
		assertEquals(1, algorithm.getViolations());
		
		sla2.setMemory(vm1.getMemory() + vm2.getMemory() + 1);
		em.simulate(2);
		algorithm.reset();
		algorithm.updateSLAViolsations(3, tmp);
		assertEquals(2, algorithm.getViolations());
	}

	@Test
	public void testBandwidthViolation() {
		vm1.setOnline(true);
		vm2.setOnline(true);
		vm1.setUsedBandwidth(1.);
		vm2.setUsedBandwidth(1.);
		em.simulate(1);
		algorithm.updateSLAViolsations(1, tmp);
		algorithm.reset();
		assertEquals(0, algorithm.getViolations());
		
		sla1.setBandwidth(vm1.getBandwidth() + vm2.getBandwidth() + 1);
		em.simulate(2);
		algorithm.reset();
		algorithm.updateSLAViolsations(2, tmp);
		assertEquals(1, algorithm.getViolations());
		
		sla2.setBandwidth(vm1.getBandwidth() + vm2.getBandwidth() + 1);
		em.simulate(2);
		algorithm.reset();
		algorithm.updateSLAViolsations(3, tmp);
		assertEquals(2, algorithm.getViolations());
	}	
	
	@Test
	public void testViolationPMOverloadedCPUs() {
		vm1.setOnline(true);
		vm2.setOnline(true);
		int diff = 10;
		vm1.setUsedCPUs(1.);
		vm2.setUsedCPUs(1.);
		vm1.setCpus(pm1.getCpus() - diff);
		vm2.setCpus(diff + 1);
		em.simulate(1);
		algorithm.reset();
		algorithm.updateSLAViolsations(1, tmp);
		assertEquals(2, algorithm.getViolations());
	}
	
	@Test
	public void testViolationWithBuffer() {
		vm1.setOnline(true);
		vm2.setOnline(true);
		vm1.setUsedCPUs(1.);
		vm2.setUsedCPUs(1.);
		int diff = 10;
		vm1.setCpus(pm1.getCpus() - diff);
		vm2.setCpus(diff);
		
		em.simulate(1);
		algorithm.reset();
		algorithm.updateSLAViolsations(1, tmp);
		assertEquals(0, algorithm.getViolations());
		algorithm.setThreshold(0.9);
		
		algorithm.reset();
		algorithm.updateSLAViolsations(1, tmp);
		assertEquals(2, algorithm.getViolations());
		
		vm1.setCpus((int) (pm1.getCpus() * 0.9 - diff));
		vm2.setCpus(diff);
		
		algorithm.reset();
		algorithm.updateSLAViolsations(1, tmp);
		assertEquals(0, algorithm.getViolations());
		
		algorithm.setThreshold(0.89);
		algorithm.reset();
		algorithm.updateSLAViolsations(1, tmp);
		assertEquals(2, algorithm.getViolations());
	}
	
}
