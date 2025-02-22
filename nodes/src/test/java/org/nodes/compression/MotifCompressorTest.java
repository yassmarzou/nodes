package org.nodes.compression;

import static java.util.Arrays.asList;
import static java.util.Collections.reverseOrder;
import static org.junit.Assert.*;
import static org.nodes.compression.Functions.tic;
import static org.nodes.compression.Functions.toc;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.junit.Test;
import org.nodes.DGraph;
import org.nodes.DNode;
import org.nodes.DegreeComparator;
import org.nodes.Graphs;
import org.nodes.Link;
import org.nodes.MapDTGraph;
import org.nodes.MapUTGraph;
import org.nodes.Subgraph;
import org.nodes.UGraph;
import org.nodes.UNode;
import org.nodes.algorithms.Nauty;
import org.nodes.motifs.MotifCompressor;
import org.nodes.motifs.UPlainMotifExtractor;
import org.nodes.random.SimpleSubgraphGenerator;
import org.nodes.random.SubgraphGenerator;
import org.nodes.util.FrequencyModel;
import org.nodes.util.Order;
import org.nodes.util.Series;

public class MotifCompressorTest
{

	@Test
	public void fastVsSlow()
	{
		boolean correct = true;
		DGraph<String> data = org.nodes.random.RandomGraphs.preferentialAttachmentDirected(10000, 3);
		
		FrequencyModel<DGraph<String>> fm = new FrequencyModel<DGraph<String>>();
		
		// * Places in the graph where each motif occurs
		Map<DGraph<String>, List<List<Integer>>> occurrences = new LinkedHashMap<DGraph<String>, List<List<Integer>>>();
		// * Those nodes that have been taken by one of the occurrence. If a new occurrence contains one of these,
		//   it will not be added
		Map<DGraph<String>, Set<Integer>> taken = new LinkedHashMap<DGraph<String>, Set<Integer>>();
		
		SubgraphGenerator<String> gen = new SubgraphGenerator<String>(data, 5, new ArrayList<DNode<String>>());

		// * Sampling
		for (int i : Series.series(1000))
		{
			if (i % 100 == 0)
				System.out.println("Samples finished: " + i);
			
			// * Sample a subgraph
			SubgraphGenerator<String>.Result result = gen.generate();
			DGraph<String> sub = Subgraph.dSubgraphIndices(data,
					result.indices());
			sub = Graphs.blank(sub, "");

			// * Reorder nodes to canonical ordering
			Order canonical = Nauty.order(sub, new Functions.NaturalComparator<String>());
			sub = Graphs.reorder(sub, canonical);
			List<Integer> indices = canonical.apply(result.indices());
			
			// * Check if any of the nodes of the occurrence have been used already
			boolean overlaps;
			if(! taken.containsKey(sub))
				overlaps = false;
			else
				overlaps = Functions.overlap(taken.get(sub), indices) > 0;
			
			// * Add it as an occurrence	
			if(! overlaps)
			{	
				fm.add(sub, correct ? result.invProbability() : 1.0);
	
				if (! occurrences.containsKey(sub))
					occurrences.put(sub, new ArrayList<List<Integer>>());
	
				occurrences.get(sub).add(indices);
				
				if (! taken.containsKey(sub))
					taken.put(sub, new HashSet<Integer>());
				
				taken.get(sub).addAll(indices);
			}
		}

		DGraph<String> max = fm.maxToken();
		System.out.println(max);
		
		System.out.println("number of occurrences " + occurrences.size());
		
		int repeats = 1;
		tic();
		
		double bitsSlow = -1.0;
		for(int i : Series.series(repeats))
			bitsSlow = MotifCompressor.size(data, max, occurrences.get(max), false, new EdgeListCompressor<String>(false));
		
		System.out.println("Bits slow : " + bitsSlow + ". Time taken " + toc() + " seconds.");
		
		tic();
		
		double bitsFast = -1.0;
		for(int i : Series.series(repeats))
			bitsFast = MotifCompressor.sizeFast(data, max, occurrences.get(max)); 
		
		System.out.println("Bits fast : " + bitsFast + ". Time taken " + toc() + " seconds.");
		
		List<List<Integer>> empty = Collections.emptyList();
		bitsFast = MotifCompressor.sizeFast(data, max, empty); 
		System.out.println("Bits no occurrences: " + bitsFast + ".");
	}
	
