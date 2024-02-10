import java.util.*;
import java.io.*;

/**
 * @authors David Park and Reseun McClendon
 * Kevin Bacon Class containing methods for reading files and creating IDmaps/bacon graphs.
 * Also contains methods for converting a path name to a recognizable string,
 * a hand coded test method, a method that returns a set of actors that are x number of 
 * connections away from the source, and methods that sort by degree and avg separation
 *
 */
public class KevinBacon {
	
	/**
	 * Method that parses the text files and creates ID maps
	 * @param filename
	 * @return
	 */
	public static Map<Integer, String> createIDMap (String filename) {
		//Initialize an Integer, String map that maps ID # keys to actor/movie names
		Map<Integer, String> IDmap = new HashMap<Integer, String>();
		BufferedReader input = null;
        
	    //Try-catch the attempt to open the file
	    try {
	    	 input = new BufferedReader(new FileReader(filename));
	    } 
	     
	    catch (FileNotFoundException e) {
	         System.err.println("Cannot open file.\n" + e.getMessage());
	    }
	    
	    //Try-catch the reading of the file and creation of the map
	    try {
	    	String line;
	    	//Read the file line by line
	    	while ((line = input.readLine()) != null) {
	    		
	    		//Split each line by the "|" character and put them into arrays
	    		String[] items = line.split("\\|");
	    		
	    		//Set parse the first array's integer and set it to the IDnumber
	    		int IDnumber = Integer.parseInt(items[0]);
	    		//The second array contains the movie or actor name
	    		String actor_or_movie_name = items[1];
	    		IDmap.put(IDnumber, actor_or_movie_name);	//Put in map
	    	}
	    }
	     
	    catch (IOException e) {
	    	System.err.println("IO error while reading.\n" + e.getMessage());
	    }
	    	 
	    //Try-Catch the closing of the file
	    try {
	    	input.close();
		}
	     
		catch (IOException e) {
			System.err.println("Cannot close file.\n" + e.getMessage());
		}
	     
	    return IDmap;
	}
	
	/**
	 * Method that takes a movie IDmap and an Actor IDmap as well as a file that essentially
	 * relates Movie IDs to Actors' IDs. First creates a map that maps Movies as keys to sets of 
	 * actors appearing in the movie as the value. Then Creates a graph that has actor names as 
	 * vertices and movie names as edges
	 * 
	 * @param movieIDMap
	 * @param actorIDMap
	 * @param filename
	 * @return
	 */
	public static Graph<String, Set<String>> createGraph (Map<Integer,String> movieIDMap, Map<Integer,String> actorIDMap, String filename) {
		//Initialize the bacon graph and the movie to actors map
		Graph<String, Set<String>> baconGraph = new AdjacencyMapGraph<String, Set<String>>();
		Map<String, ArrayList<String>> MovietoActorsMap = new HashMap<String, ArrayList<String>>();
		BufferedReader input = null;
		
	    //Try-catch the attempt to open the file
	    try {
	    	 input = new BufferedReader(new FileReader(filename));
	    } 
	     
	    catch (FileNotFoundException e) {
	         System.err.println("Cannot open file.\n" + e.getMessage());
	    }
	    
	    //Try-catch the reading of the file, creation of the map, and creation of the graph
	    try {
	    	String line;
	    	//Read through the file line by line
	    	while ((line = input.readLine()) != null) {
	    		//Split each line and parse the integers out as ID #'s
	    		String[] items = line.split("\\|");
	    		int movieID = Integer.parseInt(items[0]);
	    		int actorID = Integer.parseInt(items[1]);
	    		
	    		//Get the names of the movies/actors from the maps using the ID #'s
	    		String movieName = movieIDMap.get(movieID);
	    		String actorName = actorIDMap.get(actorID);
	    		
	    		//If the movie to actors map contains the movie as a key already
	    		if (MovietoActorsMap.containsKey(movieName)) {
	    			//Add the associated actor's name to the value arraylist
	    			MovietoActorsMap.get(movieName).add(actorName);
	    		}
	    		
	    		//If the movie to actors map does not contain the movie as a key already
	    		else {
	    			//Create a new actor list (arraylist)
	    			ArrayList<String> ActorList = new ArrayList<String>();
	    			
	    			//Add the actor to the arraylist and put the movie name actor's list key-value pairing into the map
	    			ActorList.add(actorName);
	    			MovietoActorsMap.put(movieName, ActorList);
	    		}
	    	}
			//For each ID in the actor ID map
			for (int ID: actorIDMap.keySet()){
				//Insert the actor's name into the graph as a vertex
				baconGraph.insertVertex(actorIDMap.get(ID));
			}
			
			//For each movie in the movie to actors map
			for (String movie: MovietoActorsMap.keySet()){
				//Obtain the actor's list
				ArrayList<String> actors = MovietoActorsMap.get(movie);
				
				//If the size of the list is greater than 1
				if (actors.size() > 1) {
					//Iterate through the list twice once for each actor and once for each proceeding actor in the list
					for (int i = 0; i < actors.size(); i++) {
						for (int next = i+1; next < actors.size(); next++) {
							//If the graph doesnt already have an edge between an actor and the next one in the list
							if (!baconGraph.hasEdge(actors.get(i), actors.get(next))) {
								//Create a new set and add the movie name into it, then create a new edge with the set as the label
								Set<String> movies = new HashSet<String>();
								movies.add(movie);
								baconGraph.insertUndirected(actors.get(i), actors.get(next), movies);
							}
							else {
								//Otherwise add the movie name to the label
								baconGraph.getLabel(actors.get(i), actors.get(next)).add(movie);
							}
						}
					}
				}
			}
	    }
	    
	    catch (IOException e) {
	    	System.err.println("IO error while reading or making map/graph. \n" + e.getMessage());
	    }
	    	 
	    //Try-Catch the closing of the file
	    try {
	    	input.close();
		}
	     
		catch (IOException e) {
			System.err.println("Cannot close file.\n" + e.getMessage());
		}
	     
	    return baconGraph;
	}
	
