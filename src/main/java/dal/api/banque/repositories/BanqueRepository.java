package dal.api.banque.repositories;

import org.springframework.data.mongodb.repository.MongoRepository;

import dal.api.banque.models.Banque;

public interface BanqueRepository extends MongoRepository<Banque, String> {

}
