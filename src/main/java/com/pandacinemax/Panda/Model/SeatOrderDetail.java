package com.pandacinemax.Panda.Model;

import jakarta.persistence.*;
import java.util.UUID;

@Entity
@Table(name = "SeatOrderDetail")
public class SeatOrderDetail {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    @Column(name = "Id")
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "SeatOrderId", referencedColumnName = "Id", nullable = false)
    private SeatOrder seatOrder;

    @Column(name = "MovieId")
    private UUID movieId;

    @Column(name = "RoomId")
    private int roomId;

    @Column(name = "ScreeningId")
    private int screeningId;

    @Column(name = "Quantity")
    private int quantity;

    @Column(name = "Price")
    private double price;

    @Column(name = "TimeSlot")
    private String timeSlot;

    @Column(name = "CinemaName")
    private String cinemaName;

    @Column(name = "CinemaLocation")
    private String cinemaLocation;

    @Column(name = "SeatCode")
    private String seatCode;

    @Column(name = "SeatId")
    private int seatId;

    // Constructors, getters, and setters
    public SeatOrderDetail(int screeningId, int seatId, UUID movieId, int quantity, String cinemaName, String timeSlot,
                           String cinemaLocation, String seatCode, double price, int roomId) {
        this.screeningId = screeningId;
        this.seatId = seatId;
        this.movieId = movieId;
        this.quantity = quantity;
        this.cinemaName = cinemaName;
        this.timeSlot = timeSlot;
        this.cinemaLocation = cinemaLocation;
        this.seatCode = seatCode;
        this.price = price;
        this.roomId = roomId;
    }

    public SeatOrderDetail() {
        // Default constructor
    }

    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
    }

    public SeatOrder getSeatOrder() {
        return seatOrder;
    }

    public void setSeatOrder(SeatOrder seatOrder) {
        this.seatOrder = seatOrder;
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

    public int getQuantity() {
        return quantity;
    }

    public void setQuantity(int quantity) {
        this.quantity = quantity;
    }

    public double getPrice() {
        return price;
    }

    public void setPrice(double price) {
        this.price = price;
    }

    public String getTimeSlot() {
        return timeSlot;
    }

    public void setTimeSlot(String timeSlot) {
        this.timeSlot = timeSlot;
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

    public String getSeatCode() {
        return seatCode;
    }

    public void setSeatCode(String seatCode) {
        this.seatCode = seatCode;
    }

    public int getSeatId() {
        return seatId;
    }

    public void setSeatId(int seatId) {
        this.seatId = seatId;
    }
}
