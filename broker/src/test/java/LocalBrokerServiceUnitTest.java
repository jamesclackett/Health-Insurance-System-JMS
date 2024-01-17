import org.apache.activemq.ActiveMQConnectionFactory;
import org.junit.Test;
import service.broker.LocalBrokerService;
import service.core.ClientInfo;
import service.message.ClientMessage;
import service.message.OfferMessage;

import javax.jms.*;

import static org.junit.Assert.assertEquals;

public class LocalBrokerServiceUnitTest {
    static {
        new LocalBrokerService();
    }

    @Test
    public void testService() throws Exception {
        // Note, these tests are designed to test the local environment.
        // To package a project for docker, please use the -Dmaven.test.skip option
        ConnectionFactory factory =
                new ActiveMQConnectionFactory("failover://tcp://localhost:61616");
        Connection connection = factory.createConnection();
        connection.setClientID("test");
        Session session =
                connection.createSession(false, Session.CLIENT_ACKNOWLEDGE);

        Queue queueOffers = session.createQueue("OFFERS");
        Topic topicApplications = session.createTopic("APPLICATIONS");

        MessageProducer producerApplications = session.createProducer(topicApplications);
        MessageConsumer consumerOffers = session.createConsumer(queueOffers);
        connection.start();

        producerApplications.send(
                session.createObjectMessage(
                        new ClientMessage(1L, new ClientInfo("Niki Collier",
                                ClientInfo.FEMALE, 49, 1.5494, 80, false,
                                false))));


        Message message = consumerOffers.receive();
        OfferMessage offersMessage =
                (OfferMessage) ((ObjectMessage) message).getObject();
        System.out.println("info: " + offersMessage.getInfo());
        System.out.println("quotations: " + offersMessage.getQuotations());
        message.acknowledge();
        assertEquals("Niki Collier", offersMessage.getInfo().name);
        // assertEquals(1, offersMessage.getQuotations().size());
        // This would be a good extra test. Requires a way of accessing auldfellas Main class.
    }
}
