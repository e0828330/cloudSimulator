package cloudSimulator;

import model.VirtualMachine;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

public class VMTest {

	@Test
	public void test_getDownTimeShouldBe100Percent() {
		VirtualMachine vm1 = new VirtualMachine();
		assertEquals(0., vm1.getDownTimeInPercent(1), 0.);
		vm1.incrementDownTimeCounter();
		vm1.incrementDownTimeCounter();
		vm1.incrementDownTimeCounter();
		assertEquals(1., vm1.getDownTimeInPercent(3), 0.0);
	}
	
	@Test
	public void test_getDownTimeShouldBe50Percent() {
		VirtualMachine vm1 = new VirtualMachine();
		assertEquals(0., vm1.getDownTimeInPercent(1), 0.);
		vm1.incrementDownTimeCounter();
		vm1.incrementDownTimeCounter();
		vm1.incrementDownTimeCounter();		
		assertEquals(0.5, vm1.getDownTimeInPercent(6), 0.0);
	}	

}
