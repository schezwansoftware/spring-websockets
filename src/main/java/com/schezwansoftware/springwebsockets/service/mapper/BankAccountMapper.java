package com.schezwansoftware.springwebsockets.service.mapper;

import com.schezwansoftware.springwebsockets.domain.*;
import com.schezwansoftware.springwebsockets.service.dto.BankAccountDTO;

import org.mapstruct.*;

/**
 * Mapper for the entity BankAccount and its DTO BankAccountDTO.
 */
@Mapper(componentModel = "spring", uses = {})
public interface BankAccountMapper extends EntityMapper<BankAccountDTO, BankAccount> {


}
