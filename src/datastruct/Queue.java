package datastruct;

import structures.tree.Node;

public class Queue {
    private Node front;
    private Node rear;

    public Queue() {
        front = null;
        rear = null;
    }

    public boolean isEmpty() {
        return front == null;
    }

    public void offer(Node node) {
        if (rear == null) {
            front = rear = node;
        } else {
            rear.setNext(node);
            rear = node;
        }
    }

    public Node poll() {
        Node node = front;
        front = front.getNext();
        if (front == null) {
            rear = null;
        }
        return node;
    }
}