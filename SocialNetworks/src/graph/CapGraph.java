package graph;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Queue;
import java.util.Set;
import java.util.Stack;

import util.GraphLoader;

/**
 * CapGraph implementation class.
 * 
 * @author Bhargav Ram K S.
 */
public class CapGraph implements Graph {
	
	// Number of Nodes and edges in our graph.
	private int numNodes;
	private int numEdges;
	
	// An adjacency list of vertices and list of edges.
	// The map stores the number of the node as a key and the Node as a value.
	private Map<Integer, Node> graphNodes;
	
	// Stack to store a newly added vertex.
	Stack<Integer> nodeStack;
	
	// List of all edges to easily retrieve a given edge.
	private Set<Edge> graphEdges;
	
	// To store the edge-betweenness for all edges of CapGraph
	private Map<Edge, Double> edgeBetweennessMap;
	
	public CapGraph() {
		numNodes = 0;
		numEdges = 0;
		graphNodes = new HashMap<>();
		nodeStack = new Stack<>();
		graphEdges = new HashSet<>();
		edgeBetweennessMap = new HashMap<>();
	}

	@Override
	public void addVertex(int num) {
		// Add the vertex with label num to the nodes hashMap and increase numNodes.
		graphNodes.put(num, new Node(num));
		nodeStack.add(num);
		numNodes++;
	}
	
