package de.hpi.msd.salsa.rest;

import de.hpi.msd.salsa.EdgeToAdjacencyApp;
import de.hpi.msd.salsa.algorithm.Salsa;
import de.hpi.msd.salsa.index.KeyValueGraph;
import de.hpi.msd.salsa.serde.avro.AdjacencyList;
import org.apache.kafka.streams.KafkaStreams;
import org.apache.kafka.streams.state.HostInfo;
import org.apache.kafka.streams.state.QueryableStoreTypes;
import org.apache.kafka.streams.state.ReadOnlyKeyValueStore;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.server.ServerConnector;
import org.eclipse.jetty.servlet.ServletContextHandler;
import org.eclipse.jetty.servlet.ServletHolder;
import org.glassfish.jersey.jackson.JacksonFeature;
import org.glassfish.jersey.server.ResourceConfig;
import org.glassfish.jersey.servlet.ServletContainer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.ws.rs.*;
import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.core.MediaType;
import java.net.SocketException;
import java.util.List;
import java.util.Random;

@Path("/recommendation")
public class RecommendationRestService {
    private final KafkaStreams streams;
    private final HostInfo hostInfo;
    private final Client client = ClientBuilder.newBuilder().register(JacksonFeature.class).build();
    private Server server;
    private static final Logger log = LoggerFactory.getLogger(RecommendationRestService.class);

    public RecommendationRestService(final KafkaStreams streams, final HostInfo hostInfo) {
        this.streams = streams;
        this.hostInfo = hostInfo;
    }

    @GET
    @Path("/salsa/{userId}")
    @Produces(MediaType.APPLICATION_JSON)
    public List<Long> getRecommendationsForUser(@PathParam("userId") final long userId, @QueryParam("limit") final int limit) {
        final ReadOnlyKeyValueStore<Long, AdjacencyList> leftIndex = streams.store(EdgeToAdjacencyApp.LEFT_INDEX_NAME, QueryableStoreTypes.keyValueStore());
        final ReadOnlyKeyValueStore<Long, AdjacencyList> rightIndex = streams.store(EdgeToAdjacencyApp.RIGHT_INDEX_NAME, QueryableStoreTypes.keyValueStore());

        return new Salsa(new KeyValueGraph(leftIndex, rightIndex), new Random()).compute(userId, 1000, 100, 0.1, limit);
    }

    public void start() throws Exception {
        final ServletContextHandler contextHandler = new ServletContextHandler(ServletContextHandler.SESSIONS);
        contextHandler.setContextPath("/");

        server = new Server();
        server.setHandler(contextHandler);

        final ResourceConfig config = new ResourceConfig();
        config.register(this);
        config.register(JacksonFeature.class);

        final ServletContainer container = new ServletContainer(config);
        final ServletHolder holder = new ServletHolder(container);
        contextHandler.addServlet(holder, "/*");

        final ServerConnector connector = new ServerConnector(server);
        connector.setHost(hostInfo.host());
        connector.setPort(hostInfo.port());
        server.addConnector(connector);

        contextHandler.start();

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
