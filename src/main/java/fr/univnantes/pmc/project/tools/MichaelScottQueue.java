package fr.univnantes.pmc.project.tools;

import java.util.concurrent.atomic.AtomicReference;

public class MichaelScottQueue<T> {

    private final AtomicReference<Node> head;
    private final AtomicReference<Node> tail;

    public MichaelScottQueue() {
        Node start = new Node(null);
        head = new AtomicReference<Node>(start);
        tail = new AtomicReference<Node>(start);
    }

    public void enqueue(T value) {
        Node node = new Node(value);
        while (true) {
            Node last = tail.get();
            Node next = last.next.get();
            if (next == null) {
                if (last.next.compareAndSet(next, node)) {
                    tail.compareAndSet(last, node);
                    return;
                }
            } else {
                tail.compareAndSet(last, next);
            }
        }
    }

    public T dequeue() {
        while (true) {
            Node first = head.get();
            Node last = tail.get();
            Node next = first.next.get();
            if (first == last) {
                if (next == null) {
                    return null;
                }
                tail.compareAndSet(last, next);
            } else {
                T value = next.value;
                if (head.compareAndSet(first, next))
                    return value;
            }
        }
    }

    private class Node {
        public T value;
        public AtomicReference<Node> next = new AtomicReference<Node>(null);

        public Node(T val) {
            value = val;
        }
    }
}
