package structures.tree;

public class Node {
    private String name;
    private int value;
    private Node left;
    private Node right;
    private Node next; 

    public Node (String name, int value) {
        this.name = name;
        this.value = value;
        this.left = null;
        this.right = null;
        this.next = null;
    }

    public String getName() {
        return this.name;
    }

    public int getValue() {
        return this.value;
    }
    
    public Node getLeft() {
        return this.left;
    }

    public void setLeft(Node leftNode) {
        this.left = leftNode;
    }

    public Node getRight() {
        return this.right;
    }

    public void setRight(Node rightNode) {
        this.right = rightNode;
    }

    public Node getNext() {
        return this.next;
    }

    public void setNext(Node nextNode) {
        this.next = nextNode;
    }
}