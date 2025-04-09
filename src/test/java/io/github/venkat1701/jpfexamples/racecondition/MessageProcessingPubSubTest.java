package io.github.venkat1701.jpfexamples.racecondition;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

import java.util.LinkedList;
import java.util.Queue;

/**
 * Test class for MessageProcessingPubSub.
 *
 * Note: These tests will not detect the race condition issues that JPF finds.
 * They are meant to verify the basic functionality of the producer-consumer pattern.
 *
 * @author Krish Jaiswal
 * @version 1.0
 */
public class MessageProcessingPubSubTest {

    @Test
    public void testBasicProducerConsumerOperation() throws InterruptedException {
        Queue<String> testQueue = new LinkedList<>();

        // Create producer and consumer with a small workload
        MessageProcessingPubSub.Producer producer = new MessageProcessingPubSub.Producer(testQueue) {
            @Override
            public void run() {
                // Override to produce only 3 messages
                for (int i = 0; i < 3; i++) {
                    synchronized (queue) {
                        String message = "Test Message " + i;
                        queue.add(message);
                        System.out.println("Test Producer: Added " + message);
                    }

                    try {
                        Thread.sleep(50);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
            }
        };

        // Track processed messages
        final int[] processedCount = {0};

        MessageProcessingPubSub.Consumer consumer = new MessageProcessingPubSub.Consumer(testQueue) {
            @Override
            public void run() {
                // Process only until interrupted
                while (!Thread.currentThread().isInterrupted()) {
                    synchronized (queue) {
                        if (!queue.isEmpty()) {
                            String message = queue.poll();
                            System.out.println("Test Consumer: Processed " + message);
                            processedCount[0]++;
                        }
                    }

                    try {
                        Thread.sleep(30);
                    } catch (InterruptedException e) {
                        Thread.currentThread().interrupt();
                    }
                }
            }
        };

        // Start both threads