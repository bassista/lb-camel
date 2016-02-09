package org.apache.camel.component.etcd;

import org.apache.camel.impl.DefaultProducer;

/**
 * The etcd producer.
 */
public abstract class AbstractEtcdProducer extends DefaultProducer {
    private final EtcdConfiguration etcdConfiguration;
    private final EtcdActionNamespace etcdActionNamespace;
    private final String path;

    protected AbstractEtcdProducer(AbstractEtcdEndpoint etcdEndpoint, EtcdConfiguration etcdConfiguration, EtcdActionNamespace etcdActionNamespace, String path) {
        super(etcdEndpoint);

        this.etcdConfiguration = etcdConfiguration;
        this.etcdActionNamespace = etcdActionNamespace;
        this.path = path;
    }

    protected EtcdConfiguration getEtcdConfiguration() {
        return etcdConfiguration;
    }

    protected EtcdActionNamespace getEtcdActionNamespace() {
        return etcdActionNamespace;
    }

    protected String getPath() {
        return this.path;
    }
}
