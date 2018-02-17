package eu.slipo.workbench.rpc.tests.integration.repository;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.ZonedDateTime;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.Query;
import javax.persistence.TypedQuery;

import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.FixMethodOrder;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.MethodSorters;
import org.junit.After;
import org.junit.AfterClass;

import static org.junit.Assert.*;

import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.stereotype.Repository;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.transaction.annotation.Transactional;

import com.fasterxml.jackson.databind.ObjectMapper;

import eu.slipo.workbench.common.domain.AccountEntity;
import eu.slipo.workbench.common.domain.ProcessEntity;
import eu.slipo.workbench.common.model.QueryResultPage;
import eu.slipo.workbench.common.model.poi.EnumDataFormat;
import eu.slipo.workbench.common.model.poi.EnumOntology;
import eu.slipo.workbench.common.model.process.ProcessDefinition;
import eu.slipo.workbench.common.model.process.ProcessDefinitionBuilder;
import eu.slipo.workbench.common.model.process.ProcessExecutionQuery;
import eu.slipo.workbench.common.model.process.ProcessExecutionRecord;
import eu.slipo.workbench.common.model.process.ProcessRecord;
import eu.slipo.workbench.common.model.resource.DataSource;
import eu.slipo.workbench.common.model.resource.FileSystemDataSource;
import eu.slipo.workbench.common.model.resource.ResourceMetadataCreate;
import eu.slipo.workbench.common.model.tool.ToolConfiguration;
import eu.slipo.workbench.common.model.tool.TriplegeoConfiguration;
import eu.slipo.workbench.common.repository.AccountRepository;
import eu.slipo.workbench.common.repository.DefaultProcessRepository;
import eu.slipo.workbench.common.repository.ProcessRepository;

@RunWith(SpringRunner.class)
@ActiveProfiles({ "testing" })
@EnableAutoConfiguration
@EntityScan(basePackageClasses = { eu.slipo.workbench.common.domain._Marker.class })
@EnableJpaRepositories(basePackageClasses = { eu.slipo.workbench.common.repository._Marker.class })
@SpringBootTest(
    classes = { DefaultProcessRepository.class }, webEnvironment = WebEnvironment.NONE
)
@FixMethodOrder(MethodSorters.NAME_ASCENDING)
public class DefaultProcessRepositoryTests
{
    @TestConfiguration
    public static class Configuration
    {
        @Autowired
        private AccountRepository accountRepository;
       
        @Bean
        public ProcessDefinition sampleProcessDefinition1()
        {
            final TriplegeoConfiguration configuration = new TriplegeoConfiguration();
            configuration.setInputFormat(EnumDataFormat.CSV);
            configuration.setOutputFormat(EnumDataFormat.N_TRIPLES);
            configuration.setAttrX("lon");
            configuration.setAttrX("lat");
            configuration.setDelimiter(";");
            configuration.setFeatureName("points");
            configuration.setAttrKey("id");
            configuration.setAttrName("name");
            configuration.setAttrCategory("type");
            configuration.setTargetOntology(EnumOntology.GEOSPARQL);
            
            final ResourceMetadataCreate metadata = 
                new ResourceMetadataCreate("sample-1", "A sample CSV file", EnumDataFormat.CSV);
            
            final int resourceKey = 1;
            
            final DataSource source = new FileSystemDataSource("/tmp/1.csv");
            
            ProcessDefinition definition = ProcessDefinitionBuilder.create("register-1")
                .transform(0, "triplegeo", source, configuration, resourceKey)
                .register(99, "register", metadata, resourceKey)
                .build();
            
            return definition;
        }
        
        @Bean
        public ProcessDefinition sampleProcessDefinition2()
        {
            final TriplegeoConfiguration configuration = new TriplegeoConfiguration();
            configuration.setInputFormat(EnumDataFormat.SHAPEFILE);
            configuration.setOutputFormat(EnumDataFormat.N_TRIPLES);
            configuration.setFeatureName("points");
            configuration.setAttrKey("id");
            configuration.setAttrName("name");
            configuration.setAttrCategory("type");
            configuration.setTargetOntology(EnumOntology.GEOSPARQL);
            
            final ResourceMetadataCreate metadata = 
                new ResourceMetadataCreate("sample-2", "A sample SHP file", EnumDataFormat.SHAPEFILE);
            
            final int resourceKey = 1;
            
            final DataSource source = new FileSystemDataSource("/tmp/1.zip");
            
            ProcessDefinition definition = ProcessDefinitionBuilder.create("register-2")
                .transform(0, "triplegeo", source, configuration, resourceKey)
                .register(99, "register", metadata, resourceKey)
                .build();
            
            return definition;
        }
        
