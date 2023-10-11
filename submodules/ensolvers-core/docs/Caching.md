# Introduction

Caching is an essential part of any app for improving performance and responsiveness. This section summarizes different
ways in which Core supports caching

## Programmatic way

Let's say that we discover that a method `ComputingService#costlyOperation(int)`, due to crescent database size, is
causing performance issues (high DB or CPU usage) thus slowing down an entire system. How we can solve this 
problem in a couple lines of code? Simple. Let's assume that this is the original code

```java

  T someMethod(int param) {
    return this.computingService.costlyOperation(param)  
  }

```

We just have to (1) declare in the class a cache with a loader for that method and (2) invoke the cache instead
of the method itself. We can use the 
[`Cache`](../modules/fox-java/modules/fox-cache/src/main/java/com/ensolvers/fox/cache/Caches.java) class.

```java

  SimpleLoadingCache<T> computingServiceCache = Caches.newGuavaLoadingCache(60, "computingServiceCache", 
          (key) -> this.computingService.costlyOperation(Integer.parseInt(key)));


  someMethod(int param) {
    this.computingService.costlyOperation(String.valueOf(param));  
  }

```

Some caches like Redis ones might require specifying object datatypes to serialize them correctly. The
[`Cache`](../modules/fox-java/modules/fox-cache/src/main/java/com/ensolvers/fox/cache/Caches.java) class. already provides
utils methods for that, for instance if we need to serialize a list of objects in Redis

```java
SimpleLoadingCache<List<CostlyObject> computingServiceCache = Caches.newGuavaBackedRedisAsyncCache(10, 10,
        properties.getRedisUri(), "costlyOperationCache", Caches.listType(CostlyObject.class),
        name -> this.computingService.costlyOperation(Integer.parseInt(key)))
```

Check the docs for the [`Cache`](../modules/fox-java/modules/fox-cache/src/main/java/com/ensolvers/fox/cache/Caches.java) class
to know more about different alternatives and configurations.


## Cache configuration using spring

To implement caches using Spring, implement a class for the `CacheConfiguration` interface (this class must be annotated with `@Configuration`). 

The `CacheConfiguration` interface defines a method that expects a cache manager that spring uses to handle caches, 
you must implement this method defining the caches and adding it to the manager in the cache manager as follows:

```java
@Configuration
public class ConcreteCacheConfiguration implements CacheConfiguration {
    @Override
    public void addCaches(GenericCacheManager manager) {
        // Define a Guava cache with: 
        // - name: "user"
        // - expiration: 1 hour (in seconds)
        // - allows null values
        
        SpringGuavaCache userCache = new SpringGuavaCache("user", 3600, true);
        
        // Define a Redis cache with: 
        // - name: "song"
        // - expiration: 10 minutes (in seconds)
        // - does not allow null values
        
        // This should probably be auto-wired instead of instantiated here
        var redisClient = RedisClient.create("redis://localhost:3500/0").connect().sync();
        
        SpringRedisCache songCache = new SpringRedisCache("song", redisClient, 600, false);
        
        // Define a Memcache cache with: 
        // - name: "bike"
        // - expiration: 20 minutes (in seconds)
        // - does not allow null values
        
        // This should probably be auto-wired instead of instantiated here
        MemcachedClient memcacheClient;
        try {
            memcacheClient = new MemcachedClient(new InetSocketAddress(Integer.parseInt(<memcachedPort>)));
        } catch (IOException e) {
            throw new RuntimeException("Error trying to instantiate memcached bean", e);
        }

        SpringMemcachedCache bikeCache = new SpringMemcachedCache("bike", memcacheClient, 1200, false);
        
        // Add the caches to the cache manager by specifying the key to be used in the annotations
        cacheManager.append("user", userCache).append("song", songCache).append("bike", bikeCache);
    }
}
```

### How to use
#### Caching the result of a method

```java
// The result of the method will be stored in the "user" cache and the key will be the value of the "id" parameter
@Cacheable("user")
public UserDTO getById(Long id) {
    //Actions
}
```

### Invalidate a cache key

```java
// Invalidates a key in the "user" cache. The key will be the value of the "id" parameter
@CacheEvict(value = "user")
public void deleteUserById(Long id) {
    //Actions
}
```

### More uses
We recommend looking at `SampleComponent` and the tests specified in the **fox-cache** module at **java-fox** repository for have a complete view of all actions available to do with the spring cache annotations.
