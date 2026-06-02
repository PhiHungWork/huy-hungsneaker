package com.example.QuanLiThuChi.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.io.Serializable;
import java.math.BigDecimal;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class CartItem implements Serializable {
    private Long productId;
    private Long variantId;
    private String productName;
    private String size;
    private BigDecimal price;
    private int quantity;
    private String imageUrl;
    private int maxStock;
    
    public BigDecimal getLineTotal() {
        if (price == null) return BigDecimal.ZERO;
        return price.multiply(BigDecimal.valueOf(quantity));
    }
}
