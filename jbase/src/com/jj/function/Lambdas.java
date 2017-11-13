package com.jj.function;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CountedCompleter;
import java.util.concurrent.ForkJoinPool;
import java.util.concurrent.RecursiveTask;
import java.util.concurrent.atomic.AtomicIntegerArray;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.*;
import java.util.jar.JarFile;
import java.util.logging.ConsoleHandler;
import java.util.logging.Handler;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.Stream;

/**
 * Created by donaly on 12/18/2016.
 */
public class Lambdas {

    static Logger logger = Logger.getLogger(Lambdas.class.getName());

    private static class City implements Comparable {
        private final String name;

        public City(String name) {
            this.name = name;
        }

        public String getName() {
            return name;
        }

        public boolean equals(Object name) {
            return this.name.equals(name);
        }

        public int hashCode() {
            return name.hashCode();
        }

        public int compareTo(Object o) {
            return this.getName().compareTo(((City)o).getName());
        }
    }

    private static class Person {

        private final String name;
        private final int age;
        private final String address;
        //this will make income mutable
        private double income;
        private final City city;

        public Person(String name, int age, String address, double income, City city) {
            this.name = name;
            this.age = age;
            this.address = address;
            this.income = income;
            this.city = city;
        }

        public String getName() {
            return name;
        }

        public int getAge() {
            return age;
        }
        public String getAddress() {
            return address;
        }

        public double getIncome() { return income;}
        public void setIncome(double income) {this.income = income;}

        public City getCity() { return city;}

        public String toString() {
            return name +"," + age + "," + income + "," + address + "," + city.name;
        }
    }

    static private class Averager implements IntConsumer
    {
        private int total = 0;
        private int count = 0;

        public double average() {
            return count > 0 ? ((double) total)/count : 0;
        }

        public void accept(int i) { total += i; count++; }
        public void combine(Averager other) {
            total += other.total;
            count += other.count;
        }
    }

    //simulate Stream.forEach(), using Spliterator to split and CountedCompleter which is ForkJoinTask which is a Future
    //using void means resultless
    static class ParEach<T> extends CountedCompleter<Void> {

        static <T> void parEach(List<T> a, Consumer<T> action) {
            Spliterator<T> s = a.spliterator();
            long targetBatchSize = s.estimateSize() / (ForkJoinPool.getCommonPoolParallelism() * 8);
            new ParEach(null, s, action, targetBatchSize).invoke();
        }

        final Spliterator<T> spliterator;
        final Consumer<T> action;
        final long targetBatchSize;

        ParEach(ParEach<T> parent, Spliterator<T> spliterator,
                Consumer<T> action, long targetBatchSize) {
            super(parent);
            this.spliterator = spliterator;
            this.action = action;
            this.targetBatchSize = targetBatchSize;
        }

        public void compute() {
            Spliterator<T> sub;
            while (spliterator.estimateSize() > targetBatchSize &&
                    (sub = spliterator.trySplit()) != null) {
                addToPendingCount(1);
                //recursive split, fork is like a executorService.submit using ForkJorkPool.commonPool()
                new ParEach<>(this, sub, action, targetBatchSize).fork();
            }
            spliterator.forEachRemaining(action);
            propagateCompletion();
        }
    }

    //passing in an array instead of SplitIterator, splitting manually via divide by two or iterating through
    //resultless
    static class ForEach<E> extends CountedCompleter<Void> {

        public static <E> void forEach(E[] array, Consumer<E> op) {
            new ForEach<E>(null, array, op, 0, array.length).invoke();
        }

        final E[] array;
        final Consumer<E> op;
        final int lo, hi;

        ForEach(CountedCompleter<?> p, E[] array, Consumer<E> op, int lo, int hi) {
            super(p);
            this.array = array;
            this.op = op;
            this.lo = lo;
            this.hi = hi;
        }

        public void compute() {
            // version 1
            /*if (hi - lo >= 2) {
                int mid = (lo + hi) >>> 1;
                setPendingCount(2); // must set pending count before fork
                new ForEach(this, array, op, mid, hi).fork(); // right child
                new ForEach(this, array, op, lo, mid).fork(); // left child
            }
            else if (hi > lo)
                op.accept(array[lo]);
            tryComplete();*/

            //Version 2
            /*if (hi - lo >= 2) {
                int mid = (lo + hi) >>> 1;
                setPendingCount(1); // only one pending
                new ForEach(this, array, op, mid, hi).fork(); // right child
                new ForEach(this, array, op, lo, mid).compute(); // direct invoke
            }
            else {
                if (hi > lo)
                    op.accept(array[lo]);
                tryComplete();
            }*/

            //Version 3
            int l = lo,  h = hi;
            while (h - l >= 2) {
                int mid = (l + h) >>> 1;
                addToPendingCount(1);
                new ForEach(this, array, op, mid, h).fork(); // right child
                h = mid;
            }
            if (h > l)
                op.accept(array[l]);
            propagateCompletion();
        }
    }

