package com.evv.dto;

import com.evv.database.entity.Role;
import lombok.*;
import lombok.experimental.FieldDefaults;

@Getter
@FieldDefaults(makeFinal=true, level= AccessLevel.PRIVATE)
@AllArgsConstructor
@ToString
@EqualsAndHashCode
public abstract class UserReadDto {
    Long id;
    String email;
    Role role;
}
