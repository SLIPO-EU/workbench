package eu.slipo.workbench.rpc.jobs.tasklet.docker;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.util.Assert;

import com.spotify.docker.client.messages.ContainerConfig;
import com.spotify.docker.client.messages.ContainerConfig.Healthcheck;
import com.spotify.docker.client.messages.HostConfig;
import com.spotify.docker.client.messages.PortBinding;

/**
 * A fluent configurer for a docker container.
 */
public class ContainerConfigurer
{
    public static final String DEFAULT_IMAGE = "busybox";

    /**
     * The builder for the entire container configuration
     */
    private final ContainerConfig.Builder containerConfigBuilder = ContainerConfig.builder();

    /**
     * The builder for the parts of the configuration that are also relevant to the
     * host. The product of this builder is a part of a (top-level) container configuration.
     */
    private final HostConfig.Builder hostConfigBuilder = HostConfig.builder();

    private final List<String> links = new ArrayList<>();

    private final Map<String,String> env = new LinkedHashMap<>();

    private final Map<String,List<PortBinding>> ports = new LinkedHashMap<>();

    public ContainerConfigurer()
    {
        containerConfigBuilder.image(DEFAULT_IMAGE);
    }

    /**
     * Build a configuration ({@link ContainerConfig}) by assembling pieces collected so far.
     */
    public ContainerConfig buildConfiguration()
    {
        final HostConfig hostConfig = hostConfigBuilder
            .links(links)
            .portBindings(ports)
            .build();

        final List<String> envDump = env.entrySet().stream()
            .map(e -> e.getKey() + "=" + e.getValue())
            .collect(Collectors.toList());

        containerConfigBuilder
            .hostConfig(hostConfig)
            .env(envDump)
            .exposedPorts(ports.keySet());

        return containerConfigBuilder.build();
    }

    /**
     * Publish (i.e expose) a container's port to the host.
     *
     * @param port A port specification on the container e.g. "8080/tcp" or "8080".
     * @param hostAddress The host address to bind to. If empty, it will be interpreted as
     *   all addresses (on all network interfaces)
     * @param hostPort The host port to bind to. If empty, a random ephemeral port will be
     *   assigned to it (by Docker daemon).
     * @return
     */
    public ContainerConfigurer publish(String port, String hostAddress, String hostPort)
    {
        Assert.notNull(port,
            "The port specification must be non-empty");
        Assert.isTrue(port.matches("[1-9][0-9]{1,4}([/](tcp|udp))?"),
            "The port specification is malformed (specify as 1234/(tcp|udp) or 1234)");

        PortBinding bind = PortBinding.of(
            hostAddress == null? "" : hostAddress,
            hostPort == null? "" : hostPort);
        ports.put(port, Collections.singletonList(bind));
        return this;
    }

    public ContainerConfigurer publish(String port)
    {
        return publish(port, null, null);
    }

    public ContainerConfigurer publish(int port, String hostAddress, int hostPort)
    {
        return publish(String.valueOf(port), hostAddress, String.valueOf(hostPort));
    }

    public ContainerConfigurer publish(int port)
    {
        return publish(String.valueOf(port), null, null);
    }

    /**
     * Set the docker image to be used.
     */
    public ContainerConfigurer image(String imageName)
    {
        Assert.notNull(imageName, "The image name must be non-null");
        containerConfigBuilder.image(imageName);
        return this;
    }

    /**
     * Specify a name for the container.
     *
     * @param containerName
     * @return
     */
    public ContainerConfigurer name(String containerName)
    {
        Assert.notNull(containerName, "The container name (if given) must be non-null");
        containerConfigBuilder.hostname(containerName);
        return this;
    }

    /**
     * Set an environment variable to be passed to the container.
     *
     * @param name
     * @param value
     */
    public ContainerConfigurer env(String name, String value)
    {
        Assert.notNull(name, "An environment variable should have a non-null name");
        Assert.isTrue(name.matches("[a-zA-Z][-_0-9a-zA-Z]*"), "The name is invalid");
        env.put(name, value);
        return this;
    }

    public ContainerConfigurer env(String name, Object value)
    {
        Assert.notNull(value, "A value is required");
        return this.env(name, value.toString());
    }

    /**
     * Set command to be executed inside container. An empty command does nothing, and the
     * default command (specified at build-time) will be used.
     *
     * @param command The command as an executable followed by a list of arguments.
     */
    public ContainerConfigurer command(String ...command)
    {
        containerConfigBuilder.cmd(command);
        return this;
    }

