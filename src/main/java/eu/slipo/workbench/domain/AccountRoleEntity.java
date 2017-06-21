package eu.slipo.workbench.domain;

import java.time.ZonedDateTime;

import javax.validation.constraints.NotNull;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.Table;
import javax.persistence.UniqueConstraint;
import javax.persistence.Column;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.SequenceGenerator;


import eu.slipo.workbench.model.EnumRole;

@Entity(name = "AccountRole")
@Table(
    schema = "public", name = "`account_role`",
    uniqueConstraints = {
        @UniqueConstraint(name = "uq_account_role", columnNames = {"`account`", "`role`"})
    })
public class AccountRoleEntity
{
    @Id()
    @Column(name = "`id`")
    @SequenceGenerator(
        sequenceName = "account_role_id_seq", name = "account_role_id_seq", allocationSize = 1)
    @GeneratedValue(generator = "account_role_id_seq", strategy = GenerationType.SEQUENCE)
    int id = -1;
    
    @NotNull
    @ManyToOne
    @JoinColumn(name = "`account`", nullable = false)
    AccountEntity account;
    
    @NotNull
    @Enumerated(EnumType.STRING)
    @Column(name = "`role`", nullable = false)
    EnumRole role;    
    
    @Column(name = "granted_at")
    ZonedDateTime grantedAt;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "`granted_by`")
    AccountEntity grantedBy;
    
    AccountRoleEntity() {}
    
    public AccountRoleEntity(AccountEntity account, EnumRole role)
    {
        this(account, role, null, null);
    }
    
    public AccountRoleEntity(
        AccountEntity account, EnumRole role, ZonedDateTime grantedAt, AccountEntity grantedBy)
    {
        this.account = account;
        this.role = role;
        this.grantedAt = grantedAt;
        this.grantedBy = grantedBy;
    }

    public AccountEntity getAccount()
    {
        return account;
    }

    public EnumRole getRole()
    {
        return role;
    }

    public ZonedDateTime getGrantedAt()
    {
        return grantedAt;
    }

    public AccountEntity getGrantedBy()
    {
        return grantedBy;
    }
}
