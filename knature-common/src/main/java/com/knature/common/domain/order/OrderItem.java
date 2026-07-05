package com.knature.common.domain.order;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.knature.common.domain.BaseEntity;
import com.knature.common.domain.product.Product;
import com.knature.common.domain.product.ProductOption;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "order_items")
@Getter @Setter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class OrderItem extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "order_id", nullable = false)
    @JsonIgnoreProperties({"hibernateLazyInitializer", "handler", "items", "member"})
    private Order order;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "product_id")
    @JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
    private Product product;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "product_option_id")
    @JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
    private ProductOption productOption;

    @Column(nullable = false, length = 200)
    private String productName;

    @Column(length = 100)
    private String optionName;

    @Column(nullable = false)
    private Integer quantity;

    @Column(nullable = false)
    private Long price;

    @Column(nullable = false)
    private Long totalPrice;

    @Builder
    public OrderItem(Order order, Product product, ProductOption productOption,
                     String productName, String optionName, Integer quantity, Long price) {
        this.order = order;
        this.product = product;
        this.productOption = productOption;
        this.productName = productName;
        this.optionName = optionName;
        this.quantity = quantity;
        this.price = price;
        this.totalPrice = price * quantity;
    }
}
