package com.pandacinemax.Panda.Service;

import com.pandacinemax.Panda.Model.Movie;
import com.pandacinemax.Panda.Repository.MovieRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
public class MovieService {

    @Autowired
    private MovieRepository movieRepository;

    public List<Movie> getAllMovies() {
        return movieRepository.findAll();
    }

    public Optional<Movie> getMovieById(UUID id) {
        return movieRepository.findById(id);
    }

    public Movie addMovie(Movie movie) {
        return movieRepository.save(movie);
    }

    public Movie updateMovie(UUID id, Movie movieDetails) {
        Movie movie = movieRepository.findById(id).orElseThrow(() -> new RuntimeException("Movie not found"));
        movie.setName(movieDetails.getName());
        movie.setStartDate(movieDetails.getStartDate());
        movie.setEndDate(movieDetails.getEndDate());
        movie.setImage(movieDetails.getImage());
        movie.setPrice(movieDetails.getPrice());
        movie.setDescription(movieDetails.getDescription());
        movie.setTrailer(movieDetails.getTrailer());
        movie.setDurationMinutes(movieDetails.getDurationMinutes());
        movie.setActors(movieDetails.getActors());
        movie.setProducer(movieDetails.getProducer());
        movie.setCategory(movieDetails.getCategory());
        return movieRepository.save(movie);
    }

    public void deleteMovie(UUID id) {
        Movie movie = movieRepository.findById(id).orElseThrow(() -> new RuntimeException("Movie not found"));
        movieRepository.delete(movie);
    }

}
