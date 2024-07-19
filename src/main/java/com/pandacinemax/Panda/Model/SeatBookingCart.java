package com.pandacinemax.Panda.Model;

import java.util.ArrayList;
import java.util.List;

public class SeatBookingCart {
    private List<SeatCartItem> items;

    public SeatBookingCart() {
        this.items = new ArrayList<>();
    }

    // Getters and Setters
    public List<SeatCartItem> getItems() {
        return items;
    }

    public void setItems(List<SeatCartItem> items) {
        this.items = items;
    }

    // Methods to add and remove items
    public void addItem(SeatCartItem item) {
        SeatCartItem existingItem = items.stream()
                .filter(i -> i.getSeatId() == item.getSeatId())
                .findFirst()
                .orElse(null);
        if (existingItem == null) {
            items.add(item);
        }
    }

    public void removeItem(int seatId) {
        items.removeIf(i -> i.getSeatId() == seatId);
    }
}