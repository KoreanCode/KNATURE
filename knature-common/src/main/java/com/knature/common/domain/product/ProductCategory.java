package com.knature.common.domain.product;

import com.knature.common.domain.BaseEntity;
import jakarta.persistence.*;
import lombok.*;

import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "product_categories")
@Getter @Setter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class ProductCategory extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 100)
    private String name;

    @Column(nullable = false, unique = true, length = 100)
    private String slug;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "parent_id")
    private ProductCategory parent;

    @OneToMany(mappedBy = "parent", cascade = CascadeType.ALL)
    private List<ProductCategory> children = new ArrayList<>();

    @Column(nullable = false)
    private Integer sortOrder = 0;

    @Builder
    public ProductCategory(String name, String slug, ProductCategory parent, Integer sortOrder) {
        this.name = name;
        this.slug = slug;
        this.parent = parent;
        this.sortOrder = sortOrder != null ? sortOrder : 0;
    }
}
