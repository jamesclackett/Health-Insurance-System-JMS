import org.apache.activemq.ActiveMQConnectionFactory;
import service.auldfellas.AFQService;
import service.core.Quotation;
import service.message.ClientMessage;
import service.message.QuotationMessage;

import javax.jms.*;


public class Main {
    private static AFQService service = new AFQService();

    public static void main(String[] args) {
        try {
            ConnectionFactory factory =
                    new ActiveMQConnectionFactory("failover://tcp://activemq:61616");
            Connection connection;
            connection = factory.createConnection();
            connection.setClientID("auldfellas");
            Session session = connection.createSession(false, Session.CLIENT_ACKNOWLEDGE);

            Queue queue = session.createQueue("QUOTATIONS");
            Topic topic = session.createTopic("APPLICATIONS");
            MessageConsumer consumer = session.createConsumer(topic);
            MessageProducer producer = session.createProducer(queue);
            connection.start();

            consumer.setMessageListener(message -> {
                try {
                    ClientMessage request = (ClientMessage)
                            ((ObjectMessage) message).getObject();
                    Quotation quotation = service.generateQuotation(request.getInfo());
                    Message response =
                            session.createObjectMessage(
                                    new QuotationMessage(request.getToken(), quotation));
                    producer.send(response);
                    message.acknowledge();
                } catch (JMSException e) {
                    e.printStackTrace();
                }
            });
        } catch (JMSException e) {
            throw new RuntimeException(e);
        }

    }
}
