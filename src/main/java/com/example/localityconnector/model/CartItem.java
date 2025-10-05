package com.example.localityconnector.model;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CartItem {
    
    private String itemId;
    private String itemName;
    private String businessId;
    private String businessName;
    private Double price;
    private Integer quantity;
    private String description;
    
    public Double getTotalPrice() {
        return price * quantity;
    }
}



