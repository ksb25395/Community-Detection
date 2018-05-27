package graph;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import util.GraphLoader;

/**
 * @author Bhargav Ram K S 
 *
 * Calculates dQ, change in modularity upon merging two communities
 */
public class FastNewman {
	
	//input graph for which we want to detect communities
	CapGraph inputGraph;
	
	//Constructor taking graph as argument
	public FastNewman(CapGraph graph) {
		this.inputGraph = graph;
	}
	
	/*We use this method to calculate the difference in modularity, dQ
	 *upon joining the two communities I & J, commI & commJ represented
	 *as a list of vertices belonging to the respective communities
	 *dQ = e_ij + e_ji - 2*a_i*a_j
	 *dQ = 2* (e_ij - a_i * a_j)
	 */
	public double dQ(List<Integer> commI, List<Integer> commJ) {
		//Get all the IDs of the two communities commI & commJ first in a Set
		Set<Integer> idSet_I = new HashSet<>();
		idSet_I.addAll(commI);
		
		Set<Integer> idSet_J = new HashSet<>();
		idSet_J.addAll(commJ);
		
		/*
		 * e_ij  is fraction of number of edges between communities I and J compared to total edges
		 */
		double e_ij = 0;
		
		Set<String> trackSet = new HashSet<>();
		
		for(int i : idSet_I) {
			for(int j : idSet_J) {
				if(trackSet.contains(i + "" + j) || trackSet.contains(j + "" + i))
					continue;
				trackSet.add(i + "" + j);
				trackSet.add(j + "" + i);
				e_ij += (inputGraph.isEdge(i, j)) ? 1 : 0;
			}
		}
		
		//Fraction of all edges which are between community I and community J
		e_ij /= 2 * inputGraph.getNumEdges();
		
		
		/*
		 * All neighbors of nodes in community I 
		 */
		Set<Integer> setI = new HashSet<>();
		for(int id_i : idSet_I) {
			Set<Integer> neighborsI = inputGraph.getNeighbors(id_i);
			
			
			setI.addAll(neighborsI);
		}
		
		/*
		 * All neighbors of nodes in community J 
		 */
		Set<Integer> setJ = new HashSet<>();
		for(int id_j : idSet_J) {
			Set<Integer> neighborsJ = inputGraph.getNeighbors(id_j);
			
			setJ.addAll(neighborsJ);
		}
		
		/*
		 * a_i is the measure of total degree of nodes in community I 
		 * total degree = 2 * (edges internal to community I) + edges with only one end in the community I
		 */
		double a_i = 0;
		for(int i : idSet_I) {
			for(int j : setI) {
				if(idSet_J.contains(j)) {
					a_i += inputGraph.isEdge(i, j) ? 1 : 0;
				}
				else {
					a_i += inputGraph.isEdge(i, j) ? 1 : 0;
				}
			}
		}
		a_i /= 2 * inputGraph.getNumEdges();
		
		
		/*
		 * a_j is the measure of total degree of nodes in community J
		 * total degree = 2 * (edges internal to community J) + edges with only one end in the community J
		 */
		double a_j = 0;
		for(int i : idSet_J) {
			for(int j : setJ) {
				if(idSet_I.contains(j)) {
					a_j += inputGraph.isEdge(i, j) ? 1 : 0;
				}else {
					a_j += inputGraph.isEdge(i, j) ? 1 : 0;
				}
			}
		}
		a_j /= 2 * inputGraph.getNumEdges();
		
		//System.out.println(e_ij + " " + a_i + " " + a_j);
		
		/*
		 * Total change in modularity deltaQ
		 */
		double deltaQ = 2*(e_ij - a_i * a_j);
		
		return deltaQ;
	}

	
	/*
	 * Driver program for calculating dQ
	 */
	public static void main(String[] args) {
		CapGraph g = new CapGraph();
		GraphLoader.loadGraph(g, "./data/karate.txt");
		
		List<Integer> commI = new ArrayList<>();
		commI.add(4);
		commI.add(5);
		commI.add(6);
		List<Integer> commJ = new ArrayList<>();
		commJ.add(1);
		commJ.add(2);
		commJ.add(3);

		
		FastNewman nm = new FastNewman(g);
		System.out.println(nm.dQ(commI, commJ));
	}

}