    /**
     * Set command that periodically checks health of the container (HEALTHCHECK).
     *
     * @see https://docs.docker.com/engine/reference/builder/#healthcheck
     * @see https://docs.docker.com/engine/api/v1.25/#operation/ContainerCreate
     *
     * @param testCommand The command to be executed (directly via exec) as an array.
     * @param interval The check interval (in milliseconds)
     * @param startPeriod An amount of time (in milliseconds) needed for the container to
     *   bootstrap (only after this period, a health check is meaningful).
     * @param retries The number of consecutive failures that mark a container as unhealthy
     */
    public ContainerConfigurer healthcheck(List<String> testCommand, long interval, long startPeriod, int retries)
    {
        Assert.notEmpty(testCommand, "Expected a non-empty test command");
        Assert.isTrue(interval > 0,
            "The interval is expected a positive number of milliseconds");
        Assert.isTrue(startPeriod > 0,
            "The startPeriod is expected a positive number of milliseconds");
        Assert.isTrue(retries > 0, "The number of retries is a positive integer");

        // Convert time durations to nanoseconds (as expected by docker API)
        final long NANOS_PER_MILLISECOND = 1000L * 1000L;
        final long TIMEOUT = 0L; // inherit timeout from image

        // Describe the test as a CMD (as expected by docker API)
        LinkedList<String> test = new LinkedList<>(testCommand);
        test.addFirst("CMD");

        Healthcheck h = Healthcheck.create(
            test,
            interval * NANOS_PER_MILLISECOND,
            TIMEOUT,
            retries,
            startPeriod * NANOS_PER_MILLISECOND);
        containerConfigBuilder.healthcheck(h);

        return this;
    }

    /**
     * Add a link to another container.
     *
     * @param containerName The name (or id) of the other container we should link to
     * @param name The container-local hostname for a linked container (i.e. as it will
     *   be resolved from inside our container)
     */
    public ContainerConfigurer link(String containerName, String name)
    {
        Assert.notNull(containerName, "The name of the container must be non-null");
        String spec = containerName + (name == null? "" : (":" + name));
        links.add(spec);
        return this;
    }

    public ContainerConfigurer link(String containerName)
    {
        return link(containerName, null);
    }

    /**
     * Add a volume (bind-mount) into the container.
     *
     * @param path The host-local path (either a file or a directory). It should be given as an
     *   absolute path and must correspond to an existing path on the host (to be bind-mounted into
     *   the target container).
     * @param containerPath The container-local path (either a file or a directory). It should be
     *   given as an absolute path.
     * @param readonly A flag that indicates if mount should be read-only
     */
    public ContainerConfigurer volume(Path path, Path containerPath, boolean readonly)
    {
        Assert.notNull(containerPath, "The container-local path must be non-null");
        Assert.isTrue(containerPath.isAbsolute(),
            "The container-local path must be given as an absolute path");
        Assert.notNull(path, "The host-local path must be non-null");
        Assert.isTrue(path.isAbsolute(),
            "The host-local path must be given as an absolute path");

        HostConfig.Bind bind = HostConfig.Bind
            .from(path.toString())
            .to(containerPath.toString())
            .readOnly(readonly)
            .build();

        hostConfigBuilder.appendBinds(bind);

        return this;
    }

    public ContainerConfigurer volume(Path path, Path containerPath)
    {
        return volume(path, containerPath, false);
    }

    public ContainerConfigurer volume(String path, String containerPath, boolean readonly)
    {
        return volume(Paths.get(path), Paths.get(containerPath), readonly);
    }

    public ContainerConfigurer volume(String path, String containerPath)
    {
        return volume(path, containerPath, false);
    }

    /**
     * Add a volume into the container. The volume is bind-mounted to a directory that
     * Docker will automatically create (under its managed directories).
     *
     * @param containerPath The container-local directory. This should be given as an
     *   absolute path.
     */
    public ContainerConfigurer volume(Path containerPath)
    {
        Assert.notNull(containerPath, "The container-local path must be non-null");
        Assert.isTrue(containerPath.isAbsolute(),
            "The container-local path must be given as an absolute path");

        containerConfigBuilder.addVolume(containerPath.toString());

        return this;
    }

    public ContainerConfigurer volume(String containerPath)
    {
        return volume(Paths.get(containerPath));
    }

    /**
     * Set an upper limit for memory available to the container. This limit, if given, implies
     * that an equally-sized amount of swap memory will also be available (so totally available
     * memory-like storage will reach <tt>2 * sizeBytes</tt>).
     *
     * @param sizeBytes The size to make available (expressed in bytes)
     */
    public ContainerConfigurer memory(long sizeBytes)
    {
        Assert.isTrue(sizeBytes > 0, "The size must be a positive number");
        hostConfigBuilder.memory(sizeBytes);
        return this;
    }

    /**
     * Set an upper limit for the total amount of memory+swap available to the container. If
     * memory is also constrained ({@link ContainerConfigurer#memory(long)}), then this limit must
     * be greater than the one given for memory.
     *
     * @param sizeBytes The size to make available (expressed in bytes)
     * @return
     */
    public ContainerConfigurer memoryAndSwap(long sizeBytes)
    {
        Assert.isTrue(sizeBytes > 0, "The size must be a positive number");
        hostConfigBuilder.memorySwap(sizeBytes);
        return this;
    }

    /**
     * Set the CPU share available to the container as a weighting factor.
     *
     * @param weightFactor A positive factor (default will be <tt>1.0</tt>) to be applied
     */
    public ContainerConfigurer cpu(float weightFactor)
    {
        Assert.isTrue(weightFactor > 0, "The weight must be a positive number");
        // The unit for Docker is 1024, so scale factor to what is expected
        long weight = Float.valueOf(weightFactor * 1024).longValue();
        hostConfigBuilder.cpuShares(weight);
        return this;
    }
}
