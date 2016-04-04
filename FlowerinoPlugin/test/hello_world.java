import static org.junit.Assert.*;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

public class hello_world {
	int getNr(){
		return 2;
	}
	
	@Before
	public void before(){
	}
	
	@After
	public void after(){
	}
	
	@Test
	public void test() {
		assert( 3 == getNr());
		System.out.println("Hellow World");
	}

}
