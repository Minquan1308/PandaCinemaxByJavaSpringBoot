package com.pandacinemax.Panda.Model;



import jakarta.persistence.*;
import java.util.Date;
import java.util.List;
import java.util.UUID;

@Entity
@Table(name = "SeatOrder")
public class SeatOrder {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    @Column(name = "Id")
    private UUID id;

    @Column(name = "UserId")
    private Long userId;

    @Column(name = "OrderDate")
    private Date orderDate;

    @Column(name = "TotalPrice")
    private double totalPrice;

    @Column(name = "PaymentMethods")
    private String paymentMethods;

    @Column(name = "TotalQuantitySold")
    private int totalQuantitySold;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "UserId", referencedColumnName = "Id", insertable = false, updatable = false)
    private User user;

    @OneToMany(mappedBy = "seatOrder", cascade = CascadeType.ALL)
    private List<SeatOrderDetail> seatOrderDetails;

    // Constructors, getters, and setters

    public SeatOrder() {
        // Default constructor
    }

    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
    }

    public Long getUserId() {
        return userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }

    public Date getOrderDate() {
        return orderDate;
    }

    public void setOrderDate(Date orderDate) {
        this.orderDate = orderDate;
    }

    public double getTotalPrice() {
        return totalPrice;
    }

    public void setTotalPrice(double totalPrice) {
        this.totalPrice = totalPrice;
    }

    public String getPaymentMethods() {
        return paymentMethods;
    }

    public void setPaymentMethods(String paymentMethods) {
        this.paymentMethods = paymentMethods;
    }

    public int getTotalQuantitySold() {
        return totalQuantitySold;
    }

    public void setTotalQuantitySold(int totalQuantitySold) {
        this.totalQuantitySold = totalQuantitySold;
    }

    public User getUser() {
        return user;
    }

    public void setApplicationUser(User applicationUser) {
        this.user = applicationUser;
    }

    public List<SeatOrderDetail> getSeatOrderDetails() {
        return seatOrderDetails;
    }

    public void setSeatOrderDetails(List<SeatOrderDetail> seatOrderDetails) {
        this.seatOrderDetails = seatOrderDetails;
    }
}