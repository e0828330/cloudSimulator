package cloudSimulator;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.util.ArrayList;
import java.util.concurrent.ConcurrentHashMap;

import model.PhysicalMachine;
import model.ServiceLevelAgreement;
import model.VirtualMachine;

import org.junit.After;
import org.junit.BeforeClass;
import org.junit.Test;

import simulation.DataCenter;

public class LowerPriorityAlgorithm {
	
	private static DataCenter dc;
	private static PhysicalMachine pm;

	@BeforeClass
	public static void initialize() {
		dc = new DataCenter();
		pm = new PhysicalMachine();
		pm.setBandwidth(100);
		pm.setCpus(8);
		pm.setMemory(8);
		pm.setSize(100);
		pm.setIdleStateEnergyUtilization(15.0);
		pm.setCpuPowerConsumption(10);
		pm.setMemPowerConsumption(10);
		pm.setNetworkPowerConsumption(10);
		pm.setRunning(true);
		dc.setPhysicalMachines(new ArrayList<PhysicalMachine>());
		dc.getPhysicalMachines().add(pm);
	}
	
	@After
	public void afterClass() {
    	pm.getVirtualMachines().clear();
    	dc.getPhysicalMachines().clear();
    	dc.getPhysicalMachines().add(pm);
    }
	
	
	@Test
	public void test_enoughResourcesShouldReturnNoVMs() {
		ServiceLevelAgreement sla = new ServiceLevelAgreement();
		sla.setBandwith(50);
		sla.setCpus(4);
		sla.setMaxDowntime(1.0);
		sla.setMemory(4);
		sla.setPriority(1);
		sla.setSize(30);
		
		ServiceLevelAgreement sla2 = new ServiceLevelAgreement();
		sla2.setBandwith(50);
		sla2.setCpus(4);
		sla2.setMaxDowntime(1.0);
		sla2.setMemory(4);
		sla2.setPriority(2);
		sla2.setSize(30);		

		VirtualMachine vm1 = new VirtualMachine();
		vm1.setSla(sla);
		vm1.setBandwidth(50);
		vm1.setCpus(4);
		vm1.setMemory(4);
		vm1.setOnline(true);
		vm1.setSize(20);
		vm1.setUsedBandwidth(1.);
		vm1.setUsedCPUs(1.);
		vm1.setUsedMemory(1.);
		
		VirtualMachine vm2 = new VirtualMachine();
		vm2.setSla(sla2);
		vm2.setBandwidth(50);
		vm2.setCpus(4);
		vm2.setMemory(4);
		vm2.setOnline(true);
		vm2.setSize(20);
		vm2.setUsedBandwidth(1.);
		vm2.setUsedCPUs(1.);
		vm2.setUsedMemory(1.);
		
		
		pm.getVirtualMachines().add(vm1);
		ConcurrentHashMap<PhysicalMachine, ArrayList<VirtualMachine>> result = dc.getPMWithLowerPriorityVMList(dc, vm2);
		
		assertEquals(result.size(), 1);
		assertTrue(result.containsKey(pm));
		assertEquals(result.get(pm).size(), 0);
	}
	

	@Test
	public void test_notEnoughResourcesShouldReturnVM1() {
		ServiceLevelAgreement sla = new ServiceLevelAgreement();
		sla.setBandwith(50);
		sla.setCpus(4);
		sla.setMaxDowntime(1.0);
		sla.setMemory(4);
		sla.setPriority(1);
		sla.setSize(30);
		
		ServiceLevelAgreement sla2 = new ServiceLevelAgreement();
		sla2.setBandwith(50);
		sla2.setCpus(4);
		sla2.setMaxDowntime(1.0);
		sla2.setMemory(4);
		sla2.setPriority(2);
		sla2.setSize(30);		

		VirtualMachine vm1 = new VirtualMachine();
		vm1.setSla(sla);
		vm1.setBandwidth(50);
		vm1.setCpus(5);
		vm1.setMemory(4);
		vm1.setOnline(true);
		vm1.setSize(20);
		vm1.setUsedBandwidth(1.);
		vm1.setUsedCPUs(1.);
		vm1.setUsedMemory(1.);
		
		VirtualMachine vm2 = new VirtualMachine();
		vm2.setSla(sla2);
		vm2.setBandwidth(50);
		vm2.setCpus(4);
		vm2.setMemory(4);
		vm2.setOnline(true);
		vm2.setSize(20);
		vm2.setUsedBandwidth(1.);
		vm2.setUsedCPUs(1.);
		vm2.setUsedMemory(1.);
		
		
		pm.getVirtualMachines().add(vm1);
		ConcurrentHashMap<PhysicalMachine, ArrayList<VirtualMachine>> result = dc.getPMWithLowerPriorityVMList(dc, vm2);
		
		assertEquals(result.size(), 1);
		assertTrue(result.containsKey(pm));
		assertEquals(result.get(pm).size(), 1);
		assertEquals(result.get(pm).get(0), vm1);
	}	
	
