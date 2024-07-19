package com.pandacinemax.Panda.Repository;

import com.pandacinemax.Panda.Model.Screening;
import com.pandacinemax.Panda.Model.Seat;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface SeatRepository extends JpaRepository<Seat, Integer> {
    @Query("SELECT CASE WHEN COUNT(s) > 0 THEN true ELSE false END FROM Seat s WHERE s.screening.id = :screeningId")
    boolean existsByScreeningId(int screeningId);

    @Query("SELECT s FROM Seat s WHERE s.screening.id = :screeningId")
    List<Seat> findByScreeningId(int screeningId);
}
