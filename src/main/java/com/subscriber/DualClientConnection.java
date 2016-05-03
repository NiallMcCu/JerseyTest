package com.subscriber;

import java.io.IOException;
import java.lang.management.ManagementFactory;
import java.lang.management.ThreadMXBean;

public class DualClientConnection {
	public static String thread1Output = "Thread1Output.txt";
	public static String thread2Output = "Thread2Output.txt";
	
	ThreadGroup rootThreadGroup = null;
	
	ThreadGroup getRootThreadGroup(){
		if(rootThreadGroup != null)
			return rootThreadGroup;
		ThreadGroup tg = Thread.currentThread().getThreadGroup();
		ThreadGroup ptg;
		while ((ptg = tg.getParent()) != null)
			tg = ptg;
		return tg;
	}
	
	Thread[] getAllThreads(){
		final ThreadGroup root = getRootThreadGroup();
		final ThreadMXBean thbean = ManagementFactory.getThreadMXBean();
		int nAlloc = thbean.getThreadCount();
		int n;
		Thread[] threads;
		do {
			nAlloc *= 2;
			threads = new Thread[nAlloc];
			n = root.enumerate(threads, true);
		} while (n == nAlloc);
		return java.util.Arrays.copyOf(threads, n);
	}
	
	public static void main (String [] args){
		int delay = (args.length > 0) ? Integer.parseInt(args[0]) : 1;
		DualClientConnection run = new DualClientConnection();
		run.start(delay);
	}
	
	public void start(int delay){
		try { 
			ConnectionClient client1 = new ConnectionClient(thread1Output, delay, 10);
			client1.start();
			ConnectionClient client2 = new ConnectionClient(thread2Output, 0, 10);
			client2.start();
			Thread.sleep(1000);
			for (int i=0; i < getAllThreads().length; i++){
				Thread t = getAllThreads()[i];
				System.out.println("thread name = " + t.getName());
			}
			System.out.println("getAllThreads = " + getAllThreads().length);
			Thread.sleep((delay * 60000 * 20) + 40000);
			for (int i=0; i < getAllThreads().length; i++){
				Thread t = getAllThreads()[i];
				if(t.getName().matches("Thread[0-9]Output.txt-0")){
					System.out.println("found subscription thread");
					t.stop();
				}
			}
			System.out.println("Stopping subscription threads");
		}catch(IOException | InterruptedException e){
			e.printStackTrace();
		}
	}

}
