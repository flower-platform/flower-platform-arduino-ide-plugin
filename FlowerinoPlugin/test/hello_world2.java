import static org.junit.Assert.*;

import org.junit.Test;

public class hello_world2 {
	int getNr() {
		return 3;
	}
	@Test
	public void test() {
		
		assert(4 == getNr());
		System.out.println("Hello World 2");
	}

}
