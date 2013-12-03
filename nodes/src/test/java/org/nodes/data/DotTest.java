package org.nodes.data;

import static org.junit.Assert.*;

import java.util.Random;

import org.junit.Test;
import org.nodes.Global;
import org.nodes.UTGraph;
import org.nodes.random.RandomGraphs;

public class DotTest {

	@Test
	public void testRead() 
	{
		Global.randomSeed();
		UTGraph<String, String> graph = RandomGraphs.preferentialAttachment(30, 2);
		
		UTGraph<String, String> out = Dot.read(graph.toString());
		
		assertEquals(graph.size(), out.size());
		assertEquals(graph.numLinks(), out.numLinks());
	}

}
