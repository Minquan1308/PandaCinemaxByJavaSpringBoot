package com.pandacinemax.Panda.Service;


import com.pandacinemax.Panda.Model.Cinema;
import com.pandacinemax.Panda.Model.Room;
import com.pandacinemax.Panda.Repository.CinemaRepository;
import com.pandacinemax.Panda.Repository.RoomRepository;
import jakarta.validation.constraints.NotNull;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

/**
 * Service class for managing categories.
 */
@Service
@RequiredArgsConstructor
@Transactional
public class CinemaService {
    private final CinemaRepository cinemaRepository;
    private final RoomRepository roomRepository;
    /**
     * Retrieve all categories from the database.
     * @return a list of categories
     */
    public List<Cinema> getAllCinemas() {
        return cinemaRepository.findAll();
    }
    /**
     * Retrieve a category by its id.
     * @param id the id of the category to retrieve
     * @return an Optional containing the found category or empty if not found
     */
    public Optional<Cinema> getCinemaById(Long id) {
        return cinemaRepository.findById(id);
    }

    public List<Cinema> getCinemasByIds(List<Long> ids) {
        return cinemaRepository.findAllById(ids);
    }
    public void addCinema(Cinema cinema) {
        cinemaRepository.save(cinema);
    }
    /**
     * Update an existing category.
     * @param cinema the category with updated information
     */
    public void updateCinema(@NotNull Cinema cinema) {
        Cinema existingCinema = cinemaRepository.findById(cinema.getId())
                .orElseThrow(() -> new IllegalStateException("Cinema with ID " +
                        cinema.getId() + " does not exist."));
        existingCinema.setCinemaName(cinema.getCinemaName());
        existingCinema.setLocation(cinema.getLocation());

        cinemaRepository.save(existingCinema);
    }
    /**
     * Delete a category by its id.
     * @param id the id of the category to delete
     */
    public void deleteCinemaById(Long id) {
        if (!cinemaRepository.existsById(id)) {
            throw new IllegalStateException("Cinema with ID " + id + " does not exist.");
        }
        cinemaRepository.deleteById(id);
    }

    public List<Room> getRoomsByCinemaId(Long cinemaId) {
        return roomRepository.findByCinemaId(cinemaId); // Assuming roomRepository has method to find rooms by cinemaId
    }
}

