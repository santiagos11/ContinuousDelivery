package com.mycompany.cd;

import java.util.ArrayList;

public class LoadTest {
	
	public static int previousQueueSize;
	public static QueueUtil queueUtil;

	public static void main(String[] args) throws Exception {
		queueUtil = new QueueUtil();
		queueUtil.before();
		previousQueueSize = queueUtil.getSize(); 
		
		Thread jobProducer = new Thread(new JobProducer("Job Producer"));
		jobProducer.start();

		int i = 1;
		ArrayList<Thread> threads = new ArrayList<Thread>();
		threads.add(new Thread(new ResponseConsumer("Response Consumer " + i++)));
		threads.get(threads.size() - 1).start();
		
		while (!foundOptimal()) {
			threads.add(new Thread(new ResponseConsumer("Response Consumer " + i++)));
			threads.get(threads.size() - 1).start();	
		}
		
		queueUtil.after();
		System.out.println("The optimal number of threads is " + threads.size());
	}
	
	public static boolean foundOptimal() throws Exception {
		int currentQueueSize = queueUtil.getSize();
		if (currentQueueSize >= previousQueueSize) {
			return false;
		}
		else {
			return true;
		}
	}

}
