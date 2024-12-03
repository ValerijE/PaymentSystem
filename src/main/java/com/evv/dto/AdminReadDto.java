package com.evv.dto;

import com.evv.database.entity.Role;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import lombok.Value;

@Value
@EqualsAndHashCode(callSuper = true, onlyExplicitlyIncluded = true)
@ToString(callSuper = true)
public class AdminReadDto extends UserReadDto {

    Integer correctionCount;

    public AdminReadDto(Long id, String email, Role role, Integer correctionCount) {
        super(id, email, role);
        this.correctionCount = correctionCount;
    }
}
