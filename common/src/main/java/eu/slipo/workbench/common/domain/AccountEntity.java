package eu.slipo.workbench.common.domain;

import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.EnumSet;
import java.util.List;
import java.util.Set;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.OneToMany;
import javax.persistence.SequenceGenerator;
import javax.persistence.Table;
import javax.persistence.UniqueConstraint;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Pattern;

import org.hibernate.validator.constraints.Email;

import eu.slipo.workbench.common.model.Account;
import eu.slipo.workbench.common.model.EnumRole;


@Entity(name = "Account")
@Table(
    schema = "public", name = "`account`",
    uniqueConstraints = {
        @UniqueConstraint(name = "uq_account_username", columnNames = {"`username`"}),
        @UniqueConstraint(name = "uq_account_email", columnNames = {"`email`"}),
    })
public class AccountEntity
{
    @Id
    @Column(name = "`id`")
    @SequenceGenerator(
        sequenceName = "account_id_seq", name = "account_id_seq", allocationSize = 1)
    @GeneratedValue(generator = "account_id_seq", strategy = GenerationType.SEQUENCE)
    Integer id;

    @NotNull
    @Column(name = "`username`", nullable = false)
    String username;

    @NotNull
    @Email
    @Column(name = "`email`", nullable = false)
    String email;

    @Column(name = "`given_name`")
    String givenName;

    @Column(name = "`family_name`")
    String familyName;

    @Pattern(regexp = "[a-z][a-z]")
    @Column(name = "`lang`")
    String lang;

    @Column(name = "`password`")
    String password;

    @Column(name = "`active`")
    Boolean active = true;

    @Column(name = "`blocked`")
    Boolean blocked = false;

    @Column(name = "`registered_at`")
    ZonedDateTime registeredAt;

    @OneToMany(
        mappedBy = "account",
        fetch = FetchType.EAGER,
        cascade = CascadeType.ALL, orphanRemoval = true)
    List<AccountRoleEntity> roles = new ArrayList<>();

    public AccountEntity() {}

    public AccountEntity(int uid)
    {
        this.id = uid;
    }

    public AccountEntity(String username, String email)
    {
        this.username = username;
        this.email = email;
    }

    public void setId(int id)
    {
        this.id = id;
    }

    public void setName(String givenName, String familyName)
    {
        this.givenName = givenName;
        this.familyName = familyName;
    }

    public void setLang(String lang)
    {
        this.lang = lang;
    }

    public void setPassword(String password)
    {
        this.password = password;
    }

    public void setActive(boolean active)
    {
        this.active = active;
    }

    public void setBlocked(boolean blocked)
    {
        this.blocked = blocked;
    }

    public void setRegistered(ZonedDateTime registeredAt)
    {
        this.registeredAt = registeredAt;
    }

    public int getId()
    {
        return id;
    }

    public String getUsername()
    {
        return username;
    }

    public String getPassword()
    {
        return password;
    }

    public String getEmail()
    {
        return email;
    }

    public String getGivenName()
    {
        return givenName;
    }

    public String getFamilyName()
    {
        return familyName;
    }

    public String getFullName() {
        return String.format("%s %s", this.givenName, this.familyName).trim();
    }

    public String getLang()
    {
        return lang;
    }

    public boolean isActive()
    {
        return active == null? true : active;
    }

    public boolean isBlocked()
    {
        return blocked == null? false : blocked;
    }

    public ZonedDateTime getRegisteredAt()
    {
        return registeredAt;
    }

    public Set<EnumRole> getRoles()
    {
        EnumSet<EnumRole> r = EnumSet.noneOf(EnumRole.class);
        for (AccountRoleEntity ar: roles) {
            r.add(ar.role);
        }
        return r;
    }

    public boolean hasRole(EnumRole role)
    {
        for (AccountRoleEntity ar: roles) {
            if (role == ar.role) {
                return true;
            }
        }
        return false;
    }

    public void grant(EnumRole role, AccountEntity grantedBy)
    {
        if (!hasRole(role)) {
            roles.add(new AccountRoleEntity(this, role, null, grantedBy));
        }
    }

    public void revoke(EnumRole role)
    {
        AccountRoleEntity target = null;
        for (AccountRoleEntity ar: roles) {
            if (role == ar.role) {
                target = ar;
                break;
            }
        }
        if (target != null) {
            roles.remove(target);
        }
    }

    /**
     * Convert to a DTO object
     *
     * @return a new {@link Account} instance
     */
    public Account toDto()
    {
        Account a = new Account(id, username, email);

        a.setActive(active);
        a.setBlocked(blocked);

        a.setFamilyName(familyName);
        a.setGivenName(givenName);

        a.setLang(lang);
        a.setRegisteredAt(registeredAt);
        a.setRoles(getRoles());

        return a;
    }
}
