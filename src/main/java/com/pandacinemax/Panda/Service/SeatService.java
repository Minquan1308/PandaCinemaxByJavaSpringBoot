package com.pandacinemax.Panda.Service;

import com.pandacinemax.Panda.Model.Screening;
import com.pandacinemax.Panda.Model.Seat;
import com.pandacinemax.Panda.Repository.SeatRepository;
import jakarta.servlet.http.HttpSession;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
public class SeatService {

    @Autowired
    private SeatRepository seatRepository;
    @Autowired
    private HttpSession session;

    // Lấy danh sách tất cả các ghế
    public List<Seat> getAllSeats() {
        return seatRepository.findAll();
    }

    // Lấy thông tin ghế theo ID
    public Seat getSeatById(int id) {
        return seatRepository.findById(id).orElse(null);
    }

    // Thêm mới ghế
    public Seat addSeat(Seat seat) {
        return seatRepository.save(seat);
    }

    public List<Seat> addSeats(List<Seat> seats) {
        return seatRepository.saveAll(seats);
    }
    // Cập nhật thông tin ghế
    public Seat updateSeat(Seat seat) {
        return seatRepository.save(seat);
    }

    // Xóa ghế
    public void deleteSeatById(int id) {
        seatRepository.deleteById(id);
    }


    public List<Long> getSelectedSeatIdsFromSessionOrCart() {
        // Lấy danh sách ghế đã chọn từ session hoặc cart
        List<Long> selectedSeatIds = (List<Long>) session.getAttribute("selectedSeatIds");
        return selectedSeatIds != null ? selectedSeatIds : new ArrayList<>();
    }

    public void addToCart(Long seatId) {
        // Thêm ghế vào session hoặc cart
        List<Long> selectedSeatIds = getSelectedSeatIdsFromSessionOrCart();
        if (!selectedSeatIds.contains(seatId)) {
            selectedSeatIds.add(seatId);
            session.setAttribute("selectedSeatIds", selectedSeatIds);
        }
    }

    public void removeFromCart(Long seatId) {
        // Xóa ghế khỏi session hoặc cart
        List<Long> selectedSeatIds = getSelectedSeatIdsFromSessionOrCart();
        selectedSeatIds.remove(seatId);
        session.setAttribute("selectedSeatIds", selectedSeatIds);
    }

    public void clearCart() {
        // Xóa tất cả các ghế khỏi session hoặc cart
        session.removeAttribute("selectedSeatIds");
    }
    public List<Seat> getSeatsByIds(List<Integer> seatIds) {
        return seatRepository.findAllById(seatIds);
    }
    public void updateSeats(List<Seat> seats) {
        seatRepository.saveAll(seats);
    }

    @Transactional
    public List<Seat> generateSeatsForScreening(int screeningId, int rows, int cols, int roomId) {
        List<Seat> seats = new ArrayList<>();

        // Kiểm tra xem đã có ghế cho buổi chiếu này chưa
        boolean existingSeats = seatRepository.existsByScreeningId(screeningId);

        if (existingSeats) {
            return seatRepository.findByScreeningId(screeningId);
        }

        // Tạo ghế mới nếu chưa có ghế cho buổi chiếu này
        for (int i = 1; i <= rows; i++) {
            for (int j = 1; j <= cols; j++) {
                // Tạo seatCode
                String seatCode = String.format("%c%d", (char) ('A' + i - 1), j); // Ví dụ: A1, A2, B1, B2, ...

                // Tạo đối tượng ghế mới
                Seat seat = new Seat();
                seat.setSeatCode(seatCode);
                seat.setRow(String.valueOf(i));
                seat.setNumber(j);
                seat.setAvailable(true); // Mặc định là ghế trống khi mới tạo

                // Thiết lập buổi chiếu cho ghế
                Screening screening = new Screening();
                screening.setId(screeningId);
                seat.setScreening(screening);

                // Thiết lập phòng chiếu cho ghế
                seat.setRoomId(roomId);

                // Lưu ghế vào cơ sở dữ liệu
                seat = seatRepository.save(seat);

                // Thêm ghế vào danh sách kết quả
                seats.add(seat);
            }
        }

        return seats; // Trả về danh sách các ghế đã tạo
    }

//    public List<Seat> getSeatsByScreeningId(int screeningId) {
//        return seatRepository.findByScreeningId(screeningId);
//    }
}
