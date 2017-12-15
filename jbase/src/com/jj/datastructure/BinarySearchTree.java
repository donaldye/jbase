package com.jj.datastructure;

import java.util.Comparator;

/**
 * Created by yejaz on 1/1/2017.
 */
//BST is a type of binary tree where the nodes are arranged in order
//slow in creation but are fast at insert and lookup (lg(N), log base 2)
//shape: balanced BST, left skewed BST, right skewed BST

public class BinarySearchTree <T> extends BinaryTree <T> {

    private Comparator<T> comparator;

    public BinarySearchTree(Comparator<T> comp) {
        this.comparator = comp;
    }

    /**
     Recursive insert -- given a node pointer, recur down and
     insert the given data into the tree. Returns the new
     node pointer (the standard way to communicate
     a changed pointer back to the caller).
     */
    protected Node insert(Node<T> node, T data) {
        if (node==null) {
            node = new Node(data);
        }
        else {
            if (this.comparator.compare(data, node.data) < 0) {
                node.left = insert(node.left, data);
            }
            else {
                node.right = insert(node.right, data);
            }
        }

        return(node); // in any case, return the new pointer to the caller
    }

    /**
     Recursive lookup  -- given a node, recur
     down searching for the given data.
     */
    protected boolean lookup(Node<T> node, T data) {
        if (node==null) {
            return(false);
        }

        int ret = this.comparator.compare(data, node.data);
        if (ret == 0) {
            return(true);
        }
        else if (ret < 0) {
            return(lookup(node.left, data));
        }
        else {
            return(lookup(node.right, data));
        }
    }

    protected T minValue(Node<T> node) {
        Node<T> current = node;
        while (current.left != null) {
            current = current.left;
        }

        return(current.data);
    }

    protected T maxValue(Node<T> node) {
        Node<T> current = node;
        while (current.right != null) {
            current = current.right;
        }

        return(current.data);
    }

    /**
     Tests if a tree meets the conditions to be a
     binary search tree (BST).
     */
    public boolean isBST() {
        return(isBST(root));
    }
    /**
     Recursive helper -- checks if a tree is a BST
     using minValue() and maxValue() (not efficient).
     */
    private boolean isBST(Node<T> node) {
        if (node==null) return(true);

        // do the subtrees contain values that do not
        // agree with the node?
        if (node.left!=null && this.comparator.compare(maxValue(node.left), node.data) > 0) return(false);
        if (node.right!=null && this.comparator.compare(minValue(node.right), node.data) <= 0) return(false);

        // check that the subtrees themselves are ok
        return( isBST(node.left) && isBST(node.right) );
    }

    /**
     Tests if a tree meets the conditions to be a
     binary search tree (BST). Uses the efficient
     recursive helper.
     */
    /*public boolean isBST2() {
        return( isBST2(root, Integer.MIN_VALUE, Integer.MAX_VALUE) );
    }*/
    /**
     Efficient BST helper -- Given a node, and min and max values,
     recurs down the tree to verify that it is a BST, and that all
     its nodes are within the min..max range. Works in O(n) time --
     visits each node only once.
     */
    /*private boolean isBST2(Node<T> node, int min, int max) {
        if (node==null) {
            return(true);
        }
        else {
            // left should be in range  min...node.data
            boolean leftOk = isBST2(node.left, min, node.data);

            // if the left is not ok, bail out
            if (!leftOk) return(false);

            // right should be in range node.data+1..max
            boolean rightOk = isBST2(node.right, node.data+1, max);

            return(rightOk);
        }
    }*/

    public void traverse() {

    }

}


