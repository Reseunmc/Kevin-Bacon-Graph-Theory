import java.util.*;

/**
 * @authors David Park and Reseun McClendon
 * GraphLibrary class containing methods for bfs, getting paths, finding missing vertices, and finding average separation
 *
 */
public class GraphLibrary {
	
	/**
	 * This method performs bfs on a given graph, returning a "shortest path tree"
	 * graph from all the vertices in the graph to the source.
	 * @param g
	 * @param source
	 * @return
	 */
	public static <V,E> Graph<V,E> bfs(Graph<V,E> g, V source) {
		//Initialize a queue and a graph
		Queue<V> queue = new LinkedList<V>();
		Graph<V, E> shortest_path_tree = new AdjacencyMapGraph<V, E>();
		
		//Add the source or root to the queue and the graph
		queue.add(source);
		shortest_path_tree.insertVertex(source);
		
		//While the queue isn't empty
		while (!queue.isEmpty()) {
			//Take the first thing in the queue out and return it
			V current = queue.poll();
			Iterator<V> out_iterator = g.outNeighbors(current).iterator();
			
			//Iterate through all of its neighbors
			while(out_iterator.hasNext()) {
				V child = out_iterator.next();
				
				//If the shortest path tree doesnt have the neighbor/child add it to both the tree and the queue
				if (!shortest_path_tree.hasVertex(child)) {
					shortest_path_tree.insertVertex(child);
					shortest_path_tree.insertDirected(child, current, g.getLabel(child, current));
					queue.add(child);
				}
			}
		}
		return shortest_path_tree;
	}
	
	/**
	 * Method that returns a list representing a path given a shortest path tree
	 * graph and a vertex that represents the beginning of the path to the root
	 * @param tree
	 * @param v
	 * @return
	 */
	public static <V,E> List<V> getPath(Graph<V,E> tree, V v) {
		//Initialize a path and a stack and add the starting point to both
		List<V> path = new ArrayList<V>();
		path.add(v);
		
		Stack<V> stack = new Stack<V>();
		stack.push(v);
		
		//While the stack isn't empty
		while (!stack.isEmpty()) {
			V current = stack.pop();
			
			//If the current vertex doesnt point to anything, return the path
			if (tree.outDegree(current) == 0) {
				return path;
			}
			
			//If it does, then iterate through all of its neighbors (should be only 1)
			else {
				Iterable<V> neighbors = tree.outNeighbors(current);
				for (V vertex : neighbors) {
					stack.push(vertex);	//Add the vertex that current is directed to to the stack
					path.add(vertex);	//and the path
				}
			}
		}
		return path;
	}
	
	/**
	 * Method that returns a set of vertices in the graph
	 * but not in the subgraph (shortest path tree)
	 * that are not in 
	 * @param graph
	 * @param subgraph
	 * @return
	 */
	public static <V,E> Set<V> missingVertices(Graph<V,E> graph, Graph<V,E> subgraph) {
		//Initialize a set for holding all the vertices
		Set<V> missingVertices = new HashSet<V>();
	
		Iterable<V> verticesInGraph = graph.vertices();
		
		//Iterate through all the vertices in the graph
		for (V vertex: verticesInGraph) {
			
			//If the subgraph doesnt have the vertex in the graph
			if (!subgraph.hasVertex(vertex)) {
				//Add it to the set
				missingVertices.add(vertex);
			}
		}
		return missingVertices;
		
	}
	
	/**
	 * Method that returns the average distance or separation from the root
	 * in a subgraph (or shortest path tree)
	 * @param tree
	 * @param root
	 * @return
	 */
	public static <V,E> double averageSeparation(Graph<V,E> tree, V root) {
		//Keep a total here that is not updated in the helper method
		int total = 0;
		//Create a nodes variable (that is a double) that holds the number of vertices within the graph
		double nodes = tree.numVertices();
		
		//Call the helper method to update the total
		total+=averageSeparationHelper(tree, root, total);
		
		return (total/nodes);
		
	}
	
	/**
	 * Helper method for calculating the average distance or separation from
	 * the root in a subgraph (or shortest path tree)
	 * Does the recursion needed 
	 * @param tree
	 * @param root
	 * @param height
	 * @param total
	 * @return
	 */
	public static <V,E> double averageSeparationHelper(Graph<V,E> tree, V branches, int height) {
		//Create a running total double here (separate from in the main method b/c we found that it wasnt being updated
		double totald = height;
		
		//For each of the neighbor vertices
		for(V vertex : tree.inNeighbors(branches)){
			//Recursively call and update total while incrementin the height/or level of separation away from branches
			totald += averageSeparationHelper(tree, vertex, height+1);
		}
		
		return totald;
	}
}
