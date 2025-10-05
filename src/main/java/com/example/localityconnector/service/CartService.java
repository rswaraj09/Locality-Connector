package com.example.localityconnector.service;

import com.example.localityconnector.model.Cart;
import com.example.localityconnector.model.CartItem;
import com.example.localityconnector.model.Item;
import com.example.localityconnector.repository.CartRepository;
import com.example.localityconnector.repository.ItemRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class CartService {
    
    private final CartRepository cartRepository;
    private final ItemRepository itemRepository;
    
    public Cart getOrCreateCart(String userId, String userName, String userEmail) {
        Optional<Cart> existingCart = cartRepository.findByUserId(userId);
        
        if (existingCart.isPresent()) {
            Cart cart = existingCart.get();
            cart.calculateTotals();
            return cartRepository.save(cart);
        } else {
            Cart newCart = new Cart();
            newCart.setUserId(userId);
            newCart.setUserName(userName);
            newCart.setUserEmail(userEmail);
            newCart.setItems(new ArrayList<>());
            newCart.setTotalAmount(0.0);
            newCart.setTotalItems(0);
            newCart.prePersist();
            return cartRepository.save(newCart);
        }
    }
    
    public Cart addItemToCart(String userId, String itemId, Integer quantity) {
        // Get item details
        Item item = itemRepository.findById(itemId)
                .orElseThrow(() -> new RuntimeException("Item not found"));
        
        // Get or create cart
        Cart cart = getOrCreateCart(userId, "User", "user@example.com");
        
        // Check if item already exists in cart
        List<CartItem> items = cart.getItems();
        Optional<CartItem> existingItem = items.stream()
                .filter(cartItem -> cartItem.getItemId().equals(itemId))
                .findFirst();
        
        if (existingItem.isPresent()) {
            // Update quantity
            CartItem cartItem = existingItem.get();
            cartItem.setQuantity(cartItem.getQuantity() + quantity);
        } else {
            // Add new item
            CartItem cartItem = new CartItem();
            cartItem.setItemId(item.getId());
            cartItem.setItemName(item.getName());
            cartItem.setBusinessId(item.getBusinessId());
            cartItem.setBusinessName(item.getBusinessName());
            cartItem.setPrice(item.getPrice());
            cartItem.setQuantity(quantity);
            cartItem.setDescription(item.getDescription());
            items.add(cartItem);
        }
        
        cart.setItems(items);
        cart.calculateTotals();
        cart.prePersist();
        
        return cartRepository.save(cart);
    }
    
    public Cart removeItemFromCart(String userId, String itemId) {
        Cart cart = cartRepository.findByUserId(userId)
                .orElseThrow(() -> new RuntimeException("Cart not found"));
        
        List<CartItem> items = cart.getItems();
        items.removeIf(item -> item.getItemId().equals(itemId));
        
        cart.setItems(items);
        cart.calculateTotals();
        cart.prePersist();
        
        return cartRepository.save(cart);
    }
    
    public Cart updateItemQuantity(String userId, String itemId, Integer quantity) {
        Cart cart = cartRepository.findByUserId(userId)
                .orElseThrow(() -> new RuntimeException("Cart not found"));
        
        List<CartItem> items = cart.getItems();
        items.stream()
                .filter(item -> item.getItemId().equals(itemId))
                .findFirst()
                .ifPresent(item -> {
                    if (quantity <= 0) {
                        items.remove(item);
                    } else {
                        item.setQuantity(quantity);
                    }
                });
        
        cart.setItems(items);
        cart.calculateTotals();
        cart.prePersist();
        
        return cartRepository.save(cart);
    }
    
    public Cart getCart(String userId) {
        return cartRepository.findByUserId(userId)
                .orElseThrow(() -> new RuntimeException("Cart not found"));
    }
    
    public void clearCart(String userId) {
        cartRepository.deleteByUserId(userId);
    }
}