	@Test
	public void test_notEnoughResourcesShouldReturnVM2() {
		ServiceLevelAgreement sla = new ServiceLevelAgreement();
		sla.setBandwith(50);
		sla.setCpus(4);
		sla.setMaxDowntime(1.0);
		sla.setMemory(4);
		sla.setPriority(3);
		sla.setSize(30);
		
		ServiceLevelAgreement sla2 = new ServiceLevelAgreement();
		sla2.setBandwith(50);
		sla2.setCpus(4);
		sla2.setMaxDowntime(1.0);
		sla2.setMemory(4);
		sla2.setPriority(2);
		sla2.setSize(30);		

		VirtualMachine vm1 = new VirtualMachine();
		vm1.setSla(sla);
		vm1.setBandwidth(50);
		vm1.setCpus(5);
		vm1.setMemory(4);
		vm1.setOnline(true);
		vm1.setSize(20);
		vm1.setUsedBandwidth(1.);
		vm1.setUsedCPUs(1.);
		vm1.setUsedMemory(1.);
		
		VirtualMachine vm2 = new VirtualMachine();
		vm2.setSla(sla2);
		vm2.setBandwidth(50);
		vm2.setCpus(4);
		vm2.setMemory(4);
		vm2.setOnline(true);
		vm2.setSize(20);
		vm2.setUsedBandwidth(1.);
		vm2.setUsedCPUs(1.);
		vm2.setUsedMemory(1.);
		
		
		pm.getVirtualMachines().add(vm2);
		ConcurrentHashMap<PhysicalMachine, ArrayList<VirtualMachine>> result = dc.getPMWithLowerPriorityVMList(dc, vm1);
		
		assertEquals(result.size(), 1);
		assertTrue(result.containsKey(pm));
		assertEquals(result.get(pm).size(), 1);
		assertEquals(result.get(pm).get(0), vm2);
	}	
	
	@Test
	public void test_samePriority_notEnoughResourcesShouldReturnNull() {
		ServiceLevelAgreement sla = new ServiceLevelAgreement();
		sla.setBandwith(50);
		sla.setCpus(4);
		sla.setMaxDowntime(1.0);
		sla.setMemory(4);
		sla.setPriority(1);
		sla.setSize(30);
		
		ServiceLevelAgreement sla2 = new ServiceLevelAgreement();
		sla2.setBandwith(50);
		sla2.setCpus(4);
		sla2.setMaxDowntime(1.0);
		sla2.setMemory(4);
		sla2.setPriority(1);
		sla2.setSize(30);		

		VirtualMachine vm1 = new VirtualMachine();
		vm1.setSla(sla);
		vm1.setBandwidth(50);
		vm1.setCpus(5);
		vm1.setMemory(4);
		vm1.setOnline(true);
		vm1.setSize(20);
		vm1.setUsedBandwidth(1.);
		vm1.setUsedCPUs(1.);
		vm1.setUsedMemory(1.);
		
		VirtualMachine vm2 = new VirtualMachine();
		vm2.setSla(sla2);
		vm2.setBandwidth(50);
		vm2.setCpus(4);
		vm2.setMemory(4);
		vm2.setOnline(true);
		vm2.setSize(20);
		vm2.setUsedBandwidth(1.);
		vm2.setUsedCPUs(1.);
		vm2.setUsedMemory(1.);
		
		
		pm.getVirtualMachines().add(vm1);
		ConcurrentHashMap<PhysicalMachine, ArrayList<VirtualMachine>> result = dc.getPMWithLowerPriorityVMList(dc, vm2);
		
		assertNull(result);
	}
	
