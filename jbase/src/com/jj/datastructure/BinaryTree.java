package com.jj.datastructure;

/**
 * Created by yejaz on 1/1/2017.
 */
public abstract class BinaryTree <T> {
    // Root node pointer. Will be null for an empty tree.
    protected Node<T> root;

    public BinaryTree()//int data)
    {
        root = null; //new Node(data);
    }

    /**
     Inserts the given data into the binary tree.
     Uses a recursive helper.
     */
    public void add(T data) {
        root = insert(root, data);
    }

    /**
     Recursive insert -- given a node pointer, recur down and
     insert the given data into the tree. Returns the new
     node pointer (the standard way to communicate
     a changed pointer back to the caller).
     */
    protected abstract Node insert(Node<T> node, T data);

    /**
     Returns true if the given target is in the binary tree.
     Uses a recursive helper.
     */
    public boolean lookup(T data) {
        return(lookup(root, data));
    }

    /**
     Recursive lookup  -- given a node, recur
     down searching for the given data.
     */
    protected abstract boolean lookup(Node<T> node, T data);

    /**
     Returns the number of nodes in the tree.
     Uses a recursive helper that recurs
     down the tree and counts the nodes.
     */
    public int size() {
        return(size(root));
    }

    private int size(Node<T> node) {
        if (node == null) return(0);
        else {
            return(size(node.left) + 1 + size(node.right));
        }
    }

    /**
     Returns the max root-to-leaf depth of the tree.
     Uses a recursive helper that recurs down to find
     the max depth.
     */
    public int maxDepth() {
        return(maxDepth(root));
    }
    private int maxDepth(Node<T> node) {
        if (node==null) {
            return(0);
        }
        else {
            int lDepth = maxDepth(node.left);
            int rDepth = maxDepth(node.right);

            // use the larger + 1
            return(Math.max(lDepth, rDepth) + 1);
        }
    }

    /**
     Returns the min value in a non-empty binary search tree.
     Uses a helper method that iterates to the left to find
     the min value.
     */
    public T minValue() {
        return( minValue(root) );
    }

    /**
     Finds the min value in a non-empty binary search tree.
     */
    protected abstract T minValue(Node<T> node);

    public T maxValue() {
        return( maxValue(root) );
    }

    /**
     Finds the max value in a non-empty binary search tree.
     */
    protected abstract T maxValue(Node<T> node);

    /**
     Prints the node values in the "inorder" order.
     Uses a recursive helper to do the traversal.
     */
    public void printTree() {
        printTree(root);
        System.out.println();
    }
    //print in in-order: left first, then parent, then right: in BST sorting order
    private void printTree(Node<T> node) {
        if (node == null) return;

        // left, node itself, right
        printTree(node.left);
        System.out.print(node.data + "  ");
        printTree(node.right);
    }

    /**
     Prints the node values in the "postorder" order.
     Uses a recursive helper to do the traversal.
     */
    public void printPostorder() {
        printPostorder(root);
        System.out.println();
    }

    public void printPostorder(Node<T> node) {
        if (node == null) return;

        // first recur on both subtrees
        printPostorder(node.left);
        printPostorder(node.right);

        // then deal with the node
        System.out.print(node.data + "  ");
    }

    /**
     Given a tree and a sum, returns true if there is a path from the root
     down to a leaf, such that adding up all the values along the path
     equals the given sum.
     Strategy: subtract the node value from the sum when recurring down,
     and check to see if the sum is 0 when you run out of tree.
     */
    public boolean hasPathSum(int sum) {
        return( hasPathSum(root, sum) );
    }

    boolean hasPathSum(Node<T> node, int sum) {

        //if (!(T instanceof Integer)) return false;

        // return true if we run out of tree and sum==0
        if (node == null) {
            return(sum == 0);
        }
        else {
            // otherwise check both subtrees
            int subSum = sum - (Integer)node.data;
            return(hasPathSum(node.left, subSum) || hasPathSum(node.right, subSum));
        }
    }

