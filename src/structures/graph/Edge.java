package structures.graph;

public class Edge {
    private String destination;
    private int weight;

    public Edge(String destination, int weight) {
        this.destination = destination;
        this.weight = weight;
    }

    public String getDestination() {
        return this.destination;
    }

    public int getWeight() {
        return this.weight;
    }
}