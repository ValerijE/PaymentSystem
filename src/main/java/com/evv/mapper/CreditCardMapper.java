package com.evv.mapper;

import com.evv.database.entity.CreditCard;
import com.evv.database.repository.UserRepository;
import com.evv.dto.CreditCardDto;
import com.evv.exception.ClientByIdNotFoundException;
import org.mapstruct.*;
import org.springframework.beans.factory.annotation.Autowired;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE,
        uses = {UserMapper.class})
public abstract class CreditCardMapper {

    @Autowired
    private UserRepository userRepository;

    @Mapping(target = "clientId", source = "source.client.id")
    public abstract CreditCardDto.Read.Public creditCardToCreditCardReadDto(CreditCard source);

    public abstract CreditCard creditCardCreateEditDtoToCreditCard(CreditCardDto.CreateEdit.Public source);

    @AfterMapping
    protected void injectClient(@MappingTarget CreditCard target,
                                CreditCardDto.CreateEdit.Public source) {
        target.setClient(userRepository.findClientById(source.getClientId())
                .orElseThrow(() -> new ClientByIdNotFoundException(source.getClientId())));
    }
}
