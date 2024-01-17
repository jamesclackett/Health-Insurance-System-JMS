package service.broker;

import java.util.HashMap;
import java.util.LinkedList;

import org.apache.activemq.ActiveMQConnectionFactory;
import service.core.ClientInfo;
import service.core.Quotation;
import service.message.ClientMessage;
import service.message.OfferMessage;
import service.message.QuotationMessage;
;import javax.jms.*;

public class LocalBrokerService {
	private Session session;
	private MessageConsumer consumerApplications, consumerQuotations;
	private MessageProducer producerOffers;
	private final HashMap<Long, OfferMessage> offerMessagesMap = new HashMap<>();

	public LocalBrokerService(){
		initializeMessagingService();
	}

	private void initializeMessagingService(){
		try {
			ConnectionFactory factory = new ActiveMQConnectionFactory("failover://tcp://activemq:61616");
			Connection connection = factory.createConnection();
			connection.setClientID("broker");

			session = connection.createSession(false, Session.CLIENT_ACKNOWLEDGE);
			setupSession();
			connection.start();

			initiateQuotationListener();
			initiateApplicationsListener();

		} catch (JMSException e){
			System.out.println(e.getLocalizedMessage());
		}
	}

	private void setupSession(){
		try {
			Queue queueQuotes = session.createQueue("QUOTATIONS");
			Queue queueOffers = session.createQueue("OFFERS");
			Topic topicApps = session.createTopic("APPLICATIONS");
			consumerApplications = session.createConsumer(topicApps);
			consumerQuotations = session.createConsumer(queueQuotes);
			producerOffers = session.createProducer(queueOffers);
		} catch (JMSException e){
			System.out.println(e.getLocalizedMessage());
		}
	}

	private void initiateQuotationListener(){
		try {
			// This listens to the Applications Topic for our 6 ClientMessage(s)
			consumerApplications.setMessageListener(message -> {
				try {
					ClientMessage request = (ClientMessage)
							((ObjectMessage) message).getObject();
					// Get ClientMessage info
					long token = request.getToken();
					ClientInfo info = request.getInfo();
					// Add new entry to OfferMessage Map
					OfferMessage offerMessage = new OfferMessage(info, new LinkedList<>());
					offerMessagesMap.put(token, offerMessage);

					// Wait 3 seconds for all quotations to be received.
					// Send the OfferMessage to the Offers queue
					new Thread(() -> {
						try {
							Thread.sleep(3000);
							Message response = session.createObjectMessage(offerMessagesMap.get(token));
							producerOffers.send(response);
							message.acknowledge();
						} catch (InterruptedException | JMSException e) {
							System.out.println(e.getLocalizedMessage());
						}
					}).start();

				} catch (JMSException e) {
					System.out.println(e.getLocalizedMessage());
					;
				}
			});
		} catch (JMSException e){
			System.out.println(e.getLocalizedMessage());
		}

	}

	private void initiateApplicationsListener() {
		try {
			// Listens to the Quotations Queue for QuotationMessage(s)
			consumerQuotations.setMessageListener(message -> {
				try {
					QuotationMessage request = (QuotationMessage)
							((ObjectMessage) message).getObject();
					// Get QuotationMessage info
					long token = request.getToken();
					Quotation quotation = request.getQuotation();

					// Match QuotationMessage to the right client and update offerMessageMap
					if (offerMessagesMap.containsKey(token)) {
						ClientInfo info = offerMessagesMap.get(token).getInfo();
						LinkedList<Quotation> quotations = offerMessagesMap.get(token).getQuotations();
						quotations.add(quotation);
						offerMessagesMap.put(token, new OfferMessage(info, quotations));
					}
					message.acknowledge();

				} catch (JMSException e) {
					throw new RuntimeException(e);
				}
			});

		} catch (JMSException e) {
			System.out.println(e.getLocalizedMessage());
		}
	}
}
