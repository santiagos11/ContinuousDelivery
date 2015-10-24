package com.mycompany.cd;

import java.util.Random;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import javax.jms.Connection;
import javax.jms.Destination;
import javax.jms.Message;
import javax.jms.MessageConsumer;
import javax.jms.MessageListener;
import javax.jms.Session;
import javax.jms.TextMessage;

import org.apache.activemq.ActiveMQConnectionFactory;

public class ResponseConsumer implements Runnable, MessageListener {

	private final String connectionUri = "tcp://localhost:61616?jms.prefetchPolicy.queuePrefetch=1";
    private ActiveMQConnectionFactory connectionFactory;
    private Connection connection;
    private Session session;
    private Destination jobResponseQueue;
    private MessageConsumer consumer;
    
	private Random random = new Random();
	private final double MEAN = 2000.0;;
	private final double VARIANCE = 200.0;
	private final CountDownLatch done = new CountDownLatch(7);
	
	private String threadName;

	
	public ResponseConsumer(String threadName) {
		this.threadName = threadName;
	}
	
    public void onMessage(Message message) {
        try {
            TextMessage response = (TextMessage) message;
            System.out.println(threadName + ": " + response.getText());
            double time = random.nextGaussian() * VARIANCE + MEAN;
            TimeUnit.MILLISECONDS.sleep((long) time);
        } 
        catch (Exception e) {
        	System.out.println("Caught an exception: " + e.getMessage());
        }
        done.countDown();
    }
	
    private void before() throws Exception {
        connectionFactory = new ActiveMQConnectionFactory(connectionUri);
        connection = connectionFactory.createConnection();
        session = connection.createSession(false, Session.AUTO_ACKNOWLEDGE);
        jobResponseQueue = session.createQueue("Outbound");
        consumer = session.createConsumer(jobResponseQueue);
        consumer.setMessageListener(this);
        connection.start();
    }
    
    private void after() throws Exception {
        if (connection != null) {
            connection.close();
        }
    }
    
	public void run() {
        System.out.println("Starting " + threadName);
		try {
			before();
			done.await(1, TimeUnit.MINUTES);
			after();
		}
		catch (Exception e) {
			System.out.println("Caught an exception: " + e.getMessage());
		}
        System.out.println("Finish running " + threadName);
	}
    
}
