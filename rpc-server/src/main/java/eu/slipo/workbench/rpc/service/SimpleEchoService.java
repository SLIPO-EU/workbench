package eu.slipo.workbench.rpc.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.MessageSource;
import org.springframework.stereotype.Service;

import java.util.Locale;
import java.util.concurrent.atomic.AtomicInteger;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import eu.slipo.workbench.common.model.TextMessage;
import eu.slipo.workbench.common.service.EchoService;

@Service
public class SimpleEchoService implements EchoService
{
    private static Logger logger = LoggerFactory.getLogger(SimpleEchoService.class);

    @Autowired
    MessageSource messageSource;

    private AtomicInteger serial = new AtomicInteger(0);

    @Override
    public TextMessage echo(String message, String lang)
    {
        return echo(new TextMessage(message), lang);
    }

    @Override
    public TextMessage echo(TextMessage message, String lang)
    {
        final String text = message.text();
        final Locale locale = Locale.forLanguageTag(lang);

        final String comment = messageSource.getMessage(
            "EchoService.echoedBy", new Object[] { this.toString() }, "Echoed by {0}", locale);

        final int id = serial.incrementAndGet();
        return new TextMessage(id, text, comment);
    }

}
