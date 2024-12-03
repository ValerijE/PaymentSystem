package com.evv.database.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;
import org.hibernate.annotations.DynamicUpdate;
import org.hibernate.annotations.OptimisticLockType;
import org.hibernate.annotations.OptimisticLocking;
import org.hibernate.envers.Audited;
import org.hibernate.envers.NotAudited;
import org.hibernate.proxy.HibernateProxy;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

@Getter
@Setter
@NoArgsConstructor
@ToString(callSuper = true)
@Entity
@PrimaryKeyJoinColumn(name = "users_id")
@OptimisticLocking(type = OptimisticLockType.ALL)
@DynamicUpdate
@Audited
public class Client extends User {

    private LocalDate birthDate;

    @Enumerated(EnumType.STRING)
    private ClientStatus clientStatus;

    private String image;

    @Enumerated(EnumType.STRING)
    private Gender gender;

    @ToString.Exclude
    @NotAudited
    @OneToMany(mappedBy = "client")
    private List<Account> accounts = new ArrayList<>();

    @ToString.Exclude
    @NotAudited
    @OneToMany(mappedBy = "client")
    private List<CreditCard> creditCards = new ArrayList<>();

    public Client(Long id, String email, String password, Role role,
                  LocalDate birthDate, ClientStatus clientStatus, String image, Gender gender,
                  List<Account> accounts, List<CreditCard> creditCards) {
        super(id, email, password, role);
        this.birthDate = birthDate;
        this.clientStatus = clientStatus;
        this.image = image;
        this.gender = gender;
        this.accounts = Objects.requireNonNullElseGet(accounts, ArrayList::new);
        this.creditCards = Objects.requireNonNullElseGet(creditCards, ArrayList::new);
    }

    public void addAccount(Account account) {
        accounts.add(account);
        account.setClient(this);
    }

    public void addCreditCard(CreditCard creditCard) {
        creditCards.add(creditCard);
        creditCard.setClient(this);
    }

    @Override
    public final boolean equals(Object o) {
        if (this == o) return true;
        if (o == null) return false;
        Class<?> oEffectiveClass = o instanceof HibernateProxy ? ((HibernateProxy) o).getHibernateLazyInitializer().getPersistentClass() : o.getClass();
        Class<?> thisEffectiveClass = this instanceof HibernateProxy ? ((HibernateProxy) this).getHibernateLazyInitializer().getPersistentClass() : this.getClass();
        if (thisEffectiveClass != oEffectiveClass) return false;
        Client client = (Client) o;
        return getId() != null && Objects.equals(getId(), client.getId());
    }

    @Override
    public final int hashCode() {
        return this instanceof HibernateProxy
                ? ((HibernateProxy) this).getHibernateLazyInitializer().getImplementation().hashCode()
                : super.hashCode();
    }
}
