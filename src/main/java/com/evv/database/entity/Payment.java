package com.evv.database.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;
import lombok.*;
import org.hibernate.envers.Audited;
import org.hibernate.proxy.HibernateProxy;

import java.math.BigDecimal;
import java.util.Objects;

/**
 * В целях обучения, эта сущность сделана абстрактной и родительской по отношению к
 * вспомогательным сущностям PaymentCreditCard и PaymentAccount.
 * Последние сущности нужны для обеспечения ассоциативной полиморфной связи с сущностями
 * CreditCard и Account, с одной из которых, по выбору пользователя, производится платеж.
 * Таблицы этих сущностей, согласно моей легенде, нельзя изменять в части
 * объединения генератора первичных ключей.
 * Сначала для этих целей я использовал подход Hibernate @Any, но потом от него отказался, т.к.
 * в этом случае отсутствует контроль целостности и непротиворечивости данных со стороны БД.
 * Ну и в целом @Any рекомендуется авторами Hibernate только для аудита и аналитики.
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@ToString(onlyExplicitlyIncluded = true)
@Entity
@Inheritance(strategy = InheritanceType.JOINED)
@Audited
public abstract class Payment {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @ToString.Include
    private Long id;

    @NotNull
    @PositiveOrZero
    @ToString.Include
    private BigDecimal amount;

    @OneToOne(mappedBy = "payment")
    private Purchase purchase;

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null) return false;
        Class<?> oEffectiveClass = o instanceof HibernateProxy ? ((HibernateProxy) o).getHibernateLazyInitializer().getPersistentClass() : o.getClass();
        Class<?> thisEffectiveClass = this instanceof HibernateProxy ? ((HibernateProxy) this).getHibernateLazyInitializer().getPersistentClass() : this.getClass();
        if (thisEffectiveClass != oEffectiveClass) return false;
        Payment payment = (Payment) o;
        return getId() != null && Objects.equals(getId(), payment.getId());
    }

    @Override
    public int hashCode() {
        return this instanceof HibernateProxy
                ? ((HibernateProxy) this).getHibernateLazyInitializer().getImplementation().hashCode()
                : Objects.hash(id);
    }
}
