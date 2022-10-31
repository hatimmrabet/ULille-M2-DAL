package dal.api.banque.repositories;

import org.springframework.data.mongodb.repository.MongoRepository;

import dal.api.banque.models.Account;

public interface AccountRepository extends MongoRepository<Account, String> {

}