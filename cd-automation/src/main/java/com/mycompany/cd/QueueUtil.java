package com.mycompany.cd;

import java.util.Enumeration;

import javax.jms.Connection;
import javax.jms.MapMessage;
import javax.jms.Message;
import javax.jms.MessageConsumer;
import javax.jms.MessageProducer;
import javax.jms.Queue;
import javax.jms.Session;

import org.apache.activemq.ActiveMQConnectionFactory;

public class QueueUtil {
	
	private final String connectionUri = "tcp://localhost:61616";
    private ActiveMQConnectionFactory connectionFactory;
    private Connection connection;
    private Session session;
    
    public void before() throws Exception {
        connectionFactory = new ActiveMQConnectionFactory(connectionUri);
        connection = connectionFactory.createConnection();
        connection.start();
        session = connection.createSession(false, Session.AUTO_ACKNOWLEDGE);
    }
    
    public int getSize() throws Exception {
    	Queue replyTo = session.createTemporaryQueue();
    	MessageConsumer consumer = session.createConsumer(replyTo);
    	 
    	Queue testQueue = session.createQueue("Outbound");
    	MessageProducer producer = session.createProducer(null);
    	 
    	String queueName = "ActiveMQ.Statistics.Destination." + testQueue.getQueueName();
    	Queue query = session.createQueue(queueName);
    	 
    	Message msg = session.createMessage();
    	 
    	producer.send(testQueue, msg);
    	msg.setJMSReplyTo(replyTo);
    	producer.send(query, msg);
    	MapMessage reply = (MapMessage) consumer.receive();
    	
    	return Integer.parseInt(reply.getObject("size").toString());
    	/*for (Enumeration e = reply.getMapNames();e.hasMoreElements();) {
    	    String name = e.nextElement().toString();
    	    System.err.println(name + "=" + reply.getObject(name));
    	}*/
    	//System.out.println(reply.getObject("size"));
    }
	
    public void after() throws Exception {
        if (connection != null) {
            connection.close();
        }
    }
    
    public static void main(String[] args) throws Exception {
		QueueUtil queueUtil = new QueueUtil();
		queueUtil.before();
    	System.out.println(queueUtil.getSize());
    	queueUtil.after();
    }

}
