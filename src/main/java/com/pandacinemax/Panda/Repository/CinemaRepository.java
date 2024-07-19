package com.pandacinemax.Panda.Repository;

import com.pandacinemax.Panda.Model.Cinema;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CinemaRepository extends JpaRepository<Cinema, Long> {
//    @Query("SELECT c FROM Cinema c JOIN c.movies m WHERE m.id = :movieId")
//    List<Cinema> findCinemasByMovieId(UUID movieId);
}
