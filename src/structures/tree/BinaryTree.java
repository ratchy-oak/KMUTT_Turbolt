package structures.tree;

import datastruct.Queue;

public class BinaryTree {
    private Node root;
    private Node[] nodes;
    private int nodeCount;

    public BinaryTree(int n) {
        this.nodes = new Node[n];
        this.root = null;
        this.nodeCount = 0;
    }

    public void addNode(String name, int value) {
        Node node = new Node(name, value);
        this.nodes[this.nodeCount++] = node;
    }

    public Node searchByName(String name) {
        for (int i = 0; i < this.nodeCount; i++) {
            if (this.nodes[i].getName().equals(name)) {
                return this.nodes[i];
            }
        }
        return null;
    }

    public boolean setRoot(String name) {
        Node node = searchByName(name);
        if (node == null) {
            return false;
        }
        this.root = node;
        return true;
    }

    public Node getRoot() {
        return this.root;
    }

    public void preOrder(Node root) {
        if (root == null) {
            return;
        }
        System.out.print(root.getValue() + " ");
        preOrder(root.getLeft());
        preOrder(root.getRight());
    }

    public void inOrder(Node root) {
        if (root == null) {
            return;
        }
        inOrder(root.getLeft());
        System.out.print(root.getValue() + " ");
        inOrder(root.getRight());
    }

    public void postOrder(Node root) {
        if (root == null) {
            return;
        }
        postOrder(root.getLeft());
        postOrder(root.getRight());
        System.out.print(root.getValue() + " ");
    }

    public void breadthFirstSearch() {
        if (root == null) return;

        Queue queue = new Queue();
        queue.offer(root);

        while (!queue.isEmpty()) {
            Node node = queue.poll();
            System.out.print(node.getValue() + " ");

            if (node.getLeft() != null) queue.offer(node.getLeft());
            if (node.getRight() != null) queue.offer(node.getRight());
        }
    }
}