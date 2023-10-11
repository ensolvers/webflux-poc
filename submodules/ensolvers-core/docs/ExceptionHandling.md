# Exception handling
To throw an exception that will be converted to a proper HTTP response we use the class `CoreApiException`.
For example, if you need to return a response informing the user that the phone number entered is invalid:
```
if (<invalid phone>) {
    throw CoreApiException.validationError("Invalid phone number");
}
```
The exceptions are handled in the class `ControllerExceptionHandler` (note that this class is annotated with `@ControllerAdvice`
and its methods with `@ExceptionHandler`).

# Add more handlers
To add more handlers or overwrite existing handlers, you can implement a class annotated with `@ControllerAdvice` that contains methods annotated with `@ExceptionHandler`.
Example: 
```
@ControllerAdvice
public class MyProjectExceptionHandler {

  //Overwrites fallback handler
  @ExceptionHandler(Exception.class)
  public ResponseEntity<ApiResultDTO<String>> handleException(Exception e) {
    ...
  }
    
  //New handler
  @ExceptionHandler(ObjectValidationFailed.class)
  public ResponseEntity<ApiResultDTO<String>> handleException(ObjectValidationFailed e) {
    ...
  }
}
```

# Add more variants of CoreApiException class
To define more variants of `CoreApiException` class we recommend extending this class. For example:

```
public class MyProjectApiException extends CoreApiException {
    protected MyProjectApiException(String apiResultMessage, HttpStatus httpStatus, Enum errorCode, boolean logError, Throwable e) {
        super(apiResultMessage, httpStatus, errorCode, logError, e);
    }

    protected MyProjectApiException(String apiResultMessage, HttpStatus httpStatus, Enum errorCode, boolean logError) {
        super(apiResultMessage, httpStatus, errorCode, logError);
    }

    protected MyProjectApiException(String internalMessage, String apiResultMessage, HttpStatus httpStatus, Enum errorCode, boolean logError) {
        super(internalMessage, apiResultMessage, httpStatus, errorCode, logError);
    }

    //New variant
    protected MyProjectApiException customException() {
        return new MyProjectApiException("Test", HttpStatus.NOT_FOUND, ErrorCode.OBJECT_NOT_FOUND, true);
    }
}
```
If you don't define a handler for `MyProjectApiException`, this exception will continue to be handled by `ControllerExceptionHandler` 
respecting the interface of `CoreApiException` class (downcasting).