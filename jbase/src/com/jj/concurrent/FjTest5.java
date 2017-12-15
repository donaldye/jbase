package com.jj.concurrent;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Consumer;

import com.jj.algo.Factorial;
import org.apache.commons.math3.util.*;

/**
 * CountedCompleter remembers the pending task count (just count, nothing else) and can notify the tasks implementation onCompletion method.

 This pending count is increased on each call of CountedCompleter#addToPendingCount() method by client code. We have to use this method for each new task forking.

 The method CountedCompleter#tryComplete() should be called within compute method only once when returning. This decreases the pending count.

 CountedCompleter can optionally return a computed value. We have to override method getRawResult() to return value.

 CountedCompleter is better than RecursiveTask/RecursiveAction. RecursiveAction does not return

 */

/**
 * Created by yejaz on 12/31/2016.
 * http://javaforu.blogspot.com/2013/06/forkjoin-quick-exploration-long-overdue.html
 * In my examples, I use ForkJoin to recursively split and list numbers from "start" to "end".
 * At each step if the start to end range is larger than 5 it splits that range into 2 equal halves and forks them off as sub-tasks.
 * Otherwise that task is the leaf level and just adds the numbers in a for-loop from start to end into a queue that is passed around to all tasks.
 */
public class FjTest5 {
    public static void main(String[] args) throws ExecutionException, InterruptedException {
        //Wrap FJ as an ECS.
        /*ExecutorCompletionService<ConcurrentLinkedQueue<String>> ecs =
                new ExecutorCompletionService<>(ForkJoinPool.commonPool());

        //Submit 5 tasks.
        for (int i = 0; i < 5; i++) {
            ConcurrentLinkedQueue<String> resultCollector = new ConcurrentLinkedQueue<>();
            //ECS only takes Runnable or Callable. So, create a Runnable as a Lambda expression.
            //Let the Runnable in turn call invoke() when it gets scheduled in the FJ pool.
            ecs.submit(() -> makeNew(resultCollector, 23, 145).invoke(), resultCollector);
        }

        //Wait for them to complete and pick up results one by one.
        for (int i = 0; i < 5; i++) {
            ConcurrentLinkedQueue<String> resultCollector = ecs.take().get();

            System.out.println(" Got result [" + (i + 1) + "] with [" + resultCollector.size() + " ] items");
        }*/


        //the first task splits itself is sub-tasks using a while loop. A value is also returned from the task.
        List<BigInteger> list = new ArrayList<>();
        for (int i = 3; i < 20; i++) {
            list.add(new BigInteger(Integer.toString(i)));
        }

        BigInteger sum = ForkJoinPool.commonPool().
                invoke(new FactorialTask(null,
                        new AtomicReference<>(new BigInteger("0")),
                        list));
        System.out.println("Sum of the factorials = " + sum);

        //this is better
        ForkJoinPool.commonPool().invoke(
                new FactorialTask2(null, list));
    }

    private static RecursiveNumLister5 makeNew(ConcurrentLinkedQueue<String> resultCollector, int startInc, int endEx) {
        return new RecursiveNumLister5Root(resultCollector, startInc, endEx,
                (collector) -> {
                    ArrayList<String> list = new ArrayList<>(collector);
                    Collections.sort(list);

                    System.out.printf("Listed %d items%n", list.size());
                    for (String s : list) {
                        System.out.println("  " + s);
                    }
                });
    }

    static class RecursiveNumLister5 extends CountedCompleter<Void> {
        final ConcurrentLinkedQueue<String> collector;

        final int start;

        final int end;

        RecursiveNumLister5(ConcurrentLinkedQueue<String> collector, int startInc, int endEx,
                            RecursiveNumLister5 parent) {
            //Completions will bubble up fom sub-tasks because of this link from parent to child.
            super(parent);

            this.collector = collector;
            this.start = startInc;
            this.end = endEx;
        }

