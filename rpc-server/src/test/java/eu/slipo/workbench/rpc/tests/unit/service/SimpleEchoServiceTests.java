package eu.slipo.workbench.rpc.tests.unit.service;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import static org.junit.Assert.*;

import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import static org.mockito.Mockito.*;

import java.util.Locale;

import org.springframework.context.MessageSource;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringRunner;

import eu.slipo.workbench.common.model.TextMessage;
import eu.slipo.workbench.rpc.service.SimpleEchoService;

@RunWith(SpringRunner.class)
@ActiveProfiles({ "testing" })
public class SimpleEchoServiceTests
{
    /**
     * Mock dependency of unit-tested controller 
     */
    @Mock
    private MessageSource messageSource;
    
    @InjectMocks 
    private SimpleEchoService echoService;

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
        TextMessage replyMessage = echoService.echo(message, "en");
        
        assertNotNull(replyMessage);
        assertTrue("message echoed", replyMessage.text().equals(message.text()));
        assertTrue("comment exists", replyMessage.comment().equals(ECHOED_BY));
    }
}
