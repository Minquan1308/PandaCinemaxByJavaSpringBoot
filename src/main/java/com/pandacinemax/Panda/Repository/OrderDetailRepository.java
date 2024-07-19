package com.pandacinemax.Panda.Repository;



import com.pandacinemax.Panda.Model.SeatOrder;
import com.pandacinemax.Panda.Model.SeatOrderDetail;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import org.springframework.data.jpa.repository.Query;
import java.util.List;
import java.util.UUID;

@Repository
public interface OrderDetailRepository extends JpaRepository<SeatOrderDetail, Long> {
  //  List<SeatOrder> findByUserIdOrderByOrderDateDesc(Long userId);

    @Query("SELECT detail FROM SeatOrderDetail detail WHERE detail.screeningId = :screeningId")
    List<SeatOrderDetail> findSeatOrderDetailsByScreeningId(int screeningId);
    @Query("SELECT detail FROM SeatOrderDetail detail WHERE detail.movieId = :movieId")
    List<SeatOrderDetail> findSeatOrderDetailsByMovieId(UUID movieId);

}