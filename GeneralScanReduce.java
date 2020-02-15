/**
 * @author Mitchel Downey
 * @file GeneralScanReduce.java
 *
 * CPSC 4600, Seattle University
 */


import java.util.concurrent.*;
import java.lang.Math.*;

/**
 * GeneralScanReduce is a templated class for an O(P) space efficient threaded ScanReduce. It utilises the ForkJoinPool
 * library for threading, and contains two subtasks, ReduceTask and ScanTask, for use with the ForkJoinPool. Any subclass
 * needs to override the init(), prepare(), combine(), and gen() for this class to perform as designed.
 *
 * @param <E>	The type of the initial data set to be processed
 * @param <T>	The type of the internal nodes of the tree and the output of the Scan
 */
public class GeneralScanReduce<E,T>
{

	/**
	 * ReduceTask is the task that the ForkJoinPool acts on to perform the reduce on the data set
	 */
	private class ReduceTask extends RecursiveTask<Boolean>
	{
		public ReduceTask(int i)
		{
			index = i;
		}

		protected Boolean compute()
		{
			if(!isLeaf(index))
			{
				if(index <= n_threads / 2)
				{
					ReduceTask right = new ReduceTask(right(index));
					right.fork();
					ReduceTask left = new ReduceTask(left(index));
					left.fork();
					right.join();
					left.join();
					interior[index] = combine(value(left(index)), value(right(index)));
				}
				else
				{
					schwartzReduce();
				}
			}
			return true;
		}

		private void schwartzReduce()
		{
			T count = init();
			int end = rightMost(index);
			for(int i = leftMost(index); i <= end; i++)
			{
				count = combine(count, value(i));
			}
			interior[index] = count;
		}
		private int index;
	}

	/**
	 * ScanTask is the task that the ForkJoinPool acts on to perform the scan on the data set
	 */
	private class ScanTask extends RecursiveTask<Boolean>
	{
		public ScanTask(int i, T previous, T[] output)
		{
			index = i;
			this.previous = previous;
			this.output = output;
		}

		protected Boolean compute()
		{
			if(!isLeaf(index))
			{
				if(index <= (n_threads / 2))
				{
					ScanTask right = new ScanTask(right(index), combine(previous, value(left(index))), output);
					ScanTask left = new ScanTask(left(index), previous, output);
					right.fork();
					left.fork();
					right.join();
					left.join();
				}
				else
				{
					schwartzScan();
				}
			}
			return true;
		}

		private void schwartzScan()
		{
			T count = previous;
			int end = rightMost(index);
			for(int i = leftMost(index); i <= end; i++)
			{
				count = gen(combine(count, value(i)));
				output[i - (n - 1)] = count;
			}
		}

		private int start;
		private int end;
		private int index;
		private T previous;
		private T[] output;
	}

	private static final int ROOT = 0;
	private static final int N_THREADS = 16;

	public GeneralScanReduce(E[] raw)
	{
		reduced = false;
		n = raw.length;
		data = raw;
		height = 1;
		if(n < n_threads*2)
			System.out.println("too little data! " + n);
		while(height < n)
		{
			height *= 2;
		}
		this.n_threads = N_THREADS;
		this.interior = (T[])new Object[n_threads * 2 - 1];
	}


	public T getReduction(int i)
	{
		if(i >= size())
			return null;
		if(!reduced)
		{
			threads = new ForkJoinPool(n_threads * 2);
			threads.invoke(new ReduceTask(0));
			reduced = true;
		}
		return gen(value(i));
	}

	public void getScan(T[] output)
	{
		if(!reduced)
		{
			getReduction(0);
			reduced = true;
		}
		threads = new ForkJoinPool(n_threads * 2);
		threads.invoke(new ScanTask(ROOT, init(), output));
	}

	protected T init() {return null;}
	protected T prepare(E datum) {return null;}
	protected T combine(T left, T right) {return left;}
	protected T gen(T tally) {return tally;}

	private T value(int i)
	{
		if(i < n - 1)
			return interior[i];
		else
			return prepare(data[i-(n-1)]);
	}

	private int leftMost(int i)
	{
		int next = i;
		while(!isLeaf(next))
			next = left(next);
		return next;
	}

	private int rightMost(int i)
	{
		int next = i;
		while(!isLeaf(next))
			next = right(next);
		return next;
	}

	private int size() {return (n - 1) + n;}
	private int parent(int i) {return (i - 1) / 2;}
	private int left(int i) {return (i * 2) + 1;}
	private int right(int i) {return left(i) + 1;}
	private boolean isLeaf(int i) {return i >= (n - 1);}

	private ForkJoinPool threads;
	boolean reduced;
	private int n;
	private E[] data;
	private T[] interior;
	private int height;
	private int n_threads;
}