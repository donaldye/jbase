package com.jj.datagrid;

import com.hazelcast.core.*;
import com.hazelcast.config.*;
import java.util.Map;
import java.util.Queue;
public class GettingStarted {
    public static void main(String[] args) {
        Config cfg = new Config();
        HazelcastInstance instance = Hazelcast.newHazelcastInstance(cfg);
        Map<Integer, String> mapCustomers = instance.getMap("customers");
        System.out.println("Initial Map Size:" + mapCustomers.size());
        mapCustomers.put(1, "Joe");
        mapCustomers.put(2, "Ali");
        mapCustomers.put(3, "Avi");
        System.out.println("Customer with key 1: "+ mapCustomers.get(1));
        System.out.println("Map Size:" + mapCustomers.size());
        Queue<String> queueCustomers = instance.getQueue("customers");
        System.out.println("Initial Queue size: " + queueCustomers.size());
        queueCustomers.offer("Tom");
        queueCustomers.offer("Mary");
        queueCustomers.offer("Jane");
        System.out.println("First customer: " + queueCustomers.poll());
        System.out.println("Second customer: "+ queueCustomers.peek());
        System.out.println("Queue size: " + queueCustomers.size());
    }
}

