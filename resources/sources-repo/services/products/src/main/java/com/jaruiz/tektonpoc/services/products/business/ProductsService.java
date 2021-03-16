package com.jaruiz.tektonpoc.services.products.business;

import com.jaruiz.tektonpoc.services.products.business.ports.persistence.ProductsPersistenceService;
import com.jaruiz.tektonpoc.services.products.business.model.Product;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class ProductsService {
    private ProductsPersistenceService productsPersistenceService;

    public ProductsService(ProductsPersistenceService productsPersistenceService) {
        this.productsPersistenceService = productsPersistenceService;
    }

    public List<Product> getProducts(List<String> labels) {
        return this.productsPersistenceService.fetchByLabels(labels);
    }
}