    //it returns result E
    static class Searcher<E> extends CountedCompleter<E> {

        public static <E> E search(E[] array, Predicate<E> comp) {
            return new Searcher<E>(null, array, new AtomicReference<E>(), 0, array.length, comp).invoke();
        }

        final E[] array;
        final AtomicReference<E> result;
        final int lo, hi;
        Predicate<E> comp;
        Searcher(CountedCompleter<?> p, E[] array, AtomicReference<E> result, int lo, int hi, Predicate<E> comp) {
            super(p);
            this.array = array; this.result = result; this.lo = lo; this.hi = hi;
            this.comp = comp;
        }
        public E getRawResult() { return result.get(); }
        public void compute() { // similar to ForEach version 3
            int l = lo,  h = hi;
            while (result.get() == null && h >= l) {
                if (h - l >= 2) {
                    int mid = (l + h) >>> 1;
                    addToPendingCount(1);
                    new Searcher(this, array, result, mid, h, comp).fork();
                    h = mid;
                }
                else {
                    E x = array[l];
                    if (matches(x) && result.compareAndSet(null, x))
                        quietlyCompleteRoot(); // root task is now joinable
                    break;
                }
            }
            tryComplete(); // normally complete whether or not found
        }
        boolean matches(E e) { return comp.test(e);} // return true if found
    }

    //this might not be good to divide on such a small subtask
    static class Fibonacci extends RecursiveTask<Long> {

        public static long get(long i) {
            return new Fibonacci(i).invoke();
        }

        final long n;
        final static long THRESHOLD = 100;
        Fibonacci(long n) { this.n = n; }
        public Long compute() {
            if (n <= THRESHOLD)
                return (new com.jj.algo.Fibonacci(n).get());
            Fibonacci f1 = new Fibonacci(n - 1);
            f1.fork();
            Fibonacci f2 = new Fibonacci(n - 2);
            return f2.compute() + f1.join();
        }
    }

    static class IntSum extends RecursiveTask<Long> {

        public static void addInt() {
            ForkJoinPool pool = new ForkJoinPool();
            IntSum task = new IntSum(3);
            long sum = pool.invoke(task);
            System.out.println("Sum is " + sum); //2+2+2=6
        }

        private int count;
        public IntSum(int count) {
            this.count = count;
        }

        @Override
        protected Long compute() {
            long result = 0;

            if (this.count <= 0) {
                return 0L;
            }else if (this.count == 1) {
                return (long) this.getRandomInteger();
            }
            List<RecursiveTask<Long>> forks = new ArrayList<>();
            for (int i = 0; i < this.count; i++) {
                IntSum subTask = new IntSum(1);
                subTask.fork(); // Launch the subtask
                forks.add(subTask);
            }
            // all subtasks finish and combine the result
            for (RecursiveTask<Long> subTask : forks) {
                result = result + subTask.join();
            }
            return result;
        }

        public int getRandomInteger() {
            return 2;
        }
    }