	@Test
	public void speed()
	{
		DGraph<String> data = org.nodes.random.RandomGraphs.preferentialAttachmentDirected(10000, 3);

		tic();
		for(Link<String> link : data.links())
		{
			int i = 3+5;
		}
		System.out.println(toc());
	}

	
	@Test
	public void subgraph()
	{
		DGraph<String> data = new MapDTGraph<String, String>();

		DNode<String> a = data.add("a");
		DNode<String> b = data.add("b");
		DNode<String> c = data.add("c");
		DNode<String> d = data.add("d");
		DNode<String> e = data.add("e");
		DNode<String> f = data.add("f");

		b.connect(a);
		b.connect(c);
		a.connect(c);
		
		c.connect(d);
		
		d.connect(e);
		d.connect(f);
		f.connect(e);
		
		DGraph<String> sub = new MapDTGraph<String, String>();
		DNode<String> x = sub.add("x");
		DNode<String> y = sub.add("y");
		DNode<String> z = sub.add("z");
		
		x.connect(y);
		x.connect(z);
		y.connect(z);
		
		List<List<Integer>> occurrences = new ArrayList<List<Integer>>(2);
		
		occurrences.add(Arrays.asList(1, 0, 2));
		occurrences.add(Arrays.asList(3, 5, 4));
		
		List<List<Integer>> wiring = new ArrayList<List<Integer>>(2);
		
		DGraph<String> subbed = MotifCompressor.subbedGraph(data, sub, occurrences, wiring);
		
		System.out.println(wiring);
		System.out.println(subbed);
	}
	
	@Test
	public void subbedTest()
	{
		UGraph<String> data = new MapUTGraph<String, String>();
		
		UNode<String> a = data.add("x"),
		              b = data.add("x"),
		              c = data.add("x"),
		              d = data.add("x"),
		              e = data.add("x"),
		              f = data.add("x"),
		              g = data.add("x"),
		              h = data.add("x"),
		              i = data.add("x"),
		              j = data.add("x"),
		              k = data.add("x"),
		              l = data.add("x"),
		              m = data.add("x");
		
		a.connect(b);
		b.connect(c);
		c.connect(a);
		
		d.connect(e);
		e.connect(f);
		f.connect(d);

		g.connect(h);
		h.connect(i);
		i.connect(g);
		
		j.connect(k);
		k.connect(l);
		l.connect(j);
		
		m.connect(a);
		m.connect(d);
		m.connect(g);
		m.connect(j);
		
		UPlainMotifExtractor<String> ex = new UPlainMotifExtractor<String>(data, 10000, 3, 7);
		
		for(UGraph<String> sub : ex.subgraphs())
		{
			System.out.println("subgraph: " + sub);

			List<List<Integer>> wiring = new ArrayList<List<Integer>>(); 
			UGraph<String> subbed = MotifCompressor.subbedGraph(data, sub, ex.occurrences(sub), wiring);
			System.out.println("remainder: " + subbed);
			System.out.println("wiring: " + wiring);
			
			System.out.println();
		}
	}
	
	@Test
	public void subbedTest2()
	{
		UGraph<String> data = new MapUTGraph<String, String>();
		
		UNode<String> a = data.add("x"),
		              b = data.add("x"),
		              c = data.add("x"),
		              d = data.add("x"),
		              e = data.add("x"),
		              f = data.add("x"),
		              g = data.add("x"),
		              h = data.add("x"),
		              i = data.add("x");
		            
		a.connect(b);
		a.connect(c);
		a.connect(d);
		
		b.connect(c);
		b.connect(d);
		
		c.connect(d);
		c.connect(e);
		
		d.connect(e);
		
		e.connect(f);
		
		f.connect(g);
		f.connect(h);
		f.connect(i);
		
		g.connect(h);
		
		h.connect(i);
		
		UGraph<String> sub = new MapUTGraph<String, String>();
		UNode<String> s0 = sub.add("x"),
		              s1 = sub.add("x"),
		              s2 = sub.add("x"),
		              s3 = sub.add("x");
				
		List<List<Integer>> occurrences = asList(asList(2, 4, 3, 1), asList(5, 6, 7, 8)); 
		
		List<List<Integer>> wiring = new ArrayList<List<Integer>>(); 
		UGraph<String> subbed = MotifCompressor.subbedGraph(data, sub, occurrences, wiring);
		
		System.out.println("remainder: " + subbed);
		System.out.println("wiring: " + wiring);
		
		assertEquals(asList(asList(0, 1, 2, 3), asList(0)), wiring); 
	}	

}
