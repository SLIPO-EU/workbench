package eu.slipo.workbench.rpc.tests;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

@RunWith(SpringRunner.class)
@SpringBootTest(properties = {"spring.profiles.active=testing"})
public class ApplicationTests 
{
	@Test
	public void contextLoads() {
	}

}
