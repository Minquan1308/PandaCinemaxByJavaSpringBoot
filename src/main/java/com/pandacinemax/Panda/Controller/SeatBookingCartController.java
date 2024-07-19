package com.pandacinemax.Panda.Controller;

import com.pandacinemax.Panda.Model.*;
import com.pandacinemax.Panda.Service.*;
import com.pandacinemax.Panda.config.VnpayConfig;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.io.UnsupportedEncodingException;
import java.text.DecimalFormat;
import java.time.LocalDateTime;
import java.util.Date;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Controller
@RequestMapping("/seatBookingCart")
public class SeatBookingCartController {

    private final SeatService seatService;
    private final UserService userService;
    private final MovieService movieService;
    private final RoomService roomService;
    private final CinemaService cinemaService;
    private final OrderService orderService;
    private final VnpayConfig vnpayConfig;

    @Autowired
    public SeatBookingCartController(SeatService seatService, UserService userService, MovieService movieService,
                                     RoomService roomService, CinemaService cinemaService, OrderService orderService,
                                     VnpayConfig vnpayConfig) {
        this.seatService = seatService;
        this.userService = userService;
        this.movieService = movieService;
        this.roomService = roomService;
        this.cinemaService = cinemaService;
        this.orderService = orderService;
        this.vnpayConfig = vnpayConfig;
    }

    @GetMapping("/addToCart")
    public String addToCart(@RequestParam("seatId") int seatId,
                            RedirectAttributes redirectAttributes,
                            HttpSession session) {

        Seat seat = seatService.getSeatById(seatId);

        if (seat == null || seat.getScreening() == null || seat.getScreening().getMovie() == null) {
            redirectAttributes.addFlashAttribute("errorMessage", "Không thể thêm vé vào giỏ hàng. Vui lòng thử lại sau.");
            return "redirect:/index";
        }

        // Retrieve room and cinema information
        String cinemaName = seat.getRoom().getCinema().getName();
        String cinemaLocation = seat.getRoom().getCinema().getLocation();
        String timeSlot = seat.getScreening().getTime() + " - " + seat.getScreening().getEndTime();
        String seatCode = seat.getSeatCode();
        double price = seat.getScreening().getMovie().getPrice();
        UUID movieId = seat.getScreening().getMovie().getId();
        int roomId = seat.getRoom().getId();
        int screeningId = seat.getScreening().getId();

        SeatCartItem cartItem = new SeatCartItem(seatId, movieId, roomId, screeningId,
                seat.getScreening().getMovie().getName(), cinemaName, cinemaLocation,
                timeSlot, seatCode, price, 1);

        SeatBookingCart cart = (SeatBookingCart) session.getAttribute("Cart");
        if (cart == null) {
            cart = new SeatBookingCart();
        }

        cart.addItem(cartItem);
        session.setAttribute("Cart", cart);

        return "redirect:/index";
    }

    @GetMapping("/index")
    public String index(@RequestParam("roomId") int roomId,
                        @RequestParam("screeningId") int screeningId,
                        @RequestParam("timeSlot") String timeSlot,
                        @RequestParam("movieId") UUID movieId,
                        Model model,
                        HttpSession session) {

        SeatBookingCart cart = (SeatBookingCart) session.getAttribute("Cart");

        // Logic for payment methods, user roles, etc., can be added here if needed
        model.addAttribute("paymentMethod", "VNPay");

        model.addAttribute("roomId", roomId);
        model.addAttribute("screeningId", screeningId);
        model.addAttribute("timeSlot", timeSlot);
        model.addAttribute("movieId", movieId);
        model.addAttribute("cart", cart);

        return "seatBookingCart/index"; // Replace with the appropriate view name
    }