	@Test
	public void test_notEnoughResourcesShouldReturnCorrectPMWithLowerConsumption() {
		ServiceLevelAgreement sla = new ServiceLevelAgreement();
		sla.setBandwith(50);
		sla.setCpus(4);
		sla.setMaxDowntime(1.0);
		sla.setMemory(4);
		sla.setPriority(1);
		sla.setSize(30);
		
		ServiceLevelAgreement sla2 = new ServiceLevelAgreement();
		sla2.setBandwith(50);
		sla2.setCpus(4);
		sla2.setMaxDowntime(1.0);
		sla2.setMemory(5);
		sla2.setPriority(2);
		sla2.setSize(30);
		
		ServiceLevelAgreement sla3 = new ServiceLevelAgreement();
		sla3.setBandwith(50);
		sla3.setCpus(4);
		sla3.setMaxDowntime(1.0);
		sla3.setMemory(5);
		sla3.setPriority(3);
		sla3.setSize(30);		

		VirtualMachine vm1 = new VirtualMachine();
		vm1.setSla(sla);
		vm1.setBandwidth(50);
		vm1.setCpus(4);
		vm1.setMemory(4);
		vm1.setOnline(true);
		vm1.setSize(20);
		vm1.setUsedBandwidth(1.);
		vm1.setUsedCPUs(1.);
		vm1.setUsedMemory(1.);
		
		VirtualMachine vm2 = new VirtualMachine();
		vm2.setSla(sla2);
		vm2.setBandwidth(50);
		vm2.setCpus(4);
		vm2.setMemory(4);
		vm2.setOnline(true);
		vm2.setSize(20);
		vm2.setUsedBandwidth(1.);
		vm2.setUsedCPUs(1.);
		vm2.setUsedMemory(1.);
		
		PhysicalMachine pm2 = new PhysicalMachine();
		pm2.setVirtualMachines(new ArrayList<VirtualMachine>());
		pm2.setBandwidth(100);
		pm2.setCpus(8);
		pm2.setMemory(8);
		pm2.setSize(100);
		pm2.setIdleStateEnergyUtilization(10.0); // PM2 has lower consumption in idle state
		pm2.setCpuPowerConsumption(10);
		pm2.setMemPowerConsumption(10);
		pm2.setNetworkPowerConsumption(10);
		pm2.setRunning(true);
		pm2.getVirtualMachines().add(vm1);
		pm2.getVirtualMachines().add(vm2);
		
		
		
		pm.getVirtualMachines().add(vm1);
		pm.getVirtualMachines().add(vm2);
		dc.getPhysicalMachines().add(pm2);
		
		
		VirtualMachine vm3 = new VirtualMachine();
		vm3.setSla(sla3);
		vm3.setBandwidth(50);
		vm3.setCpus(4);
		vm3.setMemory(4);
		vm3.setOnline(true);
		vm3.setSize(20);
		vm3.setUsedBandwidth(1.);
		vm3.setUsedCPUs(1.);
		vm3.setUsedMemory(1.);		
		
		ConcurrentHashMap<PhysicalMachine, ArrayList<VirtualMachine>> result = dc.getPMWithLowerPriorityVMList(dc, vm3);
		assertEquals(result.size(), 1);
		assertTrue(result.containsKey(pm2));
		assertEquals(result.get(pm2).size(), 1);
		assertEquals(result.get(pm2).get(0), vm1);
	}
	
	
	@Test
	public void test_notEnoughResourcesShouldReturnCorrectPMWithLowerConsumptionAndVMs() {
		ServiceLevelAgreement sla = new ServiceLevelAgreement();
		sla.setBandwith(50);
		sla.setCpus(4);
		sla.setMaxDowntime(1.0);
		sla.setMemory(4);
		sla.setPriority(1);
		sla.setSize(30);
		
		ServiceLevelAgreement sla2 = new ServiceLevelAgreement();
		sla2.setBandwith(50);
		sla2.setCpus(4);
		sla2.setMaxDowntime(1.0);
		sla2.setMemory(5);
		sla2.setPriority(2);
		sla2.setSize(30);
		
		ServiceLevelAgreement sla3 = new ServiceLevelAgreement();
		sla3.setBandwith(50);
		sla3.setCpus(4);
		sla3.setMaxDowntime(1.0);
		sla3.setMemory(5);
		sla3.setPriority(3);
		sla3.setSize(30);		

		VirtualMachine vm1 = new VirtualMachine();
		vm1.setSla(sla);
		vm1.setBandwidth(50);
		vm1.setCpus(4);
		vm1.setMemory(4);
		vm1.setOnline(true);
		vm1.setSize(20);
		vm1.setUsedBandwidth(1.);
		vm1.setUsedCPUs(1.);
		vm1.setUsedMemory(1.);
		
		VirtualMachine vm2 = new VirtualMachine();
		vm2.setSla(sla2);
		vm2.setBandwidth(50);
		vm2.setCpus(4);
		vm2.setMemory(4);
		vm2.setOnline(true);
		vm2.setSize(20);
		vm2.setUsedBandwidth(1.);
		vm2.setUsedCPUs(1.);
		vm2.setUsedMemory(1.);
		
		PhysicalMachine pm2 = new PhysicalMachine();
		pm2.setVirtualMachines(new ArrayList<VirtualMachine>());
		pm2.setBandwidth(100);
		pm2.setCpus(8);
		pm2.setMemory(8);
		pm2.setSize(100);
		pm2.setIdleStateEnergyUtilization(10.0); // PM2 has lower consumption in idle state
		pm2.setCpuPowerConsumption(10);
		pm2.setMemPowerConsumption(10);
		pm2.setNetworkPowerConsumption(10);
		pm2.setRunning(true);
		pm2.getVirtualMachines().add(vm1);
		pm2.getVirtualMachines().add(vm2);
		
		
		
		pm.getVirtualMachines().add(vm1);
		pm.getVirtualMachines().add(vm2);
		dc.getPhysicalMachines().add(pm2);
		
		
		VirtualMachine vm3 = new VirtualMachine();
		vm3.setSla(sla3);
		vm3.setBandwidth(100);
		vm3.setCpus(8);
		vm3.setMemory(8);
		vm3.setOnline(true);
		vm3.setSize(20);
		vm3.setUsedBandwidth(1.);
		vm3.setUsedCPUs(1.);
		vm3.setUsedMemory(1.);		
		
		ConcurrentHashMap<PhysicalMachine, ArrayList<VirtualMachine>> result = dc.getPMWithLowerPriorityVMList(dc, vm3);
		assertEquals(result.size(), 1);
		assertTrue(result.containsKey(pm2));
		assertEquals(result.get(pm2).size(), 2);
		assertTrue(result.get(pm2).contains(vm1));
		assertTrue(result.get(pm2).contains(vm2));
	}		
	
}