	/*
	 * Calculates the edge-betweenness for all the edges of the CapGraph and stores it in a hashMap
	 *  -- edgeBetweennessMap
	 *  
	 * Edge Betweenness(EB) is defined as the fraction of shortest paths between two distinct vertices 
	 * in a graph which 'flow' through a given edge. A high edge-betweenness measure is an indication 
	 * that the edge is a crucial edge and it links two closely connected communities.
	 */
	public void edgeBetweenness() {
		/*
		 * We reset the values of edge-betweenness for all the existing edges of the graph
		 * as an initialization step. After each step of calculating the betweenness and removing 
		 * the edge with highest edge-betweenness measure, we start over and calculate again 
		 * in order to give better results for the community structure.
		 */
		for (Edge graphEdge : graphEdges) {
			edgeBetweennessMap.put(graphEdge, 0.0);
		}
		
		/*
		 * We do a Breadth First Search from each of the vertices and then calculate the
		 * contribution towards edge betweenness for each of the edges from all the shortest
		 * paths starting at the Node graphNode and ending at all the nodes which are
		 * 'connected' to the graphNode or reachable from graphNode
		 */
		for (Node graphNode : graphNodes.values()) {			
			// vertexStack stores each of the vertices from the lowest or nearest
			// level to the last or farthest level so that when we compute the 
			// betweenness contribution we start from the vertices of the largest level
			// and 'cascade up' till the source vertex going level by level nearer.
			Stack<Node> vertexStack = new Stack<>();
			
			/*
			 * pMap stores the list of predecessors of each vertex in the BFS Tree
			 * starting at the node GraphNode.
			 */
			Map<Integer, List<Node>> pMap = new HashMap<>();
			
			/*
			 * Keeps track of number of shortest paths to each vertex as BFS progresses
			 * key - Node number; value : number of shortest paths.
			 */
			Map<Integer, Integer> shortestPathCount = new HashMap<>();
			
			/*
			 * Keeps track of the 'depth' or the 'level' of each vertex from the graphNode
			 * as the BFS progresses.
			 */
			Map<Integer, Integer> depth = new HashMap<>();
			
			/*
			 * Initialization of data structures required to compute edge betweenness
			 */
			for (int key : graphNodes.keySet()) {
				shortestPathCount.put(key, 0);
				depth.put(key, -1);
				pMap.put(key, new ArrayList<>());
			}
			// graphNode being the source node has only 1 shortest path to itself.
			shortestPathCount.put(graphNode.getLabel(), 1);
			
			// graphNode is at the depth or level 0 from itself.
			depth.put(graphNode.getLabel(), 0);
			
			// BFS starts from node graphNode.
			Queue<Node> q = new LinkedList<>();
			q.add(graphNode);
			while (!q.isEmpty()) {
				Node v = q.remove();
				vertexStack.push(v);
				for (Edge w : v.getAdjList()) {
					
					// get the other end of the edge w where one node
					// of the edge is v and the other node is otherVertex
					int otherVertex = (w.getFromNode() == v.getLabel()) ? w.getToNode() : w.getFromNode();
					
					// Adds the unexplored vertex to the queue and sets 
					// it's level  to one higher than its predecessor v
					if (depth.get(otherVertex) < 0) {
						q.add(graphNodes.get(otherVertex));
						depth.put(otherVertex, depth.get(v.getLabel()) + 1);
					}
					
					// In case v is the predecessor of vertex otherVertex, we set the 
					// increment the shortest path count of vertex otherVertex by that
					// of the predecessor v
					if (depth.get(otherVertex) == depth.get(v.getLabel()) + 1) {
						shortestPathCount.put(otherVertex, 
											shortestPathCount.get(otherVertex) + shortestPathCount.get(v.getLabel()));
						
						/*
						 * We add the node v to the list of predecessors of the vertex otherVertex.
						 *  This step is crucial in order to 'cascade' up from lowest depth 
						 *  towards source while calculating the betweenness contribution.
						 */
						pMap.get(otherVertex).add(v);
					}
				}
			}
			
			/*
			 * delta is a hashmap with key as the vertex label and value as the
			 * sum of centrality updates from edges originating from it.
			 */
			Map<Integer, Double> delta = new HashMap<>();
			for (int key : graphNodes.keySet()) {
				delta.put(key, 0.0);
			}
			
			/*
			 * We cascade up from the vertices which have the lowest depth or
			 * farthest from the graphNode in the BFS Tree of graphNode.
			 */
			while (!vertexStack.isEmpty()) {
				Node w = vertexStack.pop();
				for (Node vertex : pMap.get(w.getLabel())) {
					/*
					 * The update for an edge corresponds to the number of shortest paths to the
					 * originating vertex times the ratio of all the updates for all the edges originating
					 * from the edge’s destination vertex over the number of shortest paths to destination
					 * vertex plus one.
					 */
					double value = shortestPathCount.get(vertex.getLabel()) * 
									(delta.get(w.getLabel())/shortestPathCount.get(w.getLabel()) + 1);
					
					/*
		 			 * We keep track of all the edge centrality updates from all
		 			 * edges originating from each of the vertex in the BFS Tree 
		 			 * from nodeGraph.
					 */
					delta.put(vertex.getLabel(), delta.get(vertex.getLabel()) + value);
					
					Edge edge = new Edge(w.getLabel(), vertex.getLabel());
					//System.out.println(edge);
					//System.out.println(graphEdges.contains(edge));
					/*
					 * update the edge centrality contributions from bottom up
					 */
					edgeBetweennessMap.put(edge, edgeBetweennessMap.get(edge) + value);
				}
			}
		}
	}

	@Override
	public void addEdge(int from, int to) {
		// Add edge between to and from nodes. The directed edge is in the adjacency list of from.
		Node fromNode = graphNodes.get(from);
		Edge edge = new Edge(from, to);
		fromNode.getAdjList().add(edge);
		// Do not add while calculating egonet
		graphNodes.get(to).getAdjList().add(edge);
		if (!graphEdges.contains(edge)) {
			graphEdges.add(edge);
			numEdges++;
		}
	}
	
	public boolean isEdge(int i, int j) {
		return (graphEdges.contains(new Edge(i, j)) || graphEdges.contains(new Edge(j, i)));
	}
	
	public void printEdges() {
		for (Edge edge : graphEdges) {
			System.out.println(edge.getFromNode() + " " + edge.getToNode());
		}
	}
	
	public Set<Integer> getNodes() {
		return graphNodes.keySet();
	}

