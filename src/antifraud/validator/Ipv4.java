package antifraud.validator;

import jakarta.validation.Constraint;
import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import jakarta.validation.Payload;
import org.apache.commons.validator.routines.InetAddressValidator;


import java.lang.annotation.*;

@Documented
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.FIELD, ElementType.PARAMETER})
@Constraint(validatedBy = Ipv4.Validator.class)
public @interface Ipv4 {
    String message() default "Invalid Ipv4 address!";

    Class<?>[] groups() default {};

    Class<? extends Payload>[] payload() default {};

    class Validator implements ConstraintValidator<Ipv4, String> {

        @Override
        public boolean isValid(String value, ConstraintValidatorContext context) {
            InetAddressValidator validator = InetAddressValidator.getInstance();
            return validator.isValidInet4Address(value);
        }
    }
}