    @PostMapping("/checkout")
    public String checkout(@ModelAttribute("order") SeatOrder order,
                           @RequestParam("method") String method,
                           HttpSession session,
                           RedirectAttributes redirectAttributes) {

        SeatBookingCart cart = (SeatBookingCart) session.getAttribute("Cart");
        if (cart == null || cart.getItems().isEmpty()) {
            redirectAttributes.addFlashAttribute("errorMessage", "Giỏ hàng của bạn đang trống. Hãy thêm vé xem phim vào giỏ hàng trước khi tiếp tục.");
            return "redirect:/seatBookingCart/index"; // Redirect to cart index or appropriate page
        }
//        cart.getItems().stream().map(i -> i.getSeatId());
//        Seat seat =
//


        User user = userService.getCurrentUser(); // Implement method to get current logged-in user
        order.setUserId(user.getId());
        order.setOrderDate(java.sql.Timestamp.valueOf(LocalDateTime.now()));
        order.setTotalPrice(cart.getItems().stream().mapToDouble(i -> i.getPrice() * i.getQuantity()).sum());
        order.setPaymentMethods(method.equals("VNPay") ? "VNPay" : "COD");

        // Set SeatOrderDetails and link them to the SeatOrder
        List<SeatOrderDetail> details = cart.getItems().stream().map(i -> {
            SeatOrderDetail detail = new SeatOrderDetail(
                    i.getScreeningId(), i.getSeatId(), i.getMovieId(), 1, i.getCinemaName(), i.getTimeSlot(),
                    i.getCinemaLocation(), i.getSeatCode(), i.getPrice(), i.getRoomId()
            );
            detail.setSeatOrder(order); // Set the SeatOrder to link the foreign key
            return detail;
        }).toList();

        order.setSeatOrderDetails(details);
        int totalQuantitySold = 0;
        for (SeatCartItem item : cart.getItems()) {
            totalQuantitySold += item.getQuantity();
        }
        order.setTotalQuantitySold(totalQuantitySold);
        orderService.saveOrder(order); // Implement method to save order

        List<Integer> seatIds = cart.getItems().stream().map(SeatCartItem::getSeatId).toList();
        List<Seat> seatsToUpdate = seatService.getSeatsByIds(seatIds);
        seatsToUpdate.forEach(seat -> seat.setAvailable(false));
        seatService.updateSeats(seatsToUpdate);

        if (method.equals("VNPay")) {
            DecimalFormat df = new DecimalFormat("#");
            String amount = df.format(order.getTotalPrice() * 100); // Amount in VND
            String orderId = order.getId().toString();
            String paymentUrl;
            try {
                paymentUrl = vnpayConfig.createPaymentUrl(orderId, amount);
            } catch (UnsupportedEncodingException e) {
                redirectAttributes.addFlashAttribute("errorMessage", "Lỗi khi tạo URL thanh toán VNPay.");
                return "redirect:/seatBookingCart/index";
            }

            session.setAttribute("OrderId", order.getId().toString());
            return "redirect:" + paymentUrl;
        }

        session.removeAttribute("Cart");
        boolean isCustomer = userService.isCurrentUserInRole("Customer"); // Implement method to check user role
        if (isCustomer) {
            return "redirect:/order/detail?orderId=" + order.getId();
        }
        redirectAttributes.addFlashAttribute("message", "Thanh toán bằng tiền mặt thành công.");
        return "redirect:/order/detail?orderId=" + order.getId();
    }


    @GetMapping("/removeFromCart")
    public String removeFromCart(@RequestParam("seatId") int seatId,
                                 @RequestParam("roomId") int roomId,
                                 @RequestParam("screeningId") int screeningId,
                                 @RequestParam("timeSlot") String timeSlot,
                                 @RequestParam("movieId") UUID movieId,
                                 HttpSession session) {

        SeatBookingCart cart = (SeatBookingCart) session.getAttribute("Cart");
        if (cart != null) {
            cart.removeItem(seatId);
            session.setAttribute("Cart", cart);
        }

        return "redirect:/seatBookingCart/index?roomId=" + roomId + "&screeningId=" + screeningId + "&timeSlot=" + timeSlot + "&movieId=" + movieId;
    }

