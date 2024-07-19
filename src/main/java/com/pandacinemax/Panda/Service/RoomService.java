package com.pandacinemax.Panda.Service;

import com.pandacinemax.Panda.Model.Room;
import com.pandacinemax.Panda.Repository.RoomRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class RoomService {

    @Autowired
    private RoomRepository roomRepository;

    // Lấy danh sách tất cả các phòng
    public List<Room> getAllRooms() {
        return roomRepository.findAll();
    }

    // Lấy thông tin phòng theo ID
    public Room getRoomById(int id) {
        return roomRepository.findById(id).orElse(null);
    }

    // Thêm mới phòng
    public Room addRoom(Room room) {
        return roomRepository.save(room);
    }

    // Cập nhật thông tin phòng
    public Room updateRoom(Room room) {
        return roomRepository.save(room);
    }

    // Xóa phòng
    public void deleteRoomById(int id) {
        roomRepository.deleteById(id);
    }
    public List<Room> findByCinemaId(Long cinemaId) {
        return roomRepository.findByCinemaId(cinemaId);
    }
}
