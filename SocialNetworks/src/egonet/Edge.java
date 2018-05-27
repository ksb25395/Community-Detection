package egonet;

public class Edge implements Comparable<Edge> {
	
	private int fromNode;
	private int toNode;
	
	public Edge(int fromNode, int toNode) {
		this.fromNode = fromNode;
		this.toNode = toNode;
	}
	
	public int getFromNode() {
		return fromNode;
	}
	
	public int getToNode() {
		return toNode;
	}
	
	@Override
	public int compareTo(Edge o) {
		if (this.fromNode < o.fromNode && this.toNode < o.toNode) {
			return -1;
		} else if (this.fromNode > o.fromNode && this.toNode > o.toNode) {
			return 1;
		} else {
			return 0;
		}
	}
	
	@Override
	public boolean equals(Object obj) {
		Edge other = (Edge) obj;
		// in case of ego net change to 
		// return (this.fromNode == other.fromNode && this.toNode == other.toNode);
		return (this.fromNode == other.fromNode && this.toNode == other.toNode);
	}
	
	@Override
	public int hashCode() {
		return 31 + this.fromNode + this.toNode;
	}
	
	@Override
	public String toString() {
		return "(" + this.fromNode + ", " + this.toNode + ")";
	}
	
	public boolean isTailOrHead(int vertex) {
		return (fromNode == vertex || toNode == vertex);
	}
}
