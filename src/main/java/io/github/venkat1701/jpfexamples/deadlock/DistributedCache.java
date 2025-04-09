package io.github.venkat1701.jpfexamples.deadlock;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.locks.ReentrantReadWriteLock;

/**
 * DistributedCache example demonstrating potential deadlocks in cache invalidation operations.
 *
 * This class simulates a distributed cache where multiple threads can read from and invalidate
 * entries. The lock ordering issue can lead to deadlock when two threads try to access the
 * cache concurrently, one for invalidation and another for reading.
 *
 * @author Krish Jaiswal
 * @version 1.0
 */
public class DistributedCache {
    // Shared cache resource
    private final Map<String, Object> cache = new HashMap<>();

    // Two locks for controlling access to the cache
    private final ReentrantReadWriteLock lock1 = new ReentrantReadWriteLock();
    private final ReentrantReadWriteLock lock2 = new ReentrantReadWriteLock();

    /**
     * Invalidates a cache entry.
     * First acquires lock1, then tries to acquire lock2.
     *
     * @param key The key to invalidate in the cache
     */
    public void invalidate(String key) {
        // First acquire lock1
        lock1.writeLock().lock();
        try {
            System.out.println(Thread.currentThread().getName() + " acquired lock1 for invalidation");

            // Simulate some processing time
            try { Thread.sleep(100); } catch (InterruptedException e) { /* ignore */ }

            // Then try to acquire lock2
            lock2.writeLock().lock();
            try {
                System.out.println(Thread.currentThread().getName() + " acquired lock2 for invalidation");

                // Remove the entry from the cache
                cache.remove(key);
                System.out.println("Invalidated key: " + key);
            } finally {
                lock2.writeLock().unlock();
            }
        } finally {
            lock1.writeLock().unlock();
        }
    }

    /**
     * Reads a value from the cache.
     * First acquires lock2, then tries to acquire lock1.
     * Notice the different lock ordering compared to invalidate().
     *
     * @param key The key to read from the cache
     * @return The value associated with the key, or null if not found
     */
    public Object read(String key) {
        // First acquire lock2 - note the different order from invalidate
        lock2.readLock().lock();
        try {
            System.out.println(Thread.currentThread().getName() + " acquired lock2 for reading");

            // Simulate some processing time
            try { Thread.sleep(100); } catch (InterruptedException e) { /* ignore */ }

            // Then try to acquire lock1
            lock1.readLock().lock();
            try {
                System.out.println(Thread.currentThread().getName() + " acquired lock1 for reading");

                // Read from the cache
                Object value = cache.get(key);
                System.out.println("Read value for key: " + key);
                return value;
            } finally {
                lock1.readLock().unlock();
            }
        } finally {
            lock2.readLock().unlock();
        }
    }

    /**
     * Puts a value in the cache.
     * Uses a safe lock ordering to avoid deadlocks.
     *
     * @param key The key to store
     * @param value The value to associate with the key
     */
    public void put(String key, Object value) {
        // Acquire both locks in a consistent order to avoid deadlocks
        lock1.writeLock().lock();
        try {
            lock2.writeLock().lock();
            try {
                cache.put(key, value);
                System.out.println("Added key: " + key);
            } finally {
                lock2.writeLock().unlock();
            }
        } finally {
            lock1.writeLock().unlock();
        }
    }

    /**
     * Main method to demonstrate the deadlock scenario.
     */
    public static void main(String[] args) {
        final DistributedCache cache = new DistributedCache();

        // Initialize the cache with a value
        cache.put("testKey", "testValue");

        // Create two threads that will attempt operations in a way that can cause deadlock
        Thread thread1 = new Thread(() -> {
            cache.invalidate("testKey");
        }, "InvalidationThread");

        Thread thread2 = new Thread(() -> {
            cache.read("testKey");
        }, "ReadThread");

        // Start both threads
        thread1.start();
        thread2.start();

        // Wait for threads to complete (they may deadlock and never complete)
        try {
            thread1.join();
            thread2.join();
            System.out.println("Both operations completed successfully (no deadlock)");
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }
}