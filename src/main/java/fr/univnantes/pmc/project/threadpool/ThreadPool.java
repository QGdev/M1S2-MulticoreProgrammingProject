package fr.univnantes.pmc.project.threadpool;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.Future;
import java.util.concurrent.FutureTask;

/**
 * My implementation of a working threadPool that will handle Future to make
 * integration easier with existing code.
 * <p>
 * This is based on Jakob Jenkov implementation and modified to fit my needs
 * <p>
 * This implementation is very light with the implementation of the methods that
 * will be used by our program
 *
 * @author Quentin GOMES DOS REIS
 * @author Matthéo LÉCRIVAIN
 */
public class ThreadPool {

    private ConcurrentLinkedQueue<FutureTask> taskQueue = null;
    private final List<ThreadRunnable> runnables = new ArrayList<>();
    private final List<Thread> threads = new ArrayList<>();
    private boolean isStopped = false;

    /**
     * Create a new ThreadPool
     *
     * @param noOfThreads the number of threads that will be created
     */
    public ThreadPool(int noOfThreads) {

        taskQueue = new ConcurrentLinkedQueue<FutureTask>();

        // Launch all threads
        for (int i = 0; i < noOfThreads; i++) {
            ThreadRunnable threadRunnable = new ThreadRunnable(taskQueue);
            runnables.add(threadRunnable);
            threads.add(new Thread(threadRunnable));
        }

        // Start everyone
        for (Thread thread : threads) {
            thread.start();
        }
    }

    /**
     * This method will return true if the threadPool is stopped
     *
     * @return true if the threadPool is stopped
     */
    public synchronized <T> Future<T> submit(Callable<T> task) {
        FutureTask<T> createdTask = new FutureTask<T>(task);
        taskQueue.add(createdTask);
        return createdTask;
    }

    /**
     * This method will stop all threads in the pool
     */
    public synchronized void stop() {
        this.isStopped = true;
        for (ThreadRunnable runnable : runnables) {
            runnable.stop();
        }
    }

    /**
     * This method will wait until all tasks are finished
     */
    public synchronized void waitUntilAllTasksFinished() {
        while (this.taskQueue.size() > 0) {
            try {
                Thread.sleep(1);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

}