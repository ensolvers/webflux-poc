# Public endpoints

Core offers a generic framework for dealing with public endpoints (doesn't need a token to access).
Access to public endpoints is managed by class: SecurityConfiguration.
You just need to implement interface EnsolversCoreSecurityConfiguration adding your public endpoints.

Example:

    package club.pdl.configuration;

    import club.pdl.services.PDLEnvironmentPropertyService;

    import com.ensolvers.core.api.configuration.EnsolversCoreSecurityConfiguration;

    import com.ensolvers.core.common.configuration.AppEnvironment;

    import org.springframework.context.annotation.Configuration;

    import java.util.ArrayList;

    import java.util.List;

    @Configuration
    public class PDLSecurityConfiguration implements EnsolversCoreSecurityConfiguration {

    private final PDLEnvironmentPropertyService pdlEnvironmentPropertyService;

    public PDLSecurityConfiguration(PDLEnvironmentPropertyService pdlEnvironmentPropertyService) {
        this.pdlEnvironmentPropertyService = pdlEnvironmentPropertyService;
    }

    @Override
    public List<String> additionalPublicPaths() {
        var commonEndpoints = List.of(
                "/pdl-auth/sign-up",
                "/account-wait-list/create",
                "/pdl-user/invite-code/.*",
                "/pdl-auth/wait-list/sign-up",
                "/stripe/refund-updated",
                "/insurance/claim-created",
                "/stripe/charge-expired",
                "/pdl-auth/validate",
                "/content/terms-and-conditions",
                "/auth/login-as"
        );

        var swaggerEndpoints = List.of(
                "/swagger-ui/.*",
                "/v2/api-docs",
                "/swagger-ui/",
                "/v2/api-docs",
                "/webjars/springfox-swagger-ui/.*",
                "/swagger-resources/.*",
                "/swagger-resources"
        );

        var result = new ArrayList<>(commonEndpoints);

        if (pdlEnvironmentPropertyService.getEnv() != AppEnvironment.PROD && pdlEnvironmentPropertyService.getEnv() != AppEnvironment.PREPROD) {
            result.addAll(swaggerEndpoints);
        }

        return result;
    }
}
