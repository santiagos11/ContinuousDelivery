package com.mycompany.cd;

public class JobExecutor {
	
	public static final int NUMBER_OF_THREADS = 10;

	public static void main(String[] args) {
		Thread[] threads = new Thread[NUMBER_OF_THREADS];
		for (int i = 0; i < threads.length; i++) {
			threads[i] = new Thread(new Executor("Executor " + (i + 1)));
			threads[i].start();
		}
	}

}