    /**
     Given a binary tree, prints out all of its root-to-leaf
     paths, one per line. Uses a recursive helper to do the work.
     */
/*
    public void printPaths() {
        T[] path = new T[1000];
        printPaths(root, path, 0);
    }
    */
/**
     Recursive printPaths helper -- given a node, and an array containing
     the path from the root node up to but not including this node,
     prints out all the root-leaf paths.
     *//*

    private void printPaths(Node<T> node, T[] path, int pathLen) {
        if (node==null) return;

        // append this node to the path array
        path[pathLen] = node.data;
        pathLen++;

        // it's a leaf, so print the path that led to here
        if (node.left==null && node.right==null) {
            printArray(path, pathLen);
        }
        else {
            // otherwise try both subtrees
            printPaths(node.left, path, pathLen);
            printPaths(node.right, path, pathLen);
        }
    }
*/

    /**
     Utility that prints ints from an array on one line.
     */
    private void printArray(T[] path, int len) {
        int i;
        for (i=0; i<len; i++) {
            System.out.print(path[i] + " ");
        }
        System.out.println();
    }

    /**
     Changes the tree into its mirror image.

     So the tree...
     4
     / \
     2   5
     / \
     1   3

     is changed to...
     4
     / \
     5   2
     / \
     3   1

     Uses a recursive helper that recurs over the tree,
     swapping the left/right pointers.
     */
    public void mirror() {
        mirror(root);
    }

    private void mirror(Node<T> node) {
        if (node != null) {
            // do the sub-trees
            mirror(node.left);
            mirror(node.right);

            // swap the left/right pointers
            Node temp = node.left;
            node.left = node.right;
            node.right = temp;
        }
    }

    /**
     Changes the tree by inserting a duplicate node
     on each nodes's .left.


     So the tree...
     2
     / \
     1   3

     Is changed to...
     2
     / \
     2   3
     /   /
     1   3
     /
     1

     Uses a recursive helper to recur over the tree
     and insert the duplicates.
     */
    public void doubleTree() {
        doubleTree(root);
    }

    private void doubleTree(Node<T> node) {
        Node oldLeft;

        if (node == null) return;

        // do the subtrees
        doubleTree(node.left);
        doubleTree(node.right);

        // duplicate this node to its left
        oldLeft = node.left;
        node.left = new Node(node.data);
        node.left.left = oldLeft;
    }

    /*
 Compares the receiver to another tree to
 see if they are structurally identical.
*/
    public boolean sameTree(BinaryTree<T> other) {
        return( sameTree(root, other.root) );
    }
    /**
     Recursive helper -- recurs down two trees in parallel,
     checking to see if they are identical.
     */
    boolean sameTree(Node<T> a, Node<T> b) {
        // 1. both empty -> true
        if (a==null && b==null) return(true);

            // 2. both non-empty -> compare them
        else if (a!=null && b!=null) {
            return(
                    a.data == b.data &&
                            sameTree(a.left, b.left) &&
                            sameTree(a.right, b.right)
            );
        }
        // 3. one empty, one not -> false
        else return(false);
    }

    //BFS uses Queue while DFS uses Stack, both use while loop to enqueue/dequeue, or pop/push
    public abstract void traverse();
}

/*
   --Node--
   The binary tree is built using this nested node class.
   Each node stores one data element, and has left and right
   sub-tree pointer which may be null.
   The node is a "dumb" nested class -- we just use it for
   storage; it does not have any methods.
  */
class Node <T> {
    protected T data;
    protected Node<T> left;
    protected Node<T> right;
    Node (T data) {
        this.data = data;
        right = null;
        left = null;
    }

    public void setData(T data) {
        this.data = data;
    }

    public T getData() {
        return data;
    }

    public void setLeft(Node left) {
        this.left = left;
    }

    public Node getLeft() {
        return left;
    }

    public void setRight(Node right ) {
        this.right = right;
    }

    public Node getRight() {
        return right;
    }

}
