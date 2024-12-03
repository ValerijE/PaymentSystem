package com.evv.dto;

import com.evv.database.entity.Role;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotNull;
import lombok.*;
import lombok.experimental.FieldDefaults;
import org.hibernate.validator.constraints.Length;

import static com.evv.util.Util.EMAIL_REGEXP;

@Getter
@FieldDefaults(makeFinal = true, level = AccessLevel.PRIVATE)
@AllArgsConstructor
@ToString
@EqualsAndHashCode
public abstract class UserCreateEditDto {

    @Email(regexp = EMAIL_REGEXP, message = "{validation.Email.UserCreateEditDto}")
    @Length(min = 4, max = 128, message = "{validation.Length.UserCreateEditDto.email}")
    @NotNull(message = "{validation.NotNull.UserCreateEditDto.email}")
    String email;

    @Length(min = 3, max = 32, message = "{validation.Length.UserCreateEditDto.rawPassword}")
    String rawPassword;

    Role role;
}
