package egonet;

import java.util.ArrayList;
import java.util.List;

public class Node {
	
	private int nodeLabel;
	private List<Edge> adjList;
	
	public Node(int nodeLabel) {
		this.nodeLabel = nodeLabel;
		adjList = new ArrayList<>();
	}
	
	public int getLabel() {
		return nodeLabel;
	}
	
	public List<Edge> getAdjList() {
		return adjList;
	}
	
	@Override
	public boolean equals(Object obj) {
		return this.nodeLabel == ((Node) obj).nodeLabel;
	}
	
	@Override
	public int hashCode() {
		return 31 + this.nodeLabel + this.adjList.hashCode();
	}
	
	@Override
	public String toString() {
		return this.nodeLabel + "";
	}
	
}
