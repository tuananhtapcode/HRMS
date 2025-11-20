package com.project.hrms.repository;

import com.project.hrms.model.Account;
import com.project.hrms.model.Employee;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface AccountRepository extends JpaRepository<Account, Long> {
    Optional<Account> findByUsername(String username);

    Optional<Account> findByEmail(String email);

    Boolean existsByUsername(String username);

    Boolean existsByEmail(String email);

    // Kiểm tra xem một Employee đã có Account chưa
    Boolean existsByEmployee(Employee employee);

    Page<Account> findAll(Pageable pageable);

    Optional<Account> findByActivationToken(String token);

    // ✅ Tìm theo username hoặc email (có chứa, không phân biệt hoa thường)
    @Query("SELECT a FROM Account a " +
            "WHERE LOWER(a.username) LIKE LOWER(CONCAT('%', :keyword, '%')) " +
            "   OR LOWER(a.email) LIKE LOWER(CONCAT('%', :keyword, '%'))")
    Page<Account> searchAccounts(@Param("keyword") String keyword, Pageable pageable);

    /**
     * Find account by username or email
     * @param username username to search
     * @param email email to search
     * @return Optional<Account>
     */
    @Query("""
            SELECT a FROM Account a 
            WHERE a.username = :username 
            OR a.email = :email
            """)
    Optional<Account> findByUsernameOrEmail(
            @Param("username") String username,
            @Param("email") String email
    );
}
