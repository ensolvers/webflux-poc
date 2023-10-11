# Object Change Audit

Is some apps it is important to detect and store changes made to objects. Ensolvers Core makes this easy just by 
introducing a couple of changes into the code. Required steps are:

- **Step 1: Annotate properties** to be scanned for changes with `@AuditableProperty` annotation
- **Step 2: Add `@EntityListeners(ChangelogEntityListener.class)`** annotation to the Entity to be observed

That's it!
