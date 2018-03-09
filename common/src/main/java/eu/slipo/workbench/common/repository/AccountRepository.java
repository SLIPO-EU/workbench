package eu.slipo.workbench.common.repository;

import java.time.ZonedDateTime;
import java.util.Collection;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import eu.slipo.workbench.common.domain.AccountEntity;
import eu.slipo.workbench.common.model.EnumRole;
import eu.slipo.workbench.common.model.user.Account;

@Repository
@Transactional(readOnly = true)
public interface AccountRepository extends JpaRepository<AccountEntity, Integer>
{   
    @Query("FROM Account a WHERE a.username = :username")
    AccountEntity findOneByUsername(@Param("username") String username);
    
    @Query("FROM Account a WHERE a.email = :email")
    AccountEntity findOneByEmail(@Param("email") String email);
    
    @Query("FROM Account a WHERE a.registeredAt > :start")
    List<AccountEntity> findByRegisteredAfter(@Param("start") ZonedDateTime start);
    
    @Modifying
    @Query("UPDATE Account a SET a.active = :active WHERE a.id IN (:uids)")
    @Transactional(readOnly = false)
    void setActive(@Param("uids") Collection<Integer> uids, @Param("active") boolean active);
}
