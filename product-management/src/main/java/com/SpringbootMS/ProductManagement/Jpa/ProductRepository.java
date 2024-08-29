package com.SpringbootMS.ProductManagement.Jpa;

import com.SpringbootMS.ProductManagement.Model.Product;
import org.springframework.data.mongodb.repository.MongoRepository;

public interface ProductRepository extends MongoRepository<Product, String> {
}
