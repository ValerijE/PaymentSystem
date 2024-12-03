package com.evv.mapper;

import com.evv.database.entity.Admin;
import com.evv.database.entity.Client;
import com.evv.database.entity.User;
import com.evv.database.repository.UserRepository;
import com.evv.dto.AdminReadDto;
import com.evv.dto.ClientCreateEditDto;
import com.evv.dto.ClientReadDto;
import com.evv.dto.UserReadDto;
import com.evv.exception.ClientByIdNotFoundException;
import lombok.RequiredArgsConstructor;
import org.mapstruct.AfterMapping;
import org.mapstruct.Mapper;
import org.mapstruct.MappingTarget;
import org.mapstruct.ReportingPolicy;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import java.util.Optional;
import java.util.UnknownFormatConversionException;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE,
        uses = {AccountMapper.class, CreditCardMapper.class})
@RequiredArgsConstructor
public abstract class UserMapper {

    @Autowired
    protected PasswordEncoder passwordEncoder;

    @Autowired
    protected UserRepository userRepository;

    /**
     * Метод написан вручную, т.к. возвращаемое DTO является абстрактным классом.
     * Mapstruct не позволяет автоматически инстанцировать экземпляры абстрактных классов, т.к.
     * у него нет сведений о выборе целевого класса: ClientReadDto или AdminReadDto.
     * В образовательных целях, с помощью данного метода, в SecurityContext помещается, кроме стандартных
     * полей для нужд UserDetails еще и соответствующая реализация UserReadDto.
     */
    public UserReadDto userToUserReadDto(User source) {
        if (source instanceof Client cl) {
            return clientToClientReadDto(cl);
        } else if (source instanceof Admin ad) {
            return adminToAdminReadDto(ad);
        } else {
            throw new UnknownFormatConversionException(
                    "Unknown instance of type User for id = %d".formatted(source.getId()));
        }
    }

    // Метод понадобился для работы PurchaseMapper
    public Client clientReadDtoToClient(ClientReadDto source) {
        return userRepository.findClientById(source.getId())
                .orElseThrow(() -> new ClientByIdNotFoundException(source.getId()));
    }

    public abstract AdminReadDto adminToAdminReadDto(Admin source);

    public abstract ClientReadDto clientToClientReadDto(Client source);

    public abstract Client clientCreateEditDtoToClient(ClientCreateEditDto source);

    protected String map(MultipartFile value) {
        if (value != null) {
            if (!value.isEmpty()) {
                return value.getOriginalFilename();
            } else {
                return null;
            }
        } else {
            return null;
        }
    }

    @AfterMapping
    protected void injectPassword(@MappingTarget Client target, ClientCreateEditDto source) {

        Optional.ofNullable(source.getRawPassword())
                .filter(StringUtils::hasText)
                .map(passwordEncoder::encode)
                .ifPresent(target::setPassword);
    }
}
