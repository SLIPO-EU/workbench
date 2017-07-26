package eu.slipo.workbench.web.service;

import eu.slipo.workbench.common.model.TextMessage;

public interface EchoService
{
    TextMessage echo(String text);
}
