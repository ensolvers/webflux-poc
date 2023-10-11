# Job configuration with Quartz

To implement quartz jobs, extend `SpringQrtzJobsConfiguration` abstract class (note that concrete class should be annotated with `@Configuration`) and define the scheduler factory bean for each job to be created.

For example if we need to create the job `SyncDataProcessorJob` that will run each minute starting at 00:00:00, the job `RemoveDataProcessorJob` that will run each minute starting at 14:10:00 and the job `AdminWeeklyNewsProcessorJob` that will run each Monday at 8:00:00:

### Job Classes

Note that jobs should implement `org.quartz.Job` class

```java
public class SyncDataProcessorJob implements Job {
    @Autowired
    private SomeAutowiredService someAutowiredService;
    
    @Override
    public void execute(JobExecutionContext jobExecutionContext) throws JobExecutionException {
        // Do something
    }
}

public class RemoveDataProcessorJob implements Job {
    @Autowired
    private SomeAutowiredService someAutowiredService;
    
    @Override
    public void execute(JobExecutionContext jobExecutionContext) throws JobExecutionException {
        // Do something
    }
}

public class AdminWeeklyNewsProcessorJob implements Job {
    @Autowired
    private SomeAutowiredService someAutowiredService;
    
    @Override
    public void execute(JobExecutionContext jobExecutionContext) throws JobExecutionException {
        // Do something
    }
}

```

### Configuration Class

Note that we are using mysql driver to configure the DataSource (method `getMysqlConfiguration`) but you can add more driver configurations in `CoreQrtzConfiguration` if you need

```java
@Configuration
public class SpringQrtzJobsConfiguration extends CoreQrtzConfiguration {
  private static final long ONE_MINUTE_IN_MILLIS = 1000L * 60L;

  @Value("${spring.datasource.url}")
  public String qrtzDatasourceUrl;

  @Value("${spring.datasource.username}")
  public String qrtzDatasourceUser;

  @Value("${spring.datasource.password}")
  public String qrtzDatasourcePassword;

  /* -------------------------- Configuration -------------------------- */
  @Override
  public Properties getDataSourceConfiguration() {
    return this.getMysqlConfiguration(qrtzDatasourceUrl, qrtzDatasourceUser, qrtzDatasourcePassword);
  }


  /* -------------------------- SyncDataProcessorJob -------------------------- */
  @Bean
  public SchedulerFactoryBean syncDataProcessorJobScheduler() {
    return buildSchedulerFactoryBean(SyncDataProcessorJob.class, ONE_MINUTE_IN_MILLIS);
  }
  
  /* -------------------------- RemoveDataProcessorJob -------------------------- */
  @Bean
  public SchedulerFactoryBean removeDataProcessorJobScheduler() {
    return buildSchedulerFactoryBean(SyncDataProcessorJob.class, ONE_MINUTE_IN_MILLIS, 14, 10);
  }
  
  /* -------------------------- AdminWeeklyNewsProcessorJob -------------------------- */
  @Bean
  public SchedulerFactoryBean adminWeeklyNewsProcessorJobScheduler() {
    return buildCronScheduledFactoryBean(AdminWeeklyNewsProcessorJob.class, "0 0 8 * * 1"); // CRON expression: “At 08:00:00 on Monday.”
  }
}
```

For more information about CRON expressions in Spring, see: [Cron Expressions](https://docs.spring.io/spring-framework/docs/current/reference/html/integration.html#scheduling-cron-expression)

## Using a PostgreSQL datasource

To use PostgreSQL as data source, set `org.postgresql.Driver` in the `org.quartz.dataSource.myDS.driver` property of `CoreQrtzConfiguration#getMysqlConfiguration`:

```java
protected Properties getMysqlConfiguration(String databaseUrl, String user, String password) {
  Properties props = new Properties();
  props.setProperty("org.quartz.jobStore.dataSource", "myDS");
  props.setProperty("org.quartz.dataSource.myDS.driver", "org.postgresql.Driver");
  props.setProperty("org.quartz.dataSource.myDS.maxConnections", "5");
  props.setProperty("org.quartz.dataSource.myDS.validationQuery", "select 1");
  props.setProperty("org.quartz.dataSource.myDS.provider", "hikaricp");
  props.setProperty("org.quartz.dataSource.myDS.URL", databaseUrl);
  props.setProperty("org.quartz.dataSource.myDS.user", user);
  props.setProperty("org.quartz.dataSource.myDS.password", password);
  return props;
}
```

And set `org.quartz.impl.jdbcjobstore.PostgreSQLDelegate` in the `org.quartz.jobStore.driverDelegateClass` property of `qrtz.properties`:

```
# Configure Main Scheduler Properties
org.quartz.scheduler.instanceId=AUTO
org.quartz.scheduler.makeSchedulerThreadDaemon=true

# Configure ThreadPool
org.quartz.threadPool.class=org.quartz.simpl.SimpleThreadPool
org.quartz.threadPool.makeThreadsDaemons=true
org.quartz.threadPool.threadCount:20
org.quartz.threadPool.threadPriority:5

# Configure JobStore
org.quartz.jobStore.class=org.quartz.impl.jdbcjobstore.JobStoreTX
org.quartz.jobStore.driverDelegateClass = org.quartz.impl.jdbcjobstore.PostgreSQLDelegate
org.quartz.jobStore.tablePrefix=CORE_QRTZ_
org.quartz.jobStore.isClustered=false
org.quartz.jobStore.misfireThreshold=25000
```

For more information about Quartz database configuration, see: [How to Setup Databases](https://github.com/quartz-scheduler/quartz/wiki/How-to-Setup-Databases)
