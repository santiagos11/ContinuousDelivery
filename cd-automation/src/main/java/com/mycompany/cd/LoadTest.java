package com.mycompany.cd;

import java.util.ArrayList;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

public class LoadTest {
	
	private final int THRESHOLD = 500;
	private final int AMPLITUDE = 50;
	private final int NUMBER_OF_JOBS = 3000;
	private final CountDownLatch done = new CountDownLatch(NUMBER_OF_JOBS);
	private int previousQueueSize;
	private int currentQueueSize;
	private QueueUtil queueUtil;
	ArrayList<Thread> threads;
	
	public LoadTest() {
		this.queueUtil = new QueueUtil();
		this.threads = new ArrayList<Thread>();
	}
	
	public int getOptimalNumberOfThreads() {
		return threads.size();
	}
	
	public CountDownLatch getCountDownLatch() {
		return this.done;
	}
	
	private void prepare() throws Exception {
		queueUtil.before();	
	}
	
	private void produceJobs() {
		Thread jobProducer = new Thread(new JobProducer("Job Producer", NUMBER_OF_JOBS));
		jobProducer.start();
	}
	
	private boolean isAboveUpperBound(int x) {
		return x > THRESHOLD + AMPLITUDE;
	}
	
	private boolean isBelowLowerBound(int x) {
		return x < THRESHOLD - AMPLITUDE;		
	}
	
	private boolean isInRange(int x) {
		return !(isAboveUpperBound(x) || isBelowLowerBound(x));
	}
	
	private void addThread() {
		int threadNumber = threads.size() + 1;
		threads.add(new Thread(new ResponseConsumer("Response Consumer " + threadNumber, done)));
		threads.get(threads.size() - 1).start();
	}
	
	private void removeThread() {
		int threadNumber = threads.size() - 1;
		if (threadNumber >= 0) {
			threads.get(threadNumber).interrupt();
			threads.remove(threadNumber);			
		}
	}
	
	private int deltaQueueSize() throws Exception {
		previousQueueSize = queueUtil.getSize();
		TimeUnit.SECONDS.sleep(1);
		currentQueueSize = queueUtil.getSize();
		System.out.println(currentQueueSize - previousQueueSize);
		return currentQueueSize - previousQueueSize;
	}
	
	private void consumeResponses() throws Exception {
		addThread();
		TimeUnit.MILLISECONDS.sleep(1000);
		int delta = deltaQueueSize();
		while (Math.abs(delta) + done.getCount() > 0) {
			if (delta > 3) {
				addThread();
			}
			else if (delta < -3){
				removeThread();	
			}
			TimeUnit.MILLISECONDS.sleep(1000);
			delta = deltaQueueSize();
		}
	}
	
	private void tearDown() throws Exception {
		queueUtil.after();
	}
	
	public static void main(String[] args) throws Exception {
		LoadTest loadTest = new LoadTest();
		loadTest.prepare();
		loadTest.produceJobs();
		TimeUnit.SECONDS.sleep(2);
		loadTest.consumeResponses();
		loadTest.getCountDownLatch().await();
		loadTest.tearDown();
	}
	
}
