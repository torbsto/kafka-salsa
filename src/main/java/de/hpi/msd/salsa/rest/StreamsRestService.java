package de.hpi.msd.salsa.rest;

import org.apache.kafka.streams.state.HostInfo;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.server.ServerConnector;
import org.eclipse.jetty.servlet.ServletContextHandler;
import org.eclipse.jetty.servlet.ServletHolder;
import org.glassfish.jersey.jackson.JacksonFeature;
import org.glassfish.jersey.server.ResourceConfig;
import org.glassfish.jersey.servlet.ServletContainer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;
import java.net.SocketException;
import java.util.Arrays;
import java.util.List;

public class StreamsRestService {
    private final HostInfo hostInfo;
    protected final Client client = ClientBuilder.newBuilder().register(JacksonFeature.class).build();
    private Server server;
    private static final Logger log = LoggerFactory.getLogger(StreamsRestService.class);
    private List<Object> resources;

    public StreamsRestService(HostInfo hostInfo, Object... resources) {
        this.hostInfo = hostInfo;
        this.resources = Arrays.asList(resources);
    }

    public void start() throws Exception {
        final ServletContextHandler contextHandler = new ServletContextHandler(ServletContextHandler.SESSIONS);
        contextHandler.setContextPath("/");

        server = new Server();
        server.setHandler(contextHandler);

        final ResourceConfig config = new ResourceConfig();
        config.register(JacksonFeature.class);
        resources.forEach(config::register);

        final ServletContainer container = new ServletContainer(config);
        final ServletHolder holder = new ServletHolder(container);
        contextHandler.addServlet(holder, "/*");

        final ServerConnector connector = new ServerConnector(server);
        connector.setHost(hostInfo.host());
        connector.setPort(hostInfo.port());
        server.addConnector(connector);

        contextHandler.start();
        log.info("Server started");

        try {
            server.start();
        } catch (SocketException e) {
            log.error("Unavailable: {} : {}", hostInfo.host(), hostInfo.port());
            throw new Exception(e.toString());
        }
    }

    public void stop() throws Exception {
        if (server != null) {
            server.stop();
        }
    }
}
