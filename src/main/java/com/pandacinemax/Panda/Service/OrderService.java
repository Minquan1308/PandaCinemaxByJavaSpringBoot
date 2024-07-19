package com.pandacinemax.Panda.Service;

import com.pandacinemax.Panda.Model.SeatOrder;
import com.pandacinemax.Panda.Repository.OrderRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Optional;
import java.util.UUID;

@Service
public class OrderService {
    @Autowired
    private OrderRepository orderRepository;

    public void saveOrder(SeatOrder order) {
        orderRepository.save(order);
    }
    public Optional<SeatOrder> findOrderById(UUID orderId) {
        return orderRepository.findById(orderId);
    }

    public void deleteOrder(UUID orderId) {
        orderRepository.deleteById(orderId);
    }

}