	/**
	 * Method for obtaining a set of actors that are x number of connections/steps away from the center
	 * @param steps
	 * @param subgraph
	 * @return
	 */
	public static Set<String> numberActors (int steps, Graph<String, Set<String>> subgraph) {
		//Initialize a new set to contain the actors
		Set<String> numberActors = new HashSet<String>();
		//Iterate through all the vertices within the subgraph
		for (String vertex: subgraph.vertices()) {
			//If the path length from subgraph vertex to the center -1 is = to the number of steps add it to the set
			if (GraphLibrary.getPath(subgraph, vertex).size() - 1 == steps) {
				numberActors.add(vertex);
			}
		}
		return numberActors;
	}
	
	/**
	 * Method for obtaining a string form of the path,
	 * ex output) "x actor starred in x movie with y actor ..."
	 * @param center
	 * @param path
	 * @param subgraph
	 * @return
	 */
	public static String printpath (String center, List<String> path, Graph<String, Set<String>> subgraph) {
		//Initialize an empty string
		String pathstring = "";
		
		//For each actor in the path, add the actor's name + some sensible words to the empty string
		for (int i = 0; i + 1 < path.size(); i++) {
			pathstring += path.get(i) + " starred in " + subgraph.getLabel(path.get(i), path.get(i + 1)) + " with " + path.get(i + 1) + "\n";
		}
		return pathstring;
	}
	
	/**
	 * Method for sorting the vertices within a graph by their indegrees
	 * @param num_neighbors
	 * @param graph
	 * @return
	 */
	public static List<String> degreesort (Graph<String, Set<String>> graph) {
		//Initialize the list to hold the sorted vertices
		List<String> sorted_by_InDegree = new ArrayList<String>();
		
		//Iterate through each vertex within the graph
		Iterable<String> vertices = graph.vertices();
		for (String vertex : vertices) {
			sorted_by_InDegree.add(vertex);
		}
		//Sort
		sorted_by_InDegree.sort((String neighbor1, String neighbor2) -> graph.inDegree(neighbor1) - graph.inDegree(neighbor2));
		
		return sorted_by_InDegree;
	}
	
	/**
	 * Method for obtaining other bacons that have a similar average separation with the center
	 * @param averagesep
	 * @param graph
	 * @return
	 */
	public static Set<String> findAvgSepbacon (double averagesep, Graph<String, Set<String>> graph) {
		//Initialize a set to hold the bacons
		Set<String> bacons = new HashSet<String>();
		
		//Create a lower bound and an upperbound for comparison
		double lowerbound = averagesep - 0.1;
		double upperbound = averagesep + 0.1;
		
		//For each actor in the graph
		for (String actor : graph.vertices()) {
			//Create a shortest path tree with the actor as the venter
			Graph<String, Set<String>> subgraph = GraphLibrary.bfs(graph, actor);
			
			//Obtain the avg separation
			double compare = GraphLibrary.averageSeparation(subgraph, actor);
			//Compare it with the bounds and if it is close enough add it to the bacons set
			if (compare >= lowerbound && compare <= upperbound) {
				bacons.add(actor);
			}
		}
		return bacons;
	}
	
