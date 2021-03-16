package com.jaruiz.tektonpoc.services.products.api.rest;

import com.jaruiz.tektonpoc.services.products.api.rest.dto.ProductDTO;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
class ProductsRestControllerTest {

    @Autowired
    private ProductsRestController productsRestController;

    @Test
    void givenAValidLabelAndProductsAssociatedToThatLabel_whenProductsListIsRequested_thenProductsAssociatedAreRetrieved() {
        final List<String> labels = new ArrayList<>();
        labels.add("Label 1");
        final ResponseEntity<List<ProductDTO>> response = this.productsRestController.getProductsByLabels(labels);
        assertNotNull(response);
        assertNotNull(response.getBody());
        assertEquals(HttpStatus.OK, response.getStatusCode());

        final List<ProductDTO> productsRetrieved = response.getBody();
        assertTrue(productsRetrieved.size() == 1);

        final ProductDTO product = productsRetrieved.get(0);
        assertNotNull(product);
        assertNotNull(product.getName());
        assertTrue(product.getPrice() > 0.0);
    }

    @Test
    void givenSomeValidLabelsAndProductsAssociatedToThatLabel_whenProductsListIsRequested_thenProductsAssociatedAreRetrieved() {
        final List<String> labels = new ArrayList<>();
        labels.add("Label 1");
        labels.add("Label 2");
        final ResponseEntity<List<ProductDTO>> response = this.productsRestController.getProductsByLabels(labels);
        assertNotNull(response);
        assertNotNull(response.getBody());
        assertEquals(HttpStatus.OK, response.getStatusCode());

        final List<ProductDTO> productsRetrieved = response.getBody();
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
        final ResponseEntity<List<ProductDTO>> response = this.productsRestController.getProductsByLabels(labels);
        assertNotNull(response);
        assertNotNull(response.getBody());
        assertEquals(HttpStatus.OK, response.getStatusCode());

        final List<ProductDTO> productsRetrieved = response.getBody();
        assertTrue(productsRetrieved.size() == 0);
    }
}