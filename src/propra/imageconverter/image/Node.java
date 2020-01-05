package propra.imageconverter.image;

import java.util.HashMap;

/**
 * A Node in a huffman tree. A Node can either be a leave Node, or the root of a (sub-)tree.
 */
public class Node implements Comparable<Node> {

    /**
     * Symbol that is stored in a leave Node.
     */
    private byte symbol;

    /**
     * Identifies whether the current Node is a leave Node.
     */
    private boolean leave = false;

    /**
     * Parent Node.
     */
    private Node parent;

    /**
     * Left child Node.
     */
    private Node leftChild;

    /**
     * Right child Node.
     */
    private Node rightChild;

    /**
     * Weight of the current leave Node.
     */
    private int weight;

    /**
     * Constructs a Node. Used for inner Nodes when reading the tree from a file.
     */
    public Node() {
    }

    /**
     * Constructs a leave Node, that has no weight (used, when the tree is read from a file).
     *
     * @param symbol symbol to be set for the leave Node.
     */
    public Node(byte symbol) {
        this.symbol = symbol;
        this.leave = true;
    }

    /**
     * Constructs a leave Node with a symbol and its weight.
     *
     * @param symbol symbol to be set for the leave Node.
     * @param weight weight of that leave Node.
     */
    public Node(byte symbol, int weight) {
        this(symbol);
        this.weight = weight;
    }

    /**
     * Constructs a Node, with <code>leftChild</code> and <code>rightChild</code> as left and right children.
     *
     * @param leftChild  left child of the constructed Node.
     * @param rightChild right child of the constructed Node.
     */
    public Node(Node leftChild, Node rightChild) {
        this.leftChild = leftChild;
        this.rightChild = rightChild;

        leftChild.setParent(this);
        rightChild.setParent(this);
    }

    /**
     * Get the left child of this Node.
     *
     * @return leftChild.
     */
    public Node getLeftChild() {
        return leftChild;
    }

    /**
     * Get the right child of this Node.
     *
     * @return rightChild.
     */
    public Node getRightChild() {
        return rightChild;
    }

    /**
     * Check, whether this Node is the root of a tree.
     *
     * @return if this Node is the root of a tree.
     */
    public boolean isRoot() {
        return this.parent == null;
    }

    /**
     * Check, whether this Node is a leave. A leave is a Node, that stores a symbol.
     *
     * @return if this Node is a leave.
     */
    public boolean isLeave() {
        return this.leave;
    }

    /**
     * Check, whether this tree is empty. An empty tree consists only of the root Node.
     *
     * @return if tree is empty.
     */
    public boolean isEmpty() {
        return this.isRoot() &&
                this.rightChild == null &&
                this.leftChild == null;
    }

    /**
     * Get the parent of this Node.
     *
     * @return parent.
     */
    public Node getParent() {
        return parent;
    }

    /**
     * Set parent Node.
     *
     * @param parent parent Node to be set.
     */
    public void setParent(Node parent) {
        this.parent = parent;
    }

    /**
     * Get the symbol for the Node.
     *
     * @return symbol.
     */
    public byte getSymbol() {
        return symbol;
    }

    /**
     * Get weight of the current Node. This is either the value from Field {@link #weight} (if the Node is a leave),
     * or it's the sum of the whole sub-tree.
     *
     * @return weight of the current sub-tree.
     */
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

    /**
     * Calculates the depth of the sub-tree where this Node is the root of.
     *
     * @return sub-tree depth.
     */
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

    /**
     * Appends a Node at the next possible position to the tree.
     * <p>
     * Attention! This node is not the root of the tree. This Node is the Node, that got added last.
     * To find the next suitable position, this function checks, if this Node is a leave.
     * If it is, this function goes the tree upwards and always checks in it's way up,
     * if there is one Node, where <code>newNode</code> can get added to. If this Node is not a leave,
     * this function tries to append <code>newNode</code> first as left and otherwise as right child.
     * </p>
     *
     * @param newNode Node to be appended.
     * @return just appended Node.
     */
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

    /**
     * Checks, whether it is possible, to add a Node to the current tree. Checks therefore,
     * if this tree contains any leave, that is not a leave in our meaning, so this leave does not contain a symbol.
     *
     * @return if it is possible to append a Node to the tree.
     */
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

    /**
     * Builds the huffman table for this tree.
     * <p>
     * The huffman table is a HashMap with the symbol of a leave (as Byte) as key,
     * and the code (how to get to there from the root) for this leave.
     * A <code>0</code> means, you go to the left child, whereas a <code>1</code> means, you go to the right child.
     * </p>
     *
     * @param parentCode   code for the parent of a node.
     * @param huffmanTable huffman table to be built.
     */
    public void buildHuffmanTable(String parentCode, HashMap<Byte, String> huffmanTable) {
        if (this.leftChild != null) {
            String code = parentCode + "0";
            if (this.leftChild.isLeave()) {
                huffmanTable.put(this.leftChild.getSymbol(), code);
            } else {
                this.leftChild.buildHuffmanTable(code, huffmanTable);
            }
        }

        if (this.rightChild != null) {
            String code = parentCode + "1";
            if (this.rightChild.isLeave()) {
                huffmanTable.put(this.rightChild.getSymbol(), code);
            } else {
                this.rightChild.buildHuffmanTable(code, huffmanTable);
            }
        }
    }

    /**
     * Returns a String that represents this tree in Pre-Order.
     * <code>0</code> is used for each inner node (including the root node),
     * <code>1</code> is used, followed by the 8 bits from the symbol in binary notation for leaves.
     *
     * @return Pre-Order String of this tree, including the symbols as 8 bit binary.
     */
    public String getTreeInPreOrder() {
        if (this.isLeave()) {
            String binaryString = String.format("%8s", Integer.toBinaryString(Byte.toUnsignedInt(this.symbol))).replace(' ', '0');
            return "1" + binaryString;
        } else {
            return "0" + this.leftChild.getTreeInPreOrder() + this.rightChild.getTreeInPreOrder();
        }
    }

    /**
     * Compares two Nodes, based on their weight, and in case the weight is equal, based on the depth of their sub-tree.
     *
     * @param o Node to compare to.
     * @return <code>-1</code>, if this Node is less than Node <code>o</code>, otherwise returns <code>1</code>
     */
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