	/* (non-Javadoc)
	 * @see graph.Graph#getEgonet(int)
	 * We get the egonet of a graph
	 * 
	 * It is a measure of how well connected the neighbors of 
	 * the node 'center' are among each other.
	 * If the return Graph is a very sparse graph, we can conclude
	 * that the egonet, that is the neighbors are not very well connected
	 * among themselves and it is not a close knit community.
	 */
	@Override
	public Graph getEgonet(int center) {
		Node centerNode = graphNodes.get(center);
		Graph egoGraph = new CapGraph();
		// Add the center node
		egoGraph.addVertex(center);
		for (Edge adjEdge : centerNode.getAdjList()) {
			// Add end vertex and add the edge
			egoGraph.addVertex(adjEdge.getToNode());
			egoGraph.addEdge(center, adjEdge.getToNode());
		}
		Set<Integer> neighbors = getNeighbors(center);
		// Add all edges of neighbors in the egoGraph and not connected to center
		for (int nodeInt : neighbors) {
			Node node = graphNodes.get(nodeInt);
			for (Edge edgeOfList : node.getAdjList()) {
				Node otherNode = graphNodes.get(edgeOfList.getToNode());
				if (neighbors.contains(otherNode.getLabel()))
					egoGraph.addEdge(node.getLabel(), edgeOfList.getToNode());
			}
		}
		return egoGraph;
	}
	
	/*
	 * Returns the set of neighbors of the node with vertex label 'center'.
	 */
	public HashSet<Integer> getNeighbors(int center) {
		HashSet<Integer> neighbors = new HashSet<>();
		Node centerNode = graphNodes.get(center);
		for (Edge centerEdge : centerNode.getAdjList()) {
			int endNode = (centerEdge.getToNode() == center) ? centerEdge.getFromNode() : centerEdge.getToNode();
			neighbors.add(endNode);
		}
		return neighbors;
	}
	
	@Override
	public List<Graph> getSCCs() {
		Set<Integer> visited = new HashSet<>();
		Stack<Integer> finished = new Stack<>();
		while (!nodeStack.isEmpty()) {
			int v = nodeStack.pop();
			if (!visited.contains(v)) {
				DFSVisit(this, v, visited, finished);
			}
		}
		visited = new HashSet<>();
		CapGraph reverseGraph = this.reverse();
		List<Graph> graphList = new ArrayList<Graph>();
		while (!finished.isEmpty()) {
			int w = finished.pop();
			if (!visited.contains(w)) {
				CapGraph sccGraph = new CapGraph();
				DFSVisitReverse(sccGraph, reverseGraph, w, visited);
				graphList.add(sccGraph);
			}
		}
		return graphList;
	}
	
	/*
	 * Reverse DFS to find out the strongly connected components
	 * of the given graph
	 */
	private void DFSVisitReverse(CapGraph scCapGraph, CapGraph reverseGraph, int w, Set<Integer> visited) {
		visited.add(w);
		scCapGraph.addVertex(w);
		for (int n : reverseGraph.getNeighbors(w)) {
			if (!visited.contains(n)) {
				DFSVisitReverse(scCapGraph, reverseGraph, n, visited);
			}
		}
	}
	
	/*
	 * Depth First Search from vertex v
	 */
	private void DFSVisit(CapGraph capGraph, int v, Set<Integer> visited, Stack<Integer> finished) {
		visited.add(v);
		for (int n : getNeighbors(v)) {
			if (!visited.contains(n)) {
				DFSVisit(capGraph, n, visited, finished);
			}
		}
		finished.push(v);
	}
	
	
	// Reverse the Capgraph edges
	private CapGraph reverse() {
		CapGraph reverseGraph = new CapGraph();
		for (int nodeInt : graphNodes.keySet()) {
			reverseGraph.addVertex(nodeInt);
		}
		for (Edge graphEdge : graphEdges) {
			reverseGraph.addEdge(graphEdge.getToNode(), graphEdge.getFromNode());
		}
		return reverseGraph;
	}

