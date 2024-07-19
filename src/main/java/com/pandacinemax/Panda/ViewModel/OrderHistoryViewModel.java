package com.pandacinemax.Panda.ViewModel;

import java.util.Date;
import java.util.UUID;

public class OrderHistoryViewModel {

    private UUID orderId;
    private String cinemaName;
    private String movieName;
    private Date orderDate;

    public OrderHistoryViewModel(UUID orderId, String cinemaName, String movieName, Date orderDate) {
        this.orderId = orderId;
        this.cinemaName = cinemaName;
        this.movieName = movieName;
        this.orderDate = orderDate;
    }

    public UUID getOrderId() {
        return orderId;
    }

    public void setOrderId(UUID orderId) {
        this.orderId = orderId;
    }

    public String getCinemaName() {
        return cinemaName;
    }

    public void setCinemaName(String cinemaName) {
        this.cinemaName = cinemaName;
    }

    public String getMovieName() {
        return movieName;
    }

    public void setMovieName(String movieName) {
        this.movieName = movieName;
    }

    public Date getOrderDate() {
        return orderDate;
    }

    public void setOrderDate(Date orderDate) {
        this.orderDate = orderDate;
    }
}
