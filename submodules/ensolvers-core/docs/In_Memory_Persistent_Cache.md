# In Memory Persistent Cache

The idea is to implement a system that let us use a cached version of a serializable entity, loaded directly in memory.
Unlike cache implementations as Redis, the objective is to host temporarily this cached entity on an external provider
(called Source) like a S3 Bucket. The objective is to automatize the logic that let us generate this entity, send it
towards the Source, and then consume it back to the cache.

An example implementation can be seen in the class [Example Persistent Cache](../modules/ensolvers-core-common/src/main/java/com/ensolvers/core(common/performance/inmemory/ExamplePersistentCache.java)


## Multi-node considerations

An application with more than one node will not share their memory for obvious reasons. That's why we have to consider
that the operation of Upload, since it's a WRITE operation, must be done only by a single node, that's why Quartz is
used for the example.

To Sync back the content, since it's a READ operation that must be done for all the nodes, this job doesn't require
to have a Quartz control to handle the request.