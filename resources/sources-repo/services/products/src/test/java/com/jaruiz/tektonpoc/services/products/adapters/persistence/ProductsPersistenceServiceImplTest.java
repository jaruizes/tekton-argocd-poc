package com.jaruiz.tektonpoc.services.products.adapters.persistence;

import com.jaruiz.tektonpoc.services.products.business.model.Product;
import com.jaruiz.tektonpoc.services.products.business.ports.persistence.ProductsPersistenceService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

@SpringBootTest
@ExtendWith(SpringExtension.class)
class ProductsPersistenceServiceImplTest {
    @Autowired
    private ProductsPersistenceService productsPersistenceService;

    @Test
    void givenAValidLabelAndProductsAssociatedToThatLabel_whenProductsListIsRequested_thenProductsAssociatedAreRetrieved() {
        final List<String> labels = new ArrayList<>();
        labels.add("Label 1");
        final List<Product> products = productsPersistenceService.fetchByLabels(labels);
        assertNotNull(products);
        assertTrue(products.size() == 1);

        final Product product = products.get(0);
        assertNotNull(product);
        assertNotNull(product.getName());
        assertTrue(product.getPrice() > 0.0);

    }

    @Test
    void givenAValidLabelsAndProductsAssociatedToThoseLabels_whenProductsListIsRequested_thenProductsAssociatedAreRetrieved() {
        final List<String> labels = new ArrayList<>();
        labels.add("Label 1");
        labels.add("Label 2");
        final List<Product> products = productsPersistenceService.fetchByLabels(labels);
        assertNotNull(products);
        assertTrue(products.size() == 2);

        products.forEach(product -> {
            assertNotNull(product.getName());
            assertTrue(product.getPrice() > 0.0);
        });

    }

    @Test
    void givenALabelWithNoProductsAssociated_whenProductsListIsRequested_thenAnEmptyArrayIsRetrieved() {
        List<String> labels = new ArrayList<>();
        labels.add("Not Valid");
        List<Product> products = productsPersistenceService.fetchByLabels(labels);
        assertNotNull(products);
        assertTrue(products.size() == 0);
    }
}
