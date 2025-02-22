package org.nodes.data;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;

import org.nodes.Global;
import org.nodes.DGraph;
import org.nodes.DNode;
import org.nodes.DTGraph;
import org.nodes.DTNode;
import org.nodes.Graph;
import org.nodes.LightDGraph;
import org.nodes.LightUGraph;
import org.nodes.Link;
import org.nodes.MapDTGraph;
import org.nodes.MapUTGraph;
import org.nodes.UGraph;
import org.nodes.UNode;
import org.nodes.UTGraph;
import org.nodes.UTNode;

public class Data {

	
	/**
	 * Reads a file in edge-list representation into a string-labeled undirected 
	 * graph
	 * 
	 * @param file
	 * @return
	 * @throws IOException
	 */
	public static UTGraph<String, String> edgeList(File file)
			throws IOException
	{
		return edgeList(file, false);
	}
	
	
	public static UTGraph<String, String> edgeList(File file, boolean bipartite)
				throws IOException
	{
		return edgeList(file, bipartite, false);
	}
	
	public static UTGraph<String, String> edgeList(File file, boolean bipartite, boolean blank)
			throws IOException
	{
		BufferedReader reader = new BufferedReader(new FileReader(file));
		UTGraph<String, String> graph = new MapUTGraph<String, String>();
				
		String line;
		int i = 0;
		
		Map<String, UNode<String>> map = new HashMap<String, UNode<String>>();
		
		do {
			line = reader.readLine();
			i++;
			
			if(line == null)
				continue;
			if(line.trim().isEmpty())
				continue;
			if(line.trim().startsWith("#"))
				continue;
			if(line.trim().startsWith("%"))
				continue;
			
			String[] split = line.split("\\s");
			if(split.length < 2)
				throw new IllegalArgumentException("Line "+i+" does not split into two (or more) elements.");
			
			String a, b, c = null;
			a = split[0];
			b = split[1];
			
			if(bipartite)
			{
				a = "l"+a;
				b = "r"+b;
			}

			if(split.length > 2)
				c = split[2];

			UNode<String> nodeA;
			if(! map.containsKey(a))
			{
				nodeA = graph.add(blank ? "" : a);
				map.put(a, nodeA);
			} else
				nodeA = map.get(a);
				
			UNode<String> nodeB;
			if(! map.containsKey(b))
			{
				nodeB = graph.add(blank ? "" : b);
				map.put(b, nodeB);
			} else
				nodeB = map.get(b);
			
			nodeA.connect(nodeB);
			
		} while(line != null);
		
		System.out.println("\nFinished. Read " + graph.numLinks() + "edges");
		return graph;
	}	
	
	/**
	 * Reads a file in edge-list representation into a string-labeled directed 
	 * graph
	 * 
	 * @param file
	 * @return
	 * @throws IOException
	 */
	public static DTGraph<String, String> edgeListDirected(File file)
			throws IOException
	{
		return edgeListDirected(file, false);
	}
	
	public static DTGraph<String, String> edgeListDirected(File file, boolean blank)
		throws IOException
	{
		BufferedReader reader = new BufferedReader(new FileReader(file));
		DTGraph<String, String> graph = new MapDTGraph<String, String>();
				
		String line;
		int i = 0;
		
		Map<String, DNode<String>> map = new HashMap<String, DNode<String>>();
		
		do {
			line = reader.readLine();
			i++;

			if(line == null)
				continue;
			if(line.trim().isEmpty())
				continue;
			if(line.trim().startsWith("#"))
				continue;
			if(line.trim().startsWith("%"))
				continue;

			
			String[] split = line.split("\\s");
			if(split.length < 2)
				throw new IllegalArgumentException("Line "+i+" does not split into two elements.");
			
			String a, b = null;
		
			a = split[0];			
			b = split[1];
				
			DNode<String> nodeA, nodeB;
			
			if(! map.containsKey(a))
			{
				nodeA = graph.add(blank ? "" : a);
				map.put(a,  nodeA);
			} else
				nodeA= map.get(a);

			if(! map.containsKey(b))
			{
				nodeB = graph.add(blank ? "" : b);
				map.put(b,  nodeB);
			} else
				nodeB = map.get(b);
			
			nodeA.connect(nodeB);
			

		} while(line != null);
		
		System.out.println("\nFinished. Read " + graph.numLinks() + "edges");
		return graph;
	}		
	
