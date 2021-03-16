package com.jaruiz.tektonpoc.services.products.business;

import com.jaruiz.tektonpoc.services.products.business.model.Product;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

@SpringBootTest
class ProductsServiceTest {
    @Autowired
    private ProductsService productsService;

    @Test
    void givenAValidLabelAndProductsAssociatedToThatLabel_whenProductsListIsRequested_thenProductsAssociatedAreRetrieved() {
        final List<String> labels = new ArrayList<>();
        labels.add("Label 1");
        final List<Product> productsRetrieved = this.productsService.getProducts(labels);
        assertNotNull(productsRetrieved);
        assertTrue(productsRetrieved.size() == 1);

        final Product product = productsRetrieved.get(0);
        assertNotNull(product);
        assertNotNull(product.getName());
        assertTrue(product.getPrice() > 0.0);
    }

    @Test
    void givenSomeValidLabelsAndProductsAssociatedToThatLabel_whenProductsListIsRequested_thenProductsAssociatedAreRetrieved() {
        final List<String> labels = new ArrayList<>();
        labels.add("Label 1");
        labels.add("Label 2");
        final List<Product> productsRetrieved = this.productsService.getProducts(labels);
        assertNotNull(productsRetrieved);
        assertTrue(productsRetrieved.size() == 2);

        productsRetrieved.forEach(product -> {
            assertNotNull(product.getName());
            assertTrue(product.getPrice() > 0.0);
        });
    }

    @Test
    void givenALabelWithNoProductsAssociated_whenProductsListIsRequested_thenAnEmptyArrayIsRetrieved() {
        List<String> labels = new ArrayList<>();
        labels.add("Not Valid");
        List<Product> products = this.productsService.getProducts(labels);
        assertNotNull(products);
        assertTrue(products.size() == 0);
    }
}