        @PostConstruct
        public void loadData()
        {
            AccountEntity a = new AccountEntity("baz", "baz@example.com");
            a.setBlocked(false);
            a.setActive(true);
            a.setRegistered(ZonedDateTime.now());
            accountRepository.save(a);
        }
        
        @PreDestroy
        public void deleteData()
        {
            // accountRepository.deleteAllInBatch();
        }
    }
    
    /** The repository under testing */
    @Autowired
    @Qualifier("defaultProcessRepository")
    private ProcessRepository processRepository;
    
    @Autowired
    private AccountRepository accountRepository;
    
    @Autowired
    private ProcessDefinition sampleProcessDefinition1;
    
    @Autowired
    private ProcessDefinition sampleProcessDefinition2;
    
    @Autowired
    private ObjectMapper objectMapper;
    
    @Before
    public void setup() throws Exception 
    {
    }
    
    @After
    public void teardown() throws Exception 
    {
    }
    
    //
    // Tests
    //
    
    @Test
    public void test1_createAndUpdate() throws Exception
    {
        AccountEntity createdBy = accountRepository.findOneByUsername("baz");
        assertNotNull(createdBy);
        
        ProcessRecord record1 = 
            processRepository.create(sampleProcessDefinition1, createdBy.getId());
        assertNotNull(record1);
        assertTrue(record1.getId() > 0 && record1.getVersion() > 0);
        
        final long id = record1.getId();
        assertEquals(1L, record1.getVersion());
        
        ProcessRecord record2 = 
            processRepository.update(id, sampleProcessDefinition2, createdBy.getId());
        
        assertEquals(id, record2.getId());
        assertEquals(2L, record2.getVersion());
        
        List<ProcessRecord> revisions = record2.getRevisions().stream()
            .sorted(Comparator.comparingLong(ProcessRecord::getVersion))
            .collect(Collectors.toList());
        assertTrue(revisions.size() == 2);
            
        ProcessRecord rev1 = revisions.get(0);
        ProcessRecord rev2 = revisions.get(1);
        ProcessRecord rev1a = processRepository.findOne(id, 1L);
        ProcessRecord rev2a = processRepository.findOne(id, 2L);
        
        assertEquals(id, rev1.getId());
        assertEquals(1L, rev1.getVersion());
        assertEquals(id, rev1a.getId());
        assertEquals(1L, rev1a.getVersion());
        assertEquals(id, rev2.getId());
        assertEquals(2L, rev2.getVersion());
        assertEquals(id, rev2a.getId());
        assertEquals(2L, rev2a.getVersion());
        
        assertEquals(rev1.getCreatedOn(), rev2.getCreatedOn());
        assertEquals(rev1.getCreatedBy().getId(), rev2.getCreatedBy().getId());
        
        assertEquals(
            objectMapper.writeValueAsString(sampleProcessDefinition1), 
            objectMapper.writeValueAsString(rev1.getDefinition()));
        assertEquals(
            objectMapper.writeValueAsString(sampleProcessDefinition1), 
            objectMapper.writeValueAsString(rev1a.getDefinition()));
        assertEquals(
            objectMapper.writeValueAsString(sampleProcessDefinition2), 
            objectMapper.writeValueAsString(rev2.getDefinition()));
        assertEquals(
            objectMapper.writeValueAsString(sampleProcessDefinition2), 
            objectMapper.writeValueAsString(rev2a.getDefinition()));
    }
    
    @Test
    public void test2_createExecution() throws Exception
    {
        AccountEntity createdBy = accountRepository.findOneByUsername("baz");
        assertNotNull(createdBy);
        AccountEntity submittedBy = createdBy;
        
        ProcessRecord record1 = 
            processRepository.create(sampleProcessDefinition1, createdBy.getId());
        assertNotNull(record1);
        
        final long id = record1.getId();
        final long version = record1.getVersion();
        
        ProcessExecutionRecord executionRecord1 = 
            processRepository.createExecution(id, version, submittedBy.getId());
        assertNotNull(executionRecord1);
        
        final long xid = executionRecord1.getId();
        assertEquals(id, executionRecord1.getProcess().getId());
        assertEquals(version, executionRecord1.getProcess().getVersion());
        assertEquals(submittedBy.getId(), executionRecord1.getSubmittedBy().getId());
        assertNull(executionRecord1.getStartedOn());
        assertNull(executionRecord1.getCompletedOn());
        assertEquals(Collections.emptyList(), executionRecord1.getSteps());
        
        ProcessRecord record1a = processRepository.findOne(id, version, true);
        assertNotNull(record1a);
        assertEquals(1, record1a.getExecutions().size());
        
        ProcessExecutionRecord executionRecord1a = record1a.getExecutions().get(0);
        assertEquals(executionRecord1.getId(), executionRecord1a.getId());
        assertEquals(executionRecord1.getProcess().toString(), executionRecord1a.getProcess().toString());
    }
    
}
