package com.mycompany.cd;

import javax.jms.Connection;
import javax.jms.Destination;
import javax.jms.MessageProducer;
import javax.jms.Session;
import javax.jms.TextMessage;

import org.apache.activemq.ActiveMQConnectionFactory;

public class JobProducer implements Runnable {
	
	private final String connectionUri = "tcp://localhost:61616";
    private ActiveMQConnectionFactory connectionFactory;
    private Connection connection;
    private Session session;
    private Destination jobRequestQueue;
    
    private final int NUMBER_OF_JOBS = 1000;
	private String threadName;
	
	public JobProducer(String threadName) {
		this.threadName = threadName;
	}
	
    public void before() throws Exception {
        connectionFactory = new ActiveMQConnectionFactory(connectionUri);
        connection = connectionFactory.createConnection();
        session = connection.createSession(false, Session.AUTO_ACKNOWLEDGE);
        jobRequestQueue = session.createQueue("Inbound");
    }
    
    public void execute() throws Exception {
        MessageProducer producer = session.createProducer(jobRequestQueue);

        for (int i = 0; i < NUMBER_OF_JOBS; ++i) {
            TextMessage message = session.createTextMessage("Job number: " + i);
            message.setIntProperty("JobID", i);
            producer.send(message);
        }

        producer.close();
    }
	
    public void after() throws Exception {
        if (connection != null) {
            connection.close();
        }
    }    

	public void run() {
        System.out.println("Starting " + threadName);
		try {
			before();
			execute();
			after();
		}
		catch (Exception e) {
			System.out.println("Caught an exception: " + e.getMessage());
		}
        System.out.println("Finish running " + threadName);
	}

}
