package eu.slipo.workbench.rpc.jobs;

import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.file.FileAlreadyExistsException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.nio.file.StandardOpenOption;

import javax.annotation.PostConstruct;

import org.apache.commons.codec.digest.DigestUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.StepContribution;
import org.springframework.batch.core.StepExecution;
import org.springframework.batch.core.configuration.annotation.JobScope;
import org.springframework.batch.core.job.builder.FlowBuilder;
import org.springframework.batch.core.job.flow.Flow;
import org.springframework.batch.core.scope.context.ChunkContext;
import org.springframework.batch.core.step.tasklet.Tasklet;
import org.springframework.batch.item.ExecutionContext;
import org.springframework.batch.repeat.RepeatStatus;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.stereotype.Component;
import org.springframework.util.Assert;

import eu.slipo.workbench.rpc.jobs.listener.ExecutionContextPromotionListeners;

@Component
public class DownloadFileJobConfiguration extends BaseJobConfiguration
{
    @PostConstruct
    private void setupDataDirectory() throws IOException
    {
        super.setupDataDirectory("downloadFile");
    }

    /**
     * A simple tasklet that downloads a URL to a local output file.
     * <p>The download is not resumable (if failed or interrupted will start from the beginning).
     */
    public class DownloadFileTasklet implements Tasklet
    {
        private final Path outputDir;

        private final Path outputName;

        private final URL url;

        private final String checksum;

        /**
         * Create an instance of {@link DownloadFileTasklet}
         *
         * @param url The target URL to download from
         * @param checksum A SHA-256 checksum to verify against (may be <tt>null</tt>, in such a
         *   case no verification will be performed)
         * @param outputDir The output directory
         * @param outputName A file name to save our download under
         */
        public DownloadFileTasklet(URL url, String checksum, Path outputDir, String outputName)
        {
            Assert.notNull(url, "Expected a non-null source URL ");
            Assert.notNull(outputDir, "Expected a non-null output directory");
            Assert.isTrue(outputDir.isAbsolute(),
                "The output directory is expected as an absolute path");
            Assert.isTrue(!StringUtils.isEmpty(outputName),
                "Expected a non-empty name for the output file");

            this.url = url;
            this.checksum = checksum;
            this.outputDir = outputDir;
            this.outputName = Paths.get(outputName);
        }

        @Override
        public RepeatStatus execute(StepContribution contribution, ChunkContext chunkContext)
            throws Exception
        {
            StepExecution stepExecution = chunkContext.getStepContext().getStepExecution();
            ExecutionContext executionContext = stepExecution.getExecutionContext();

            // Check if already complete (a restart of a stopped yet complete execution)

            if (executionContext.containsKey("outputDir")) {
                Assert.state(outputDir.toString().equals(executionContext.getString("outputDir")),
                    "The tasklet is expected to write its outputDir into execution context");
                Assert.state(executionContext.containsKey("outputName"),
                    "The tasklet is expected to contain an `outputName` entry");
                Assert.state(outputName.toString().equals(executionContext.getString("outputName")),
                    "The tasklet is expected to write its outputName into execution context");
                return RepeatStatus.FINISHED;
            }

            // Create parent directory if needed

            try {
                Files.createDirectories(outputDir);
            } catch (FileAlreadyExistsException ex) {}

            Assert.state(Files.isDirectory(outputDir) && Files.isWritable(outputDir),
                "Expected outputDir to be a writable directory");

            // Download file

            final Path target = outputDir.resolve(outputName);
            try (InputStream in = url.openStream()) {
                Files.copy(in, target, StandardCopyOption.REPLACE_EXISTING);
            }

            // Verify download

            if (checksum != null) {
                String computedChecksum = null;
                try (InputStream s = Files.newInputStream(target, StandardOpenOption.READ)) {
                    computedChecksum = DigestUtils.sha256Hex(s);
                }
                if (!checksum.equalsIgnoreCase(computedChecksum))
                    throw new IllegalStateException("checksum verification has failed");
            }

            // Update execution context

            executionContext.put("outputDir", outputDir.toString());
            executionContext.put("outputName", outputName.toString());

            return RepeatStatus.FINISHED;
        }
    }

    @Bean("downloadFile.tasklet")
    @JobScope
    public DownloadFileTasklet tasklet(
        @Value("#{jobParameters['url']}") String url,
        @Value("#{jobParameters['outputName']}") String outputName,
        @Value("#{jobParameters['checksum']}") String checksum,
        @Value("#{jobExecution.jobInstance.id}") Long jobId)
        throws MalformedURLException
    {
        Path outputDir = dataDir.resolve(String.valueOf(jobId));
        return new DownloadFileTasklet(new URL(url), checksum, outputDir, outputName);
    }

    @Bean("downloadFile.step")
    public Step step(@Qualifier("downloadFile.tasklet") Tasklet downloadTasklet)
        throws Exception
    {
        return stepBuilderFactory.get("downloadFile")
            .tasklet(downloadTasklet)
            .listener(ExecutionContextPromotionListeners.fromKeys("outputDir", "outputName"))
            .build();
    }

    @Bean("downloadFile.flow")
    public Flow flow(@Qualifier("downloadFile.step") Step step)
    {
        return new FlowBuilder<Flow>("downloadFile").start(step).end();
    }
}
