# Ensolvers Core / Security

Ensolvers Core relies on Spring Security for both authentication and authorization.

Several authentication strategies can be used:
1. Username + password
2. Validation code sent to email address / SMS
3. OAuth (via common providers like Google / Facebook)

## Implementation

Ensolvers Core uses a custom Spring `OncePerRequestFilter` which default implementation is `AuthenticationServiceFilter`. That filter implements the core aspects like managing which endpoints are whitelisted and setting up the propert Spring `SecurityContext` (if the user is authenticated) to the following filters in the chain.

Authentication tokens are managed and stored directly on the underlying relational database - table `core_auth_token`. This allows the framework to issue tokens independently of the authentication method used. 

## Admin endpoints

There are a subset of critical tasks that require a high security clearance (for instance, creating admins or super-admins). For those tasks, a set of so called "admin endpoints" are provided. Those endpoints are provided by rotating secrets that are only accessed by the engineering team - or a subset of it. In general, those secrets are generated randomly altough they can be configured to rotate every X amount of minutes if required.

These endpoints can be available just by setting

```
application.admin-endpoints.enabled=true
```

Below, a list of sample cURL calls are listed showing what can be done by using those endpoints. These calls can be used as a template just by changing the hostname/port to the correct one and the `SECRET` to the actual secret  - if these endpoints are available of course.

NOTE: Current secret can be found just looking at the `OTAS:` string in the logs - OTAS stands for One Time Admin Secret

### Admin creation

```shell
curl -v -X POST http://localhost:8080/admin/public/admins -d '{"firstName":"Admin", "lastName":"User", "email":"admin@ensolvers.com", "password": "abc123", "phone":"123456789", "passwordConfirmation":"abc123"}' -H "x-otas: $SECRET" -H "Content-Type: application/json"
```

### Role addition and removal

```shell
curl -v -X POST http://localhost:8080/admin/public/roles -d '{"email":"email@ensolvers.com", "roleName":"ROLE_MANAGER"}' -H "x-otas: $SECRET" -H "Content-Type: application/json"
```

```shell
curl -v -X POST http://localhost:8080/admin/public/remove-role -d '{"email":"email@ensolvers.com", "roleName":"ROLE_MANAGER"}' -H "x-otas: $SECRET" -H "Content-Type: application/json"
```

### Disable / Enable a user
```shell
curl -v -X POST http://localhost:8080/admin/public/user-status -d '{"email":"email@ensolvers.com", "enabled":false}' -H "x-otas: $SECRET" -H "Content-Type: application/json"
```

## Validation code sent to email address/SMS
### Sign-up
```shell
curl --location --request POST 'localhost:8080/auth/sign-up' \
--header 'Content-Type: application/json' \
--data-raw '{
"firstName": "First Name",
"lastName": "Last Name",
"password" : "password",
"passwordConfirmation" :  "password",
"email" : "xxxxx@xxxxxxx.xxx"
}'
```

This method does the following actions:
- Create user in CORE USER table as disabled.
- Generate validation code link:
```shell
localhost:8080/auth/validate?code=941246&email=xxxxx@xxxxxxx.xxx
```
- Send validation email:
  - To: email
  - Subject: "Validate your account"
  - Template: defined in: templates/validation-email.html property + generated validation link.

Note:
In PDL we had to overwrite it to include extra validation like for example, more than 18 years or to integrate with recaptcha.

### Validate account via mail
```shell
curl --location --request GET 'localhost:8080/auth/validate?code=941246&email=xxxxx@xxxxxxx.xxx' \
--header 'Content-Type: application/json'
```
This method does the following actions:
- Validate email and code
- Mark user as enabled

Note: 
In PDL we created a different method: validate-account to send additionally a welcome mail.
We updated the following property to redirect app to the new endpoint. 
- application.validation.base-url=${application.frontend.base-url}/validate-account

### Login
```shell
curl --location --request POST 'localhost:8080/auth/login' \
--header 'Content-Type: application/json' \
--data-raw '{
    "email" : "xxxxx@xxxxxxx.xxx",
    "password" : "password!"
}'
```
To enable login user must be activated with validation account via mail.

## OAuth 2.0 set up

`ensolvers-core-backend-api` provides authentication via OAuth 2.0 out of the box. To use it, first you need to enable it

```
core.ensolvers.oauth.enabled=true
```

### Error / Success management

If the auth process succeeds or fails, the backend will redirect to the URLs specified by the `success-page` and `error-page` properties, for instance

```
core.ensolvers.oauth.success-page=${application.base-url}/login-success
core.ensolvers.oauth.error-page=${application.base-url}/error?message=oauth-failed
```

In the case of succeed, the fresh issues authentication token for the user will be attached with the query string param `t`, for instance

```
http://localhost:8080/oauth/connect/succeed?t=123abc
```

If an error occurs, then the error will be described via a `message` query string param

```
http://localhost:8080/oauth/connect/succeed?message=User+not+allowed
```

### Customizing the flow

Depending on the app domain and requriements, some parts of the flow might want to be personalized/adapted. This can be done
just configuring a bean of type [OAuthFlowAdapter](../modules/ensolvers-core-backend-api/src/main/java/com/ensolvers/core/api/configuration/OAuthFlowAdapter.java). Check the javadoc of this class to 
know more about what parts of the process can be adapted.

Example below:

```java
@Configuration
public class MyProjectOAuthFlowAdapter implements OAuthFlowAdapter {

    private OtherService service;

    public MyProjectOAuthFlowAdapter(OtherService service) {
        this.service = service;
    }

    @Override
    public boolean acceptLogin(String email) {
        // all users are accepted
        return true;
    }

    @Override
    public void afterLogin(AuthenticationToken token) {
        otherService.doSomethingWithThisUser(token.getUser());
    }
}
```

### Parameters per provider 

Below you can find the parameters to be configured in each case and the corresponding URL to be used for starting the
flow

#### For Google
Properties:
```
core.ensolvers.oauth.google.client-id=<client-id>
core.ensolvers.oauth.google.client-secret=<client-secret>
```

URL: http://localhost:8080/oauth/connect/google

### Public Endpoints
* [Public Endpoints](Public_Endpoints.md)
