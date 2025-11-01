package com.project.hrms.service;

import com.project.hrms.dto.AccountDTO;
import com.project.hrms.model.Account;
import com.project.hrms.response.AccountResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;

import java.util.List;

public interface IAccountService {
    Account create(AccountDTO accountDTO);

    AccountResponse update(Long id, AccountDTO accountDTO);

    void delete(Long id);

    Page<AccountResponse> getAllPaged(PageRequest pageRequest);

    Page<AccountResponse> searchAccounts(String keyword, PageRequest pageRequest);

    Account getById(Long id);

    Account getByEmail(String email);

    Account getByUsername(String username);

    boolean existsByUsername(String username);

    boolean existsByEmail(String email);

    Account changePassword(String username, String oldPassword, String newPassword);

    Account resetPassword(Long accountId);
}
