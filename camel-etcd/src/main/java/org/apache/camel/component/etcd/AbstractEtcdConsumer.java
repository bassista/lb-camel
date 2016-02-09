package org.apache.camel.component.etcd;

import org.apache.camel.Processor;
import org.apache.camel.impl.DefaultConsumer;

/**
 * The etcd consumer.
 */
public abstract class AbstractEtcdConsumer extends DefaultConsumer {
    private final EtcdConfiguration etcdConfiguration;
    private final EtcdActionNamespace etcdActionNamespace;
    private final String path;

    protected AbstractEtcdConsumer(AbstractEtcdEndpoint etcdEndpoint, Processor processor, EtcdConfiguration etcdConfiguration, EtcdActionNamespace etcdActionNamespace, String path) {
        super(etcdEndpoint, processor);

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
