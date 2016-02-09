package org.apache.camel.component.etcd;

import java.util.Map;

import org.apache.camel.CamelContext;
import org.apache.camel.Endpoint;
import org.apache.camel.component.etcd.internal.EtcdStatsEndpoint;
import org.apache.camel.impl.UriEndpointComponent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Represents the component that manages {@link AbstractEtcdEndpoint}.
 */
public class EtcdComponent extends UriEndpointComponent {
    private static final Logger LOGGER = LoggerFactory.getLogger(EtcdComponent.class);
    
    public EtcdComponent() {
        super(AbstractEtcdEndpoint.class);
    }

    public EtcdComponent(CamelContext context) {
        super(context, AbstractEtcdEndpoint.class);
    }

    @Override
    protected Endpoint createEndpoint(String uri, String remaining, Map<String, Object> parameters) throws Exception {
        EtcdConfiguration configuration = new EtcdConfiguration();
        setProperties(configuration, parameters);

        String[] args = remaining.split("/");
        EtcdActionNamespace actionNamespace = EtcdActionNamespace.fromName(args[0]);

        LOGGER.debug("NameSpace: {}, path={}", args[0], remaining);

        switch(actionNamespace) {
            case STATS:
                return new EtcdStatsEndpoint(
                    uri,
                    this,
                    configuration,
                    actionNamespace,
                    remaining
                );

            default:
                throw new IllegalStateException("No endpoint for " + args[0]);
        }
    }
}
