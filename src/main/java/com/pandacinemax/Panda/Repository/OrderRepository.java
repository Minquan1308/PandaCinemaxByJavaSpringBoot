package com.pandacinemax.Panda.Repository;

import com.pandacinemax.Panda.Model.SeatOrder;
import com.pandacinemax.Panda.Model.SeatOrderDetail;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import org.springframework.data.jpa.repository.Query;
import java.util.List;
import java.util.UUID;

@Repository
public interface OrderRepository extends JpaRepository<SeatOrder, UUID> {
    List<SeatOrder> findByUserIdOrderByOrderDateDesc(Long userId);

    @Query("SELECT detail FROM SeatOrderDetail detail WHERE detail.seatOrder.id = :orderId")
    List<SeatOrderDetail> findSeatOrderDetailsByOrderId(UUID orderId);
}
