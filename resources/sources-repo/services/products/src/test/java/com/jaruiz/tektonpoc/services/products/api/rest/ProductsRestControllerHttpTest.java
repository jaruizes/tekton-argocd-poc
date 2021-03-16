package com.jaruiz.tektonpoc.services.products.api.rest;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.jaruiz.tektonpoc.services.products.business.ProductsService;
import com.jaruiz.tektonpoc.services.products.business.model.Product;
import com.jaruiz.tektonpoc.services.products.api.rest.dto.ProductDTO;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(ProductsRestController.class)
class ProductsRestControllerHttpTest {
    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private ProductsService productsService;

    @Test
    void givenAValidLabelAndProductsAssociatedToThatLabel_whenProductsListIsRequested_thenProductsAssociatedAreRetrieved() throws Exception {
        final List<String> labels = new ArrayList<>();
        labels.add("Label 1");
        when(productsService.getProducts(labels)).thenReturn(buildProductsFake(1));

        MvcResult result = this.mockMvc.perform(get("/api/products?labels=Label 1"))
                .andExpect(status().isOk())
                .andReturn();


        ObjectMapper mapper = new ObjectMapper();
        final List<ProductDTO> productsRetrieved = mapper.readValue(result.getResponse().getContentAsString(StandardCharsets.UTF_8), new TypeReference<List<ProductDTO>>() {});

        assertTrue(productsRetrieved.size() == 1);

        final ProductDTO product = productsRetrieved.get(0);
        assertNotNull(product);
        assertNotNull(product.getName());
        assertTrue(product.getPrice() > 0.0);
    }

    @Test
    void givenSomeValidLabelsAndProductsAssociatedToThatLabel_whenProductsListIsRequested_thenProductsAssociatedAreRetrieved() throws Exception {
        final List<String> labels = new ArrayList<>();
        labels.add("Label 1");
        labels.add("Label 2");
        when(productsService.getProducts(labels)).thenReturn(buildProductsFake(2));
        MvcResult result = this.mockMvc.perform(get("/api/products?labels=Label 1,Label 2"))
                .andExpect(status().isOk())
                .andReturn();


        ObjectMapper mapper = new ObjectMapper();
        final List<ProductDTO> productsRetrieved = mapper.readValue(result.getResponse().getContentAsString(), new TypeReference<List<ProductDTO>>() {});

        assertTrue(productsRetrieved.size() == 2);

        productsRetrieved.forEach(product -> {
            assertNotNull(product.getName());
            assertTrue(product.getPrice() > 0.0);
        });
    }

    @Test
    void givenALabelWithNoProductsAssociated_whenProductsListIsRequested_thenAnEmptyArrayIsRetrieved() throws Exception {
        List<String> labels = new ArrayList<>();
        labels.add("Not Valid");
        when(productsService.getProducts(labels)).thenReturn(buildProductsFake(0));
        MvcResult result = this.mockMvc.perform(get("/api/products?labels=Label 1"))
                .andExpect(status().isOk())
                .andReturn();


        ObjectMapper mapper = new ObjectMapper();
        final List<ProductDTO> productsRetrieved = mapper.readValue(result.getResponse().getContentAsString(), new TypeReference<List<ProductDTO>>() {});

        assertTrue(productsRetrieved.size() == 0);
    }

    private List<Product> buildProductsFake(int size) {
        final List<Product> productsFake = new ArrayList<>(size);
        for (int i=0; i<size; i++) {
            productsFake.add(Product.builder()
                    .id(i)
                    .name("Product " + i)
                    .price(20.00)
                    .build());
        }

        return productsFake;
    }
}