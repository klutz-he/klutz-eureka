package com.netflix.discovery.shared.transport.jersey2;

import com.netflix.appinfo.EurekaClientIdentity;
import com.netflix.appinfo.InstanceInfo;
import com.netflix.discovery.EurekaClientConfig;
import com.netflix.discovery.shared.resolver.EurekaEndpoint;
import com.netflix.discovery.shared.transport.EurekaHttpClient;
import com.netflix.discovery.shared.transport.TransportClientFactory;
import com.netflix.discovery.shared.transport.decorator.MetricsCollectingEurekaHttpClient;
import com.netflix.discovery.shared.transport.jersey.EurekaJerseyClient;
import com.netflix.discovery.shared.transport.jersey.TransportClientFactories;

import javax.ws.rs.client.ClientRequestFilter;
import java.util.Collection;

public class Jersey2TransportClientFactories implements TransportClientFactories<ClientRequestFilter> {
    
    private static final Jersey2TransportClientFactories INSTANCE = new Jersey2TransportClientFactories();
    
    public static Jersey2TransportClientFactories getInstance() {
        return INSTANCE;
    }

    @Override
    public TransportClientFactory newTransportClientFactory(final EurekaClientConfig clientConfig,
                                                                   final Collection<ClientRequestFilter> additionalFilters,
                                                                   final InstanceInfo myInstanceInfo) {
        final TransportClientFactory jerseyFactory = Jersey2ApplicationClientFactory.create(
                clientConfig,
                additionalFilters,
                myInstanceInfo,
                new EurekaClientIdentity(myInstanceInfo.getIPAddr(), "Jersey2DefaultClient")
        );
        final TransportClientFactory metricsFactory = MetricsCollectingEurekaHttpClient.createFactory(jerseyFactory);

        return new TransportClientFactory() {
            @Override
            public EurekaHttpClient newClient(EurekaEndpoint serviceUrl) {
                return metricsFactory.newClient(serviceUrl);
            }

            @Override
            public void shutdown() {
                metricsFactory.shutdown();
                jerseyFactory.shutdown();
            }
        };
    }

    @Override
    public TransportClientFactory newTransportClientFactory(Collection<ClientRequestFilter> additionalFilters,
            EurekaJerseyClient providedJerseyClient) {
        throw new UnsupportedOperationException();
    }

}