package com.jj.concurrent;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.Executor;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Created by yejaz on 1/1/2017.
 * count can't bre reset. use CyclicBarrier instead of CountDownLatch if need to reset the count
 * count down can't be repeated. if need repeat, use CyclicBarrier instead
 */
public class CountDownLatchTest {
    public static void main(String[] args) throws InterruptedException {
        //initializeDriverAndWaitAll();
        divideNPartsAndWaitForDone();
    }

    public static void initializeDriverAndWaitAll() throws InterruptedException  {
        int N = 5;
        //The first is a start signal that prevents any worker from proceeding until the driver is ready for them to proceed;
        //The second is a completion signal that allows the driver to wait until all workers have completed.
        CountDownLatch startSignal = new CountDownLatch(1);
        CountDownLatch doneSignal = new CountDownLatch(N);

        for (int i = 0; i < N; ++i) // create and start threads
            new Thread(new Worker(startSignal, doneSignal)).start();

        //doSomethingElse();            // don't let run yet
        System.out.println(Thread.currentThread().getName() + " initializing drivers before the threads can start ...");
        Thread.sleep(5000);
        System.out.println(Thread.currentThread().getName() + " drivers are initialized");
        //not sure if this would be useful as we would have done this before creating threads
        startSignal.countDown();      // let all threads proceed
        //doSomethingElse();
        System.out.println(Thread.currentThread().getName() + " doing other stuffs ...");
        Thread.sleep(2000);
        //this is more useful as we don't have to call Thread.join() on each thread
        doneSignal.await();           // wait for all to finish
    }

    public static void divideNPartsAndWaitForDone() throws InterruptedException  {
        int N = 5;
        CountDownLatch doneSignal = new CountDownLatch(N);
        ExecutorService e = Executors.newFixedThreadPool(3);

        try {
            for (int i = 1; i <= N; ++i) // create and start threads
                e.execute(new WorkerRunnable(doneSignal, i));

            System.out.println(Thread.currentThread().getName() + " waiting ...");
            doneSignal.await();           // wait for all to finish
            System.out.println(Thread.currentThread().getName() + " all works are done");
        }
        finally {
            e.shutdown();
        }
    }
}

class Worker implements Runnable {
    private final CountDownLatch startSignal;
    private final CountDownLatch doneSignal;
    Worker(CountDownLatch startSignal, CountDownLatch doneSignal) {
        this.startSignal = startSignal;
        this.doneSignal = doneSignal;
    }
    public void run() {
        try {
            startSignal.await();
            doWork();
            doneSignal.countDown();
        } catch (InterruptedException ex) {
            Thread.currentThread().interrupt();
        } // return;
    }

    void doWork()
        throws InterruptedException  {
        System.out.println(Thread.currentThread().getName() + " doing works ...");
        Thread.sleep(4000);
        System.out.println(Thread.currentThread().getName() + " works are done.");
    }
}

class WorkerRunnable implements Runnable {
    private final CountDownLatch doneSignal;
    private final int iWork;
    WorkerRunnable(CountDownLatch doneSignal, int iWork) {
        this.doneSignal = doneSignal;
        this.iWork = iWork;
    }
    public void run() {
        try {
            doWork();
            doneSignal.countDown();
        } catch (InterruptedException ex) {
            Thread.currentThread().interrupt();
        } // return;
    }

    void doWork() throws InterruptedException {
        System.out.println(Thread.currentThread().getName() + " doing work part " + iWork);
        Thread.sleep(4000);
        System.out.println(Thread.currentThread().getName() + " work part " + iWork + " is done.");
    }
}