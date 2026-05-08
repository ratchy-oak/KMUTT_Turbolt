package structures.tree;

public class BST {
    private Node root;

    public BST() {
        this.root = null;
    }

    public Node getRoot() {
        return this.root;
    }

    public void insert(int value) {
        Node newNode = new Node("", value);

        if (root == null) {
            root = newNode;
            return;
        }

        Node current = root;

        while (true) {
            if (value < current.getValue()) {
                if (current.getLeft() == null) {
                    current.setLeft(newNode);
                    return;
                }
                current = current.getLeft();
            } else {
                if (current.getRight() == null) {
                    current.setRight(newNode);
                    return;
                }
                current = current.getRight();
            }
        }
    }

    // ดึงมาจาก BinaryTree

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
        if (root == null) {
            return;
        }

        Node[] queue = new Node[100];
        int front = 0;
        int rear = 0;

        queue[rear] = root;
        rear++;

        while (front < rear) {
            Node current = queue[front];
            front++;

            System.out.print(current.getValue() + " ");

            if (current.getLeft() != null) {
                queue[rear] = current.getLeft();
                rear++;
            }

            if (current.getRight() != null) {
                queue[rear] = current.getRight();
                rear++;
            }
        }
    }
}