	/**
	 * Reads a file in edge-list representation into a string-labeled directed 
	 * graph. Requires that the edges are represented using _consecutive_ 
	 * integers.
	 *  
	 * @param file
	 * @return 
	 * @throws IOException
	 */
	public static DGraph<String> edgeListDirectedUnlabeled(File file, boolean clean)
			throws IOException
	{
		BufferedReader reader = new BufferedReader(new FileReader(file));
		LightDGraph<String> graph = new LightDGraph<String>();
				
		String line;
		int i = 0;
		
		do
		 {
			line = reader.readLine();
			i++;
		
			if(line == null)
				continue;
			if(line.trim().isEmpty())
				continue;
			if(line.trim().startsWith("#"))
				continue;
			if(line.trim().startsWith("%"))
				continue;
			
			String[] split = line.split("\\s");
			if(split.length < 2)
				throw new IllegalArgumentException("Line "+i+" does not split into two elements.");
			
			Integer a, b, c = null;
			try {
				a = Integer.parseInt(split[0]);
			} catch(NumberFormatException e)
			{
				throw new RuntimeException("The first element on line "+i+" ("+split[0]+") cannot be parsed into an integer.", e);
			}
			
			try {
				b = Integer.parseInt(split[1]);
			} catch(NumberFormatException e)
			{
				throw new RuntimeException("The second element on line "+i+" ("+split[1]+") cannot be parsed into an integer.", e);
			}
						
			ensure(graph, Math.max(a, b));
			
			DNode<String> nodeA = graph.get(a);
			DNode<String> nodeB = graph.get(b);

			nodeA.connect(nodeB);
			
			int links = graph.numLinks();
			if(links%500000 == 0)
				Global.log().info("Loaded " + links + " links (n="+graph.size()+", l="+graph.numLinks()+")");
			if(links%5000000 == 0)
			{			
				Global.log().info("Compacting");
				graph.compact(5);
				Global.log().info("Done");
			}
			
		} while(line != null);
		
		Global.log().info("Graph loaded (n="+graph.size()+", l="+graph.numLinks()+").");

		Global.log().info("Sorting");
		graph.sort();
		
		if(clean)
		{
			Global.log().info("Compacting");
			graph.compact(0);
			Global.log().info("Done");
		}
		
		return graph;
	}
	
