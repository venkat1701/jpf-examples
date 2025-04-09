package io.github.venkat1701.jpfexamples.deadlock;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

/**
 * Test class for DistributedCache.
 *
 * Note: These tests will not detect the deadlock issues that JPF finds.
 * They are meant to verify the basic functionality of the cache operations.
 *
 * @author Krish Jaiswal
 * @version 1.0
 */
public class DistributedCacheTest {

    @Test
    public void testBasicCacheOperations() {
        DistributedCache cache = new DistributedCache();

        // Put a value in the cache
        cache.put("testKey", "testValue");

        // Read the value
        assertEquals("testValue", cache.read("testKey"));

        // Invalidate the value
        cache.invalidate("testKey");

        // Verify it's gone
        assertNull(cache.read("testKey"));
    }

    @Test
    public void testConcurrentAccess() throws InterruptedException {
        DistributedCache cache = new DistributedCache();

        // Put multiple values
        for (int i = 0; i < 5; i++) {
            cache.put("key" + i, "value" + i);
        }

        // Create reader thread
        Thread readerThread = new Thread(() -> {
            for (int i = 0; i < 5; i++) {
                cache.read("key" + i);
            }
        });

        // Create invalidator thread accessing different keys (to avoid deadlock)
        Thread invalidatorThread = new Thread(() -> {
            for (int i = 5; i < 10; i++) {
                cache.invalidate("key" + i);
            }
        });

        // Start both threads
        readerThread.start();
        invalidatorThread.start();

        // Wait for completion
        readerThread.join(2000);
        invalidatorThread.join(2000);

        // Verify threads completed
        assertFalse(readerThread.isAlive(), "Reader thread should have completed");
        assertFalse(invalidatorThread.isAlive(), "Invalidator thread should have completed");
    }
}