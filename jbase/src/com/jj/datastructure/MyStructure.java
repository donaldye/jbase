package com.jj.datastructure;

import java.util.*;

/**
 * Created by yejaz on 12/31/2016.
 */
public class MyStructure {
    public static void main(String args[]) {

    }

    public static void useBinaryTree() {
        //already binary tree implementation
        TreeSet mySet = new TreeSet();

    }
    public static void useLinkedList() {
        LinkedList<String> list = new LinkedList();
        //List interface, array based, iterating
        list.add("This");
        list.add("is");
        list.add("quite");
        list.add("stupid");

        //only return the value but not the reference
        //Deque interface
        String first = list.getFirst();
        String last = list.getLast();

        //list interface
        Iterator<String> descendingIter = list.descendingIterator();
        Iterator<String> ascendingIter = list.iterator();
        ListIterator<String> listIter = list.listIterator(2);
        //functional? late binding, list.stream.foreEach() internally uses CountedCompleter to fork
        //  see StreamSupport -> ReferencePipeline->ForEachOps->to do the actual action when encountering only TerminalOp
        Spliterator<String> splitIter = list.spliterator();

        //Queue interface
        list.offer("extra");
        list.offerLast("last");
        list.peek();
        list.poll();

        //stack interface
        list.push("last item");
        list.pop();
    }
}
