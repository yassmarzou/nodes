package org.nodes.util;

import java.util.*;

/**
 * From http://stackoverflow.com/questions/2000048/stepping-through-all-permutations-one-swap-at-a-time
 * 
 * Based on https://en.wikipedia.org/wiki/Steinhaus%E2%80%93Johnson%E2%80%93Trotter_algorithm#Even.27s_speedup
 * @author peter
 *
 */

public class Permutations implements Iterable<int[]>
{
	private int size;
	
	public Permutations(int size) 
	{
		this.size = size;
	}

	public class PermIterator implements Iterator<int[]> 
	{
		private int[] next = null;
	
		private final int n;
		private int[] perm;
		private int[] dirs;
	
		public PermIterator(int size) 
		{
			n = size;
			if (n <= 0) {
				perm = (dirs = null);
			} else {
				perm = new int[n];
				dirs = new int[n];
				for (int i = 0; i < n; i++) {
					perm[i] = i;
					dirs[i] = -1;
				}
				dirs[0] = 0;
			}
	
			next = perm;
		}
	
		@Override
		public int[] next() 
		{
			int[] r = makeNext();
			next = null;
			return r;
		}
	
		@Override
		public boolean hasNext() 
		{
			return (makeNext() != null);
		}
	
		@Override
		public void remove() 
		{
			throw new UnsupportedOperationException();
		}
	
		private int[] makeNext() 
		{
			if (next != null)
				return next;
			if (perm == null)
				return null;
	
			// find the largest element with != 0 direction
			int i = -1, e = -1;
			for (int j = 0; j < n; j++)
				if ((dirs[j] != 0) && (perm[j] > e)) {
					e = perm[j];
					i = j;
				}
	
			if (i == -1) // no such element -> no more premutations
				return (next = (perm = (dirs = null))); // no more permutations
	
			// swap with the element in its direction
			int k = i + dirs[i];
			swap(i, k, dirs);
			swap(i, k, perm);
			// if it's at the start/end or the next element in the direction
			// is greater, reset its direction.
			if ((k == 0) || (k == n - 1) || (perm[k + dirs[k]] > e))
				dirs[k] = 0;
	
			// set directions to all greater elements
			for (int j = 0; j < n; j++)
				if (perm[j] > e)
					dirs[j] = (j < k) ? +1 : -1;
	
			return (next = perm);
		}

	}

	@Override
	public Iterator<int[]> iterator() 
	{
		return new PermIterator(size);
	}	
		
	protected static void swap(int i, int j, int[] arr) 
	{
		int v = arr[i];
		arr[i] = arr[j];
		arr[j] = v;
	}
}