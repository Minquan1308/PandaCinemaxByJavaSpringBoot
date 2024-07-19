package com.pandacinemax.Panda.Model;

import java.util.UUID;

public class SeatCartItem {
    private int seatId;
    private UUID movieId;
    private int roomId;
    private int screeningId;
    private String movieName;
    private String cinemaName;
    private String cinemaLocation;
    private String timeSlot;
    private String seatCode;
    private double price;
    private int quantity;

    // Constructors
    public SeatCartItem() {
        this.quantity = 1; // Default quantity
    }

    public SeatCartItem(int seatId, UUID movieId, int roomId, int screeningId, String movieName, String cinemaName, String cinemaLocation, String timeSlot, String seatCode, double price, int quantity) {
        this.seatId = seatId;
        this.movieId = movieId;
        this.roomId = roomId;
        this.screeningId = screeningId;
        this.movieName = movieName;
        this.cinemaName = cinemaName;
        this.cinemaLocation = cinemaLocation;
        this.timeSlot = timeSlot;
        this.seatCode = seatCode;
        this.price = price;
        this.quantity = quantity;
    }

    // Getters and Setters
    public int getSeatId() {
        return seatId;
    }

    public void setSeatId(int seatId) {
        this.seatId = seatId;
    }

    public UUID getMovieId() {
        return movieId;
    }

    public void setMovieId(UUID movieId) {
        this.movieId = movieId;
    }

    public int getRoomId() {
        return roomId;
    }

    public void setRoomId(int roomId) {
        this.roomId = roomId;
    }

    public int getScreeningId() {
        return screeningId;
    }

    public void setScreeningId(int screeningId) {
        this.screeningId = screeningId;
    }

    public String getMovieName() {
        return movieName;
    }

    public void setMovieName(String movieName) {
        this.movieName = movieName;
    }

    public String getCinemaName() {
        return cinemaName;
    }

    public void setCinemaName(String cinemaName) {
        this.cinemaName = cinemaName;
    }

    public String getCinemaLocation() {
        return cinemaLocation;
    }

    public void setCinemaLocation(String cinemaLocation) {
        this.cinemaLocation = cinemaLocation;
    }

    public String getTimeSlot() {
        return timeSlot;
    }

    public void setTimeSlot(String timeSlot) {
        this.timeSlot = timeSlot;
    }

    public String getSeatCode() {
        return seatCode;
    }

    public void setSeatCode(String seatCode) {
        this.seatCode = seatCode;
    }

    public double getPrice() {
        return price;
    }

    public void setPrice(double price) {
        this.price = price;
    }

    public int getQuantity() {
        return quantity;
    }

    public void setQuantity(int quantity) {
        this.quantity = quantity;
    }
}