    public static void main(String[] args) throws IOException {

        //asList returns ArrayList by default
        //List<Integer> scores = new ArrayList<Integer>(Arrays.asList(60, 100, 70, 80));
        List<Integer> scores = Arrays.asList(60, 100, 70, 80);

        Predicate funcArgs = new Predicate<Integer>() {
            public boolean test(Integer i) {
                return i != 48;
            }
        };

        Function mapper = new Function<Integer, Integer>() {
            public Integer apply(Integer i) {
                return i;
            }
        };

        Comparator comp = new Comparator<Integer>() {
            @Override
            public int compare(Integer o1, Integer o2) {
                return Integer.compare(o1, o2);
            }
        };

        //lib handles the iteration
        // may not serial/could be parallel.
        //  that is, lib could spawn threads to split the items and then do map-reducing
        // lazy traversal ?
        // thread safe/client logic is stateless. values couldn't be amended during traversal
        // but ugly codes above to define Predicate, function and comparator
        System.out.println(scores.stream().filter(funcArgs).map(mapper).max(comp).get());

        System.out.println("simplified=" +
                        scores.stream()
                                //filter expects Predicate which has a test() abstract method
                                //do use parentheses around the arguments to make it recognize its constructor
                                .filter((Integer i) -> i != 48)
                                .map((Integer i) -> i)
                                //three parts: argument list/like an anonymous constructor,
                                //  arrow token
                                // and body/statement blocks; if using blocks, need to return a result explicitly if needed
                                //why this would work: max(Comparator) but Comparator has two abstract methods compareTo and equals
                                //.max((Integer x, Integer y) -> {return x.compareTo(y);})
                                //replace this with static method reference
                                .max(Integer::compare)
                //would this work? this depends on whether that method defines a compare(o1, o2)
                //this doesn't define
                //.max(Integer::compareTo)
        );

        logger.info("use Lambdas");
        System.out.println(scores.stream().filter(s->s!= 48)
                .max(Integer::compare).get()
            );


        //simplified
        //how does it know which method it is calling this println? the only abstract method run() it is
        Runnable r2 = () -> {System.out.println("Hello World!");};
        r2.run();
        (new Thread(()->System.out.println("Hello World in one line!"))).start();

        //how to test Lambdas in standalone statement
        //?

        //need to define the generic class Person here for Lambdas to work
        City manhanttan = new City("Manhattan");
        City statenIsland = new City("Staten Island");
        List<Person> persons = new ArrayList<Person>();
        persons.add(new Person("Albert", 80, "154 Barclay Ave", 40000, statenIsland));
        persons.add(new Person("Ben", 15, "1585 Broadway", 100000, manhanttan));
        persons.add(new Person("Charlote", 20, "1301 Ave of Americas", 150000, manhanttan));
        persons.add(new Person("Dean", 6, "World Trade 7", 200, manhanttan));
        persons.add(new Person("Elaine", 17, "World Trade 7", 70000, new City("Manhattan")));

        System.out.println("Listing the ages here:" + listAllAges(persons, Person::getAge));
        System.out.println("Listing the ages here:" + listAllAges(persons));

        System.out.println("Persons with ages bigger than 18:" +
                //persons.stream().filter((Person x) -> x.getAge() > 18).collect(Collectors.toList()));
                //return the list
                //persons.stream().filter(x -> x.getAge() > 18).collect(Collectors.toList()));
                //this involves two traverses
                //listAllAges(persons.stream().filter(x -> x.getAge() > 18).collect(Collectors.toList())));
                //how to print each Person in one traversal
                listAllAges(persons.stream().filter(x -> x.getAge() > 18).collect(Collectors.toList())));

        //sum up the income: stream -> intermediate operations->terminal operation
        //this will evaluate the getIncome since the terminal operation is called
        logger.info("Income Total=" +persons.stream().mapToDouble(Person::getIncome).sum());

        //some pre-defined FunctionalInterface: Consumer
        //has input parameters but no output: void accept(T)
        Consumer<String> consumer = Lambdas::printNames;
        //this actually does printing, amazing !
        consumer.accept("Jeremy");
        consumer.accept("Paul");
        consumer.accept("Richard");

        //Other List Functional Interfaces =>
        //Supplier: take no arguments but return a value: get()
        List names = new ArrayList();
        names.add( "David");
        names.add( "Sam");
        names.add( "Ben");
        names.stream()
                //forEach takes Consumer: input, no output
                .forEach((x) ->
                                //printNames takes Supplier: no input, return a result like below
                                //this is strange !
                                {printNames(() -> x);}
                        );
        //don't need to call stream()
        names.forEach(s->System.out.println(s));
        //or using method reference
        names.forEach(System.out::println);
        //this would need the List to be defined with Generics using <>
        List<String> names2 = new ArrayList<>();
        names2.add( "David");
        names2.add( "Sam");
        names2.add( "Ben");
        names2.removeIf(s->s.length() == 3);
        System.out.println();
        names2.forEach(System.out::println);
        //UnaryOperator just has one interface method: identity()
        //UnaryOperator: single input and return the same type: T apply(T)
        //this method is not on the List itself but on the list element which is String, in this case
        names2.replaceAll(s->s.toUpperCase());
        names2.forEach(System.out::println);
        //or using Method Reference
        names2.replaceAll(String::toLowerCase);
        names2.forEach(System.out::println);

        //List default Algorithm
        List<String> names3 = new ArrayList<>();
        names3.add( "David");
        names3.add( "Sam");
        names3.add( "Ben");
        //takes Comparator functional interface: compareTo(a, b),
        //instead of Collections.sort(List l, Comparator c)
        names3.sort((x,y) -> x.compareTo(y));
        //or
        names3.sort(String::compareTo);
        System.out.println();
        names3.forEach(System.out::println);
        // not like Visitor pattern, not Comparable interface: compareTo(T a, T b) which the object must implement otherwise
        // Visitor pattern define a series of compareTo for different Generic type
        //  and it's passed into each object for later virtual function usage
        //  like, compareTo(CompactCar a, b), compareTo(Truck a, b), compareTo(SUV a, b), ...
        // not Decorator pattern, which enables you to add functionality to an object without having to change the object's source code
        // it is a Strategy pattern:

        //even if the current level=INFO, Lambdas.complexMessageLogic() is still being called
        logger.log(Level.FINER, Lambdas.complexMessageLogic());
        //take supplier argument, the function interface can't have arguments
        // Lambdas::complexMessageLogic is a supplier
        //lazy calling, only called when the finer is enabled. Hence complexMessageLogic is not called here
        logger.log(Level.FINER, Lambdas::complexMessageLogic);
        logger.finer("test");
        //this won't work
        logger.setLevel(Level.FINER);
        //need to set logging.properties
        // java.util.logging.ConsoleHandler.level = FINER
        //or
        Handler consoleHandler = new ConsoleHandler();
        consoleHandler.setLevel(Level.FINER);
        logger.addHandler(consoleHandler);
        //this might not work
        //logger.getAnonymousLogger().addHandler(consoleHandler);
        //now it's called since FINER is enabled
        logger.finer(Lambdas::complexMessageLogic);
        //Stream.of()

        exercise4();

    //Stream interface method

        //how to return Stream
        //StreamSupport.stream(spliterator(), false);
        persons.stream(); //sequential and returning an Array by StreamSupport
        persons.parallelStream(); //used in fork/join, only available in Collection

        //Files.find()
        Random random = new Random();
        random.ints();
        try {
            JarFile jarFile = new JarFile("test.jar");
            jarFile.stream();

            BufferedReader bf = new BufferedReader(new FileReader("test.txt"));
            bf.lines();
        }
        catch (IOException exp) {
        }

        Pattern pattern = Pattern.compile("a*b"); //like aaaab
        Matcher m = pattern.matcher("aaaaab");
        boolean isMatched = m.matches();
        pattern = Pattern.compile(",");
        pattern.splitAsStream("1,2,3,4").forEach(System.out::println);
        pattern.split("1,2,3,4"); //Array not a stream
        Arrays.asList(pattern.split("1,2,3,4")).forEach(System.out::println);

        CharSequence charSeq = "1234";
        System.out.println("printing CharSequence...");
        charSeq.chars().forEach(System.out::println);
        charSeq.codePoints().forEach(System.out::println);

        System.out.println("printing BitSet...");
        byte[] bytes = {1,1};
        BitSet bSet = BitSet.valueOf(bytes);
        bSet.stream().forEach(System.out::println);

        //Stream static methods
        //concat, of, range, rangeClosed, generate, iterate,

        // intermediate operations
        //distinct, groupingBy, filter, map (1:1), mapToInt, ...
        //skip, limit
        //sorted, unordered which is good for distinct, groupingBy
        //peek
        //flatMap, 1:M mapping
        //persons.stream().map(Function T, R)
        //persons.stream().flatMap(Function T, Stream T)
        System.out.println("constructing flagMap=");
        persons.stream().flatMap(person -> Stream.of(person.getName(), person.getAge(), person.getIncome()))
                .forEach(System.out::println);
        //store the flagMap into a list
        List<Object> output = persons.stream().flatMap(person -> Stream.of(person.getName(), person.getAge(), person.getIncome()))
                                    .collect(Collectors.toList());
        //joining them as String
        System.out.println("Print Persons into one line=" + persons.stream()
                .flatMap(person -> Stream.of(person.getName(),
                                    String.valueOf(person.getAge()),
                                    String.valueOf(person.getIncome())))
                .collect(Collectors.joining(","))
            );

        //peek(action): return the same Stream after an action
        //different from map which won't change the data but create a new Stream
        System.out.println("income increase by 1000=" + persons.stream().peek(s->s.setIncome(s.getIncome() + 1000))
                .map(s->String.valueOf(s.getIncome()))
                .collect(Collectors.joining(",")));

        //many terminal operations are reduction operations. Here we could use reduce operator to achieve the same and even more customization
        //it is not returning the Optional but the same type as the Stream item, which getIncome, Double object
        //a is the partial result which starts from 0.0 and b is the next element, so a+b is same as a=a+b,
        //  or partialResult = partialResult + nextElement
        System.out.println(persons.stream().map(Person::getIncome).reduce(0.0, (a,b)-> a+b));
        //collect
        //already used in, collect(collector), return
        //listAllAges(persons.stream().filter(x -> x.getAge() > 18).collect(Collectors.toList())));
        Averager averageCollect = persons.stream()
                .map(Person::getAge)
                //take Supplier, Accumulator, and Combiner
                .collect(Averager::new, Averager::accept, Averager::combine);

        System.out.println("Average age of male members: " + averageCollect.average());
        //or
        System.out.println("Average age of male members: " +
                        persons.stream().mapToInt(Person::getAge).average().getAsDouble());




        // short-circuited, like findFirst

        //lazy
        //pipeline is only evaluated when the terminal operation is called
        //getIncome is not called since no terminal operation is called
        System.out.println("Income Total=" +persons.stream().mapToDouble(Person::getIncome));

        persons.stream().distinct();

        //Java is not a pure OO lang
        //instead of object streams, use primitive streams to avoid autoboxing and unboxing
        //like the mapToDouble above to generate DoubleStream

        //terminal operations: lazy merging, could be done parallel
        //findAny same findFirst but for parallel stream
        //xxxMatch
        //collect
        //count, max, min for object stream
        // average,sum only for Primitive type Stream
        persons.stream().toArray();

        //Iteration: forEach, forEachOrdered. don't use imperative style here
        System.out.println("Print Persons in parallel but in order");
        persons.parallelStream().forEachOrdered(System.out::println);

        //shrink a stream, not returning Stream
        //reduce(initial, accumulator, combiner)

        //Optional
        //could avoid returning null, a wrapper on null or non-null object
        //without it, it could need to check null,
        persons.get(0).getIncome();
        System.out.println(scores.stream().filter(funcArgs).map(mapper).max(comp).isPresent());
        scores.stream().filter(funcArgs).map(mapper).max(comp).ifPresent(System.out::println);
        //Optional contains many powerful operations on the resulting object
        //scores.stream().filter(funcArgs).map(mapper).max(comp).filter()
        //now instead of returning an object, to return an Optional to wrap that object, like
        //  class Person { public Optional<Company> getCompany() {...} }
        Optional thisPerson = Optional.ofNullable(persons.get(0));

        exercise3();

        //lesson2Eexercise4();
        //lesson2Exercise5();

        //reduce
        //find the length of the longest line
/*
        System.out.println(String.format("Length of the longest line=%s",
            Files.lines(
                    Paths.get("D:\\allbooks\\IT\\Oracle\\Oracle Massive Open Online Course Java SE 8 Lambdas and Streams\\lesson2\\Sonnetl.txt"))
                    //not so good to use filter as it would need to store the intermediate currentMaxLength which is not thread safe, not stateless
                    //.filter(s->s.length())
                    .mapToInt(String::length)
                    .max()
                    .getAsInt()
                ));
        //find the longest line using normal Stream
        System.out.println(String.format("the longest line using normal Stream=%s",
            Files.lines(
                    Paths.get("D:\\allbooks\\IT\\Oracle\\Oracle Massive Open Online Course Java SE 8 Lambdas and Streams\\lesson2\\Sonnetl.txt"))
                    //this sorting might take more than O(n), more time in a big file
                    //not efficient as we don't need to sort it for the longest one
                    .sorted((a,b)-> b.length() - a.length()) //in descending order
                    .findFirst()
                    .get()
        ));

        //using traditional way, but it is not thread safe, serial, not functional
        */
/*String longest = "";
        BufferedReader reader = Files.newBufferedReader(
                Paths.get("D:\\allbooks\\IT\\Oracle\\Oracle Massive Open Online Course Java SE 8 Lambdas and Streams\\lesson2\\Sonnetl.txt"));
        while ( (String s = reader.readLine()) != null) {
            if (s.length() > longest.length()) longest = s;
        }*//*


        //traditional recursive approach: solve the thread safe issue but may need a lot of stack memory for large file and still serial
        //start with empty string, index=0
        BufferedReader reader = Files.newBufferedReader(
                Paths.get("D:\\allbooks\\IT\\Oracle\\Oracle Massive Open Online Course Java SE 8 Lambdas and Streams\\lesson2\\Sonnetl.txt"));
        System.out.println(String.format("Longest line using recursion=%s",
                findLongestString("", 0, reader.lines().collect(Collectors.toList()))));

        //Stream reduce approach, O(n), still recursive but no sorting, no stack frames, not sure it's parallel though as it needs recursive
        //not imperative, no looping, no resource overhead
        System.out.println(String.format("Longest line using Stream Reduce=%s",
            Files.lines(
                    Paths.get("D:\\allbooks\\IT\\Oracle\\Oracle Massive Open Online Course Java SE 8 Lambdas and Streams\\lesson2\\Sonnetl.txt"))
                //identity to start with "", then the accumulator is a BinaryOperator that takes the partial result and next element
                    .reduce(
                        "",
                        //accomulator is a BinaryOperator, need to look at the parameter type !!!
                        //the 3rd parameter Combiner is also a BinaryOperator
                        (partialResult, nextElement)->
                                partialResult.length() > nextElement.length() ? partialResult: nextElement
                        ))
        );

        //this is even the simplest
        System.out.println(
                String.format("Longest line using max with special Comparator=%s",
                        Files.lines(
                            Paths.get("D:\\allbooks\\IT\\Oracle\\Oracle Massive Open Online Course Java SE 8 Lambdas and Streams\\lesson2\\Sonnetl.txt"))
                            //use a customized Comparator
                        //.max((x, y) -> x.length() - y.length())
                        //or, amazing !!
                        .max(Comparator.comparingInt(String::length))
                        .get()
                )
        );
*/

        //finite and infinite
            //findFirst(Predicate)
            //findAny(Predicate)
        //https://www.mkyong.com/java/java-generate-random-integers-in-a-range/
        Random r = new Random();
        //finite, stop the 1st
        int i = r.ints().filter(x->x>256).findFirst().getAsInt();
        System.out.println(String.format("First random int bigger than 256: %s", i));

        //generate an infinite Fahrenheit stream ranging from -60 to 120 degree
        /*System.out.println("converting to Fahrenheit: ");
        r.ints(-60, 120)
                .map(t->((t-32)*5/9))
                .forEach(System.out::println);
        */

        //avoid forEach
        //try to use map and reduce as much as possible, which create temporary Stream/variable on the recursion
        //try not to modify any other variable

        //Collector: perform mutable reduction on a stream, using temporary result container, does the termination
        //Collectors has many method to generate Collector
        /*Collectors.collectingAndThen()
        Collectors.groupingBy()
        Collectors.mapping()
        Collectors.partitioningBy()
        Collectors.toCollection()
        Collectors.toList()
        Collectors.toSet()
        Collectors.toMap()*/
        //collect to a map
        Map<Person, Double> personToIncome =
                persons.stream().collect(
                        Collectors.toMap(
                            Function.identity(),// same as: person->person,
                            person->person.getIncome())
                        );
        Map<String, String> occupants = persons.stream()
                .collect(Collectors.toMap(
                                Person::getAddress, //person -> person.getAddress()
                                Person::getName, //person -> person.getName()
                                //mergeFunction is a BinaryOperator
                                    //for removing duplication by combining those with the same address as the key into the value
                                    //"World Trade 7" -> "Dean,Elaine"
                                (x, y) -> x + "," + y
                            )
                        );

        BufferedReader fileReader = Files.newBufferedReader(
                Paths.get("D:\\allbooks\\IT\\Oracle\\Oracle Massive Open Online Course Java SE 8 Lambdas and Streams\\lesson2\\Sonnetl.txt"),
                StandardCharsets.UTF_8);
        //Lambdas won't create new class, just layout the method calls,
        List<String> words =
                fileReader.lines()
                        //use peek() and method reference to simplify debugging
                        //for debugging: print lines
                        //another way is to extract the code into a separate method
                        //  and replace the Lambda with a method reference
                        //  set breakpoints on the new method
                        .peek(System.out::println)
                        .flatMap(line->Stream.of(line.split(WORD_REGEXP)))
                        //No-Op Lambda: set a breakpoint here
                        //.peek(s -> s)
                        //print words
                        .peek(System.out::println)
                        .map(String::toLowerCase)
                        .distinct()
                        //try to create a method reference for debugging
                        .sorted((x, y) -> x.length() - y.length())
                        .collect(Collectors.toList());
        //Map<k,<List<v> per k>
        Map map = words.stream().collect(Collectors.groupingBy(String::length));
        //Map<k, [count per k]>
        Map mapCount = words.stream().collect(Collectors.groupingBy(String::length, Collectors.counting()));

        //Collectors.joining(delimite, prefix, suffix)

        //Numeric Collectors
/*
        Collectors.averagingInt()
        Collectors.maxBy()
        Collectors.summingLong()
        //count, sum, min, max, average
        Collectors.summarizingLong()
*/
        /*Collectors.reducing()
        //Map<boolean, List>, only two groups
        Collectors.partitioningBy(Predicate)
        Collectors.mapping()*/
        Map<City, Set<String>> namesByCity =
                persons.stream()
                        //this groupingBy does the grouping by the actual object pointer but not by the equals, hashCode, compareTo
                    .collect(Collectors.groupingBy(Person::getCity,
                            //mpaaing(Function, Collector)
                            Collectors.mapping(Person::getName, Collectors.toSet())));

        System.out.println("");
        //parallel stream
        String forkJoinPoolPropName = "java.util.concurrent.ForkJoinPool.common.parallelism";
        System.out.println(forkJoinPoolPropName + "=" + System.getProperty(forkJoinPoolPropName));

        //turn sequential to parallel at any point, the last wins
        persons.stream().parallel();
        //vice versa
        persons.parallelStream().sequential();
        //non-deterministic, better for parallel stream performance
        persons.stream().findAny();
        //deterministic
        persons.stream().findFirst();
        //non-deterministic result, better for parallel stream performance
        persons.stream().forEach(System.out::println);
        //for deterministic result
        persons.stream().forEachOrdered(System.out::println);

        //Parallel streams use a fork/join model when they execute.
        // This means that the task is split into subtasks.
        // Each subtask is executed and the results combined.
        // In this example, the list is partitioned and each partition is summed.
        // These partitions are then summed until we have one final answer.
        persons.parallelStream().mapToInt(p->p.getAge()).average();
        LinkedList<Person> persons2 = new LinkedList<Person>();
        persons2.add(new Person("Albert", 80, "154 Barclay Ave", 40000, statenIsland));
        persons2.add(new Person("Ben", 15, "1585 Broadway", 100000, manhanttan));
        persons2.add(new Person("Charlote", 20, "1301 Ave of Americas", 150000, manhanttan));
        persons2.add(new Person("Dean", 6, "World Trade 7", 200, manhanttan));
        persons2.add(new Person("Elaine", 17, "World Trade 7", 70000, new City("Manhattan")));
        persons2.parallelStream().mapToInt(p->p.getAge()).average();
        //Clearly, this partitioning will be most effective when the partitions are roughly equal.
        // An ArrayList supports random access, so partitioning is much quicker and easier than with a LinkedList;
        // decomposing the list into these partitions is O(n).
        //at the stream level, it doesn't loop through each item to partition but using math and index position
        //hence ArrayList uses the advantages

        //for HashSet (using key buckets), TreeSet (using binary tree), using parallelStream is a bit better than LinkedList
        // but not better than ArrayList
        // read https://docs.oracle.com/javase/8/docs/api/java/util/stream/package-summary.html


        //AtomicIntegerArray

        //debugging

        //unit testing

        //advanced: reimplement parallelStream.forEach using ForkJoin/CountedCompleter
        System.out.println("My ParEach");
        ParEach.parEach(persons, System.out::println);
        System.out.println("My ForEach");
        ForEach.forEach(persons.toArray(), System.out::println);
        //simulate parallelStream.filter using ForkJoin/CountedCompleter
        System.out.println("found Person=" + Searcher.search(persons.toArray(new Person[0]), x->x.getName().equals("Elaine")));

        //this will use 100% CPU and very slow. don't do it
        //System.out.printf("Fibonacci(%s)=%s %n", 200, Fibonacci.get(200));

        ConcurrentHashMap<String, Integer> chm = new ConcurrentHashMap<String, Integer>();
        chm.put("word1", 1);
        chm.put("word2", null);
        chm.put("word3", 3);
        String word = "word2";
        //atomic operation. other threads may be blocked while computation is in progress, depending what bucket other threads are running
        chm.compute(word, (k,v)-> v == null ? 1 : v + 1);
    }

