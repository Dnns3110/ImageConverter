package propra.imageconverter.image;

import java.util.HashMap;

public class Node implements Comparable<Node> {

    private byte symbol;
    private boolean leave = false;
    private Node parent;
    private Node leftChild;
    private Node rightChild;
    private int weight;

    public Node() {
    }

    public Node(byte symbol) {
        this.symbol = symbol;
        this.leave = true;
    }

    public Node(byte symbol, int weight) {
        this(symbol);
        this.weight = weight;
    }

    public Node(Node leftChild, Node rightChild) {
        this.leftChild = leftChild;
        this.rightChild = rightChild;

        leftChild.setParent(this);
        rightChild.setParent(this);
    }

    public Node getLeftChild() {
        return leftChild;
    }

    public Node getRightChild() {
        return rightChild;
    }

    public boolean isRoot() {
        return this.parent == null;
    }

    public boolean isLeave() {
        return this.leave;
    }

    public boolean isLeftChild() {
        if (this.parent != null) {
            return this.parent.leftChild == this;
        }

        return false;
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

    public int getWeight() {
        if (this.isLeave()) {
            return weight;
        } else {
            int weight = 0;
            if (this.leftChild != null) {
                weight += this.leftChild.getWeight();
            }
            if (this.rightChild != null) {
                weight += this.rightChild.getWeight();
            }

            return weight;
        }
    }

    public int getNodeDepth() {
        int leftDepth = 0;
        int rightDepth = 0;

        if (this.leftChild == null && this.rightChild == null) {
            return 1;
        }
        if (this.leftChild != null) {
            leftDepth = this.leftChild.getNodeDepth();
        }
        if (this.rightChild != null) {
            rightDepth = this.rightChild.getNodeDepth();
        }

        return Math.max(leftDepth, rightDepth);
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

    public void getCode(String parentCode, HashMap<Byte, String> huffmanTable) {
        if (this.leftChild != null) {
            String code = parentCode + "0";
            if (this.leftChild.isLeave()) {
                huffmanTable.put(this.leftChild.getSymbol(), code);
            } else {
                this.leftChild.getCode(code, huffmanTable);
            }
        }

        if (this.rightChild != null) {
            String code = parentCode + "1";
            if (this.rightChild.isLeave()) {
                huffmanTable.put(this.rightChild.getSymbol(), code);
            } else {
                this.rightChild.getCode(code, huffmanTable);
            }
        }
    }

    public String getTreeInPreOrder() {
        if (this.isLeave()) {
            String binaryString = String.format("%8s", Integer.toBinaryString(Byte.toUnsignedInt(this.symbol))).replace(' ', '0');
            return "1" + binaryString;
        } else {
            return "0" + this.leftChild.getTreeInPreOrder() + this.rightChild.getTreeInPreOrder();
        }
    }

    @Override
    public int compareTo(Node o) {
        if (this.getWeight() == o.getWeight()) {
            if (this.getNodeDepth() < o.getNodeDepth()) {
                return -1;
            }

            return 1;
        } else if (this.getWeight() > o.getWeight()) {
            return 1;
        }
        return -1;
    }
}
