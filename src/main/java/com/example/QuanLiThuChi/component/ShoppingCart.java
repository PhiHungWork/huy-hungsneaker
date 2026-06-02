package com.example.QuanLiThuChi.component;

import com.example.QuanLiThuChi.dto.CartItem;
import org.springframework.stereotype.Component;
import org.springframework.web.context.annotation.SessionScope;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.Map;

@Component("shoppingCart")
@SessionScope
public class ShoppingCart implements Serializable {
    private final Map<Long, CartItem> items = new LinkedHashMap<>();

    public void addItem(CartItem item) {
        if (items.containsKey(item.getVariantId())) {
            CartItem existing = items.get(item.getVariantId());
            int newQty = existing.getQuantity() + item.getQuantity();
            if (newQty > existing.getMaxStock()) {
                newQty = existing.getMaxStock();
            }
            existing.setQuantity(newQty);
        } else {
            items.put(item.getVariantId(), item);
        }
    }

    public void updateItem(Long variantId, int quantity) {
        CartItem item = items.get(variantId);
        if (item != null) {
            if (quantity <= 0) {
                items.remove(variantId);
            } else {
                if (quantity > item.getMaxStock()) {
                    quantity = item.getMaxStock();
                }
                item.setQuantity(quantity);
            }
        }
    }

    public void removeItem(Long variantId) {
        items.remove(variantId);
    }

    public void clear() {
        items.clear();
    }

    public Collection<CartItem> getItems() {
        return items.values();
    }

    public int getItemCount() {
        return items.values().stream().mapToInt(CartItem::getQuantity).sum();
    }

    public BigDecimal getTotalPrice() {
        return items.values().stream()
                .map(CartItem::getLineTotal)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    public boolean isEmpty() {
        return items.isEmpty();
    }
}
