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
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import dal.api.banque.exceptions.StockException;
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

@ExtendWith(SpringExtension.class)
@SpringBootTest
@ActiveProfiles("test")
@AutoConfigureMockMvc
@TestInstance(Lifecycle.PER_CLASS)
class BanqueApplicationTests {

	@Autowired
	private AccountRepository accountRepository;
	@Autowired
	private AccountService accountService;
	@Autowired
	private BanqueRepository banqueRepository;
	@Autowired
	private BanqueService banqueService;
	@Autowired
	private QuotationRepository quotationRepository;
	@Autowired
	private QuotationService quotationService;
	@Autowired
	private StockService stockService;

	@BeforeAll
	void initData() {
		banqueService.createBanque();
		Account entry = new Account();
		entry.setId("1");
		entry.setName("test");
		entry.setPassword("test");
		entry.setMoney(1000);
		entry.setFee(16);
		entry.setStock(stockService.getStocks());
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
	void testTransformation() throws StockException {
		Account entry = accountService.getAccount("test");
		// check if have 10 products
		assertTrue(entry.getStock().size() == 10);
		Stock chaise = new Stock("chaise", 3, 100);
		// ajouter les ressources
		for (Stock ressource : stockService.getRulesForProduct(chaise.getType())) {
			ressource.setPrice(entry.getStock(ressource.getType()).getPrice());
			ressource.setQuantity(ressource.getQuantity() * chaise.getQuantity());
			entry.addStock(ressource);
		}
		accountService.transform(entry, chaise);
		// check balance didn't change
		assertTrue(entry.getMoney() == 1000);
		// check stock quantity changed
		for (Stock s : entry.getStock()) {
			if (s.getType().equals(chaise.getType())) {
				assertTrue(s.getQuantity() == 3);
			} else {
				for (Stock ressource : stockService.getRulesForProduct(chaise.getType())) {
					if (s.getType().equals(ressource.getType())) {
						assertTrue(s.getQuantity() == 0);
						break;
					}
				}
			}
		}
		// check if have 10 products
		assertTrue(entry.getStock().size() == 10);
	}

	@Test
	void testQuotationRefused() {
		// create a quotaion entry
		QuotationEntry quotationEntry = new QuotationEntry();
		Stock chaise = new Stock("chaise", 100, 100);
		quotationEntry.setCart(List.of(chaise));
		Quotation qot = quotationService.createQuotation(quotationEntry, "test",
				"test2");
		// check quotation status
		assertTrue(qot.getStatus().equals(Status.REFUSED));
		// check still have same money
		assertTrue(accountService.getAccount("test").getMoney() == 1000);
		assertTrue(accountService.getAccount("test2").getMoney() == 1000);
		// check quantity of products
		for (Stock s : accountService.getAccount("test").getStock()) {
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
		Quotation qot = quotationService.createQuotation(quotationEntry, "test",
				"test2");
		// check quotation status
		assertTrue(qot.getStatus().equals(Status.PENDING));
		// check still have same money
		assertTrue(accountService.getAccount("test").getMoney() == 1000);
		assertTrue(accountService.getAccount("test2").getMoney() == 1000);
		// check quantity of products
		for (Stock s : accountService.getAccount("test").getStock()) {
			assertTrue(s.getQuantity() == 0);
		}
	}

	@Test
	void testTransaction() {
		Quotation qot = quotationRepository.findAll().get(0);
		assertNotNull(qot);
		quotationService.validateQuotation(qot.getId());
		assertEquals(accountService.getAccount(qot.getSeller().getName()).getMoney(),
				1000 + qot.getHT());
		assertEquals(accountService.getAccount(qot.getBuyer().getName()).getMoney(),
				1000 - qot.getTTC());
		assertEquals(banqueService.getMyBanque().getCapital(), 1000 +
				qot.getTTC() - qot.getHT());

		for (Stock s : accountService.getAccount(qot.getBuyer().getName()).getStock()) {
			if (s.getType().equals("chaise")) {
				assertEquals(2, s.getQuantity());
			}
		}

	}

}
