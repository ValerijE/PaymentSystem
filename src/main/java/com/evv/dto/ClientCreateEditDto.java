package com.evv.dto;

import com.evv.database.entity.ClientStatus;
import com.evv.database.entity.Gender;
import com.evv.database.entity.Role;
import com.evv.dto.validation.Adult;
import lombok.EqualsAndHashCode;
import lombok.Value;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDate;

@Value
@EqualsAndHashCode(callSuper = true, onlyExplicitlyIncluded = true)
public class ClientCreateEditDto extends UserCreateEditDto {

    @DateTimeFormat(pattern = "yyyy-MM-dd")
    @Adult
    LocalDate birthDate;

    ClientStatus clientStatus;

    MultipartFile image;

    Gender gender;

    public ClientCreateEditDto(String email, String rawPassword, Role role, LocalDate birthDate,
                               ClientStatus clientStatus, MultipartFile image, Gender gender) {
        super(email, rawPassword, role);
        this.birthDate = birthDate;
        this.clientStatus = clientStatus;
        this.image = image;
        this.gender = gender;
    }
}
