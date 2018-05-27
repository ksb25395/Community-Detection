package graph;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import util.GraphLoader;

/**
 * @author Bhargav Ram K S 
 * 
 * Implementation of Newman's Fast Greedy agglomerative algorithm for community
 * detection.We calculate the communities using the idea that best choices for 
 * communities happen when the 'quality function', the modularity Q is maximized.
 * 
 * We greedily merge two communities which result in the max increase or minimum
 * decrease in dQ, the change in modularity resulting from merging two communities.
 * 
 * Modularity maximization is an NP Hard problem so what we are now doing is 
 * an approximation algorithm.
 * 
 */
public class FastNewmanImpl {
	
	// Input graph on which algorithm operates.
	private CapGraph inputGraph;
	
	// Number of steps of iteration
	private final int numSteps;
	
	/*
	 * result contains the set of communities for each
	 * of the numSteps + 1 number of iterations 
	 * For example, the key 0 contains just the collection
	 * of vertices as independent communities and as we
	 * keep merging the subsequent steps contain the 
	 * communities with each community having the lowest
	 * member as the 'leader'.
	 */
	Map<Integer, Map<Integer, List<Integer>>> result;
	
	public FastNewmanImpl(CapGraph graph) {
		this.inputGraph = graph;
		int V = inputGraph.getNumNodes();
		
		// V - 1 iterations of running the cluster() method.
		numSteps = V - 1;
		
		// Initialize result.
		result = new HashMap<Integer, Map<Integer, List<Integer>>>();
		
		// Initial list of communities with each node as its own community.
		Map<Integer, List<Integer>> init = new HashMap<>();
		
		for (int node : inputGraph.getNodes()) {
			List<Integer> list = new ArrayList<>();
			list.add(node);
			init.put(node, list);
		}
		//System.out.println(init + "\n\n\n");
		result.put(0, init);
	}
	
	public void merge() {
		// When all nodes are disconnected, modularity = 0.
		double mod = 0;
		
		/*
		 * Join two communities with highest increase in dQ each of the numsSteps steps.
		 */
		for (int i = 1; i <= numSteps; i++) {
			int prev = i - 1;
			
			// previous community arrangement for iteration i - 1
			Map<Integer, List<Integer>> prevCommunity = result.get(prev);
			
			// We find out the maximum increase in modularity upon merging any two communities.
			double deltaQ = Double.NEGATIVE_INFINITY;
			int communityIdOne = -1;
			int communityIdTwo = -1;
			
			for (Integer keyOne : prevCommunity.keySet()) {
				List<Integer> commOne = prevCommunity.get(keyOne);
				for (Integer keyTwo : prevCommunity.keySet()) {
					List<Integer> commTwo = prevCommunity.get(keyTwo);
					
					if(keyOne == keyTwo) {
						continue;
					}
					
					// Calculate deltaQ upon merging commOne and commTwo.
					double tempDeltaQ = new FastNewman(inputGraph).dQ(commOne, commTwo);
					
					// Update if tempdeltaQ is higher than deltaQ.
					if(tempDeltaQ > deltaQ) {
						deltaQ = tempDeltaQ;
						communityIdOne = keyOne;
						communityIdTwo = keyTwo;
					} else if (tempDeltaQ == deltaQ) {
						//System.out.println("equal" + tempDeltaQ);
					}
				}
			}
			// Increment modularity with max deltaQ.
			mod += deltaQ;
			
			// Get vertex label lists of the requisite communities.
			List<Integer> commOneList = prevCommunity.get(communityIdOne);
			List<Integer> commTwoList = prevCommunity.get(communityIdTwo);
			
			// Merge both the communities.
			List<Integer> mergeCommList = new ArrayList<>();
			mergeCommList.addAll(commOneList);
			mergeCommList.addAll(commTwoList);
			
			// Update leaders for communities.
			Map<Integer, List<Integer>> currentCommunity = new HashMap<>();
			
			for (Integer prevKey : prevCommunity.keySet()) {
				if (prevKey != communityIdOne && prevKey != communityIdTwo) {
					// Keep leaders of other communities the same.
					currentCommunity.put(prevKey, prevCommunity.get(prevKey));
				} else {
					//System.out.println(deltaQ + " " + prevKey + ": Merge!");
				}
			}
			
			// Update the leader for merged community.
			int lesser = (communityIdOne < communityIdTwo) ? communityIdOne : communityIdTwo;
			currentCommunity.put(lesser, mergeCommList);
			
			// Update results of current iteration.
			result.put(i, currentCommunity);
			
			//System.out.println(currentCommunity + "\n\n\n");
			
			/*
			 * Every time a maxima of Q is reached, in the next iteration
			 * dQ < 0. Then we can print the previous community as a
			 * 'greedily' optimal community arrangement since it has 
			 * locally maximal modularity. 
			 */
			if (deltaQ < 0) {
				// maxima modularity
				System.out.println(mod - deltaQ);
				// maxima community
				System.out.println(prevCommunity);
			}
		}
	}

