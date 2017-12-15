package com.jj.function;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.*;
import java.util.Map.Entry;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.function.Supplier;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class Util {

    public static void main(String[] args) {
	// write your code here
        HashMap <Dog, Integer> hashMap = new HashMap <Dog, Integer>();
        //LinkedHashMap <Dog, Integer> hashMap = new LinkedHashMap <Dog, Integer>();

        Dog d1 = new Dog("red");
        Dog d2 = new Dog("black");
        Dog d3 = new Dog("white");
        System.out.println("d3's hashCode=" + d3.hashCode());
        Dog d4 = new Dog("white");
        System.out.println("d4's hashCode=" + d4.hashCode());
        hashMap.put(d1, 10);
        hashMap.put(d2, 15);
        hashMap.put(d3, 5);
        hashMap.put(d4, 20);
//print size
        System.out.println(hashMap.size());
//loop HashMap
        for (Map.Entry<Dog, Integer> entry : hashMap.entrySet()) {
            System.out.println(entry.getKey().toString() + " - " + entry.getValue());
        }

        //Java 8 Puzzlers
        //1. function Math::max is not a comparator but a function that has the same arguments as an Integer Comparator
        // a comparator return 0, -1, or 1 but max return the bigger one of two number,
        //  int max(int i1, int, i2), max(-3, -2)=-2, max(-2 -1)=-1 means less than as a comparator, max(-1, 0)=0, means equal
        //      max(-1, 1)=1 means -1 is bigger, max(-1, 2)=2 means -1 is bigger,... so returns -1 in the end
        System.out.println("Sugar !: "
                +Stream.of(-3, -2, -1, 0, 1, 2, 3).max(Math::max).get()
        );
        System.out.println("Works !: "
                +Stream.of(-3, -2, -1, 0, 1, 2, 3).max(Comparator.comparing(i->i)).get()
        );
        System.out.println("Works too!: "
                +Stream.of(-3, -2, -1, 0, 1, 2, 3).max(Integer::compare).get()
        );

        //2. amazing behaviors, try it a few times
        List<String> list = new ArrayList<> ();
        list.add("milk");
        list.add("bread");
        list.add("sausage");
        //without this sublist call, it will print out everything.
        // This subList() is not a late binding, a JVM bug in Java8, fixed in Java9
        list = list.subList(0, 2);
        //steam() is a lazy binding.
        Stream<String> stream = list.stream();
        list.add("eggs, don't forget eggs!");
        System.out.println("so far so good");
        //stream.forEach(System.out::println);

        //3.
        //
        class Dogcat implements DogI, CatI {}
        //this takes Object as arg and tries to call methods on both interfaces
        test(new Dogcat());
        //these will also print
        //Dogcat d2 = new Dogcat(); //it will complain variable d2 is already defined
        Object obj = new Dogcat();
        ((DogI & CatI) obj).meow(); ((DogI & CatI) obj).bark();
        ((CatI)obj).meow();((DogI)obj).bark();

        //4. Callable and Runnable in submit
        //killAll();

        //5.
        Map<String, String> oldSchool = new HashMap<String, String>();
        oldSchool.put("buildTool", "maven");
        oldSchool.put("lang", "java");
        oldSchool.put("db", "db2");
        Map<String, String> proper = new HashMap<String, String>();
        proper.put("buildTool", "npm");
        proper.put("lang", "javascript");
        proper.put("db", "elastic");
        //weird =>
        //this will call proper::put to put oldSchool's value per key into proper
        //the put returns the proper's original value into oldSchool's setValue
        oldSchool.replaceAll(proper::put);
        //ahah, maps will swap
        System.out.println(oldSchool);
        System.out.println(proper);

        //6.
        List<String> kitties = Arrays.asList("Soft", "Warm", "Purr");
        //what if this?
        //List<String> kitties = Arrays.asList("Soft", null, "Purr");
        Comparator<String> kittiesComparator = Comparator.nullsLast(Comparator.naturalOrder());
        //different ways to get max
        System.out.println(Collections.max(kitties, kittiesComparator));
        //if Warm is null, then the below twos will throw exception since the get is run on the null object
        //stream() introduce Optional, maxBy() returns a Collector that contains Optional while max() returns an Optional
        //Optional is a wrapper on an object found. It is nicer way to catch null pointer,
        // instead, maxBy throws NoSuchElement exception while max() throws nullPointer
        System.out.println(kitties.stream().collect(Collectors.maxBy(kittiesComparator)).get());
        System.out.println(kitties.stream().max(kittiesComparator).get());

        //7. s1 uses pointer and hence reflects the latest String
        String str = "hello";
        Supplier<String> s1 = str::toUpperCase;
        //this won't work as it expects str to be final
        //Supplier<String> s2 = () -> str.toUpperCase();
        str = "Hotel Echo Lima Lima Oscar";
        System.out.println(s1.get());
        //System.out.println(s2.get());

        //8.
        //asList creates ArrayList by default but List interface does not support remove
        //so wrap it with ArrayList
        List<String> list1 = new ArrayList<>(Arrays.asList("Arnie", "Chuck", "Slay"));
        //List<String> list1 = Arrays.asList("Arnie", "Chuck", "Slay");
        /*list1.stream().forEach(x -> {
            //after Chuck is removed, need to compare the last element which is null
            if (x.equals("Chuck")) {
                //when it's Chuck in the 2nd, this will actually change the arraylist to [Arnie, Slay, null]
                list1.remove(x);
            }
        });*/
        //a usual way to fix the null pointer but fails in ConcurrentModificationException
        /*list1.stream().forEach(x -> {
            //after Chuck is removed, need to compare the last element which is null
            if ("Chuck".equals(x)) {
                //when it's Chuck in the 2nd, this will actually change the arraylist to [Arnie, Slay, null]
                list1.remove(x);
            }
        });*/

        //still gets ConcurrentModificationException
        /*list1.stream().forEach(x -> {
            //after Chuck is removed, need to compare the last element which is null
            list1.removeIf("Chuck"::equals);
        });*/

        //if non Java 8, create an empty list and add those not "Chuck" in a loop or use Iterator.remove()
        //if Java8
        list1 = list1.stream().filter(x -> !"Chuck".equals(x)).collect(Collectors.toList());
        System.out.println(list1);

        //9.
        //value is present, so it didn't invoke other which is null. otherwise it would have thrown nullPointer
        System.out.println(Optional.of("rtfm").orElseGet(null));
        //this will throw NullPointerException as the function to map() is null. it never calls orElse
        //System.out.println(Optional.empty().map(null).orElse("rtfm"));

        //ArrayList l;
        //LinkedList ll
    }

    private static void killAll() {
        ExecutorService ex = Executors.newSingleThreadExecutor();
        List<String> sentence = Arrays.asList("Punish");
        //accepts the Callable/Runnable arg, one returns and can throw exception while the other doesn't
        //returns a Future
        //implicit return, this will be casted into Callable since it returns and can throw exception
        ex.submit(() -> Files.write(Paths.get("Sentence.txt"), sentence));
        //if using {, need to do explicit return
        ex.submit(() -> {return Files.write(Paths.get("Sentence.txt"), sentence);});
        //IOException is a CheckedException, need to handle in compile time
        //this is casted as Runnable, since it does't throw exception, so we'd need to catch the exception explicitly
        ex.submit(() -> {
            try {
                Files.write(Paths.get("Sentence.txt"), sentence);
            }
            catch (IOException ioe) {
                System.out.println(ioe.getMessage());
            }
        });

    }

    interface CatI {default void meow() {System.out.println("meow");}}
    interface DogI {default void bark() {System.out.println("woof");}}

    static void test(Object obj) {
        // Optional.of is a way to wrap a type, inference
        Optional.of((DogI& CatI) obj).ifPresent(
                x -> {
                    x.meow();
                    x.bark();
                });
    }
}

class Dog {
    String color;
    Dog(String c) {
        color = c;
    }
    public String toString(){
        return color + " dog";
    }

    public boolean equals(Object o) {
        return ((Dog) o).color == this.color;
    }
    public int hashCode() {return color.length();}
}



