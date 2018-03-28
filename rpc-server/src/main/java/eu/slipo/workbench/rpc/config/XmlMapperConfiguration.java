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
        xmlMapper.configure(ToXmlGenerator.Feature.WRITE_XML_DECLARATION, true);

        return xmlMapper;
    }
}
