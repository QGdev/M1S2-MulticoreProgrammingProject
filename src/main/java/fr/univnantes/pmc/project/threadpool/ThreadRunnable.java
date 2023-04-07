package fr.univnantes.pmc.project.threadpool;

import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.FutureTask;

/**
 * This is the hearth of our custom ThreadPool this will make each threads able
 * to process the list of FutureTask in total autonomy by fetching one from the
 * provided queue
 *
 * @author Quentin GOMES DOS REIS
 * @author Matthéo LÉCRIVAIN
 */
public class ThreadRunnable implements Runnable {

    private Thread thread = null;
    private ConcurrentLinkedQueue<FutureTask> taskQueue = null;
    private boolean isStopped = false;

    /**
     * Create a new ThreadRunnable
     *
     * @param queue the queue that will be used to fetch the tasks
     */
    public ThreadRunnable(ConcurrentLinkedQueue<FutureTask> queue) {
        taskQueue = queue;
    }

    /**
     * This method will be called by the thread to process the tasks
     */
    public void run() {
        this.thread = Thread.currentThread();
        while (!isStopped()) {
            try {
                // Take a task from queue and runs it
                Runnable task = taskQueue.poll();
                if (task != null)
                    task.run();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * This method will stop the thread
     */
    public synchronized void stop() {
        isStopped = true;
        thread.interrupt();
    }

    /**
     * This method will return true if the thread is stopped
     *
     * @return true if the thread is stopped
     */
    public synchronized boolean isStopped() {
        return isStopped;
    }
}