	/**
	 * Test method that uses the graph diagram provided in the PS-4 page
	 * Should test all 4 main methods from the Graph Library
	 */
	public static void test1() {
		//Initialize the graph and insert all the names into it
		Graph<String, Set<String>> testgraph = new AdjacencyMapGraph<String, Set<String>>();
		testgraph.insertVertex("Alice");
		testgraph.insertVertex("Charlie");
		testgraph.insertVertex("Bob");
		testgraph.insertVertex("Kevin Bacon");
		testgraph.insertVertex("Dartmouth");
		testgraph.insertVertex("Nobody");
		testgraph.insertVertex("Nobody's Friend");
		
		//Create sets to hold the movies and insert undirected edges between the vertices
		Set<String> bob_alice = new HashSet<String>();
		bob_alice.add("Movie A");
		testgraph.insertUndirected("Bob", "Alice", bob_alice);
		
		Set<String> bob_kevinbacon = new HashSet<String>();
		bob_kevinbacon.add("Movie A");
		testgraph.insertUndirected("Bob", "Kevin Bacon", bob_kevinbacon);
		
		Set<String> kevinbacon_alice = new HashSet<String>();
		kevinbacon_alice.add("Movie A");
		kevinbacon_alice.add("Movie E");
		testgraph.insertUndirected("Kevin Bacon", "Alice", kevinbacon_alice);
		
		Set<String> alice_charlie = new HashSet<String>();
		alice_charlie.add("Movie D");
		testgraph.insertUndirected("Charlie", "Alice", alice_charlie);
		
		Set<String> charlie_bob = new HashSet<String>();
		charlie_bob.add("Movie C");
		testgraph.insertUndirected("Bob", "Charlie", charlie_bob);
		
		Set<String> charlie_dartmouth = new HashSet<String>();
		charlie_dartmouth.add("Movie B");
		testgraph.insertUndirected("Dartmouth", "Charlie", charlie_dartmouth);
		
		Set<String> nobody_nobodyf = new HashSet<String>();
		nobody_nobodyf.add("Movie F");
		testgraph.insertUndirected("Nobody", "Nobody's Friend", nobody_nobodyf);
		
		//Print out the graph
		System.out.println("Test Graph:\n" + testgraph + "\n");
		
		Graph<String, Set<String>> kevinbacon_shorttree = GraphLibrary.bfs(testgraph, "Kevin Bacon");
		//Create the shortest path tree of Kevin Bacon
		System.out.println("Kevin Bacon sub-graph:\n" + kevinbacon_shorttree + "\n");
		
		//Print out the path from "Dartmouth" to "Kevin Bacon"
		System.out.println("Path from Dartmouth to Kevin Bacon:\n" + GraphLibrary.getPath(kevinbacon_shorttree, "Dartmouth") + "\n");
		
		//Print out the path from "Charlie" to "Kevin Bacon"
		System.out.println("Path from Charlie to Kevin Bacon:\n" + GraphLibrary.getPath(kevinbacon_shorttree, "Charlie") + "\n");
		
		//Print out the path from "Alice" to "Kevin Bacon"
		System.out.println("Path from Alice to Kevin Bacon:\n" + GraphLibrary.getPath(kevinbacon_shorttree, "Alice") + "\n");
		
		//Print out the missing vertices from the graph
		System.out.println("Missing Verticies from Graph:\n" + GraphLibrary.missingVertices(testgraph, kevinbacon_shorttree) + "\n");
		
		//Print out the average separation
		System.out.println("Average Separation from Kevin Bacon:\n" + GraphLibrary.averageSeparation(kevinbacon_shorttree, "Kevin Bacon") + "\n\n");
	}
	
