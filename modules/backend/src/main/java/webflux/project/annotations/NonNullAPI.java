package webflux.project.annotations;

import jakarta.annotation.Nonnull;

import javax.annotation.meta.TypeQualifierDefault;
import java.lang.annotation.*;

@Target({ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
@Documented
@Nonnull
@TypeQualifierDefault({ElementType.METHOD, ElementType.PARAMETER})
public @interface NonNullAPI {
}
