package com.pandacinemax.Panda.Repository;

import com.pandacinemax.Panda.Model.Room;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface RoomRepository extends JpaRepository<Room, Integer> {
    List<Room> findByCinemaId(Long cinemaId);

    // Định nghĩa thêm các phương thức tùy chỉnh nếu cần thiết
}