	/**
	 * Main method, contains the console/scanner code in order to create the interactive interface.
	 * Game cannot access other parts of the game through key-presses, must run multiple times to change 
	 * the center of the universe, the path finding vertex, and etc.
	 * @param args
	 */
	public static void main(String[] args) {	
		
		//Run test 1
		test1();
		
		
		//Read the files and create the ID maps as well as the Movie Actor Graph
		Map<Integer, String> ActorIDMap = createIDMap("Inputs/actors.txt");
		Map<Integer, String> MovieIDMap = createIDMap("Inputs/movies.txt");
		Graph<String, Set<String>> MovieActorGraph = createGraph (MovieIDMap, ActorIDMap, "Inputs/movie-actors.txt");
		
		
		//Initialize the subgraph and the missing vertices subset
		Graph<String, Set<String>> subgraph = null;
		Set<String> vertices_not_in_graph = null;
		
		
		//Obtain input from the console to set as the center of the universe
		Scanner actorscanner= new Scanner(System.in);
		System.out.println("Enter the Actor that you want as the center of the KB game Universe:\n");
		String actorcenter=actorscanner.nextLine();
		
		//Try-catch the creation of the subgraph and the set of vertices in the graph but not in the subgraph
		try {
			subgraph = GraphLibrary.bfs(MovieActorGraph, actorcenter);
			vertices_not_in_graph = GraphLibrary.missingVertices(MovieActorGraph, subgraph);
		}
		catch (Exception e) {
			System.out.println("\nActor not in database, please be case specific! Run again!\n");
			return;
		}
		
		
		//Obtain input from the console to set as the point from which the path is drawn
		Scanner scanshortpath= new Scanner(System.in);
		System.out.println("\nYour center of the universe is "+ actorcenter+", enter the actor that you want to draw the shortest path to:\n");
		String shortpathactor= scanshortpath.nextLine();
		
		//Try-catch the creation of the path
		try {
			//If the other actor is not in the subgraph then his/her number is infinity
			if (vertices_not_in_graph.contains(shortpathactor)) {
				System.out.println("This actor's number is inifinity! Run Again!");
				return;
			}
			
			//Otherwise print out the path
			else {
				System.out.println("\n" + shortpathactor + "'s number is " + (GraphLibrary.getPath(subgraph, shortpathactor).size()-1) + "\n");
				System.out.println(printpath(actorcenter, GraphLibrary.getPath(subgraph, shortpathactor), subgraph));
				System.out.println("Path: " + GraphLibrary.getPath(subgraph, shortpathactor) + "\n");
			}
		}
		catch (Exception e) {
			System.out.println("\nActor not in database, please be case specific! Run again!\n");
			return;
		}
		
		
		//Obtain input from the console (int) to use in order to find the actors that are that (int) steps away from the center
		Scanner scansteps= new Scanner(System.in);
		System.out.println("Next type in an integer to find the actors who are that number of steps away from the center:\n");
		Integer steps = scansteps.nextInt();
		System.out.println("\n" + numberActors(steps, subgraph) + "\n");

		
		//Obtain input from the scanner
		Scanner scanavepath = new Scanner(System.in);
		System.out.println("Please type in (avg) if you want to display the average path length over all actors who "
				+ "are connected by some path to the current center \nor press anyother key to continue to another option:\n");
		String avepath = scanavepath.nextLine();
		int comparevar = avepath.compareTo("avg");
		
		//If the input is avg, then find the average separation and obtain a list of actors with similar average separations 
		//in their own respective shortest path trees
		//End the game upon completion
		if (comparevar == 0) {
			System.out.println("\n" + GraphLibrary.averageSeparation(subgraph, actorcenter) + "\n" + "\nPLEASE WAIT AROUND 2 MINUTES, "
					+ "currently obtaining other actors with similar average separations...");
			System.out.println("\nThese are the actors with similar average separations as " + actorcenter + ":\n" + 
			findAvgSepbacon(GraphLibrary.averageSeparation(subgraph, actorcenter), MovieActorGraph) + "\n");
			
			System.out.println("The game has ended! Run again to play again!");
			return;
		}
		
		//If the input is any other key then obtain new input from the console
		else {
			Scanner scanmissing = new Scanner(System.in);
			System.out.println("\nPlease type in (mis) if you want to display the actors who have a bacon number of infinity "
					+ "with respect to Kevin Bacon (this list will be very long) \nor press any other key to continue to another option\n");
			String missing = scanmissing.nextLine();
			int comparevar2 = missing.compareTo("mis");
			
			//If the input is (mis) then print a list of all the vertices within the subgraph and now the graph
			//End the game upon completion
			if (comparevar2 == 0) {
				System.out.println("\n" + GraphLibrary.missingVertices(MovieActorGraph, subgraph) + "\n");
				System.out.println("The game has ended! Run again to play again!");
				return;
			}
			
			//If the input is any other key then obtain new input from the console
			else {
				Scanner scandegree = new Scanner(System.in);
				System.out.println("\nPlease type in (deg) if you want to display the actors within the subgraph sorted by degree\n");
				String degree = scandegree.nextLine();
				int comparevar3 = degree.compareTo("deg");
				
				//If the input is (deg) then print a sorted list from most connections to least of the actors within the graph
				//End the game upon completion
				if (comparevar3 == 0) {
					System.out.println("/nThe sorted list from most connections to least:\n\n" + degreesort(subgraph) + "\n");
					System.out.println("The Game has ended! Run again to play again!");
					return;
				}
				
				//Otherwise end the game!
				else {
					System.out.println("\nNo more options! The game has ended! Run again to play again!");
					return;
				}
			}
		}
	}
}


	
	