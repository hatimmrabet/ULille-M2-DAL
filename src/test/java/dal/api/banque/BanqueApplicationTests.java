package dal.api.banque;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.api.TestInstance.Lifecycle;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.mongo.embedded.EmbeddedMongoAutoConfiguration;
import org.springframework.boot.test.autoconfigure.data.mongo.DataMongoTest;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.MongoDBContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import dal.api.banque.models.Account;
import dal.api.banque.models.Quotation;
import dal.api.banque.models.Status;
import dal.api.banque.models.Stock;
import dal.api.banque.models.entry.QuotationEntry;
import dal.api.banque.repositories.AccountRepository;
import dal.api.banque.repositories.BanqueRepository;
import dal.api.banque.repositories.QuotationRepository;
import dal.api.banque.services.AccountService;
import dal.api.banque.services.BanqueService;
import dal.api.banque.services.QuotationService;
import dal.api.banque.services.StockService;

@Testcontainers
@DataMongoTest(excludeAutoConfiguration = EmbeddedMongoAutoConfiguration.class)
@TestInstance(Lifecycle.PER_CLASS)
class BanqueApplicationTests {

	@Container
	static MongoDBContainer mongoDBContainer = new MongoDBContainer("mongo:6.0.2");

	@DynamicPropertySource
	static void setProperties(DynamicPropertyRegistry registry) {
		registry.add("spring.data.mongodb.uri", mongoDBContainer::getReplicaSetUrl);
	}
	
	@Autowired private AccountRepository accountRepository;
	@Autowired private AccountService accountService;
	@Autowired private BanqueRepository banqueRepository;
	@Autowired private BanqueService banqueService;
	@Autowired private QuotationRepository quotationRepository;
	@Autowired private QuotationService quotationService;
	@Autowired private StockService stockService;

	
	@BeforeAll
	void initData() {
		banqueService.createBanque();
		Account entry = new Account();
			entry.setId("1");
			entry.setName("test");
			entry.setPassword("test");
			entry.setBalance(1000);
			entry.setFee(16);
			entry.setStocks(stockService.getStocks());
		accountService.saveAccount(entry);
		entry.setId("2");
		entry.setName("test2");
		accountService.saveAccount(entry);
	}

	@AfterAll
	void cleanData() {
		banqueRepository.deleteAll();
		accountRepository.deleteAll();
		quotationRepository.deleteAll();
	}

	@Test
	void testTransformation() {
		Account entry = accountService.getAccount("test");
		// check if have 10 products
		assertTrue(entry.getStocks().size() == 10);
		Stock chaise = new Stock("chaise", 100, 100);
		accountService.transform(entry, chaise);
		//check balance didn't change
		assertTrue(entry.getBalance() == 1000);
		//check stock quantity changed
		for(Stock s : entry.getStocks()) {
			if(s.getName().equals(chaise.getName())) {
				assertTrue(s.getQuantity() == 100);
			}
			else 
			{
				for(Stock ressource : stockService.getRulesForProduct(chaise.getName())) {
					if(s.getName().equals(ressource.getName())) {
						assertTrue(s.getQuantity() == -ressource.getQuantity()*chaise.getQuantity());
						break;
					}
				}
			}
		}
		// check if have 10 products
		assertTrue(entry.getStocks().size() == 10);
	}

	@Test
	void testQuotationRefused() {
		// create a quotaion entry
		QuotationEntry quotationEntry = new QuotationEntry();
		Stock chaise = new Stock("chaise", 100, 100);
		quotationEntry.setCart(List.of(chaise));
		Quotation qot = quotationService.createQuotation(quotationEntry, "test", "test2");
		// check quotation status
		assertTrue(qot.getStatus().equals(Status.REFUSED));
		// check still have same money
		assertTrue(accountService.getAccount("test").getBalance() == 1000);
		assertTrue(accountService.getAccount("test2").getBalance() == 1000);
		// check quantity of products
		for( Stock s : accountService.getAccount("test").getStocks())
		{
			assertTrue(s.getQuantity() == 0);
		}
		// supress quotation
		quotationRepository.delete(qot);
	}

	@Test
	void testQuotationPending() {
		// create a quotaion entry
		QuotationEntry quotationEntry = new QuotationEntry();
		Stock chaise = new Stock("chaise", 2, 100000);
		quotationEntry.setCart(List.of(chaise));
		Quotation qot = quotationService.createQuotation(quotationEntry, "test", "test2");
		// check quotation status
		assertTrue(qot.getStatus().equals(Status.PENDING));
		// check still have same money
		assertTrue(accountService.getAccount("test").getBalance() == 1000);
		assertTrue(accountService.getAccount("test2").getBalance() == 1000);
		// check quantity of products
		for( Stock s : accountService.getAccount("test").getStocks())
		{
			assertTrue(s.getQuantity() == 0);
		}
	}

	@Test
	void testTransaction() {
		Quotation qot = quotationRepository.findAll().get(0);
		assertNotNull(qot);
		quotationService.validateQuotation(qot.getId());
		assertEquals(accountService.getAccount(qot.getSeller().getName()).getBalance(), 1000+qot.getTotalHT());
		assertEquals(accountService.getAccount(qot.getBuyer().getName()).getBalance(), 1000-qot.getTotalTTC());
		assertEquals(banqueService.getMyBanque().getCapital(), 1000 + qot.getTotalTTC() - qot.getTotalHT());
		
		for( Stock s : accountService.getAccount(qot.getBuyer().getName()).getStocks())
		{
			if(s.getName().equals("chaise")) {
				assertEquals(2, s.getQuantity());
			}
		}
		
	}


}
