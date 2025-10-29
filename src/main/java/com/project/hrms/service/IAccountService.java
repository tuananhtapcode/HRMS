package com.project.hrms.service;

import com.project.hrms.dto.AccountDTO;
import com.project.hrms.model.Account;

import java.util.List;
import java.util.Optional;

public interface IAccountService {
    Account create(AccountDTO accountDTO);

    Account update(Long id, AccountDTO accountDTO);

    Account delete(Long id);

    List<Account> getAll();

    Account getById(Long id);

    Account getByEmail(String email);

    Account getByUsername(String username);

    boolean existsByUsername(String username);

}
