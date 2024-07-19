package com.pandacinemax.Panda.Service;

import com.pandacinemax.Panda.Model.Cinema;
import com.pandacinemax.Panda.Model.Movie;
import com.pandacinemax.Panda.Model.Screening;
import com.pandacinemax.Panda.Repository.CinemaRepository;
import com.pandacinemax.Panda.Repository.MovieRepository;
import com.pandacinemax.Panda.Repository.ScreeningRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
public class ScreeningService {

    @Autowired
    private ScreeningRepository screeningRepository;

    @Autowired
    private MovieRepository movieRepository;

    @Autowired
    private CinemaRepository cinemaRepository;

    // Get all screenings
    public List<Screening> getAllScreenings() {
        return screeningRepository.findAll();
    }
    public List<Screening> findByCinemaIdAndDate(Long cinemaId, LocalDate date) {
        return screeningRepository.findByCinemaIdAndDate(cinemaId, date);
    }

    // Get screening by ID
    public Screening getScreeningById(int id) {
        Optional<Screening> optionalScreening = screeningRepository.findById(id);
        return optionalScreening.orElse(null);
    }

    // Find screenings by movie ID
    public List<Screening> findByMovieId(UUID movieId) {
        return screeningRepository.findByMovieId(movieId);
    }

    // Add new screening
    public Screening addScreening(Screening screening) {
        return screeningRepository.save(screening);
    }

    // Update screening
    public Screening updateScreening(Screening screening) {
        return screeningRepository.save(screening);
    }

    // Delete screening by ID
    public void deleteScreeningById(int id) {
        screeningRepository.deleteById(id);
    }

    // Find screenings by movie ID and date
    public List<Screening> findByMovieIdAndDate(UUID movieId, LocalDate date) {
        return screeningRepository.findByMovieIdAndDate(movieId, date);
    }

    // Get movie by ID
    public Movie getMovieById(UUID movieId) {
        return movieRepository.findById(movieId).orElse(null);
    }

    // Get cinema by ID
    public Cinema getCinemaById(Long cinemaId) {
        return cinemaRepository.findById(cinemaId).orElse(null);
    }
}
