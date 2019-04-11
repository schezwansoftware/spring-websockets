package com.schezwansoftware.springwebsockets.repository;

import com.schezwansoftware.springwebsockets.domain.BankAccount;
import org.springframework.data.cassandra.repository.CassandraRepository;
import org.springframework.stereotype.Repository;

import java.util.UUID;

/**
 * Spring Data Cassandra repository for the BankAccount entity.
 */
@SuppressWarnings("unused")
@Repository
public interface BankAccountRepository extends CassandraRepository<BankAccount, UUID> {

}