	/**
	 * Reads a file in edge-list representation into a string-labeled directed 
	 * graph. Requires that the edges are represented using _consecutive_ 
	 * integers. Ignores multiple edges and self-loops.
	 *  
	 * @param file
	 * @return 
	 * @throws IOException
	 */
	public static DGraph<String> edgeListDirectedUnlabeledSimple(File file)
			throws IOException
	{
		BufferedReader reader = new BufferedReader(new FileReader(file));
		LightDGraph<String> graph = new LightDGraph<String>();
				
		String line;
		int i = 0;
		
		do
		 {
			line = reader.readLine();
			i++;
		
			if(line == null)
				continue;
			if(line.trim().isEmpty())
				continue;
			if(line.trim().startsWith("#"))
				continue;
			if(line.trim().startsWith("%"))
				continue;
			
			String[] split = line.split("\\s");
			if(split.length < 2)
				throw new IllegalArgumentException("Line "+i+" does not split into two elements.");
			
			Integer a, b, c = null;
			try {
				a = Integer.parseInt(split[0]);
			} catch(NumberFormatException e)
			{
				throw new RuntimeException("The first element on line "+i+" ("+split[0]+") cannot be parsed into an integer.", e);
			}
			
			try {
				b = Integer.parseInt(split[1]);
			} catch(NumberFormatException e)
			{
				throw new RuntimeException("The second element on line "+i+" ("+split[1]+") cannot be parsed into an integer.", e);
			}
			
			if(!a.equals(b))
			{	
				ensure(graph, Math.max(a, b));
				
				DNode<String> nodeA = graph.get(a);
				DNode<String> nodeB = graph.get(b);
				
				if(!nodeA.connectedTo(nodeB))
					nodeA.connect(nodeB);
			}
			
			int links = graph.numLinks();
			if(links%500000 == 0)
				Global.log().info("Loaded " + links + " links (n="+graph.size()+", l="+graph.numLinks()+")");
			if(links%5000000 == 0)
			{			
				Global.log().info("Compacting");
				graph.compact(5);
				Global.log().info("Done");
			}
			
		} while(line != null);
		
		Global.log().info("Graph loaded (n="+graph.size()+", l="+graph.numLinks()+").");

		Global.log().info("Sorting");
		graph.sort();

		Global.log().info("Compacting");
		graph.compact(0);
		Global.log().info("Done");
		
		return graph;
	}
	
	
	/**
	 * Reads a file in edge-list representation into a string-labeled directed 
	 * graph. Requires that the edges are represented using _consecutive_ 
	 * integers.
	 *  
	 * @param file
	 * @return 
	 * @throws IOException
	 */
	public static UGraph<String> edgeListUndirectedUnlabeled(File file, boolean clean)
			throws IOException
	{
		BufferedReader reader = new BufferedReader(new FileReader(file));
		LightUGraph<String> graph = new LightUGraph<String>();
				
		String line;
		int i = 0;
		
		do {
			line = reader.readLine();
			i++;
		
			if(line == null)
				continue;
			if(line.trim().isEmpty())
				continue;
			if(line.trim().startsWith("#"))
				continue;
			if(line.trim().startsWith("%"))
				continue;
			
			String[] split = line.split("\\s");
			if(split.length < 2)
				throw new IllegalArgumentException("Line "+i+" does not split into two elements.");
			
			Integer a, b, c = null;
			try {
				a = Integer.parseInt(split[0]);
			} catch(NumberFormatException e)
			{
				throw new RuntimeException("The first element on line "+i+" ("+split[0]+", line = "+line+") cannot be parsed into an integer.", e);
			}
			
			try {
				b = Integer.parseInt(split[1]);
			} catch(NumberFormatException e)
			{
				throw new RuntimeException("The second element on line "+i+" ("+split[1]+") cannot be parsed into an integer.", e);
			}
						
			ensure(graph, Math.max(a, b));
			
			UNode<String> nodeA = graph.get(a);
			UNode<String> nodeB = graph.get(b);

			nodeA.connect(nodeB);
			
			int links = graph.numLinks();
			if(links%100000 == 0)
				Global.log().info("Loaded " + links + " links (n="+graph.size()+", l="+graph.numLinks()+")");
			
			
		} while(line != null);
		
		Global.log().info("Sorting");
		graph.sort();
		
		if(clean)
		{
			Global.log().info("Compacting");
			graph.compact(0);
			Global.log().info("Done");

		}
		
		return graph;
	}	

	private static void ensure(Graph<String> graph, int max)
	{
		while(graph.size() < max + 1)
			graph.add(null);
	}		
	
	/**
	 * Writes the graph structure to the file as an edge list. 
	 * Only the structure is written and the labels and tags are discarded 
	 */
	public static <L> void writeEdgeList(Graph<L> graph, File file) 
			throws IOException
	{
		BufferedWriter writer = new BufferedWriter(new FileWriter(file));
		for(Link<L> link : graph.links())
			writer.write(link.first().index() + "\t" + link.second().index() + "\n");
		
		writer.close();
	}
}
