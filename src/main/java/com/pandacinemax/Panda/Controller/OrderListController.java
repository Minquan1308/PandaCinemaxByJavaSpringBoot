package com.pandacinemax.Panda.Controller;

import com.pandacinemax.Panda.Model.Movie;
import com.pandacinemax.Panda.Model.SeatOrder;
import com.pandacinemax.Panda.Model.SeatOrderDetail;
import com.pandacinemax.Panda.Model.User;
import com.pandacinemax.Panda.Repository.CinemaRepository;
import com.pandacinemax.Panda.Repository.MovieRepository;
import com.pandacinemax.Panda.Repository.OrderRepository;
import com.pandacinemax.Panda.Service.MovieService;
import com.pandacinemax.Panda.Service.UserService;
import com.pandacinemax.Panda.ViewModel.OrderHistoryViewModel;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.*;
import java.util.stream.Collectors;

@Controller
@RequestMapping("/orderList")
public class OrderListController {

    private final UserService userService;
    private final OrderRepository orderRepository;
    private final MovieRepository movieRepository;
    private final CinemaRepository cinemaRepository;

    @Autowired
    private MovieService movieService;

    @Autowired
    public OrderListController(UserService userService, OrderRepository orderRepository,
                               MovieRepository movieRepository, CinemaRepository cinemaRepository) {
        this.userService = userService;
        this.orderRepository = orderRepository;
        this.movieRepository = movieRepository;
        this.cinemaRepository = cinemaRepository;
    }


//    @GetMapping("/index")
//    public String index(@AuthenticationPrincipal UserDetails userDetails, Model model) {
//        Optional<User> optionalUser = userService.findByUsername(userDetails.getUsername());
//        if (optionalUser.isEmpty()) {
//            return "not_found";
//        }
//
//        User user = optionalUser.get();
//        List<SeatOrder> seatOrders = orderRepository.findByUserIdOrderByOrderDateDesc(user.getId());
//        List<OrderHistoryViewModel> orderHistory = new ArrayList<>();
//
//        for (SeatOrder seatOrder : seatOrders) {
//            for (SeatOrderDetail detail : seatOrder.getSeatOrderDetails()) {
//                String cinemaName = detail.getCinemaName();
//                UUID movieId = detail.getMovieId();
//
//                orderHistory.add(new OrderHistoryViewModel(
//                        seatOrder.getId(),
//                        cinemaName,
//                        movieRepository.getNameById(movieId),
//                        seatOrder.getOrderDate()
//                ));
//            }
//        }
//
//        model.addAttribute("orderHistory", orderHistory);
//        return "orderList/index";
//    }

    @GetMapping("/index")
    public String index(@AuthenticationPrincipal UserDetails userDetails, Model model) {
        Optional<User> optionalUser = userService.findByUsername(userDetails.getUsername());
        if (optionalUser.isEmpty()) {
            return "not_found";
        }

        User user = optionalUser.get();
        List<SeatOrder> seatOrders = orderRepository.findByUserIdOrderByOrderDateDesc(user.getId());
        List<OrderHistoryViewModel> orderHistory = new ArrayList<>();

        // Tạo một danh sách tạm để lưu trữ các OrderHistoryViewModel đã được nhóm
        Map<String, OrderHistoryViewModel> tempMap = new HashMap<>();

        // Duyệt qua từng SeatOrder
        for (SeatOrder seatOrder : seatOrders) {
            for (SeatOrderDetail detail : seatOrder.getSeatOrderDetails()) {
                String key = detail.getCinemaName() + "-" + detail.getMovieId();
                UUID orderId = seatOrder.getId();
                String cinemaName = detail.getCinemaName();
                UUID movieId = detail.getMovieId();
                Date orderDate = seatOrder.getOrderDate();

                boolean found = false;
                for (OrderHistoryViewModel viewModel : orderHistory) {
                    if (viewModel.getCinemaName().equals(cinemaName)
                            && viewModel.getMovieName().equals(movieRepository.getNameById(movieId))
                            && viewModel.getOrderId().equals(orderId)) {
                        found = true;
                        break;
                    }
                }

                // Nếu chưa tồn tại, thêm vào orderHistory
                if (!found) {
                    orderHistory.add(new OrderHistoryViewModel(orderId, cinemaName,
                            movieRepository.getNameById(movieId), orderDate));
                }
            }
        }

        // Lấy danh sách OrderHistoryViewModel từ tempMap và thêm vào orderHistory
        orderHistory.addAll(tempMap.values());

        model.addAttribute("orderHistory", orderHistory);
        return "orderList/index";
    }



    @GetMapping("/detail")
    public String detail(@RequestParam("orderId") UUID orderId, Model model, @ModelAttribute("message") String message) {
        List<SeatOrderDetail> details = orderRepository.findSeatOrderDetailsByOrderId(orderId);
        if (details == null || details.isEmpty()) {
            return "not_found";
        }

        SeatOrder seatOrder = orderRepository.findById(orderId).orElse(null);
        if (seatOrder == null) {
            return "not_found";
        }

        UUID movieId = details.get(0).getMovieId();
        String movieName = movieRepository.getNameById(movieId);

        Movie movie = movieService.getMovieById(movieId)
                .orElseThrow(() -> new IllegalArgumentException("Invalid movie Id:" + movieId));
        model.addAttribute("movie", movie);
        model.addAttribute("soLuong", seatOrder.getTotalQuantitySold());
        model.addAttribute("tongTien", seatOrder.getTotalPrice());
        model.addAttribute("tenPhim", movieName);
        model.addAttribute("orderDate", seatOrder.getOrderDate().toString());
        model.addAttribute("roomNumber", details.get(0).getRoomId());
        model.addAttribute("cinemaName", details.get(0).getCinemaName());
        model.addAttribute("timeSlot", details.get(0).getTimeSlot());
        model.addAttribute("seatNumbers", details.stream().map(SeatOrderDetail::getSeatCode).collect(Collectors.joining(",")));
        model.addAttribute("details", details);
        if (message != null && !message.isEmpty()) {
            model.addAttribute("notificationMessage", message);
        }
        return "orderList/detail";
    }


}
