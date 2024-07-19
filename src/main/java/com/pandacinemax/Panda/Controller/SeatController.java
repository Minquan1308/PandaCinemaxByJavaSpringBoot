package com.pandacinemax.Panda.Controller;
import com.pandacinemax.Panda.Model.*;
import com.pandacinemax.Panda.Repository.MovieRepository;
import com.pandacinemax.Panda.Repository.RoomRepository;
import com.pandacinemax.Panda.Repository.ScreeningRepository;
import com.pandacinemax.Panda.Repository.SeatRepository;
import com.pandacinemax.Panda.Service.SeatService;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import jakarta.validation.Valid;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

@Controller
@RequestMapping("/seat")
public class SeatController {


    private final SeatService seatService;
    private final MovieRepository movieRepository;
    private final RoomRepository roomRepository;
    private final ScreeningRepository screeningRepository;
    private final SeatRepository seatRepository;

    @Autowired
    public SeatController(SeatService seatService, MovieRepository movieRepository,
                          RoomRepository roomRepository, ScreeningRepository screeningRepository,
                          SeatRepository seatRepository) {
        this.seatService = seatService;
        this.movieRepository = movieRepository;
        this.roomRepository = roomRepository;
        this.screeningRepository = screeningRepository;
        this.seatRepository = seatRepository;
    }

//    @GetMapping("/choose")
//    public String chooseSeats(Model model,
//                              @RequestParam("roomId") int roomId,
//                              @RequestParam("screeningId") int screeningId,
//                              @RequestParam("timeSlot") String timeSlot,
//                              @RequestParam("movieId") UUID movieId,
//                              HttpSession session) {
//
//        if (screeningId == 0 || roomId == 0 || movieId == null) {
//            // Xử lý lỗi ở đây nếu cần thiết
//            return "redirect:/error";
//        }
//
//        Room room = roomRepository.findById(roomId).orElse(null);
//        if (room == null) {
//            // Xử lý lỗi ở đây nếu không tìm thấy phòng
//            return "redirect:/error";
//        }
//
//        int soHang = room.getSoHang();
//        int soCot = room.getSoCot();
//
//        List<Seat> seats = seatService.generateSeatsForScreening(screeningId, soHang, soCot, roomId);
//
//        // Lấy giỏ hàng từ session hoặc tạo mới nếu chưa có
//        SeatBookingCart cart = (SeatBookingCart) session.getAttribute("Cart");
//        if (cart == null) {
//            cart = new SeatBookingCart();
//            session.setAttribute("Cart", cart);
//        }
//
//        List<Integer> selectedSeatIds = cart.getItems().stream()
//                .map(SeatCartItem::getSeatId)
//                .toList();
//
//        Movie movie = movieRepository.findById(movieId).orElse(null);
//        if (movie == null) {
//            // Xử lý lỗi ở đây nếu không tìm thấy phim
//            return "redirect:/error";
//        }
//
//        model.addAttribute("row", soHang);
//        model.addAttribute("number", soCot);
//        model.addAttribute("seats", seats);
//        model.addAttribute("roomId", roomId);
//        model.addAttribute("screeningId", screeningId);
//        model.addAttribute("timeSlot", timeSlot);
//        model.addAttribute("movieName", movie.getName());
//        model.addAttribute("movieId", movieId);
//        model.addAttribute("selectedSeatIds", selectedSeatIds);
//        model.addAttribute("ticketPrice", movie.getPrice());
//
//        return "seat/choose";
//    }
@GetMapping("/choose")
public String chooseSeats(Model model,
                          @RequestParam("roomId") int roomId,
                          @RequestParam("screeningId") int screeningId,
                          @RequestParam("timeSlot") String timeSlot,
                          @RequestParam("movieId") UUID movieId,
                          HttpSession session) {

    // Các xử lý khác ở đây
            if (screeningId == 0 || roomId == 0 || movieId == null) {
            // Xử lý lỗi ở đây nếu cần thiết
            return "redirect:/error";
        }

    Room room = roomRepository.findById(roomId).orElse(null);
    if (room == null) {
        // Xử lý lỗi ở đây nếu không tìm thấy phòng
        return "redirect:/error";
    }

    int soHang = room.getSoHang();
    int soCot = room.getSoCot();

    List<Seat> seats = seatService.generateSeatsForScreening(screeningId, soHang, soCot, roomId);
        // Lấy giỏ hàng từ session hoặc tạo mới nếu chưa có
        SeatBookingCart cart = (SeatBookingCart) session.getAttribute("Cart");
        if (cart == null) {
            cart = new SeatBookingCart();
            session.setAttribute("Cart", cart);
        }
    // Tính toán seatCode và thêm vào từng Seat
    for (int i = 0; i < soHang; i++) {
        for (int j = 0; j < soCot; j++) {
            int seatIndex = i * soCot + j;
            Seat seat = seats.get(seatIndex);
            char rowChar = (char) ('A' + i); // Tính toán ký tự 'A' + số thứ tự hàng
            String seatCode = String.valueOf(rowChar) + (j + 1); // ví dụ: A1, A2, B1, B2, ...
            seat.setSeatCode(seatCode);
        }
    }

    // Các xử lý khác ở đây
            List<Integer> selectedSeatIds = cart.getItems().stream()
                .map(SeatCartItem::getSeatId)
                .toList();
            Movie movie = movieRepository.findById(movieId).orElse(null);
        if (movie == null) {
            // Xử lý lỗi ở đây nếu không tìm thấy phim
            return "redirect:/error";
        }

    model.addAttribute("row", soHang);
    model.addAttribute("number", soCot);
    model.addAttribute("seats", seats);
    model.addAttribute("roomId", roomId);
    model.addAttribute("screeningId", screeningId);
    model.addAttribute("timeSlot", timeSlot);
    model.addAttribute("movieName", movie.getName());
    model.addAttribute("movieId", movieId);
    model.addAttribute("selectedSeatIds", selectedSeatIds);
    model.addAttribute("ticketPrice", movie.getPrice());

    return "seat/choose";
}
    @PostMapping("/bookTicket")
    public String bookTicket(@RequestParam("roomId") int roomId,
                             @RequestParam("screeningId") int screeningId,
                             @RequestParam("timeSlot") String timeSlot,
                             @RequestParam("selectedSeatIds") List<String> selectedSeatIds,
                             @RequestParam("movieId") String movieId,
                             RedirectAttributes redirectAttributes) {

        Screening screening = screeningRepository.findById(screeningId).orElse(null);
        if (screening == null) {
            // Xử lý lỗi ở đây nếu không tìm thấy buổi chiếu
            return "redirect:/error";
        }

        double ticketPrice = screening.getMovie().getPrice();
        double totalPrice = ticketPrice * selectedSeatIds.size();

        // Chuyển hướng đến trang giỏ hàng
        redirectAttributes.addAttribute("roomId", roomId);
        redirectAttributes.addAttribute("screeningId", screeningId);
        redirectAttributes.addAttribute("timeSlot", timeSlot);
        redirectAttributes.addAttribute("selectedSeatIds", selectedSeatIds);
        redirectAttributes.addAttribute("movieId", movieId);

        return "redirect:/seatBookingCart/index";
    }

    @GetMapping("/ticketDetails")
    public String ticketDetails(Model model,
                                @RequestParam("screeningId") int screeningId,
                                @RequestParam("timeSlot") String timeSlot,
                                @RequestParam("selectedSeatIds") String selectedSeatIds,
                                @RequestParam("totalPrice") double totalPrice) {

        Screening screening = screeningRepository.findById(screeningId).orElse(null);
        if (screening == null) {
            // Xử lý lỗi ở đây nếu không tìm thấy buổi chiếu
            return "redirect:/error";
        }

        model.addAttribute("movieName", screening.getMovie().getName());
        model.addAttribute("timeSlot", timeSlot);
        model.addAttribute("cinemaName", screening.getRoom().getCinema().getName());
        model.addAttribute("cinemaLocation", screening.getRoom().getCinema().getLocation());

        List<String> selectedSeatIdList = new ArrayList<>();
        if (!selectedSeatIds.isEmpty()) {
            selectedSeatIdList = List.of(selectedSeatIds.split(","));
        }

        model.addAttribute("selectedSeatIds", selectedSeatIdList);
        model.addAttribute("totalPrice", totalPrice);

        return "ticket-details";
    }

    // Các phương thức khác nếu cần thiết
}