package com.cloudcar.college.fiber;

import co.paralleluniverse.fibers.Suspendable;
import co.paralleluniverse.strands.concurrent.ReentrantLock;

public class Singleton
{
	private static Singleton instance;
	private static ReentrantLock lock = new ReentrantLock();

	private Singleton(){}

	@Suspendable
	private void init()
	{

	}

	@Suspendable
	public static Singleton getInstance()
	{
		if (instance == null)
		{
			lock.lock();
			try
			{
				if (instance == null)
					instance = new Singleton();
				instance.init();
			}
			finally
			{
				lock.unlock();
			}
		}
		return instance;
	}
}
