package eu.slipo.workbench.rpc.tests.integration.repository;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.time.ZonedDateTime;
import java.util.List;
import java.util.UUID;

import javax.annotation.PostConstruct;

import org.apache.commons.beanutils.BeanUtils;
import org.junit.After;
import org.junit.Before;
import org.junit.FixMethodOrder;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.MethodSorters;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Scope;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringRunner;

import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.GeometryFactory;
import com.vividsolutions.jts.geom.Point;

import eu.slipo.workbench.common.domain.AccountEntity;
import eu.slipo.workbench.common.model.poi.EnumDataFormat;
import eu.slipo.workbench.common.model.poi.EnumResourceType;
import eu.slipo.workbench.common.model.resource.EnumDataSourceType;
import eu.slipo.workbench.common.model.resource.ResourceMetadataView;
import eu.slipo.workbench.common.model.resource.ResourceRecord;
import eu.slipo.workbench.common.repository.AccountRepository;
import eu.slipo.workbench.common.repository.DefaultResourceRepository;
import eu.slipo.workbench.common.repository.ResourceRepository;


@RunWith(SpringRunner.class)
@ActiveProfiles({ "testing" })
@EnableAutoConfiguration
@EntityScan(basePackageClasses = { eu.slipo.workbench.common.domain._Marker.class })
@EnableJpaRepositories(basePackageClasses = { eu.slipo.workbench.common.repository._Marker.class })
@SpringBootTest(classes = { DefaultResourceRepository.class }, webEnvironment = WebEnvironment.NONE)
@FixMethodOrder(MethodSorters.NAME_ASCENDING)
public class DefaultResourceRepositoryTests
{
    @TestConfiguration
    public static class Configuration
    {
        @Autowired
        private AccountRepository accountRepository;

        @Bean
        @Scope("prototype")
        public ResourceRecord sampleResourceRecord1()
        {
            GeometryFactory geomFactory = new GeometryFactory();
            Point point = geomFactory.createPoint(new Coordinate(0.5, -1.5));

            ResourceRecord record = new ResourceRecord();
            record.setType(EnumResourceType.POI_DATA);
            record.setSourceType(EnumDataSourceType.FILESYSTEM);
            record.setInputFormat(EnumDataFormat.GEOJSON);
            record.setFormat(EnumDataFormat.N_TRIPLES);
            record.setFilePath("/tmp/1.nt");
            record.setFileSize(1000L);
            record.setTableName(UUID.randomUUID());
            record.setBoundingBox(point);
            record.setMetadata("sample-1", "A sample N-TRIPLES file");
            return record;
        }

        @Bean
        @Scope("prototype")
        public ResourceRecord sampleResourceRecord1v2(ResourceRecord sampleResourceRecord1)
            throws Exception
        {
            ResourceRecord record = (ResourceRecord) BeanUtils.cloneBean(sampleResourceRecord1);

            record.setFilePath("/tmp/1-2.n3");
            record.setFileSize(2000L);
            record.setFormat(EnumDataFormat.N3);
            record.setMetadata("sample-1-2", "A sample N3 file");

            return record;
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
    }

    /**
     * The repository under testing
     */
    @Autowired
    @Qualifier("defaultResourceRepository")
    private ResourceRepository resourceRepository;

    @Autowired
    private AccountRepository accountRepository;

    @Autowired
    private ResourceRecord sampleResourceRecord1;

    @Autowired
    private ResourceRecord sampleResourceRecord1v2;

    @Before
    public void setup() throws Exception {}

    @After
    public void teardown() throws Exception {}

    @Test
    public void test1_createAndUpdate() throws Exception
    {
        ResourceRecord record1 = sampleResourceRecord1;
        ResourceMetadataView metadata1 = record1.getMetadata();

        Integer createdBy = accountRepository.findOneByUsername("baz").getId();

        // Create

        ResourceRecord record1a = resourceRepository.create(record1, createdBy);
        assertNotNull(record1a);

        final long id = record1a.getId();
        assertTrue(id > 0);
        assertEquals(1L, record1a.getVersion());
        assertEquals(createdBy, record1a.getCreatedBy().getId());
        assertNotNull(record1a.getCreatedOn());
        assertEquals(createdBy, record1a.getUpdatedBy().getId());
        assertNotNull(record1a.getUpdatedOn());
        assertEquals(record1.getInputFormat(), record1a.getInputFormat());
        assertEquals(record1.getFormat(), record1a.getFormat());
        assertEquals(record1.getType(), record1a.getType());
        assertEquals(record1.getSourceType(), record1a.getSourceType());
        assertEquals(record1.getFilePath(), record1a.getFilePath());
        assertEquals(record1.getFileSize(), record1a.getFileSize());
        assertEquals(metadata1.getName(), record1a.getName());
        assertEquals(metadata1.getDescription(), record1a.getDescription());
        assertEquals(record1.getBoundingBox(), record1a.getBoundingBox());
        assertEquals(record1.getTableName(), record1a.getTableName());

        List<ResourceRecord> revisions1a = record1a.getRevisions();
        assertNotNull(revisions1a);
        assertTrue(revisions1a.size() == 1);
        ResourceRecord revision1a1 = revisions1a.get(0);
        assertTrue(revision1a1.getId() > 0);
        assertEquals(1L, revision1a1.getVersion());

        ResourceRecord record1b = resourceRepository.findOne(record1.getName(), createdBy);
        assertNotNull(record1b);
        assertEquals(record1a.getId(), record1b.getId());
        assertEquals(record1a.getVersion(), record1b.getVersion());

        // Update with a new revision

        ResourceRecord record2 = sampleResourceRecord1v2;
        ResourceMetadataView metadata2 = record2.getMetadata();
        Integer updatedBy = createdBy;

        ResourceRecord record2a = resourceRepository.update(id, record2, updatedBy);
        assertNotNull(record2a);
        assertEquals(id, record2a.getId());
        assertEquals(2L, record2a.getVersion());

        assertEquals(createdBy, record2a.getCreatedBy().getId());
        assertEquals(record1a.getCreatedOn(), record2a.getCreatedOn());
        assertEquals(updatedBy, record2a.getUpdatedBy().getId());
        assertNotNull(record2a.getUpdatedOn());
        assertTrue(record1a.getUpdatedOn().isBefore(record2a.getUpdatedOn()));
        assertEquals(record2.getInputFormat(), record2a.getInputFormat());
        assertEquals(record2.getFormat(), record2a.getFormat());
        assertEquals(record2.getType(), record2a.getType());
        assertEquals(record2.getSourceType(), record2a.getSourceType());
        assertEquals(record2.getFilePath(), record2a.getFilePath());
        assertEquals(record2.getFileSize(), record2a.getFileSize());
        assertEquals(metadata2.getName(), record2a.getName());
        assertEquals(metadata2.getDescription(), record2a.getDescription());
        assertEquals(record2.getBoundingBox(), record2a.getBoundingBox());
        assertEquals(record2.getTableName(), record2a.getTableName());

        List<ResourceRecord> revisions2a = record2a.getRevisions();
        assertNotNull(revisions1a);
        assertTrue(revisions2a.size() == 2);
        ResourceRecord revision2a2 = revisions2a.get(1);
        assertEquals(id, revision2a2.getId());
        assertEquals(2L, revision2a2.getVersion());

        ResourceRecord record2b = resourceRepository.findOne(record2.getName(), createdBy);
        assertNotNull(record2b);
        assertEquals(record2a.getId(), record2b.getId());
        assertEquals(record2a.getVersion(), record2b.getVersion());

    }
}
