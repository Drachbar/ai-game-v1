package se.drachbar.aigamev1.repository;

import org.springframework.data.mongodb.repository.ReactiveMongoRepository;
import reactor.core.publisher.Flux;
import se.drachbar.aigamev1.model.User;

public interface UserRepository extends ReactiveMongoRepository<User, String> {
    Flux<User> findByName(String name);
}
