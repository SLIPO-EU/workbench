package eu.slipo.workbench.rpc.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.fasterxml.jackson.dataformat.xml.XmlMapper;
import com.fasterxml.jackson.dataformat.xml.ser.ToXmlGenerator;

@Configuration
public class XmlMapperConfiguration
{
    @Bean({ "xmlMapper", "defaultXmlMapper" })
    public XmlMapper defaultXmlMapper()
    {
        XmlMapper xmlMapper = new XmlMapper();
        return xmlMapper;
    }
}