    @GetMapping("/clearCart")
    public String clearCart(HttpSession session,
                            @RequestParam("movieId") UUID movieId) {

        session.removeAttribute("Cart");

        return "redirect:/screenings/User/show?movieId=" + movieId; // Redirect to screening index or appropriate page
    }



//    @GetMapping("/paymentCallback")
//    public String paymentCallback(@RequestParam("roomId") int roomId,
//                                  @RequestParam("screeningId") int screeningId,
//                                  @RequestParam("timeSlot") String timeSlot,
//                                  @RequestParam("movieId") UUID movieId,
//                                  HttpSession session,
//                                  RedirectAttributes redirectAttributes) {
//
//        // Assume payment response logic is implemented in vnpayConfig.handlePaymentResponse
//        PaymentResponse response = vnpayConfig.handlePaymentResponse(); // Implement this method
//
//        if (response == null || !"00".equals(response.getResponseCode())) {
//            redirectAttributes.addFlashAttribute("errorMessage", "Thanh toán thất bại. Vui lòng thử lại sau.");
//            return "redirect:/seatBookingCart/index?roomId=" + roomId + "&screeningId=" + screeningId + "&timeSlot=" + timeSlot + "&movieId=" + movieId;
//        }
//
//        UUID orderId = UUID.fromString((String) session.getAttribute("OrderId"));
//        session.removeAttribute("Cart");
//        session.removeAttribute("OrderId");
//
//        return "redirect:/seatBookingCart/orderCompleted?orderId=" + orderId;
//    }
    @GetMapping("/paymentCallback")
    public String paymentCallback(@RequestParam(name = "roomId", required = false) Integer roomId,
                                  @RequestParam(name = "screeningId", required = false) Integer screeningId,
                                  @RequestParam(name = "timeSlot", required = false) String timeSlot,
                                  @RequestParam(name = "movieId", required = false) UUID movieId,
                                  HttpSession session,
                                  RedirectAttributes redirectAttributes) {

        PaymentResponse response = vnpayConfig.handlePaymentResponse();

        if (response == null || !"00".equals(response.getResponseCode())) {
            redirectAttributes.addFlashAttribute("errorMessage", "Thanh toán thất bại. Vui lòng thử lại sau.");

            String orderIdString = (String) session.getAttribute("OrderId");
            if (orderIdString != null) {
                UUID orderId = UUID.fromString(orderIdString);
                Optional<SeatOrder> optionalOrder = orderService.findOrderById(orderId);

                if (optionalOrder.isPresent()) {
                    SeatOrder order = optionalOrder.get();

                    List<SeatOrderDetail> details = order.getSeatOrderDetails();
                    List<Integer> seatIds = details.stream().map(SeatOrderDetail::getSeatId).toList();
                    List<Seat> seatsToUpdate = seatService.getSeatsByIds(seatIds);
                    seatsToUpdate.forEach(seat -> seat.setAvailable(true));
                    seatService.updateSeats(seatsToUpdate);

                    if (details.isEmpty()) {
                        roomId = roomId != null ? roomId : 0;
                        screeningId = screeningId != null ? screeningId : 0;
                        timeSlot = timeSlot != null ? timeSlot : "";
                        movieId = movieId != null ? movieId : UUID.randomUUID();
                    } else {
                        roomId = details.get(0).getRoomId();
                        screeningId = details.get(0).getScreeningId();
                        timeSlot = details.get(0).getTimeSlot();
                        movieId = details.get(0).getMovieId();
                    }

                    orderService.deleteOrder(orderId);
                }

                session.removeAttribute("OrderId");
            }

            return "redirect:/seatBookingCart/index?roomId=" + roomId + "&screeningId=" + screeningId + "&timeSlot=" + timeSlot + "&movieId=" + movieId;
        }

        UUID orderId = UUID.fromString((String) session.getAttribute("OrderId"));
        session.removeAttribute("Cart");
        session.removeAttribute("OrderId");
        redirectAttributes.addFlashAttribute("message", "Thanh toán VNPay thành công.");
        return "redirect:/orderList/detail?orderId=" + orderId;
    }

}
