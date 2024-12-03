package com.evv.dto;

import com.evv.database.entity.ClientStatus;
import com.evv.database.entity.Gender;
import com.evv.database.entity.Role;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import lombok.Value;

import java.time.LocalDate;

@Value
@EqualsAndHashCode(callSuper = true, onlyExplicitlyIncluded = true)
@ToString(callSuper = true)
public class ClientReadDto extends UserReadDto {

    LocalDate birthDate;

    ClientStatus clientStatus;

    String image;

    Gender gender;

    public ClientReadDto(Long id, String email, Role role, LocalDate birthDate, ClientStatus clientStatus, String image, Gender gender) {
        super(id, email, role);
        this.birthDate = birthDate;
        this.clientStatus = clientStatus;
        this.image = image;
        this.gender = gender;
    }
}
