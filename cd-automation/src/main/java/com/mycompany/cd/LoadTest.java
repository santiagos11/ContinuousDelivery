package com.mycompany.cd;

import java.util.ArrayList;
import java.util.concurrent.TimeUnit;

public class LoadTest {
	
	public static int previousQueueSize;
	public static final int THRESHOLD = 10;
	public static final int DISTANCE = 2;
	public static QueueUtil queueUtil;

	public static void main(String[] args) throws Exception {
		queueUtil = new QueueUtil();
		queueUtil.before();
		previousQueueSize = queueUtil.getSize();
		
		Thread jobProducer = new Thread(new JobProducer("Job Producer"));
		jobProducer.start();

		TimeUnit.MILLISECONDS.sleep(10000);
		
		int i = 1;
		ArrayList<Thread> threads = new ArrayList<Thread>();
		threads.add(new Thread(new ResponseConsumer("Response Consumer " + i++)));
		threads.get(threads.size() - 1).start();
		
		while (!foundOptimal()) {
			threads.add(new Thread(new ResponseConsumer("Response Consumer " + i++)));
			threads.get(threads.size() - 1).start();
			TimeUnit.MILLISECONDS.sleep(1000);
		}
		
		queueUtil.after();
		System.out.println("The optimal number of threads is " + threads.size());
	}
	
	public static boolean foundOptimal() throws Exception {
		if (queueUtil.getSize() >= THRESHOLD) {
			return false;
		}
		else {
			return true;
		}
	}

}
