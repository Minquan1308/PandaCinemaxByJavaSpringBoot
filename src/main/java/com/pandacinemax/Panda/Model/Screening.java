package com.pandacinemax.Panda.Model;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Setter
@Getter
@RequiredArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "screenings")
public class Screening {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int id;


    private LocalDate date;

    private String time;
    private String endTime;


    @NotNull
    @ManyToOne
    @JoinColumn(name = "room_id")
    private Room room;

    @NotNull
    @ManyToOne
    @JoinColumn(name = "movie_id")
    private Movie movie;

    @NotNull
    @ManyToOne
    @JoinColumn(name = "cinema_id")
    private Cinema cinema;

    // Getters and setters
    public UUID getMovieId() {
        return this.movie != null ? this.movie.getId() : null;
    }

    public Long getCinemaId() {
        return this.cinema != null ? this.cinema.getId() : null;
    }
    public void setCinemaId(Long cinemaId) {
        if (this.cinema == null) {
            this.cinema = new Cinema();
        }
        this.cinema.setId(cinemaId);
    }
    public String getCinemaName() {
        return this.cinema != null ? this.cinema.getName() : null;
    }

    public String getMovieName() {
        return this.movie != null ? this.movie.getName() : null;
    }
}
