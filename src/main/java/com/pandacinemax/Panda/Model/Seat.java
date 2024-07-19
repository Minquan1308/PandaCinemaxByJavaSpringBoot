package com.pandacinemax.Panda.Model;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;

@Setter
@Getter
@RequiredArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "seats")
public class Seat {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int id;

    @NotNull
    @Column(name = "seat_row") // Đổi tên cột row để tránh từ khóa SQL
    private String row;

    @NotNull
    private int number;

    @NotNull
    private String seatCode;

    @NotNull
    private boolean isAvailable;

    @NotNull
    @ManyToOne
    @JoinColumn(name = "screening_id")
    private Screening screening;

    @NotNull
    @ManyToOne
    @JoinColumn(name = "room_id")
    private Room room;

    public void setAvailable(boolean isAvailable) {
        this.isAvailable = isAvailable;
    }

    public void setScreeningId(int screeningId) {
        if (this.screening == null) {
            this.screening = new Screening();
        }
        this.screening.setId(screeningId);
    }

    public void setRoomId(int roomId) {
        if (this.room == null) {
            this.room = new Room();
        }
        this.room.setId(roomId);
    }
}
