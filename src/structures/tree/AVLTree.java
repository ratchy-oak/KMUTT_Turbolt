package structures.tree;

public class AVLTree {
    private AVLNode root;

    public AVLTree() {
        this.root = null;
    }

    public AVLNode getRoot() {
        return this.root;
    }

    public int height(AVLNode node) {
        if (node == null) {
            return 0;
        }
        return node.getHeight();
    }

    public int max(int a, int b) {
        if (a > b) {
            return a;
        }
        return b;
    }

    public int getBalance(AVLNode node) {
        if (node == null) {
            return 0;
        }
        return height(node.getLeft()) - height(node.getRight());
    }

    public AVLNode rightRotate(AVLNode y) {
        AVLNode x = y.getLeft();
        AVLNode t2 = x.getRight();

        x.setRight(y);
        y.setLeft(t2);

        y.setHeight(max(height(y.getLeft()), height(y.getRight())) + 1);
        x.setHeight(max(height(x.getLeft()), height(x.getRight())) + 1);

        return x;
    }

    public AVLNode leftRotate(AVLNode x) {
        AVLNode y = x.getRight();
        AVLNode t2 = y.getLeft();

        y.setLeft(x);
        x.setRight(t2);

        x.setHeight(max(height(x.getLeft()), height(x.getRight())) + 1);
        y.setHeight(max(height(y.getLeft()), height(y.getRight())) + 1);

        return y;
    }

    public AVLNode insert(AVLNode node, int value) {
        if (node == null) {
            return new AVLNode(value);
        }

        if (value < node.getValue()) {
            node.setLeft(insert(node.getLeft(), value));
        } else if (value > node.getValue()) {
            node.setRight(insert(node.getRight(), value));
        } else {
            return node;
        }

        node.setHeight(max(height(node.getLeft()), height(node.getRight())) + 1);

        int balance = getBalance(node);

        // LL
        if (balance > 1 && value < node.getLeft().getValue()) {
            return rightRotate(node);
        }

        // RR
        if (balance < -1 && value > node.getRight().getValue()) {
            return leftRotate(node);
        }

        // LR
        if (balance > 1 && value > node.getLeft().getValue()) {
            node.setLeft(leftRotate(node.getLeft()));
            return rightRotate(node);
        }

        // RL
        if (balance < -1 && value < node.getRight().getValue()) {
            node.setRight(rightRotate(node.getRight()));
            return leftRotate(node);
        }

        return node;
    }

    public void insert(int value) {
        root = insert(root, value);
    }

    public void preOrder(AVLNode root) {
        if (root == null) {
            return;
        }
        System.out.print(root.getValue() + " ");
        preOrder(root.getLeft());
        preOrder(root.getRight());
    }

    public void inOrder(AVLNode root) {
        if (root == null) {
            return;
        }
        inOrder(root.getLeft());
        System.out.print(root.getValue() + " ");
        inOrder(root.getRight());
    }

    public void postOrder(AVLNode root) {
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

        AVLNode[] queue = new AVLNode[100];
        int front = 0;
        int rear = 0;

        queue[rear] = root;
        rear++;

        while (front < rear) {
            AVLNode current = queue[front];
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