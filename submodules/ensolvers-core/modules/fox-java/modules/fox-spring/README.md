# Introduction to fox-spring

`fox-spring` is the `fox-java` module for Spring utilities

## AutomaticLogger

Annotation that automates method invocation logging using AspectJ. 

To add it to your current Spring Boot app simply Add `@Import(AutomaticLoggerImpl)` to the main app class, for instance

```java

@SpringBootApplication
@Import(AutomaticLoggingImpl.class)
public class MyApplication {

	public static void main(String[] args) {
		SpringApplication.run(MyApplication.class, args);
	}

}

```

And then use it for every method you want to log

The annotation has the allows (optional) parameters:

- `String logSuffix` specifies a suffix for every log entry written by the annotation - default is empty String
- `Boolean includeParameters` specifies if it must log the concrete parameters used in method call - default value is 
  `true`
- `Boolean logCollectionSizeOnly` when collections are passed as parameters and `includeParameters` is `true`, it specifies if 
  all the inner parameters should be logged or only it must log the size - default value is `true`
- `Boolean timeElapsedLogging` specifies if the time that passed between the invocation and the return of the function 
  must be logged - default value is `false`