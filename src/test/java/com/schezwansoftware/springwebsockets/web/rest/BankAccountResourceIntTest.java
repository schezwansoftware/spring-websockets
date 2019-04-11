package com.schezwansoftware.springwebsockets.web.rest;

import com.schezwansoftware.springwebsockets.AbstractCassandraTest;
import com.schezwansoftware.springwebsockets.SpringWebsocketsApp;

import com.schezwansoftware.springwebsockets.domain.BankAccount;
import com.schezwansoftware.springwebsockets.repository.BankAccountRepository;
import com.schezwansoftware.springwebsockets.service.BankAccountService;
import com.schezwansoftware.springwebsockets.service.dto.BankAccountDTO;
import com.schezwansoftware.springwebsockets.service.mapper.BankAccountMapper;
import com.schezwansoftware.springwebsockets.web.rest.errors.ExceptionTranslator;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.MockitoAnnotations;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.web.PageableHandlerMethodArgumentResolver;
import org.springframework.http.MediaType;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.validation.Validator;

import java.math.BigDecimal;
import java.util.List;

import java.util.UUID;

import static com.schezwansoftware.springwebsockets.web.rest.TestUtil.createFormattingConversionService;
import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.hasItem;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Test class for the BankAccountResource REST controller.
 *
 * @see BankAccountResource
 */
@RunWith(SpringRunner.class)
@SpringBootTest(classes = SpringWebsocketsApp.class)
public class BankAccountResourceIntTest extends AbstractCassandraTest {

    private static final String DEFAULT_USER_ID = "AAAAAAAAAA";
    private static final String UPDATED_USER_ID = "BBBBBBBBBB";

    private static final String DEFAULT_BANK_NAME = "AAAAAAAAAA";
    private static final String UPDATED_BANK_NAME = "BBBBBBBBBB";

    private static final String DEFAULT_ACCOUNT_NUMBER = "AAAAAAAAAA";
    private static final String UPDATED_ACCOUNT_NUMBER = "BBBBBBBBBB";

    private static final BigDecimal DEFAULT_CURRENTBALANCE = new BigDecimal(1);
    private static final BigDecimal UPDATED_CURRENTBALANCE = new BigDecimal(2);

    @Autowired
    private BankAccountRepository bankAccountRepository;

    @Autowired
    private BankAccountMapper bankAccountMapper;

    @Autowired
    private BankAccountService bankAccountService;

    @Autowired
    private MappingJackson2HttpMessageConverter jacksonMessageConverter;

    @Autowired
    private PageableHandlerMethodArgumentResolver pageableArgumentResolver;

    @Autowired
    private ExceptionTranslator exceptionTranslator;

    @Autowired
    private Validator validator;

    private MockMvc restBankAccountMockMvc;

    private BankAccount bankAccount;

    @Before
    public void setup() {
        MockitoAnnotations.initMocks(this);
        final BankAccountResource bankAccountResource = new BankAccountResource(bankAccountService);
        this.restBankAccountMockMvc = MockMvcBuilders.standaloneSetup(bankAccountResource)
            .setCustomArgumentResolvers(pageableArgumentResolver)
            .setControllerAdvice(exceptionTranslator)
            .setConversionService(createFormattingConversionService())
            .setMessageConverters(jacksonMessageConverter)
            .setValidator(validator).build();
    }

    /**
     * Create an entity for this test.
     *
     * This is a static method, as tests for other entities might also need it,
     * if they test an entity which requires the current entity.
     */
    public static BankAccount createEntity() {
        BankAccount bankAccount = new BankAccount()
            .userId(DEFAULT_USER_ID)
            .bankName(DEFAULT_BANK_NAME)
            .accountNumber(DEFAULT_ACCOUNT_NUMBER)
            .currentbalance(DEFAULT_CURRENTBALANCE);
        return bankAccount;
    }

    @Before
    public void initTest() {
        bankAccountRepository.deleteAll();
        bankAccount = createEntity();
    }

