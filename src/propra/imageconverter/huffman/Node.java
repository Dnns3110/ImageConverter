package propra.imageconverter.huffman;

public class Node {

    private byte symbol;
    private boolean leave = false;
    private Node parent;
    private Node leftChild;
    private Node rightChild;

    public Node() {
    }

    public Node(byte symbol) {
        this.symbol = symbol;
        this.leave = true;
    }

    public Node getLeftChild() {
        return leftChild;
    }

    public void setLeftChild(Node leftChild) {
        this.leftChild = leftChild;
    }

    public Node getRightChild() {
        return rightChild;
    }

    public void setRightChild(Node rightChild) {
        this.rightChild = rightChild;
    }

    public boolean isLeftChild(Node otherNode) {
        return this == this.parent.leftChild;
    }

    public boolean isRightChild(Node otherNode) {
        return this == this.parent.rightChild;
    }

    public boolean isRoot() {
        return this.parent == null;
    }

    public boolean isLeave() {
        return this.leave;
    }

    public boolean isInnerNode() {
        return !this.leave;
    }

    public boolean isEmpty() {
        return this.isRoot() &&
                this.rightChild == null &&
                this.leftChild == null;
    }

    public Node getParent() {
        return parent;
    }

    public void setParent(Node parent) {
        this.parent = parent;
    }

    public byte getSymbol() {
        return symbol;
    }

    public Node appendNode(Node newNode) {
        Node currentNode = this;

        while (!currentNode.isRoot() || currentNode.rightChild == null) {
            if (currentNode.isLeave()) {
                currentNode = currentNode.parent;
                continue;
            }

            if (currentNode.leftChild == null) {
                newNode.setParent(currentNode);
                currentNode.leftChild = newNode;

                break;
            } else if (currentNode.rightChild == null) {
                newNode.setParent(currentNode);
                currentNode.rightChild = newNode;

                break;
            } else if (currentNode.rightChild != null && currentNode.leftChild != null) {
                currentNode = currentNode.parent;
            }
        }

        return newNode;
    }

    public boolean isAppendable() {
        Node currentNode = this;

        while (!currentNode.isRoot() || currentNode.rightChild == null) {
            if (currentNode.isLeave()) {
                currentNode = currentNode.parent;
                continue;
            }

            if (currentNode.leftChild == null || currentNode.rightChild == null) {
                return true;
            } else if (currentNode.rightChild != null && currentNode.leftChild != null) {
                currentNode = currentNode.parent;
            }
        }

        return false;
    }
}
