package com.mycompany.cd;

import java.util.Random;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import javax.jms.Connection;
import javax.jms.Destination;
import javax.jms.Message;
import javax.jms.MessageConsumer;
import javax.jms.Session;
import javax.jms.TextMessage;

import org.apache.activemq.ActiveMQConnectionFactory;

public class ResponseConsumer implements Runnable {

	private final String connectionUri = "tcp://localhost:61616?jms.prefetchPolicy.queuePrefetch=1";
    private ActiveMQConnectionFactory connectionFactory;
    private Connection connection;
    private Session session;
    private Destination jobResponseQueue;
    private MessageConsumer consumer;
    
	private Random random = new Random();
	private final double MEAN = 1000.0;;
	private final double VARIANCE = 100.0;
	private final CountDownLatch done;
	
	private String threadName;

	
	public ResponseConsumer(String threadName, CountDownLatch done) {
		this.threadName = threadName;
		this.done = done;
	}
	
    private void before() throws Exception {
        connectionFactory = new ActiveMQConnectionFactory(connectionUri);
        connection = connectionFactory.createConnection();
        session = connection.createSession(false, Session.AUTO_ACKNOWLEDGE);
        jobResponseQueue = session.createQueue("Outbound");
        consumer = session.createConsumer(jobResponseQueue);
        connection.start();
    }
    
    private void execute() throws Exception {
    	while (!Thread.currentThread().isInterrupted()) {
    		Message response = consumer.receive();
    		if (response instanceof TextMessage) {
    			//System.out.println(threadName + ": " + ((TextMessage) response).getText() + response.getIntProperty("JobId"));
                done.countDown();
    			double time = random.nextGaussian() * VARIANCE + MEAN;
                TimeUnit.MILLISECONDS.sleep((long) time);
    		}
    	}
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
			execute();
		}
		catch (Exception e) {
			System.out.println("Caught an exception: " + e.getMessage() + " in " + threadName);
		}
		finally {
		 	try {
				after();
			} 
		 	catch (Exception e) {
				System.out.println("Caught an exception: " + e.getMessage());
			}
			System.out.println("Finish running " + threadName);	
		}
	}
    
}
