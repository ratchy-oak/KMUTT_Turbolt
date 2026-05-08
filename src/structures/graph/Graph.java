package structures.graph;

import structures.tree.Node;

public class Graph {
    private String[] vertices;
    private Edge[][] adjacencyList;
    private int[] edgeCount;
    private int nVertices;

    public Graph(int maxVertices) {
        vertices = new String[maxVertices];
        adjacencyList = new Edge[maxVertices][100];
        edgeCount = new int[maxVertices];
        nVertices = 0;
    }

    public void addVertex(String v) {
        vertices[nVertices] = v;
        edgeCount[nVertices] = 0;
        nVertices++;
    }

    public int indexOfVertex(String v) {
        for (int i = 0; i < nVertices; i++) {
            if (vertices[i].equals(v)) {
                return i;
            }
        }
        return -1;
    }

    public void addEdge(String source, String destination, int weight) {
        int sourceIndex = indexOfVertex(source);

        if (sourceIndex == -1) {
            return;
        }

        adjacencyList[sourceIndex][edgeCount[sourceIndex]] = new Edge(destination, weight);
        edgeCount[sourceIndex]++;
    }

    public void addEdge(String source, String destination) {
        addEdge(source, destination, 1);
    }

    public void printGraph() {
        for (int i = 0; i < nVertices; i++) {
            System.out.print(vertices[i] + " -> ");

            if (edgeCount[i] == 0) {
                System.out.print("none");
            } else {
                for (int j = 0; j < edgeCount[i]; j++) {
                    Edge e = adjacencyList[i][j];
                    System.out.print(e.getDestination() + "(" + e.getWeight() + ")");
                    if (j < edgeCount[i] - 1) {
                        System.out.print(" -> ");
                    }
                }
            }

            System.out.println();
        }
    }

    public int outDegree(String vertex) {
        int index = indexOfVertex(vertex);
        if (index == -1) {
            return 0;
        }
        return edgeCount[index];
    }

    public int inDegree(String vertex) {
        int count = 0;

        for (int i = 0; i < nVertices; i++) {
            for (int j = 0; j < edgeCount[i]; j++) {
                if (adjacencyList[i][j].getDestination().equals(vertex)) {
                    count++;
                }
            }
        }

        return count;
    }

    public int weightedDegree(String vertex) {
        int sum = 0;
        int index = indexOfVertex(vertex);

        if (index == -1) {
            return 0;
        }

        for (int j = 0; j < edgeCount[index]; j++) {
            sum += adjacencyList[index][j].getWeight();
        }

        for (int i = 0; i < nVertices; i++) {
            for (int j = 0; j < edgeCount[i]; j++) {
                if (adjacencyList[i][j].getDestination().equals(vertex)) {
                    sum += adjacencyList[i][j].getWeight();
                }
            }
        }

        return sum;
    }

    public void printDegrees() {
        for (int i = 0; i < nVertices; i++) {
            String v = vertices[i];
            System.out.println(outDegree(v) + " " + inDegree(v) + " " + weightedDegree(v));
        }
    }

    public void dfs(String start) {
        boolean[] visited = new boolean[nVertices];
        dfsHelper(start, visited);
        System.out.println();
    }

    private void dfsHelper(String vertex, boolean[] visited) {
        int index = indexOfVertex(vertex);
        if (index == -1 || visited[index]) return;

        visited[index] = true;
        System.out.print(vertex + " ");

        sortEdges(index);

        for (int i = 0; i < edgeCount[index]; i++) {
            String next = adjacencyList[index][i].getDestination();
            dfsHelper(next, visited);
        }
    }

    public void bfs(String start) {
        boolean[] visited = new boolean[nVertices];

        Node front = null;
        Node rear = null;

        // enqueue start
        front = rear = new Node(start, 0);

        while (front != null) {
            String vertex = front.getName();

            // dequeue
            front = front.getNext();
            if (front == null) rear = null;

            int index = indexOfVertex(vertex);
            if (visited[index]) continue;

            visited[index] = true;
            System.out.print(vertex + " ");

            // sort ก่อน
            sortEdges(index);

            for (int i = 0; i < edgeCount[index]; i++) {
                String next = adjacencyList[index][i].getDestination();
                int nextIndex = indexOfVertex(next);

                if (!visited[nextIndex]) {
                    Node newNode = new Node(next, 0);

                    if (rear == null) {
                        front = rear = newNode;
                    } else {
                        rear.setNext(newNode);
                        rear = newNode;
                    }
                }
            }
        }
        System.out.println();
    }

    private void sortEdges(int index) {
        for (int i = 0; i < edgeCount[index] - 1; i++) {
            for (int j = i + 1; j < edgeCount[index]; j++) {
                String a = adjacencyList[index][i].getDestination();
                String b = adjacencyList[index][j].getDestination();

                if (a.compareTo(b) > 0) {
                    Edge temp = adjacencyList[index][i];
                    adjacencyList[index][i] = adjacencyList[index][j];
                    adjacencyList[index][j] = temp;
                }
            }
        }
    }

        private int findMinDistance(int[] dist, boolean[] visited) {
        int min = 1000000000;
        int minIndex = -1;

        for (int i = 0; i < nVertices; i++) {
            if (!visited[i] && dist[i] < min) {
                min = dist[i];
                minIndex = i;
            }
        }

        return minIndex;
    }

    public void dijkstra(String start, String destination) {
        int startIndex = indexOfVertex(start);
        int destIndex = indexOfVertex(destination);

        int[] dist = new int[nVertices];
        boolean[] visited = new boolean[nVertices];
        int[] prev = new int[nVertices];

        for (int i = 0; i < nVertices; i++) {
            dist[i] = 1000000000;
            visited[i] = false;
            prev[i] = -1;
        }

        dist[startIndex] = 0;

        for (int count = 0; count < nVertices; count++) {
            int u = findMinDistance(dist, visited);

            if (u == -1) {
                break;
            }

            visited[u] = true;

            for (int j = 0; j < edgeCount[u]; j++) {
                Edge e = adjacencyList[u][j];
                int v = indexOfVertex(e.getDestination());
                int weight = e.getWeight();

                if (!visited[v] && dist[u] + weight < dist[v]) {
                    dist[v] = dist[u] + weight;
                    prev[v] = u;
                }
            }
        }

        int[] path = new int[nVertices];
        int pathCount = 0;
        int current = destIndex;

        while (current != -1) {
            path[pathCount] = current;
            pathCount++;
            current = prev[current];
        }

        for (int i = pathCount - 1; i >= 0; i--) {
            System.out.print(vertices[path[i]]);
            if (i > 0) {
                System.out.print(" ");
            }
        }
        System.out.println();

        System.out.println(dist[destIndex]);
    }
}