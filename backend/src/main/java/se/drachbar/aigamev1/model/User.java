package se.drachbar.aigamev1.model;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

@Document(collection = "users")
public record User(@Id String id, String name, String email) {
}
