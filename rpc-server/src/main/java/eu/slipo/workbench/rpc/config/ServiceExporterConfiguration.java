package eu.slipo.workbench.rpc.config;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.remoting.httpinvoker.HttpInvokerServiceExporter;

import eu.slipo.workbench.common.service.EchoService;
import eu.slipo.workbench.common.service.ProcessOperator;

@Configuration
public class ServiceExporterConfiguration
{
    @Bean(name = "/echoService")
    HttpInvokerServiceExporter echoServiceExporter(
        @Qualifier("simpleEchoService") EchoService echoService)
    {
        HttpInvokerServiceExporter exporter = new HttpInvokerServiceExporter();
        exporter.setServiceInterface(EchoService.class);
        exporter.setService(echoService);
        return exporter;
    }
    
    @Bean(name = "/processOperator")
    HttpInvokerServiceExporter processOperatorExporter(
        @Qualifier("simpleProcessOperator") ProcessOperator processOperator)
    {
        HttpInvokerServiceExporter exporter = new HttpInvokerServiceExporter();
        exporter.setServiceInterface(ProcessOperator.class);
        exporter.setService(processOperator);
        return exporter;
    }
}
