package com.schezwansoftware.springwebsockets.service;

import com.schezwansoftware.springwebsockets.domain.BankAccount;
import com.schezwansoftware.springwebsockets.repository.BankAccountRepository;
import com.schezwansoftware.springwebsockets.service.dto.BankAccountDTO;
import com.schezwansoftware.springwebsockets.service.mapper.BankAccountMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.springframework.stereotype.Service;

import java.util.LinkedList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * Service Implementation for managing BankAccount.
 */
@Service
public class BankAccountService {

    private final Logger log = LoggerFactory.getLogger(BankAccountService.class);

    private final BankAccountRepository bankAccountRepository;

    private final BankAccountMapper bankAccountMapper;

    public BankAccountService(BankAccountRepository bankAccountRepository, BankAccountMapper bankAccountMapper) {
        this.bankAccountRepository = bankAccountRepository;
        this.bankAccountMapper = bankAccountMapper;
    }

    /**
     * Save a bankAccount.
     *
     * @param bankAccountDTO the entity to save
     * @return the persisted entity
     */
    public BankAccountDTO save(BankAccountDTO bankAccountDTO) {
        log.debug("Request to save BankAccount : {}", bankAccountDTO);
        BankAccount bankAccount = bankAccountMapper.toEntity(bankAccountDTO);
        bankAccount = bankAccountRepository.save(bankAccount);
        return bankAccountMapper.toDto(bankAccount);
    }

    /**
     * Get all the bankAccounts.
     *
     * @return the list of entities
     */
    public List<BankAccountDTO> findAll() {
        log.debug("Request to get all BankAccounts");
        return bankAccountRepository.findAll().stream()
            .map(bankAccountMapper::toDto)
            .collect(Collectors.toCollection(LinkedList::new));
    }


    /**
     * Get one bankAccount by id.
     *
     * @param id the id of the entity
     * @return the entity
     */
    public Optional<BankAccountDTO> findOne(UUID id) {
        log.debug("Request to get BankAccount : {}", id);
        return bankAccountRepository.findById(id)
            .map(bankAccountMapper::toDto);
    }

    /**
     * Delete the bankAccount by id.
     *
     * @param id the id of the entity
     */
    public void delete(UUID id) {
        log.debug("Request to delete BankAccount : {}", id);        bankAccountRepository.deleteById(id);
    }
}
