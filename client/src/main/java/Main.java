import org.apache.activemq.ActiveMQConnectionFactory;
import service.core.ClientInfo;
import service.core.Quotation;
import service.message.ClientMessage;
import service.message.OfferMessage;
import service.message.QuotationMessage;

import javax.jms.*;
import java.text.NumberFormat;
import java.util.HashMap;
import java.util.LinkedList;

public class Main {

	public static void main(String[] args) {
		try {
			ConnectionFactory factory =
					new ActiveMQConnectionFactory("failover://tcp://localhost:61616");
			Connection connection = factory.createConnection();
			connection.setClientID("client");
			Session session = connection.createSession(false, Session.CLIENT_ACKNOWLEDGE);

			Topic topicApps = session.createTopic("APPLICATIONS");
			Queue queueOffers = session.createQueue("OFFERS");
			MessageProducer producer = session.createProducer(topicApps);
			MessageConsumer consumer = session.createConsumer(queueOffers);
			connection.start();

			consumer.setMessageListener(message -> {
				try {
					OfferMessage request = (OfferMessage) ((ObjectMessage) message).getObject();
					displayProfile(request.getInfo());
					LinkedList<Quotation> quotes = request.getQuotations();

					for (Quotation q : quotes) displayQuotation(q);

					message.acknowledge();
				} catch (JMSException e) {
					throw new RuntimeException(e);
				}
			});

			long token = 0; //This could be replaced with some hash based on age/name etc.
			for (ClientInfo info : clients){
				token++;
				producer.send(session.createObjectMessage(new ClientMessage(token, info)));
			}

		} catch (JMSException e){
			System.out.println(e.getLocalizedMessage());
		}
	}
	
	/**
	 * Display the client info nicely.
	 * 
	 * @param info
	 */
	public static void displayProfile(ClientInfo info) {
		System.out.println("|=================================================================================================================|");
		System.out.println("|                                     |                                     |                                     |");
		System.out.println(
				"| Name: " + String.format("%1$-29s", info.name) + 
				" | Gender: " + String.format("%1$-27s", (info.gender==ClientInfo.MALE?"Male":"Female")) +
				" | Age: " + String.format("%1$-30s", info.age)+" |");
		System.out.println(
				"| Weight/Height: " + String.format("%1$-20s", info.weight+"kg/"+info.height+"m") + 
				" | Smoker: " + String.format("%1$-27s", info.smoker?"YES":"NO") +
				" | Medical Problems: " + String.format("%1$-17s", info.medicalIssues?"YES":"NO")+" |");
		System.out.println("|                                     |                                     |                                     |");
		System.out.println("|=================================================================================================================|");
	}

	/**
	 * Display a quotation nicely - note that the assumption is that the quotation will follow
	 * immediately after the profile (so the top of the quotation box is missing).
	 * 
	 * @param quotation
	 */
	public static void displayQuotation(Quotation quotation) {
		System.out.println(
				"| Company: " + String.format("%1$-26s", quotation.company) + 
				" | Reference: " + String.format("%1$-24s", quotation.reference) +
				" | Price: " + String.format("%1$-28s", NumberFormat.getCurrencyInstance().format(quotation.price))+" |");
		System.out.println("|=================================================================================================================|");
	}
	
	/**
	 * Test Data
	 */
	public static final ClientInfo[] clients = {
		new ClientInfo("Niki Collier", ClientInfo.FEMALE, 49, 1.5494, 80, false, false),
		new ClientInfo("Old Geeza", ClientInfo.MALE, 65, 1.6, 100, true, true),
		new ClientInfo("Hannah Montana", ClientInfo.FEMALE, 21, 1.78, 65, false, false),
		new ClientInfo("Rem Collier", ClientInfo.MALE, 49, 1.8, 120, false, true),
		new ClientInfo("Jim Quinn", ClientInfo.MALE, 55, 1.9, 75, true, false),
		new ClientInfo("Donald Duck", ClientInfo.MALE, 35, 0.45, 1.6, false, false)
	};
}
