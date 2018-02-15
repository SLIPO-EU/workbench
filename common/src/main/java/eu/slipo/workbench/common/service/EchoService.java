package eu.slipo.workbench.common.service;

import eu.slipo.workbench.common.model.TextMessage;

public interface EchoService
{
    TextMessage echo(String message, String lang);
    
    TextMessage echo(TextMessage message, String lang);
}