    public static String complexMessageLogic() {
        return "doing complex message logic here ...=";
    }

    //Function has one abstract method: R apply(T t);
    public static List listAllAges(List persons, Function<Person, Integer> f) {
        List result = new ArrayList();
        //expects Consumer with one abstract method accept(T) with no return
        //no need to declare p as Person but do a type cast in apply
        persons.forEach(p -> result.add(f.apply((Person)p)));
        return result;
    }

    public static List listAllAges(List<Person> persons) {
        return persons.stream().map(x -> x.getAge()).collect(Collectors.toList());
    }

    /**
     * Exercise 4
     *
     * Convert every key-value pair of the map into a string and append them all
     * into a single string, in iteration order.
     */
    private static void exercise4() {
        Map<String, Integer> map = new TreeMap<>();
        map.put("c", 3);
        map.put("b", 2);
        map.put("a", 1);

        /* YOUR CODE HERE */
        StringBuilder sb = new StringBuilder();
        /*map.keySet().forEach(s -> {
            sb.append(s + "=" + map.get(s) + " ");
        });*/
        //this is better, using the Iteration Order
        map.forEach((k, v) -> sb.append(String.format("%s=%s ", k, v)));

        //why this is logged twice?
        //logger.info(sb.toString());
        System.out.println(sb);
    }

