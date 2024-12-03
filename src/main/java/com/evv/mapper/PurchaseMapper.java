package com.evv.mapper;

import com.evv.database.entity.Payment;
import com.evv.database.entity.PaymentAccount;
import com.evv.database.entity.PaymentCreditCard;
import com.evv.database.entity.Purchase;
import com.evv.database.repository.PaymentRepository;
import com.evv.dto.PaymentReadDto;
import com.evv.dto.PurchaseCreateEditDto;
import com.evv.dto.PurchaseReadDto;
import com.evv.exception.PaymentNotFoundException;
import org.hibernate.Hibernate;
import org.hibernate.proxy.HibernateProxy;
import org.mapstruct.*;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.stream.Collectors;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE,
        uses = {UserMapper.class, ProductMapper.class, PaymentMapper.class},
        nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
public abstract class PurchaseMapper {

    @Autowired
    protected PaymentRepository paymentRepository;

    @Autowired
    protected UserMapper userMapper;

    @Autowired
    protected ProductMapper productMapper;

    @Autowired
    protected PaymentMapper paymentMapper;

    public abstract Purchase purchaseCreateEditDtoToPurchase(PurchaseCreateEditDto source);

    @Mapping(target = "client", ignore = true) // т.к. поле client при обновлении не подлежит изменению
    @Mapping(target = "id", ignore = true) // т.к. поле id при обновлении не подлежит изменению
    public abstract Purchase purchaseUpdateFromPurchaseCreateEditDto(@MappingTarget Purchase target, PurchaseCreateEditDto source);

    /**
     * Метод написан вручную, т.к. заинжектать поле в PurchaseReadDto с помощью @AfterMapping
     * не представляется возможным, т.к. DTO традиционно @Value, а значит отсутствуют сеттеры.
     * Если код из этого метода повторится еще где-нибудь, то можно сделать фабричный
     * метод в PurchaseReadDto, отметить его @ObjectFactory и использовать фабричный метод как
     * сказано в документации:
     * <a href="https://mapstruct.org/documentation/stable/reference/html/#object-factories">...</a>
     */
    public PurchaseReadDto purchaseToPurchaseReadDto(Purchase source) {

        // Данная проверка на instanceof HibernateProxy появилась в результате проблемы при работе
        // метода @GetMapping findAllByClient(..) в PurchaseRestController.
        // На выходе этого метода в PageResponse<PurchaseReadDto> в полях payment был null,
        // т.к. поле payment у сущности Purchase обернуто в HibernateProxy
        // При получении PurchaseReadDto сразу после сохранения Purchase такой проблемы не было.
        Payment p = null;
        if (source.getPayment() instanceof HibernateProxy) {
            p = Hibernate.unproxy(source.getPayment(), Payment.class);
        } else {
            p = source.getPayment();
        }

        PaymentReadDto prd = null;
        if (p instanceof PaymentCreditCard pcc) {
            prd = paymentMapper.paymentCreditCardToPaymentCreditCardReadDto(pcc);
        } else if (p instanceof PaymentAccount pa) {
            prd = paymentMapper.paymentAccountToPaymentAccountReadDto(pa);
        }

        return new PurchaseReadDto(
                source.getId(),
                userMapper.clientToClientReadDto(source.getClient()),
                source.getProducts().stream()
                        .map(productMapper::productToProductReadDto)
                        .collect(Collectors.toSet()),
                prd
        );
    }

    @AfterMapping
    protected void injectPayment(@MappingTarget Purchase target,
                                 PurchaseCreateEditDto source) {
        target.setPayment(
                paymentRepository.findById(source.getPaymentId())
                        .orElseThrow(() -> new PaymentNotFoundException(source.getPaymentId()))
        );
    }
}
