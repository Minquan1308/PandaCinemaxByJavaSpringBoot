package com.pandacinemax.Panda.Repository;

import com.pandacinemax.Panda.Model.Screening;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

@Repository
public interface ScreeningRepository extends JpaRepository<Screening, Integer> {
    // Định nghĩa thêm các phương thức tùy chỉnh nếu cần thiết
    @Query("SELECT s FROM Screening s WHERE s.movie.id = :movieId")
    List<Screening> findByMovieId(UUID movieId);

    @Query("SELECT s FROM Screening s WHERE s.movie.id = :movieId AND s.date = :date")
    List<Screening> findByMovieIdAndDate(UUID movieId, LocalDate date);

    @Query("SELECT s FROM Screening s WHERE s.cinema.id = :cinemaId AND s.date = :date")
    List<Screening> findByCinemaIdAndDate(Long cinemaId, LocalDate date);
}
