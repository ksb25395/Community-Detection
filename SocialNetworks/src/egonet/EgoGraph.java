/**
 * 
 */
package egonet;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Queue;
import java.util.Set;
import java.util.Stack;
import java.util.TreeSet;

import com.sun.org.apache.bcel.internal.generic.NEW;

import graph.Graph;
import util.GraphLoader;

/**
 * @author Bhargav Ram K S 
 *
 * Here we find the EgoNetwork of a given node.
 */
public class EgoGraph implements Graph {
	
	//an adjacency list of vertices and list of Edges
	//The map stores the number of the node as a key
	//and the Node as a value
	private Map<Integer, Node> graphNodes;
	
	//As we add a vertex, we store it in a stack nodeStack
	Stack<Integer> nodeStack = new Stack<>();
	
	//List of all edges to easily retrieve a given edge
	private List<Edge> graphEdges;
	
	//Number of Nodes and edges in our graph
	private int numNodes;
	private int numEdges;
	
	/*
	 * intialize all the fields described above
	 */
	public EgoGraph() {
		graphNodes = new HashMap<>();
		graphEdges = new ArrayList<>();
		numEdges = 0;
		numNodes = 0;
	}
	
	/* (non-Javadoc)
	 * @see graph.Graph#addVertex(int)
	 */
	@Override
	public void addVertex(int num) {
		// Add the vertex with label num to the nodes hashMap and increase numNodes
		graphNodes.put(num, new Node(num));
		nodeStack.add(num);
		numNodes++;
	}

	/* (non-Javadoc)
	 * @see graph.Graph#addEdge(int, int)
	 */
	@Override
	public void addEdge(int from, int to) {
		// add edge between to and from nodes. The directed edge is in the adj list of from
		Node fromNode = graphNodes.get(from);
		Edge edge = new Edge(from, to);
		fromNode.getAdjList().add(edge);
		graphEdges.add(edge);
		numEdges++;
	}
	
	public boolean isEdge(int i, int j) {
		return graphEdges.contains(new Edge(i, j));
	}
	
	public void printEdges() {
		for(Edge edge : graphEdges) {
			System.out.println(edge.getFromNode() + " " + edge.getToNode());
		}
	}
	
	public Set<Integer> getNodes(){
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
		long time = System.nanoTime();
		Node centerNode = graphNodes.get(center);
		Graph egoGraph = new EgoGraph();
		//add the center node
		egoGraph.addVertex(center);
		for(Edge adjEdge : centerNode.getAdjList()) {
			//add other end of the edge and add the edge
			int otherEnd = adjEdge.getToNode();
			egoGraph.addVertex(otherEnd);
			egoGraph.addEdge(center, otherEnd);
		}
		Set<Integer> neighbors = getNeighbors(center);
		//add all edges of neighbors in the egoGraph and not connected to center
		for(int nodeInt : neighbors) {
			Node node = graphNodes.get(nodeInt);
			for(Edge edgeOfList : node.getAdjList()) {
				
				if(neighbors.contains(edgeOfList.getToNode()))
					egoGraph.addEdge(node.getLabel(), edgeOfList.getToNode());
			}
		}
		System.out.print((double)(System.nanoTime() - time)/1000000000);
		return egoGraph;
	}
	
	/*
	 * Returns the set of neighbors of the node 
	 * with vertex label 'center'
	 */
	public HashSet<Integer> getNeighbors(int center){
		HashSet<Integer> neighbors = new HashSet<>();
		Node centerNode = graphNodes.get(center);
		for(Edge centerEdge : centerNode.getAdjList()) {
			int endNode = centerEdge.getToNode();
			neighbors.add(endNode);
		}
		return neighbors;
	}
	

	/* (non-Javadoc)
	 * @see graph.Graph#getSCCs()
	 */
	@Override
	public List<Graph> getSCCs() {
		Set<Integer> visited = new HashSet<>();
		Stack<Integer> finished = new Stack<>();
		while(!nodeStack.isEmpty()) {
			int v = nodeStack.pop();
			if(!visited.contains(v)) {
				DFSVisit(this, v, visited, finished);
			}
		}
		visited = new HashSet<>();
		EgoGraph reverseGraph = this.reverse();
		List<Graph> graphList = new ArrayList<Graph>();
		while(!finished.isEmpty()) {
			int w = finished.pop();
			if(!visited.contains(w)) {
				EgoGraph sccGraph = new EgoGraph();
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
	private void DFSVisitReverse(EgoGraph scEgoGraph, EgoGraph reverseGraph, int w, Set<Integer> visited) {
		visited.add(w);
		scEgoGraph.addVertex(w);
		for(int n : reverseGraph.getNeighbors(w)) {
			if(!visited.contains(n)) {
				DFSVisitReverse(scEgoGraph, reverseGraph, n, visited);
			}
		}
	}
	
	/*
	 * Depth First Search from vertex v
	 */
	private void DFSVisit(EgoGraph EgoGraph, int v, Set<Integer> visited, Stack<Integer> finished) {
		visited.add(v);
		for(int n : getNeighbors(v)) {
			if(!visited.contains(n)) {
				DFSVisit(EgoGraph, n, visited, finished);
			}
		}
		finished.push(v);
	}
	
	
	//reverse the EgoGraph edges
	public EgoGraph reverse() {
		EgoGraph R = new EgoGraph();
		for(int nodeInt : graphNodes.keySet()) {
			R.addVertex(nodeInt);
		}
		for(Edge graphEdge : graphEdges) {
			R.addEdge(graphEdge.getToNode(), graphEdge.getFromNode());
		}
		return R;
	}

	/* (non-Javadoc)
	 * @see graph.Graph#exportGraph()
	 */
	@Override
	public HashMap<Integer, HashSet<Integer>> exportGraph() {
		HashMap<Integer, HashSet<Integer>> mapGraph = new HashMap<>();
		for(int nodeInt : graphNodes.keySet()) {
			mapGraph.put(nodeInt, getNeighbors(nodeInt));
		}
		return mapGraph;
	}
	
	//Number of Nodes
	public int getNumNodes() {
		return numNodes;
	}
	
	//Number of Edges
	public int getNumEdges() {
		return numEdges;
	}

	//Print the graph
	public void printGraph() {
		Set<Integer> visited = new HashSet<>();
		for(Node x : graphNodes.values()) {
			if(!visited.contains(x.getLabel())) {
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
		for(int n : getNeighbors(v)) {
			if(!visited.contains(n)) {
				DFSNode(n, visited);
			}
		}
	}
	
	/*
	 * Program driver for the Finding the egonet for a given node.
	 */
	public static void main(String args[]) {
		long time = System.nanoTime();
		Graph graph = new EgoGraph();
		GraphLoader.loadGraph(graph, "./data/facebook_ucsd.txt");
		Graph egoNetGraph = graph.getEgonet(0);
		System.out.println(egoNetGraph.exportGraph());
		System.out.println((double)(System.nanoTime() - time)/1000000000);
	}

}