    @Test
    public void createBankAccount() throws Exception {
        int databaseSizeBeforeCreate = bankAccountRepository.findAll().size();

        // Create the BankAccount
        BankAccountDTO bankAccountDTO = bankAccountMapper.toDto(bankAccount);
        restBankAccountMockMvc.perform(post("/api/bank-accounts")
            .contentType(TestUtil.APPLICATION_JSON_UTF8)
            .content(TestUtil.convertObjectToJsonBytes(bankAccountDTO)))
            .andExpect(status().isCreated());

        // Validate the BankAccount in the database
        List<BankAccount> bankAccountList = bankAccountRepository.findAll();
        assertThat(bankAccountList).hasSize(databaseSizeBeforeCreate + 1);
        BankAccount testBankAccount = bankAccountList.get(bankAccountList.size() - 1);
        assertThat(testBankAccount.getUserId()).isEqualTo(DEFAULT_USER_ID);
        assertThat(testBankAccount.getBankName()).isEqualTo(DEFAULT_BANK_NAME);
        assertThat(testBankAccount.getAccountNumber()).isEqualTo(DEFAULT_ACCOUNT_NUMBER);
        assertThat(testBankAccount.getCurrentbalance()).isEqualTo(DEFAULT_CURRENTBALANCE);
    }

    @Test
    public void createBankAccountWithExistingId() throws Exception {
        int databaseSizeBeforeCreate = bankAccountRepository.findAll().size();

        // Create the BankAccount with an existing ID
        bankAccount.setId(UUID.randomUUID());
        BankAccountDTO bankAccountDTO = bankAccountMapper.toDto(bankAccount);

        // An entity with an existing ID cannot be created, so this API call must fail
        restBankAccountMockMvc.perform(post("/api/bank-accounts")
            .contentType(TestUtil.APPLICATION_JSON_UTF8)
            .content(TestUtil.convertObjectToJsonBytes(bankAccountDTO)))
            .andExpect(status().isBadRequest());

        // Validate the BankAccount in the database
        List<BankAccount> bankAccountList = bankAccountRepository.findAll();
        assertThat(bankAccountList).hasSize(databaseSizeBeforeCreate);
    }

