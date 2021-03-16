package com.jaruiz.tektonpoc.services.products.api.rest;

import com.jaruiz.tektonpoc.services.products.api.rest.dto.ProductDTO;
import com.jaruiz.tektonpoc.services.products.business.ProductsService;
import com.jaruiz.tektonpoc.services.products.business.model.Product;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.ArrayList;
import java.util.List;

@RestController
@RequestMapping("/api/products")
public class ProductsRestController {

    private final ProductsService productsService;

    public ProductsRestController(ProductsService productsService) {
        this.productsService = productsService;
    }

    @GetMapping
    public ResponseEntity<List<ProductDTO>> getProductsByLabels(@RequestParam List<String> labels) {
        final List<Product> productsRetrieved = this.productsService.getProducts(labels);
        final List<ProductDTO> productDTOS = new ArrayList<>(productsRetrieved.size());
        productsRetrieved.forEach(product -> {
            productDTOS.add(ProductDTO.builder()
                    .id(product.getId())
                    .name(product.getName())
                    .price(product.getPrice())
                    .build());
        });

        return ResponseEntity.ok(productDTOS);

    }
}
