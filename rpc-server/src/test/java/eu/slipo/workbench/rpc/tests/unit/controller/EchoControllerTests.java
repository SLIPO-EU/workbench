package eu.slipo.workbench.rpc.tests.unit.controller;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import static org.junit.Assert.*;

import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import static org.mockito.Mockito.*;

import java.util.Locale;

import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.MessageSource;
import org.springframework.test.context.junit4.SpringRunner;

import eu.slipo.workbench.common.model.RestResponse;
import eu.slipo.workbench.common.model.TextMessage;
import eu.slipo.workbench.rpc.controller.EchoController;

@RunWith(SpringRunner.class)
@SpringBootTest(properties = {"spring.profiles.active=testing"})
public class EchoControllerTests
{
    /**
     * Mock dependency of unit-tested controller 
     */
    @Mock
    private MessageSource messageSource;
    
    @InjectMocks 
    private EchoController controller;

    private static String ECHOED_BY = "echoed by me";
    
    @Before
    public void setup() throws Exception 
    {
        // Initialize mocks that are present here
        
        MockitoAnnotations.initMocks(this);
        
        // Configure mocked dependencies
        
        when(messageSource.getMessage(
                anyString(), anyObject(), anyString(), any(Locale.class)))
            .thenReturn(ECHOED_BY);
    }
    
    @Test
    public void testEcho() throws Exception 
    {
        TextMessage message = new TextMessage("hello world!");
        
        RestResponse<TextMessage> response = controller.echo(message, "en");
        
        assertNotNull(response);
        
        TextMessage result = response.getResult();
        
        assertNotNull(result);
        assertTrue("message echoed", result.text().equals(message.text()));
        assertTrue("comment exists", result.comment().equals(ECHOED_BY));
    }
}
