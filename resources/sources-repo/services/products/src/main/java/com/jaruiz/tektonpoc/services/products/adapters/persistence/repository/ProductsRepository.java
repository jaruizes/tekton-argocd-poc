package com.jaruiz.tektonpoc.services.products.adapters.persistence.repository;

import com.jaruiz.tektonpoc.services.products.adapters.persistence.entities.ProductEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface ProductsRepository extends JpaRepository<ProductEntity, Long> {

    @Query(
        value="SELECT p.* from products p where p.id in (select lp.product_id from labels l, labels_products lp where l.name in (:labels) and lp.label_id = l.id)",
        nativeQuery = true
    )
    List<ProductEntity> findByLabels(List<String> labels);

}
