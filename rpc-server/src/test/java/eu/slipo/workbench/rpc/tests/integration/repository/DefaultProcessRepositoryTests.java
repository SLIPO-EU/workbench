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

import org.junit.Before;
import org.junit.FixMethodOrder;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.MethodSorters;
import org.junit.After;

import static org.junit.Assert.*;

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
import eu.slipo.workbench.common.model.poi.EnumOperation;
import eu.slipo.workbench.common.model.poi.EnumTool;
import eu.slipo.workbench.common.model.process.EnumProcessExecutionStatus;
import eu.slipo.workbench.common.model.process.EnumStepFile;
import eu.slipo.workbench.common.model.process.ProcessDefinition;
import eu.slipo.workbench.common.model.process.ProcessDefinitionBuilder;
import eu.slipo.workbench.common.model.process.ProcessExecutionQuery;
import eu.slipo.workbench.common.model.process.ProcessExecutionRecord;
import eu.slipo.workbench.common.model.process.ProcessExecutionStepFileRecord;
import eu.slipo.workbench.common.model.process.ProcessExecutionStepRecord;
import eu.slipo.workbench.common.model.process.ProcessRecord;
import eu.slipo.workbench.common.model.resource.DataSource;
import eu.slipo.workbench.common.model.resource.FileSystemDataSource;
import eu.slipo.workbench.common.model.resource.ResourceMetadataCreate;
import eu.slipo.workbench.common.model.tool.ToolConfiguration;
import eu.slipo.workbench.common.model.tool.TriplegeoConfiguration;
import eu.slipo.workbench.common.repository.AccountRepository;
import eu.slipo.workbench.common.repository.DefaultProcessRepository;
import eu.slipo.workbench.common.repository.DefaultResourceRepository;
import eu.slipo.workbench.common.repository.ProcessRepository;
import eu.slipo.workbench.common.repository.ResourceRepository;

@RunWith(SpringRunner.class)
@ActiveProfiles({ "testing" })
@EnableAutoConfiguration
@EntityScan(basePackageClasses = { eu.slipo.workbench.common.domain._Marker.class })
@EnableJpaRepositories(basePackageClasses = { eu.slipo.workbench.common.repository._Marker.class })
@SpringBootTest(
    classes = { DefaultResourceRepository.class, DefaultProcessRepository.class }, 
    webEnvironment = WebEnvironment.NONE
)
@FixMethodOrder(MethodSorters.NAME_ASCENDING)
public class DefaultProcessRepositoryTests
{
    @TestConfiguration
    public static class Configuration
    {
        @Autowired
        private AccountRepository accountRepository;
       
        @Autowired
        private ResourceRepository resourceRepository;
        
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
    public void test2_createAndUpdateExecution() throws Exception
    {
        AccountEntity createdBy = accountRepository.findOneByUsername("baz");
        assertNotNull(createdBy);
        AccountEntity submittedBy = createdBy;
        
        ProcessRecord record1 = 
            processRepository.create(sampleProcessDefinition1, createdBy.getId());
        assertNotNull(record1);
        
        final long id = record1.getId();
        final long version = record1.getVersion();
        
        // Create an execution on this process revision
        
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
        assertEquals(EnumProcessExecutionStatus.UNKNOWN, executionRecord1a.getStatus());
        assertEquals(executionRecord1.getId(), executionRecord1a.getId());
        assertEquals(executionRecord1.getProcess().toString(), executionRecord1a.getProcess().toString());
        
        // Update basic execution metadata for status: UNKNOWN -> RUNNING
        
        ZonedDateTime started = executionRecord1a.getSubmittedOn().plusMinutes(1);
        executionRecord1a.setStartedOn(started);
        executionRecord1a.setStatus(EnumProcessExecutionStatus.RUNNING);
        
        ProcessExecutionRecord executionRecord1b = 
            processRepository.updateExecution(xid, executionRecord1a);
        assertEquals(EnumProcessExecutionStatus.RUNNING, executionRecord1b.getStatus());
        assertEquals(started, executionRecord1b.getStartedOn());
        assertNull(executionRecord1b.getCompletedOn());
        
        ProcessExecutionRecord executionRecord1c = processRepository.findExecution(xid); 
        assertNotNull(executionRecord1c);
        assertEquals(EnumProcessExecutionStatus.RUNNING, executionRecord1c.getStatus());
        assertEquals(started, executionRecord1c.getStartedOn());
        assertEquals(Collections.emptyList(), executionRecord1c.getSteps());
        assertNull(executionRecord1c.getCompletedOn());
        
        // Update execution by adding one step as RUNNING
        
        final int step1Key = 9;
        final long step1JobExecutionId = 1999L;
        final long fileSize1p1 = 1000L, fileSize1p2 = 2000L;
        
        ProcessExecutionStepFileRecord fileRecord1p1 = 
            new ProcessExecutionStepFileRecord(EnumStepFile.INPUT, "/tmp/1-1.csv", fileSize1p1);
        ProcessExecutionStepFileRecord fileRecord1p2 = 
            new ProcessExecutionStepFileRecord(EnumStepFile.INPUT, "/tmp/1-2.csv", fileSize1p2);
        
        ProcessExecutionStepRecord stepRecord1 = 
            new ProcessExecutionStepRecord(-1, step1Key, "triplegeo-1");
        stepRecord1.setOperation(EnumOperation.TRANSFORM);
        stepRecord1.setTool(EnumTool.TRIPLEGEO);
        stepRecord1.setJobExecutionId(step1JobExecutionId);
        stepRecord1.setStartedOn(started.plusMinutes(1));
        stepRecord1.setStatus(EnumProcessExecutionStatus.RUNNING);
        stepRecord1.addFiles(Arrays.asList(fileRecord1p1, fileRecord1p2));
        
        ProcessExecutionRecord executionRecord1d = 
            processRepository.createExecutionStep(xid, stepRecord1);
        assertNotNull(executionRecord1d);
        List<ProcessExecutionStepRecord> executionRecord1dSteps = executionRecord1d.getSteps();
        assertTrue(executionRecord1dSteps.size() == 1);
        
        ProcessExecutionStepRecord stepRecord1a = executionRecord1dSteps.get(0);
        assertEquals(step1Key, stepRecord1a.getKey());
        assertEquals("triplegeo-1", stepRecord1a.getName());
        assertEquals(EnumProcessExecutionStatus.RUNNING, stepRecord1a.getStatus());
        assertEquals(EnumOperation.TRANSFORM, stepRecord1a.getOperation());
        assertEquals(EnumTool.TRIPLEGEO, stepRecord1a.getTool());
        assertEquals(step1JobExecutionId, stepRecord1a.getJobExecutionId());
        
        List<ProcessExecutionStepFileRecord> fileRecords = stepRecord1a.getFiles();
        assertEquals(2, fileRecords.size());
        assertEquals("/tmp/1-1.csv", fileRecords.get(0).getFilePath());
        assertEquals(EnumStepFile.INPUT, fileRecords.get(0).getType());
        assertEquals(fileSize1p1, fileRecords.get(0).getFileSize().longValue());
        assertEquals("/tmp/1-2.csv", fileRecords.get(1).getFilePath());
        assertEquals(EnumStepFile.INPUT, fileRecords.get(1).getType());
        assertEquals(fileSize1p2, fileRecords.get(1).getFileSize().longValue());
        
        // Update execution by updating 1st step as COMPLETED
        
        // Todo
        
        // Update execution 
    }
    
}
