# Custom Java Migrations

To add a custom migration in a project, the class [`MigrationService`](../modules/ensolvers-core-common/src/main/java/com/ensolvers/core/common/services/MigrationService.java) 
should be injected into a component that runs when the application starts.
MigrationService has a runMigration method that runs as a transaction, and should be called like so:

```java
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;

/**
 * This component's method "runMigrations" should run after application is started
 */
@Component
public class MigrationsComponent {
  @Autowired
  MigrationService migrationService;

  @PostConstruct
  public void runMigrations() {
    // The migration key must be unique, and will only run once
    migrationService.runMigration("fake_migration",() -> {
      // Here is the code of the migration, if this fails, migration doesn't complete and a message is logged

      // try {
          // User user = someService.readUser("someUser");
          // user.setEmail("test@email.com");
          // someService.save(user);
      // } catch(Exception e) {
        // This will catch the try block exception that is rethrown by the migration service
        // Try-catch block can be removed, if user doesn't want application to start if migrations don't run
      // }
    });
  }
}
```