    /**
     * Exercise 3
     *
     * Join the second, third and forth strings of the list into a single string,
     * where each word is separated by a hyphen (-). Print the resulting string.
     */
    private static void exercise3() {
        List<String> list = Arrays.asList(
                "The", "quick", "brown", "fox", "jumped", "over", "the", "lazy", "dog");

    /* YOUR CODE HERE */
        System.out.println("Joining [2-4]=" + list.stream().skip(1).limit(3).collect(Collectors.joining("-")));
    }

    /**
     * Count the number of lines in the file using the BufferedReader provided
     */
    private static void lesson2Eexercise4() {
        try (BufferedReader reader = Files.newBufferedReader(
                Paths.get("D:\\allbooks\\IT\\Oracle\\Oracle Massive Open Online Course Java SE 8 Lambdas and Streams\\lesson2\\Sonnetl.txt"),
                StandardCharsets.UTF_8)) {
      /* YOUR CODE HERE */
            System.out.println("total # of lines=" +reader.lines().count());
        }
        catch (IOException exp) {
            exp.printStackTrace();
        }
    }

    /**
     * Using the BufferedReader to access the file, create a list of words with
     * no duplicates contained in the file.  Print the words.
     *
     * HINT: A regular expression, WORD_REGEXP, is already defined for your use.
     */
    private static final String WORD_REGEXP = "[- .:,]+";

