package structures.tree;

public class AVLNode {
    private int value;
    private int height;
    private AVLNode left;
    private AVLNode right;

    public AVLNode(int value) {
        this.value = value;
        this.height = 1;
        this.left = null;
        this.right = null;
    }

    public int getValue() {
        return this.value;
    }

    public int getHeight() {
        return this.height;
    }

    public void setHeight(int height) {
        this.height = height;
    }

    public AVLNode getLeft() {
        return this.left;
    }

    public void setLeft(AVLNode left) {
        this.left = left;
    }

    public AVLNode getRight() {
        return this.right;
    }

    public void setRight(AVLNode right) {
        this.right = right;
    }
}