        @Override
        public void compute() {
            if (end - start < 5) {
                String s = Thread.currentThread().getName();

                for (int i = start; i < end; i++) {
                    collector.add(String.format("%5d_%s", i, s));
                }

                //Signal that this is now complete. The completions will bubble up automatically.
                tryComplete();
            }
            else {
                int m = (end + start) / 2;

                RecursiveNumLister5 left = new RecursiveNumLister5(collector, start, m, this);
                RecursiveNumLister5 right = new RecursiveNumLister5(collector, m, end, this);

                //Only the left sub-task is forked, so set the pending count to 1.
                setPendingCount(1);
                left.fork();
                //Right sub-task is executed synchronously.
                right.compute();
            }

            //propagateCompletion(); <-- Not here. This would prematurely complete this task while children are still running.
        }
    }

    static class RecursiveNumLister5Root extends RecursiveNumLister5 {
        final Consumer<ConcurrentLinkedQueue<String>> completionListener;

        RecursiveNumLister5Root(ConcurrentLinkedQueue<String> collector, int startInc, int endEx,
                                Consumer<ConcurrentLinkedQueue<String>> completionListener) {
            super(collector, startInc, endEx, null);

            this.completionListener = completionListener;
        }

        @Override
        public void onCompletion(CountedCompleter<?> caller) {
            completionListener.accept(collector);
        }
    }


    private static class FactorialTask extends CountedCompleter<BigInteger> {
        private static int SEQUENTIAL_THRESHOLD = 5;
        private List<BigInteger> integerList;
        private AtomicReference<BigInteger> result;

        private FactorialTask (CountedCompleter<BigInteger> parent,
                               AtomicReference<BigInteger> result,
                               List<BigInteger> integerList) {
            super(parent);
            this.integerList = integerList;
            this.result = result;
        }

        @Override
        public BigInteger getRawResult () {
            return result.get();
        }

        @Override
        public void compute () {

            //this example creates all sub-tasks in this while loop
            while (integerList.size() > SEQUENTIAL_THRESHOLD) {

                //end of the list containing SEQUENTIAL_THRESHOLD items.
                List<BigInteger> newTaskList = integerList.subList(integerList.size() -
                        SEQUENTIAL_THRESHOLD, integerList.size());

                //remaining list
                integerList = integerList.subList(0, integerList.size() -
                        SEQUENTIAL_THRESHOLD);

                addToPendingCount(1);
                FactorialTask task = new FactorialTask(this, result, newTaskList);
                task.fork();
            }
            //find sum of factorials of the remaining this.integerList
            sumFactorials();
            propagateCompletion();
        }


        private void addFactorialToResult (BigInteger factorial) {
            result.getAndAccumulate(factorial, (b1, b2) -> b1.add(b2));
        }

        private void sumFactorials () {
            for (BigInteger i : integerList) {
                addFactorialToResult(BigInteger.valueOf(Factorial.compute(i.intValue())));
            }
        }
    }


    private static class FactorialTask2 extends CountedCompleter<Void> {

        private static int SEQUENTIAL_THRESHOLD = 5;
        private List<BigInteger> integerList;
        private int numberCalculated;

        private FactorialTask2 (CountedCompleter<Void> parent,
                               List<BigInteger> integerList) {
            super(parent);
            this.integerList = integerList;
        }


        @Override
        public void compute () {
            if (integerList.size() <= SEQUENTIAL_THRESHOLD) {
                showFactorial();
            } else {
                int middle = integerList.size() / 2;
                List<BigInteger> rightList = integerList.subList(middle,
                        integerList.size());
                List<BigInteger> leftList = integerList.subList(0, middle);
                addToPendingCount(2);
                FactorialTask2 taskRight = new FactorialTask2(this, rightList);
                FactorialTask2 taskLeft = new FactorialTask2(this, leftList);
                taskLeft.fork();
                taskRight.fork();
            }
            tryComplete();
        }

        @Override
        public void onCompletion (CountedCompleter<?> caller) {
            if (caller == this) {
                System.out.printf("completed thread : %s numberCalculated=%s%n", Thread
                        .currentThread().getName(), numberCalculated);
            }
        }

        private void showFactorial () {

            for (BigInteger i : integerList) {
                BigInteger factorial = BigInteger.valueOf(Factorial.compute(i.intValue()));//CalcUtil.calculateFactorial(i);
                System.out.printf("%s! = %s, thread = %s%n", i, factorial, Thread
                        .currentThread().getName());
                numberCalculated++;
            }
        }
    }
}