    private static void lesson2Exercise5() {
        try (BufferedReader reader = Files.newBufferedReader(
                Paths.get("D:\\allbooks\\IT\\Oracle\\Oracle Massive Open Online Course Java SE 8 Lambdas and Streams\\lesson2\\Sonnetl.txt"),
                StandardCharsets.UTF_8)) {
      /* YOUR CODE HERE */
            reader.lines()
                    .flatMap(s->Stream.of(s.split(WORD_REGEXP)))
                    .distinct()
                    //sorted by the length instead of natural order
                    //.sorted((x, y)->Integer.compare(x.length(), y.length()))
                    //or
                    //.sorted((a, b) -> a.length() - b.length())
                    .collect(Collectors.toList())
                    .forEach(System.out::println);
        }
        catch (IOException exp) {
            exp.printStackTrace();
        }
    }

    private static String findLongestString(String s, int index, List<String> l) {
        //first test the exit condition
        if (l == null || index >= l.size()) {
            return s;
        }

        //if looping to the last element
        if (index == l.size() - 1) {
            if (l.get(index) == null || s.length() > l.get(index).length()) {
                return s;
            }
            else {
                return l.get(index);
            }
        }

        String s2 = findLongestString(l.get(index), index+1, l);
        if (s2 == null || (s != null && s.length() > s2.length())) {
            return s;
        }
        else {
            return s2;
        }
    }

    private static void printNames(String name) {
        System.out.println(name);
    }

    static void printNames(Supplier arg) {
        System.out.println(arg.get());
    }

    public static boolean isPrime(final int number) {
        return number > 1 &&
                IntStream.range(2, number)
                            .noneMatch(index -> number % index == 0);
    }
}
