package dal.api.banque.repositories;

import dal.api.banque.models.Quotation;
import org.springframework.data.mongodb.repository.MongoRepository;

public interface QuotationRepository extends MongoRepository<Quotation, String> {
    boolean existsById(String id);
}
