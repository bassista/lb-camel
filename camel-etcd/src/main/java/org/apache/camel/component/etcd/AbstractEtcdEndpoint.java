package org.apache.camel.component.etcd;

import java.net.URI;

import mousio.etcd4j.EtcdClient;
import mousio.etcd4j.EtcdSecurityContext;
import org.apache.camel.impl.DefaultEndpoint;
import org.apache.camel.spi.UriEndpoint;
import org.apache.camel.spi.UriPath;
import org.apache.camel.util.ObjectHelper;

/**
 * Represents a etcd endpoint.
 */
@UriEndpoint(scheme = "etcd", title = "etcd", syntax="etcd:actionDomain", consumerClass = AbstractEtcdConsumer.class, label = "etcd")
public abstract class AbstractEtcdEndpoint<C extends EtcdConfiguration> extends DefaultEndpoint {

    @UriPath(description = "The namespace")
    private final EtcdActionNamespace etcdActionNamespace;
    private final C etcdConfiguration;
    private final String path;

    private EtcdClient etcdClient;

    protected AbstractEtcdEndpoint(String uri, EtcdComponent component, C etcdConfiguration, EtcdActionNamespace etcdActionNamespace, String path) {
        super(uri, component);

        this.etcdConfiguration = etcdConfiguration;
        this.etcdActionNamespace = etcdActionNamespace;
        this.etcdClient = null;
        this.path = path;
    }

    @Override
    public boolean isSingleton() {
        return true;
    }

    @Override
    protected void doStart() throws Exception {
        URI[] uris;
        if (etcdConfiguration.hasUris()) {
            uris = new URI[etcdConfiguration.getUris().size()];

            int i = 0;
            for (String uri : etcdConfiguration.getUris()) {
                uris[++i] = URI.create(getCamelContext().resolvePropertyPlaceholders(uri));
            }
        } else {
            uris = new URI[] {
                URI.create("http://localhost:4001")
            };
        }

        etcdClient = new EtcdClient(
            new EtcdSecurityContext(
                etcdConfiguration.createSslContext(),
                etcdConfiguration.getUserName(),
                etcdConfiguration.getPassword()),
            uris
        );

        super.doStart();
    }
    @Override
    protected void doStop() throws Exception {
        if (etcdClient != null) {
            etcdClient.close();
        }

        super.doStop();
    }

    public C getConfiguration() {
        return this.etcdConfiguration;
    }

    public EtcdActionNamespace getActionNamespace() {
        return this.etcdActionNamespace;
    }

    public EtcdClient getClient() {
        return this.etcdClient;
    }

    public String getPath() {
        return this.path;
    }

    public String getRemainingPath(String defaultPath) {
        String path = getPath().substring(etcdActionNamespace.path().length());
        if (ObjectHelper.isEmpty(path)) {
            path = defaultPath;
        }

        return path;
    }
}
