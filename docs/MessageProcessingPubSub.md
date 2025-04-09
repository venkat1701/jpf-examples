# Message Processing PubSub Example

## Overview

The Message Processing PubSub example demonstrates a race condition in a producer-consumer pattern. It simulates a publish-subscribe system where a producer thread adds messages to a queue and a consumer thread processes them. A shared flag is used to control synchronization, but the non-atomic operations on this flag can lead to race conditions.

## Problem Description

The system consists of two main components:

1. **Producer**: Continuously adds messages to a shared message queue
2. **Consumer**: Continuously takes messages from the queue and processes them

These operations are performed on the shared resources:
- `messageQueue`: A queue holding messages
- `isProcessing`: A boolean flag indicating if a message is currently being processed

The race condition occurs because operations on the `isProcessing` flag are not properly synchronized:

### Race Condition Scenario

1. **Producer**:
    - Checks if `isProcessing` is `false` before adding a message
    - Sets `isProcessing` to `true` after adding a message

2. **Consumer**:
    - Processes a message from the queue
    - Sets `isProcessing` to `false` after processing

The race condition can manifest in several ways:

1. **Multiple Messages Being Processed**: The producer might add a new message before the consumer has finished processing the previous one
2. **Lost Updates**: The consumer might reset `isProcessing` to `false` right before the producer sets it to `true`, causing both operations to think they have exclusive access
3. **Queue Overflow**: The producer might add messages faster than the consumer can process them, leading to an overflow

## Code Explanation

```java
// Producer thread run method
synchronized (queue) {
    // Check if no message is being processed
    if (!isProcessing) {
        // Add a new message to the queue
        String message = "New Message " + i;
        queue.add(message);
        System.out.println("Producer: Added " + message);
        
        // Set the processing flag to true
        isProcessing = true;
    }
}

// Consumer thread run method
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
```

Note that while the queue operations are synchronized, the critical check-then-act on the `isProcessing` flag is not atomic.

## Running with JPF

To analyze this code with Java PathFinder:

```bash
java -jar $JPF_HOME/build/RunJPF.jar src/main/resources/jpf/MessageProcessingPubSub.jpf
```

## JPF Output Analysis

When JPF analyzes this code, it will detect the potential race condition by exploring different thread interleavings. The output will show:

1. The sequence of events leading to the race condition
2. The shared field (`isProcessing`) being accessed by multiple threads
3. The thread states at the point of the race condition
4. The specific code locations where the conflicting accesses occur

## Solutions

There are several ways to fix this race condition:

1. **Use Atomic Operations**: Replace the boolean flag with an `AtomicBoolean` to ensure atomic check-then-act operations

```java
private static AtomicBoolean isProcessing = new AtomicBoolean(false);

// In producer
if (isProcessing.compareAndSet(false, true)) {
    // Add message
}

// In consumer
// Process message
isProcessing.set(false);
```

2. **Use Condition Variables**: Use condition variables with a lock to signal between producer and consumer

```java
private final Lock lock = new ReentrantLock();
private final Condition notProcessing = lock.newCondition();

// In producer
lock.lock();
try {
    while (isProcessing) {
        notProcessing.await();
    }
    isProcessing = true;
    // Add message
} finally {
    lock.unlock();
}

// In consumer
lock.lock();
try {
    // Process message
    isProcessing = false;
    notProcessing.signal();
} finally {
    lock.unlock();
}
```

3. **Use BlockingQueue**: Replace the entire implementation with a `BlockingQueue` which handles synchronization internally

```java
private static BlockingQueue<String> messageQueue = new LinkedBlockingQueue<>();

// In producer
messageQueue.put("New Message");

// In consumer
String message = messageQueue.take();
// Process message
```
