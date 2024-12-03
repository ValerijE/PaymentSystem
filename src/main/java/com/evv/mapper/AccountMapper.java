package com.evv.mapper;

import com.evv.database.entity.Account;
import com.evv.database.repository.UserRepository;
import com.evv.dto.AccountDto;
import com.evv.exception.ClientByIdNotFoundException;
import org.mapstruct.*;
import org.springframework.beans.factory.annotation.Autowired;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public abstract class AccountMapper {

    @Autowired
    private UserRepository userRepository;

    @Mapping(target = "clientId", source = "source.client.id")
    public abstract AccountDto.Read.Public accountToAccountReadDto(Account source);

    public abstract Account accountCreateEditDtoToAccount(AccountDto.CreateEdit.Public accountCreateEditDto);

    @AfterMapping
    protected void injectClient(@MappingTarget Account target,
                                AccountDto.CreateEdit.Public source) {
        target.setClient(userRepository.findClientById(source.getClientId())
                .orElseThrow(() -> new ClientByIdNotFoundException(source.getClientId())));
    }
}
