package com.ensolvers.fox.services.util;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicBoolean;

public class KeyBasedExecutor {
  private final ExecutorService threadPoolExecutor = Executors.newCachedThreadPool();
  private final ConcurrentHashMap<String, Object> threadLock = new ConcurrentHashMap<>();
  private final ConcurrentHashMap<String, AtomicBoolean> mutualExclusionLocks = new ConcurrentHashMap<>();

  /**
   * Ensures that only one instance of a thread is running given a specific key, doing nothing if there is one in
   * execution currently
   * 
   * @param key      the key to filter the thread
   * @param runnable the action to execute
   */
  public void runThreadBasedOnKey(String key, Runnable runnable) {
    if (threadLock.putIfAbsent(key, new Object()) == null) {
      threadPoolExecutor.submit(() -> {
        try {
          runnable.run();
        } finally {
          threadLock.remove(key);
        }
      });
    }
  }

  /**
   * Ensures that only one thread is running the runnable provided given a specific key. The rest of the threads will wait
   * until the thread that acquired the lock finish, doing nothing (his runnable will be not executed) So, if multiple
   * threads call this method with the same key at the same time, only one thread will be able to execute his runnable.
   * The rest of the threads will wait to return until the runnable that acquired the lock finish his execution. This
   * method has no relation with {@link KeyBasedExecutor#runThreadBasedOnKey(String, Runnable)}
   * 
   * @param key      the key to filter the runnable
   * @param runnable the action to execute
   */
  public <E extends Exception> void runJustOnceBasedOnKey(String key, FailableRunnable<E> runnable) throws E {
    AtomicBoolean lock = this.mutualExclusionLocks.computeIfAbsent(key, k -> new AtomicBoolean(false));
    synchronized (lock) {
      // If runnable is not executed
      if (!lock.get()) {
        // We check that the key not exists
        runnable.run();
      }
      lock.compareAndSet(false, true);
      // Release lock for key
      this.mutualExclusionLocks.remove(key);
    }
  }
}
