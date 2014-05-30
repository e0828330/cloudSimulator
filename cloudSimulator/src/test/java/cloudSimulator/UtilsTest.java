package cloudSimulator;

import static org.junit.Assert.assertEquals;

import java.util.ArrayList;

import model.ServiceLevelAgreement;
import model.VirtualMachine;

import org.junit.Test;

import utils.Utils;





public class UtilsTest {

	@Test
	public void testUtilsOrder() {
		ArrayList<VirtualMachine> list = new ArrayList<VirtualMachine>();
		ServiceLevelAgreement sla1 = new ServiceLevelAgreement();
		sla1.setPriority(1);
		ServiceLevelAgreement sla2 = new ServiceLevelAgreement();
		sla2.setPriority(2);
		
		VirtualMachine vm1 = new VirtualMachine();
		vm1.setSla(sla1);
		
		VirtualMachine vm2 = new VirtualMachine();
		vm2.setSla(sla2);
		
		list.add(vm1);
		list.add(vm2);
		
		Utils.orderVMsByPriorityAscending(list);
		
		assertEquals(vm1, list.get(0));
		assertEquals(vm2, list.get(1));
		
		Utils.orderVMsByPriorityDescending(list);
		
		assertEquals(vm2, list.get(0));
		assertEquals(vm1, list.get(1));
		
	}
	
}