	@Override
	public HashMap<Integer, HashSet<Integer>> exportGraph() {
		HashMap<Integer, HashSet<Integer>> mapGraph = new HashMap<>();
		for (int nodeInt : graphNodes.keySet()) {
			mapGraph.put(nodeInt, getNeighbors(nodeInt));
		}
		return mapGraph;
	}
	
	public int getNumNodes() {
		return numNodes;
	}
	
	public int getNumEdges() {
		return numEdges;
	}

	/*
	 * Print the different communities after removing the edge with
	 * highest betweenness centrality
	 */
	public void printGraph() {
		Set<Integer> visited = new HashSet<>();
		for (Node x : graphNodes.values()) {
			if (!visited.contains(x.getLabel())) {
				DFSNode(x.getLabel(), visited);
				System.out.println();
			}
		}
	}
	
	/*
	 * Helper method for DFS in printGraph
	 */
	private void DFSNode(int v, Set<Integer> visited) {
		visited.add(v);
		System.out.print(v + " ");
		for (int n : getNeighbors(v)) {
			if (!visited.contains(n)) {
				DFSNode(n, visited);
			}
		}
	}
	
	/*
	 * Removes the edge with highest betweenness from the graph and edge betweenness map 
	 * and the adjacent lists of the head and the tail.
	 */
	private void removeEdge(Edge graphEdge) {
		graphEdges.remove(graphEdge);
		edgeBetweennessMap.remove(graphEdge);
		Node node1 = graphNodes.get(graphEdge.getFromNode());
		Node node2 = graphNodes.get(graphEdge.getToNode());
		node1.getAdjList().remove(new Edge(graphEdge.getFromNode(), graphEdge.getToNode()));
		node2.getAdjList().remove(new Edge(graphEdge.getFromNode(), graphEdge.getToNode()));
		node1.getAdjList().remove(new Edge(graphEdge.getToNode(), graphEdge.getFromNode()));
		node2.getAdjList().remove(new Edge(graphEdge.getToNode(), graphEdge.getFromNode()));
		numEdges--;
	}
	
	/*
	 * Returns the list of edges with the maximum measure of betweenness centrality. 
	 * We then remove all the edges with the maximum BW centrality measure
	 */
	private List<Edge> getMaxBW() {
		double maxBW = 0;
		List<Edge> maxBWEdgeList = new ArrayList<>();
		for (Edge graphEdge : edgeBetweennessMap.keySet()) {
			if (edgeBetweennessMap.get(graphEdge) > maxBW) {
				maxBW = edgeBetweennessMap.get(graphEdge);
			}
		}
		for (Edge graphEdge : edgeBetweennessMap.keySet()) {
			if (edgeBetweennessMap.get(graphEdge) == maxBW) {
				maxBWEdgeList.add(graphEdge);
			}
		}
		return maxBWEdgeList;
	}
	
	/*
	 * Program driver for the Girvan Newman algorithm for detecting 
	 * communities using edge betweenness centrality measure.
	 */
	public static void main(String args[]) {
		CapGraph graph = new CapGraph();
		GraphLoader.loadGraph(graph, "./data/football.txt");
		
		/*
		 * We remove the edges with highest betweenness 
		 * one after another until none of the edges
		 * are remaining. This is hence a divisive 
		 * community detection algorithm.
		 * The end result is a dendrogram
		 * A dendrogram  is a tree diagram frequently used to illustrate 
		 * the arrangement of the clusters produced by hierarchical clustering
		 */
		while (graph.getNumEdges() > 0) {
			/*
			 * Recalculate the edge betweenness every time we remove
			 * the edges with the highest edge betweennness measure
			 */
			graph.edgeBetweenness();
			
			/*
			 * List of edges with highest Edge BW measure
			 */
			List<Edge> maxBW = graph.getMaxBW();
			/*
			 * Remove all edges with highest Edge BW
			 */
			for (Edge maxBWEdge : maxBW)
				graph.removeEdge(maxBWEdge);
			
			// At each step print the resulting communities
			graph.printGraph();
		}	
	}

}
