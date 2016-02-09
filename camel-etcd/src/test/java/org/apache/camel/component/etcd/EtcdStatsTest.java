package org.apache.camel.component.etcd;

import mousio.etcd4j.responses.EtcdLeaderStatsResponse;
import mousio.etcd4j.responses.EtcdSelfStatsResponse;
import mousio.etcd4j.responses.EtcdStoreStatsResponse;
import org.apache.camel.Exchange;
import org.apache.camel.Predicate;
import org.apache.camel.builder.RouteBuilder;
import org.apache.camel.component.mock.MockEndpoint;
import org.apache.camel.test.junit4.CamelTestSupport;
import org.junit.Ignore;
import org.junit.Test;

@Ignore("Etcd must be started manually")
public class EtcdStatsTest extends CamelTestSupport {

    @Test
    public void testStats() throws Exception {
        testStatsConsumer("mock:stats-leader-consumer", EtcdConstants.ETCD_LEADER_STATS_PATH, EtcdLeaderStatsResponse.class);
        testStatsConsumer("mock:stats-self-consumer"  , EtcdConstants.ETCD_SELF_STATS_PATH  , EtcdSelfStatsResponse.class);
        testStatsConsumer("mock:stats-store-consumer" , EtcdConstants.ETCD_STORE_STATS_PATH , EtcdStoreStatsResponse.class);

        testStatsProducer("direct:stats-leader", "mock:stats-leader-producer", EtcdConstants.ETCD_LEADER_STATS_PATH, EtcdLeaderStatsResponse.class);
        testStatsProducer("direct:stats-self"  , "mock:stats-self-producer"  , EtcdConstants.ETCD_SELF_STATS_PATH  , EtcdSelfStatsResponse.class);
        testStatsProducer("direct:stats-store" , "mock:stats-store-producer" , EtcdConstants.ETCD_STORE_STATS_PATH , EtcdStoreStatsResponse.class);
    }

    protected void testStatsConsumer(String mockEnpoint, String expectedPath, final Class<?> expectedType) throws Exception {
        MockEndpoint mock = getMockEndpoint(mockEnpoint);
        mock.expectedMinimumMessageCount(1);
        mock.expectedHeaderReceived(EtcdConstants.ETCD_ACTION_PATH, expectedPath);
        mock.expectedMessagesMatches(new Predicate() {
            @Override
            public boolean matches(Exchange exchange) {
                return exchange.getIn().getBody().getClass() == expectedType;
            }
        });

        assertMockEndpointsSatisfied();
    }

    protected void testStatsProducer(String producerEnpoint, String mockEnpoint, String expectedPath, final Class<?> expectedType) throws Exception {
        sendBody(producerEnpoint, "");

        testStatsConsumer(mockEnpoint, expectedPath, expectedType);
    }

    @Override
    protected RouteBuilder createRouteBuilder() throws Exception {
        return new RouteBuilder() {
            public void configure() {
                // CONSUMER
                from("etcd://stats/leader?consumer.delay=50&consumer.initialDelay=0")
                    .to("mock:stats-leader-consumer");
                from("etcd://stats/self?consumer.delay=50&consumer.initialDelay=0")
                    .to("mock:stats-self-consumer");
                from("etcd://stats/store?consumer.delay=50&consumer.initialDelay=0")
                    .to("mock:stats-store-consumer");

                // PRODUCER
                from("direct:stats-leader")
                    .to("etcd://stats/leader")
                        .to("mock:stats-leader-producer");
                from("direct:stats-self")
                    .to("etcd://stats/self")
                        .to("mock:stats-self-producer");
                from("direct:stats-store")
                    .to("etcd://stats/store")
                        .to("mock:stats-store-producer");
            }
        };
    }
}
