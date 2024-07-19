package com.pandacinemax.Panda.Repository;

import com.pandacinemax.Panda.Model.Movie;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.UUID;

public interface MovieRepository extends JpaRepository<Movie, UUID> {
    default String getNameById(UUID id) {
        return findById(id).map(Movie::getName).orElse(null);
    }

    default String getPosterById(UUID id) {
        return findById(id).map(Movie::getImage).orElse(null);
    }
    @Query("SELECT m FROM Movie m WHERE LOWER(m.name) LIKE %:keyword% ")
    List<Movie> searchMovies( @Param("keyword") String keyword);

}
