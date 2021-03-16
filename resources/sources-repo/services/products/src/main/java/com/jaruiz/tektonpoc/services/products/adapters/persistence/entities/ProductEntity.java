package com.jaruiz.tektonpoc.services.products.adapters.persistence.entities;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.persistence.*;
import java.util.Set;

@Entity
@Getter
@NoArgsConstructor
@Table(name="products")
public class ProductEntity {

    @Id
    private long id;
    private String name;
    private double price;

    @ManyToMany(cascade = CascadeType.ALL)
    @JoinTable(
            name = "labels_products",
            joinColumns = { @JoinColumn(name = "product_id") },
            inverseJoinColumns = { @JoinColumn(name = "label_id") }
    )
    private Set<LabelEntity> labels;
}