	// Main Driver for merge().
	public static void main(String[] args) {
		long begin = System.nanoTime();
		CapGraph g = new CapGraph();
		GraphLoader.loadGraph(g, "./data/foodweb_big.txt");
		
		FastNewmanImpl impl = new FastNewmanImpl(g);
		
		impl.merge();
		System.out.println("\n\n\n" + (double)(System.nanoTime() - begin) / 1000000000);
		
		/*
		 * 	Karate club results
		 * 
		 * 	0.45378647326699273
			{0=[16, 0, 27, 30, 24, 28, 33, 9, 31, 19, 21, 23, 15], 
			1=[1, 6, 17, 7, 5, 11, 12, 20], 
			2=[2, 18, 22, 3, 10, 4, 13, 8, 14], 
			25=[32, 25, 26, 29]}
			
			0.44619666048237483
			{0=[16, 0, 27, 30, 24, 28, 33, 9, 31, 19, 21, 23, 15], 
			1=[1, 6, 17, 7, 5, 11, 12, 20, 2, 18, 22, 3, 10, 4, 13, 8, 14], 
			25=[32, 25, 26, 29]}
			
			0.43270365997638727
			{0=[16, 0, 27, 30, 24, 28, 33, 9, 31, 19, 21, 23, 15, 32, 25, 26, 29], 
			1=[1, 6, 17, 7, 5, 11, 12, 20, 2, 18, 22, 3, 10, 4, 13, 8, 14]}

		 */
		
		/*
		 * 	Political BOOKS
		 * 
		 * 	0.5155053707045932
			{1=[1, 2, 4, 11, 16, 17, 56, 20, 13, 19, 7, 6, 5, 3, 8, 15, 26, 23, 12, 30, 18, 14, 22, 9, 33, 24, 34, 45, 27, 41, 48, 46, 25, 28, 10, 42, 55, 21, 54, 43, 40, 47, 44, 57, 36, 38, 39, 35, 37],
			 49=[49, 58, 50], 
			 51=[51, 68, 104, 105, 52, 70, 65, 66, 69, 59, 53, 86], 29=[29, 93, 88, 99, 78, 91, 92, 32, 79, 71, 81, 72, 84, 77, 76, 83, 73, 80, 75, 31, 89, 103, 82, 90, 94, 67, 98, 97, 95, 74, 102, 87, 101, 85, 60, 64, 61, 63, 100, 62, 96]}
			
			0.5148009317105526
			{1=[1, 2, 4, 11, 16, 17, 56, 20, 13, 19, 7, 6, 5, 3, 8, 15, 26, 23, 12, 30, 18, 14, 22, 9, 33, 24, 34, 45, 27, 41, 48, 46, 25, 28, 10, 42, 55, 21, 54, 43, 40, 47, 44, 57, 36, 38, 39, 35, 37], 
			49=[49, 58, 50, 51, 68, 104, 105, 52, 70, 65, 66, 69, 59, 53, 86], 
			29=[29, 93, 88, 99, 78, 91, 92, 32, 79, 71, 81, 72, 84, 77, 76, 83, 73, 80, 75, 31, 89, 103, 82, 90, 94, 67, 98, 97, 95, 74, 102, 87, 101, 85, 60, 64, 61, 63, 100, 62, 96]}
			
			0.4607185277739213
			{1=[1, 2, 4, 11, 16, 17, 56, 20, 13, 19, 7, 6, 5, 3, 8, 15, 26, 23, 12, 30, 18, 14, 22, 9, 33, 24, 34, 45, 27, 41, 48, 46, 25, 28, 10, 42, 55, 21, 54, 43, 40, 47, 44, 57, 36, 38, 39, 35, 37], 
			29=[49, 58, 50, 51, 68, 104, 105, 52, 70, 65, 66, 69, 59, 53, 86, 29, 93, 88, 99, 78, 91, 92, 32, 79, 71, 81, 72, 84, 77, 76, 83, 73, 80, 75, 31, 89, 103, 82, 90, 94, 67, 98, 97, 95, 74, 102, 87, 101, 85, 60, 64, 61, 63, 100, 62, 96]}
		 */
	}

}
