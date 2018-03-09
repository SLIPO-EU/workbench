package eu.slipo.workbench.web.config;

import java.net.URI;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.remoting.httpinvoker.HttpInvokerProxyFactoryBean;

import eu.slipo.workbench.common.service.EchoService;
import eu.slipo.workbench.common.service.ProcessOperator;

@Configuration
public class RpcClientConfiguration
{
    private URI rootUrl;
    
    @Autowired
    private void setRootUrl(
        @Value("${slipo.rpc-server.url:http://localhost:8080}") String rootUrl)
    {
        this.rootUrl = URI.create(rootUrl).normalize();
    }
    
    @Bean
    public HttpInvokerProxyFactoryBean echoServiceFactory()
    {
        final String serviceUrl = rootUrl.resolve("/echoService").toString();
        
        HttpInvokerProxyFactoryBean factory = new HttpInvokerProxyFactoryBean();
        factory.setServiceInterface(EchoService.class);
        factory.setServiceUrl(serviceUrl);
        return factory;
    }
    
    @Bean
    public HttpInvokerProxyFactoryBean processOperatorFactory()
    {
        final String serviceUrl = rootUrl.resolve("/processOperator").toString();
        
        HttpInvokerProxyFactoryBean factory = new HttpInvokerProxyFactoryBean();
        factory.setServiceInterface(ProcessOperator.class);
        factory.setServiceUrl(serviceUrl);
        return factory;
    }
    
}
