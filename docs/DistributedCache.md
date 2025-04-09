# DistributedCache Example

## Overview

The DistributedCache example demonstrates a classic deadlock scenario that can occur in distributed cache systems. Two threads perform different operations (invalidation and reading) on a shared cache resource, each acquiring locks in different orders, which can lead to deadlock.


## Problem Description

The DistributedCache class initializes two `ReentrantReadWriteLock` instances (`lock1` and `lock2`) to control concurrent access to a shared cache resource. The class provides three main operations:

1. `invalidate(String key)`: Removes an entry from the cache
    - First acquires `lock1` (write lock)
    - Then attempts to acquire `lock2` (write lock)

2. `read(String key)`: Retrieves a value from the cache
    - First acquires `lock2` (read lock)
    - Then attempts to acquire `lock1` (read lock)

3. `put(String key, Object value)`: Adds an entry to the cache
    - Uses a consistent lock ordering (always `lock1` then `lock2`)

The deadlock occurs due to inconsistent lock ordering between the `invalidate()` and `read()` operations:

### Deadlock Scenario

1. **Thread #1** (invalidation):
    - Acquires `lock1`
    - Attempts to acquire `lock2`, but must wait if `lock2` is held by another thread

2. **Thread #2** (reading):
    - Acquires `lock2`
    - Attempts to acquire `lock1`, but must wait if `lock1` is held by another thread

3. **Result**: Both threads are stuck waiting indefinitely, as neither can proceed without the lock held by the other thread.

## Code Explanation

```java
// In the invalidate() method - acquires lock1 first, then lock2
lock1.writeLock().lock();
try {
    // ...processing...
    lock2.writeLock().lock();
    try {
        // ...more processing...
    } finally {
        lock2.writeLock().unlock();
    }
} finally {
    lock1.writeLock().unlock();
}

// In the read() method - acquires lock2 first, then lock1
lock2.readLock().lock();
try {
    // ...processing...
    lock1.readLock().lock();
    try {
        // ...more processing...
    } finally {
        lock1.readLock().unlock();
    }
} finally {
    lock2.readLock().unlock();
}
```

## Running with JPF

To analyze this code with Java PathFinder:

```bash
java -jar $JPF_HOME/build/RunJPF.jar src/main/resources/jpf/DistributedCache.jpf
```

## JPF Output Analysis

When JPF analyzes this code, it will detect the potential deadlock by exploring different thread interleavings. The output will show:

1. The sequence of events leading to the deadlock
2. The thread states at the point of deadlock
3. Which locks are held by which threads
4. The cycle in the resource dependency graph

## Solution

There are several ways to fix this deadlock issue:

1. **Consistent Lock Ordering**: Ensure all methods acquire locks in the same order (as demonstrated in the `put()` method)
2. **Lock Timeout**: Use timed lock acquisition to detect and recover from potential deadlocks
3. **Lock Hierarchy**: Design a clear lock hierarchy and only allow locking from higher to lower levels

The simplest fix is to ensure both `invalidate()` and `read()` methods acquire locks in the same order:

```java
// Fix for read() method - acquire locks in the same order as invalidate()
public Object read(String key) {
    lock1.readLock().lock();  // First lock1, matching the order in invalidate()
    try {
        lock2.readLock().lock();
        try {
            return cache.get(key);
        } finally {
            lock2.readLock().unlock();
        }
    } finally {
        lock1.readLock().unlock();
    }
}
```
