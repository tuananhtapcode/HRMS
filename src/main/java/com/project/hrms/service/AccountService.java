package com.project.hrms.service;

import com.project.hrms.dto.AccountDTO;
import com.project.hrms.exception.DataNotFoundException;
import com.project.hrms.model.Account;
import com.project.hrms.model.Employee;
import com.project.hrms.model.Role;
import com.project.hrms.repository.AccountRepository;
import com.project.hrms.repository.EmployeeRepository;
import com.project.hrms.repository.RoleRepository;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class AccountService implements IAccountService {

    private final ModelMapper modelMapper;
    private final AccountRepository accountRepository;
    private final EmployeeRepository employeeRepository;
    private final RoleRepository roleRepository;

    @PostConstruct
    public void settupMapper(){
        modelMapper.typeMap(AccountDTO.class, Account.class)
                .addMappings(mapper -> mapper.skip(Account::setAccountId));
    }

    @Override
    public Account create(AccountDTO accountDTO) {
        if (existsByUsername(accountDTO.getUsername())) {
            throw new RuntimeException("Username already exists");
        }

        Role role = roleRepository.findById(accountDTO.getRoleId())
                .orElseThrow(() -> new DataNotFoundException("Role not found with id: " + accountDTO.getRoleId()));

        Employee employee = employeeRepository.findById(accountDTO.getEmployeeId())
                .orElseThrow(() -> new DataNotFoundException("Employee not found with id: " + accountDTO.getEmployeeId()));

        Account account = modelMapper.map(accountDTO, Account.class);
        account.setRole(role);
        account.setEmployee(employee);
        account.setIsActive(true);

        return accountRepository.save(account);
    }

    @Override
    public Account update(Long id, AccountDTO accountDTO) {
        Account existingAccount = getById(id);

        Role role = roleRepository.findById(accountDTO.getRoleId())
                .orElseThrow(() -> new DataNotFoundException("Role not found with id: " + accountDTO.getRoleId()));

        Employee employee = employeeRepository.findById(accountDTO.getEmployeeId())
                .orElseThrow(() -> new DataNotFoundException("Employee not found with id: " + accountDTO.getEmployeeId()));
        modelMapper.map(accountDTO, existingAccount);
        existingAccount.setRole(role);
        existingAccount.setEmployee(employee);
        return accountRepository.save(existingAccount);
    }

    @Override
    public Account delete(Long id) {
        Account account = getById(id);
        accountRepository.delete(account);
        return null;
    }

    @Override
    public List<Account> getAll() {
        return accountRepository.findAll();
    }

    @Override
    public Account getById(Long id) {
        return accountRepository.findById(id).
                orElseThrow(() -> new DataNotFoundException("Account not found with id" + id));
    }

    @Override
    public Account getByEmail(String email) {
        return accountRepository.findByEmail(email)
                .orElseThrow(() -> new DataNotFoundException("Account not found with email" + email));
    }

    @Override
    public Account getByUsername(String username) {
        return accountRepository.findByUsername(username)
                .orElseThrow(() -> new DataNotFoundException("Account not found with username: " + username));
    }

    @Override
    public boolean existsByUsername(String username) {
        return accountRepository.existsByUsername(username);
    }
}
