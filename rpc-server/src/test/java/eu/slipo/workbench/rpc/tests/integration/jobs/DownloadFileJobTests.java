package eu.slipo.workbench.rpc.tests.integration.jobs;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.atomic.AtomicReference;

import org.apache.commons.codec.digest.DigestUtils;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.batch.core.BatchStatus;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.JobExecution;
import org.springframework.batch.core.JobExecutionException;
import org.springframework.batch.core.JobExecutionListener;
import org.springframework.batch.core.JobParameters;
import org.springframework.batch.core.JobParametersBuilder;
import org.springframework.batch.core.configuration.annotation.JobBuilderFactory;
import org.springframework.batch.core.explore.JobExplorer;
import org.springframework.batch.core.job.flow.Flow;
import org.springframework.batch.core.launch.JobLauncher;
import org.springframework.batch.core.launch.JobOperator;
import org.springframework.batch.core.listener.JobExecutionListenerSupport;
import org.springframework.batch.item.ExecutionContext;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringRunner;

import eu.slipo.workbench.rpc.Application;

@RunWith(SpringRunner.class)
@ActiveProfiles({ "testing" })
@SpringBootTest(classes = { Application.class }, webEnvironment = WebEnvironment.NONE)
public class DownloadFileJobTests
{
    private static Logger logger = LoggerFactory.getLogger(DownloadFileJobTests.class);

    private static class Fixture
    {
        /**
         * The target URL to download from */
        URL url;

        /**
         * The SHA-256 checksum
         */
        String checksum;

        public Fixture(URL url, String checksum)
        {
            this.url = url;
            this.checksum = checksum;
        }
    }


    @Autowired
    private JobLauncher jobLauncher;

    @Autowired
    private JobOperator jobOperator;

    @Autowired
    private JobExplorer jobExplorer;

    @Autowired
    private JobBuilderFactory jobBuilderFactory;

    @Autowired
    @Qualifier("downloadFile.flow")
    private Flow downloadFileFlow;

    @Autowired
    private Path jobDataDirectory;

    private void testFixture(Fixture fixture)
        throws InterruptedException, JobExecutionException, IOException
    {
        final Path fileName = Paths.get(fixture.url.getPath()).getFileName();

        // Build parameters

        JobParameters parameters = new JobParametersBuilder()
            .addString("_id", Long.toHexString(System.currentTimeMillis()))
            .addString("url", fixture.url.toString())
            .addString("outputName", fileName.toString())
            .addString("checksum", fixture.checksum)
            .toJobParameters();

        // Setup listeners

        final CountDownLatch done = new CountDownLatch(1);

        final AtomicReference<Path> outputDirReference = new AtomicReference<>();
        final AtomicReference<Path> outputNameReference = new AtomicReference<>();

        JobExecutionListener listener = new JobExecutionListenerSupport()
        {
            @Override
            public void afterJob(JobExecution jobExecution)
            {
                ExecutionContext executionContext = jobExecution.getExecutionContext();
                if (jobExecution.getStatus() == BatchStatus.COMPLETED) {
                    if (executionContext.containsKey("outputDir"))
                        outputDirReference.set(Paths.get(executionContext.getString("outputDir")));
                    if (executionContext.containsKey("outputName"))
                        outputNameReference.set(Paths.get(executionContext.getString("outputName")));
                }
                // Done
                done.countDown();
            }
        };

        // Build job from flow

        Job job = jobBuilderFactory.get("downloadFile")
            .start(downloadFileFlow).end()
            .listener(listener)
            .build();

        // Launch job and wait

        jobLauncher.run(job, parameters);
        done.await();

        // Check results

        final Path outputDir = outputDirReference.get();
        assertNotNull(outputDir);
        assertTrue(outputDir.isAbsolute());
        assertTrue(Files.isDirectory(outputDir) && outputDir.startsWith(jobDataDirectory));

        final Path outputName = outputNameReference.get();
        assertNotNull(outputName);
        assertEquals(fileName, outputName);

        logger.debug("The job has completed succesfully: outputDir={}", outputDir);

        final Path output = outputDir.resolve(fileName);
        assertTrue(Files.isRegularFile(output));
        assertTrue(Files.isReadable(output));

        String checksum = null;
        try (InputStream s = Files.newInputStream(output, StandardOpenOption.READ)) {
            checksum = DigestUtils.sha256Hex(s);
            checksum = checksum.toLowerCase();
        }

        logger.debug("Computed SHA-256 checksum for {}: {}", output, checksum);
        assertEquals(fixture.checksum, checksum);
    }

    @Test(timeout = 2000)
    public void test1() throws Exception
    {
        URL url = new URL(
            "https", "raw.githubusercontent.com",
            "/SLIPO-EU/workbench/73b53f93b2ebce0e457a67b46f28955ccbd6fd47/pom.xml");
        String checksum = "efe46c6558bd39fada65b59c6367e9590b1df067096563cc17d2680cc18275f1";

        Fixture f = new Fixture(url, checksum);
        testFixture(f);
    }
}