    @Test
    public void getAllBankAccounts() throws Exception {
        // Initialize the database
        bankAccount.setId(UUID.randomUUID());
        bankAccountRepository.save(bankAccount);

        // Get all the bankAccountList
        restBankAccountMockMvc.perform(get("/api/bank-accounts"))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_UTF8_VALUE))
            .andExpect(jsonPath("$.[*].id").value(hasItem(bankAccount.getId().toString())))
            .andExpect(jsonPath("$.[*].userId").value(hasItem(DEFAULT_USER_ID.toString())))
            .andExpect(jsonPath("$.[*].bankName").value(hasItem(DEFAULT_BANK_NAME.toString())))
            .andExpect(jsonPath("$.[*].accountNumber").value(hasItem(DEFAULT_ACCOUNT_NUMBER.toString())))
            .andExpect(jsonPath("$.[*].currentbalance").value(hasItem(DEFAULT_CURRENTBALANCE.intValue())));
    }
    
    @Test
    public void getBankAccount() throws Exception {
        // Initialize the database
        bankAccount.setId(UUID.randomUUID());
        bankAccountRepository.save(bankAccount);

        // Get the bankAccount
        restBankAccountMockMvc.perform(get("/api/bank-accounts/{id}", bankAccount.getId()))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_UTF8_VALUE))
            .andExpect(jsonPath("$.id").value(bankAccount.getId().toString()))
            .andExpect(jsonPath("$.userId").value(DEFAULT_USER_ID.toString()))
            .andExpect(jsonPath("$.bankName").value(DEFAULT_BANK_NAME.toString()))
            .andExpect(jsonPath("$.accountNumber").value(DEFAULT_ACCOUNT_NUMBER.toString()))
            .andExpect(jsonPath("$.currentbalance").value(DEFAULT_CURRENTBALANCE.intValue()));
    }

    @Test
    public void getNonExistingBankAccount() throws Exception {
        // Get the bankAccount
        restBankAccountMockMvc.perform(get("/api/bank-accounts/{id}", UUID.randomUUID().toString()))
            .andExpect(status().isNotFound());
    }

    @Test
    public void updateBankAccount() throws Exception {
        // Initialize the database
        bankAccount.setId(UUID.randomUUID());
        bankAccountRepository.save(bankAccount);

        int databaseSizeBeforeUpdate = bankAccountRepository.findAll().size();

        // Update the bankAccount
        BankAccount updatedBankAccount = bankAccountRepository.findById(bankAccount.getId()).get();
        updatedBankAccount
            .userId(UPDATED_USER_ID)
            .bankName(UPDATED_BANK_NAME)
            .accountNumber(UPDATED_ACCOUNT_NUMBER)
            .currentbalance(UPDATED_CURRENTBALANCE);
        BankAccountDTO bankAccountDTO = bankAccountMapper.toDto(updatedBankAccount);

        restBankAccountMockMvc.perform(put("/api/bank-accounts")
            .contentType(TestUtil.APPLICATION_JSON_UTF8)
            .content(TestUtil.convertObjectToJsonBytes(bankAccountDTO)))
            .andExpect(status().isOk());

        // Validate the BankAccount in the database
        List<BankAccount> bankAccountList = bankAccountRepository.findAll();
        assertThat(bankAccountList).hasSize(databaseSizeBeforeUpdate);
        BankAccount testBankAccount = bankAccountList.get(bankAccountList.size() - 1);
        assertThat(testBankAccount.getUserId()).isEqualTo(UPDATED_USER_ID);
        assertThat(testBankAccount.getBankName()).isEqualTo(UPDATED_BANK_NAME);
        assertThat(testBankAccount.getAccountNumber()).isEqualTo(UPDATED_ACCOUNT_NUMBER);
        assertThat(testBankAccount.getCurrentbalance()).isEqualTo(UPDATED_CURRENTBALANCE);
    }

    @Test
    public void updateNonExistingBankAccount() throws Exception {
        int databaseSizeBeforeUpdate = bankAccountRepository.findAll().size();

        // Create the BankAccount
        BankAccountDTO bankAccountDTO = bankAccountMapper.toDto(bankAccount);

        // If the entity doesn't have an ID, it will throw BadRequestAlertException
        restBankAccountMockMvc.perform(put("/api/bank-accounts")
            .contentType(TestUtil.APPLICATION_JSON_UTF8)
            .content(TestUtil.convertObjectToJsonBytes(bankAccountDTO)))
            .andExpect(status().isBadRequest());

        // Validate the BankAccount in the database
        List<BankAccount> bankAccountList = bankAccountRepository.findAll();
        assertThat(bankAccountList).hasSize(databaseSizeBeforeUpdate);
    }

    @Test
    public void deleteBankAccount() throws Exception {
        // Initialize the database
        bankAccount.setId(UUID.randomUUID());
        bankAccountRepository.save(bankAccount);

        int databaseSizeBeforeDelete = bankAccountRepository.findAll().size();

        // Delete the bankAccount
        restBankAccountMockMvc.perform(delete("/api/bank-accounts/{id}", bankAccount.getId())
            .accept(TestUtil.APPLICATION_JSON_UTF8))
            .andExpect(status().isOk());

        // Validate the database is empty
        List<BankAccount> bankAccountList = bankAccountRepository.findAll();
        assertThat(bankAccountList).hasSize(databaseSizeBeforeDelete - 1);
    }

    @Test
    public void equalsVerifier() throws Exception {
        TestUtil.equalsVerifier(BankAccount.class);
        BankAccount bankAccount1 = new BankAccount();
        bankAccount1.setId(UUID.randomUUID());
        BankAccount bankAccount2 = new BankAccount();
        bankAccount2.setId(bankAccount1.getId());
        assertThat(bankAccount1).isEqualTo(bankAccount2);
        bankAccount2.setId(UUID.randomUUID());
        assertThat(bankAccount1).isNotEqualTo(bankAccount2);
        bankAccount1.setId(null);
        assertThat(bankAccount1).isNotEqualTo(bankAccount2);
    }

    @Test
    public void dtoEqualsVerifier() throws Exception {
        TestUtil.equalsVerifier(BankAccountDTO.class);
        BankAccountDTO bankAccountDTO1 = new BankAccountDTO();
        bankAccountDTO1.setId(UUID.randomUUID());
        BankAccountDTO bankAccountDTO2 = new BankAccountDTO();
        assertThat(bankAccountDTO1).isNotEqualTo(bankAccountDTO2);
        bankAccountDTO2.setId(bankAccountDTO1.getId());
        assertThat(bankAccountDTO1).isEqualTo(bankAccountDTO2);
        bankAccountDTO2.setId(UUID.randomUUID());
        assertThat(bankAccountDTO1).isNotEqualTo(bankAccountDTO2);
        bankAccountDTO1.setId(null);
        assertThat(bankAccountDTO1).isNotEqualTo(bankAccountDTO2);
    }
}
