package io.github.venkat1701.jpfexamples.racecondition;

import java.util.LinkedList;
import java.util.Queue;

/**
 * Message Processing PubSub example demonstrating race conditions in a producer-consumer model.
 *
 * This class simulates a publish-subscribe system where a producer thread adds messages
 * to a queue and a consumer thread processes them. A shared flag is used to control
 * synchronization, but non-atomic operations on this flag can lead to race conditions.
 *
 * @author Krish Jaiswal
 * @version 1.0
 */
public class MessageProcessingPubSub {
    // Shared message queue
    private static Queue<String> messageQueue = new LinkedList<>();

    // Shared flag to control whether a message is being processed
    private static volatile boolean isProcessing = false;

    /**
     * Producer class that adds messages to the queue.
     */
    static class Producer extends Thread {
        private final Queue<String> queue;

        public Producer(Queue<String> queue) {
            this.queue = queue;
        }

        @Override
        public void run() {
            for (int i = 0; i < 10; i++) {
                try {
                    // Add a small delay to make the race condition more likely
                    Thread.sleep(100);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }

                synchronized (queue) {
                    // Check if no message is being processed
                    if (!isProcessing) {
                        // Add a new message to the queue
                        String message = "New Message " + i;
                        queue.add(message);
                        System.out.println("Producer: Added " + message);

                        // Set the processing flag to true
                        isProcessing = true;
                    } else {
                        System.out.println("Producer: Waiting for consumer to process previous message");
                        i--; // Retry this message
                    }
                }
            }
        }
    }

    /**
     * Consumer class that processes messages from the queue.
     */
    static class Consumer extends Thread {
        private final Queue<String> queue;

        public Consumer(Queue<String> queue) {
            this.queue = queue;
        }

        @Override
        public void run() {
            while (true) {
                try {
                    // Add a small delay to make the race condition more likely
                    Thread.sleep(200);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }

                synchronized (queue) {
                    // Check if there are messages to process
                    if (!queue.isEmpty()) {
                        // Get the next message
                        String message = queue.poll();
                        System.out.println("Consumer: Processing " + message);

                        // Simulate message processing
                        try {
                            Thread.sleep(100);
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }

                        System.out.println("Consumer: Finished processing " + message);

                        // Mark processing as complete
                        isProcessing = false;
                    }
                }

                // Check if all messages have been processed
                if (Thread.currentThread().isInterrupted()) {
                    break;
                }
            }
        }
    }

    /**
     * Main method to demonstrate the race condition scenario.
     */
    public static void main(String[] args) {
        // Create producer and consumer threads
        Producer producer = new Producer(messageQueue);
        Consumer consumer = new Consumer(messageQueue);

        // Start both threads
        producer.start();
        consumer.start();

        // Wait for producer to finish
        try {
            producer.join();
            System.out.println("Producer has finished");

            // Give consumer some time to process remaining messages
            Thread.sleep(1000);

            // Interrupt consumer
            consumer.interrupt();
            consumer.join();

            System.out.println("Consumer has finished");
            System.out.println("Final queue size: " + messageQueue.size());
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }
}