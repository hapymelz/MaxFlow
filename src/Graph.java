import java.io.File;
import java.util.*;

public class Graph {
    private int vertexCt;  // Number of vertices in the graph.
    private int[][] capacity;  // Adjacency  matrix
    private int[][] residual; // residual matrix
    private int[][] edgeCost; // cost of edges in the matrix
    private String graphName;  //The file from which the graph was created.
    private int totalFlow; // total achieved flow
    private int source = 0; // start of all paths
    private int sink; // end of all paths
    int [] pred;
    int [] cost;

    public Graph(String fileName) {
        this.vertexCt = 0;
        source  = 0;
        this.graphName = "";
        makeGraph(fileName);
        pred = new int[vertexCt];
        cost = new int[vertexCt];
        totalFlow = Integer.MAX_VALUE;
    }

    /**
     * Method to add an edge
     *
     * @param source      start of edge
     * @param destination end of edge
     * @param cap         capacity of edge
     * @param weight      weight of edge, if any
     * @return edge created
     */
    private boolean addEdge(int source, int destination, int cap, int weight) {
        if (source < 0 || source >= vertexCt) return false;
        if (destination < 0 || destination >= vertexCt) return false;
        capacity[source][destination] = cap;
        residual[source][destination] = cap;
        edgeCost[source][destination] = weight;
        edgeCost[destination][source] = -weight;
        return true;
    }

    /**
     * Method to get a visual of the graph
     *
     * @return the visual
     */
    public String printMatrix(String label, int[][] m) {
        StringBuilder sb = new StringBuilder();
        sb.append("\n " + label+ " \n     ");
        for (int i=0; i < vertexCt; i++)
            sb.append(String.format("%5d", i));
        sb.append("\n");
        for (int i = 0; i < vertexCt; i++) {
            sb.append(String.format("%5d",i));
            for (int j = 0; j < vertexCt; j++) {
                sb.append(String.format("%5d",m[i][j]));
            }
            sb.append("\n");
        }
        return sb.toString();
    }

    /**
     * Method to make the graph
     *
     * @param filename of file containing data
     */
    private void makeGraph(String filename) {
        try {
            graphName = filename;
            System.out.println("\n************ Find Flow " + filename + " ***************************");
            Scanner reader = new Scanner(new File(filename));
            vertexCt = reader.nextInt();
            capacity = new int[vertexCt][vertexCt];
            residual = new int[vertexCt][vertexCt];
            edgeCost = new int[vertexCt][vertexCt];
            for (int i = 0; i < vertexCt; i++) {
                for (int j = 0; j < vertexCt; j++) {
                    capacity[i][j] = 0;
                    residual[i][j] = 0;
                    edgeCost[i][j] = 0;
                }
            }

            // If weights, need to grab them from file
            while (reader.hasNextInt()) {
                int v1 = reader.nextInt();
                int v2 = reader.nextInt();
                int cap = reader.nextInt();
                int weight = reader.nextInt();
                if (!addEdge(v1, v2, cap, weight))
                    throw new Exception();
            }

            reader.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
        sink = vertexCt - 1;
        System.out.println( printMatrix("Edge Cost" ,edgeCost));
    }

    private boolean cheapestPath(int source, int destination) {
        for (int i = 0; i < vertexCt; i++) {
            cost[i] =  Integer.MAX_VALUE;
            pred[i] = 0;
        }
        cost[source] = 0;

        for (int i = 0; i < vertexCt; i++) {
            for (int u = 0; u < vertexCt; u++) {
                for(int v = 0; v < vertexCt; v++) {
                    if (cost[u] != Integer.MAX_VALUE && cost[u] + edgeCost[u][v] < cost[v] && residual[u][v] > 0) {
                        cost[v] = cost[u] + edgeCost[u][v];
                        pred[v] = u;
                    }
                }
            }
        }
        return pred[destination] > 0;
    }

    private void finalEdgeFlow() {
        //List of all paths found with cheapestPath
        ArrayList<ArrayList<Integer>> paths = new ArrayList<>();

        //Finding a path until there are no more left
        while (cheapestPath(source, sink)) {
            int cost = 0;

            //Creating the currant path and initializing it with the sink.
            ArrayList<Integer> currentPath = new ArrayList<>();
            currentPath.add(sink);
            int index = sink;

            //Iterating through pred backwards, starting from sink
            while (true) {
                int u = pred[index];

                //if the flow from the path is less
                // than the current flow, update the totalFlow
                if (residual[u][index] < totalFlow) {
                    totalFlow = residual[u][index];
                }

                //recording the cost of the path so far
                cost += edgeCost[u][index];

                //updating the residual graph
                residual[u][index] --;
                residual[index][u] ++;

                //add the node to the path
                currentPath.add(0, pred[index]);
                if (pred[index] == 0) break;
                index = u;
            }

            paths.add(currentPath);

            //Printing the path
            String path = "(" + currentPath.get(0);
            for (int i = 1; i < currentPath.size(); i++) {
                path += " -> " + currentPath.get(i);
            }
            path += ") (" + totalFlow + ") $" + cost;
            System.out.println(path);
        }
        //Printing all the flows
        findAllFlows();
    }

    public void findAllFlows() {
        //iterating through the matrices
        for (int u = 0; u < vertexCt; u++) {
            for (int v = 0; v < vertexCt; v++) {
                //if capacity is greater than the residual, then a path was made
                //if you want me to include all paths, delete '&& edgecost[u][v] > 0'
                if (capacity[u][v] > residual[u][v] && edgeCost[u][v] > 0) {
                    String flow = "Flow " + u + " -> " + v + " (" + capacity[u][v] + ") $" + edgeCost[u][v];
                    System.out.println(flow);
                }
            }
        }
    }

    public void minCostMaxFlow(){
        System.out.println( printMatrix("Capacity", capacity));
        finalEdgeFlow();
        System.out.println(printMatrix("Residual", residual));
    }

    public static void main(String[] args) {
        String[] files = {"src/match0.txt", "src/match1.txt", "src/match2.txt", "src/match3.txt", "src/match4.txt", "src/match5.txt","src/match6.txt", "src/match7.txt", "src/match8.txt", "src/match9.txt"};
        for (String fileName : files) {
            Graph graph = new Graph(fileName);
            graph.minCostMaxFlow();
        }
    }
}