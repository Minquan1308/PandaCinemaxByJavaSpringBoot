package com.pandacinemax.Panda.Controller;

import com.pandacinemax.Panda.Model.DetailWithMovieInfoModel;
import com.pandacinemax.Panda.Model.Movie;
import com.pandacinemax.Panda.Model.SeatOrder;
import com.pandacinemax.Panda.Model.SeatOrderDetail;
import com.pandacinemax.Panda.Repository.MovieRepository;
import com.pandacinemax.Panda.Service.MovieService;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Controller
@RequestMapping("/admin")
public class AdminController {

    @PersistenceContext
    private EntityManager entityManager;

    @Autowired
    private MovieRepository movieRepository;

    @Autowired
    private MovieService movieService;

    @GetMapping("/dailyRevenue")
    public String getDailyRevenue(Model model) {
        List<String> cinemas = entityManager.createQuery("SELECT c.cinemaName FROM Cinema c", String.class).getResultList();

        // Add "All" to cinemas list if not already present
        if (!cinemas.contains("All")) {
            cinemas.add(0, "All");
        }

        model.addAttribute("cinemas", cinemas);

        // Generate dateList
        LocalDate startDate = LocalDate.of(2024, 6, 20); // Replace with your specific start date
        LocalDate currentDate = LocalDate.now();
        List<String[]> dateList = new ArrayList<>();
        DateTimeFormatter isoFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
        DateTimeFormatter displayFormatter = DateTimeFormatter.ofPattern("dd/MM/yyyy");
        while (!startDate.isAfter(currentDate)) {
            dateList.add(new String[]{startDate.format(isoFormatter), startDate.format(displayFormatter)});
            startDate = startDate.plusDays(1);
        }
        model.addAttribute("dateList", dateList);

        LocalDate date = LocalDate.now();
        String firstCinema = cinemas.isEmpty() ? "All" : cinemas.get(0);
        return dailyRevenue(date, firstCinema, model);
    }


    @PostMapping("/dailyRevenue")
    public String postDailyRevenue(@RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date,
                                   @RequestParam String location, Model model) {
        // Regenerate dateList and cinemas for the post request
        List<String> cinemas = entityManager.createQuery("SELECT c.cinemaName FROM Cinema c", String.class).getResultList();

        // Add "All" to cinemas list if not already present
        if (!cinemas.contains("All")) {
            cinemas.add(0, "All");
        }

        model.addAttribute("cinemas", cinemas);

        LocalDate startDate = LocalDate.of(2024, 6, 20); // Replace with your specific start date
        LocalDate currentDate = LocalDate.now();
        List<String[]> dateList = new ArrayList<>();
        DateTimeFormatter isoFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
        DateTimeFormatter displayFormatter = DateTimeFormatter.ofPattern("dd/MM/yyyy");
        while (!startDate.isAfter(currentDate)) {
            dateList.add(new String[]{startDate.format(isoFormatter), startDate.format(displayFormatter)});
            startDate = startDate.plusDays(1);
        }
        model.addAttribute("dateList", dateList);

        return dailyRevenue(date, location, model);
    }


    private String dailyRevenue(LocalDate date, String location, Model model) {
        model.addAttribute("selectedCinema", location);
        model.addAttribute("selectedDate", date.toString());

        // Convert LocalDate to java.util.Date
        Date utilDate = Date.from(date.atStartOfDay(ZoneId.systemDefault()).toInstant());
        Date utilStartOfDay = Date.from(date.atStartOfDay(ZoneId.systemDefault()).toInstant());
        Date utilEndOfDay = Date.from(date.atStartOfDay(ZoneId.systemDefault()).plusDays(1).minusSeconds(1).toInstant());

        // Calculate end of month
        LocalDate endOfMonth = date.withDayOfMonth(date.lengthOfMonth());
        Date utilEndOfMonth = Date.from(endOfMonth.atStartOfDay(ZoneId.systemDefault()).plusDays(1).minusSeconds(1).toInstant());

        List<SeatOrderDetail> orderDetails;
        BigDecimal totalDailyRevenue;
        BigDecimal monthlyRevenueUpToSelectedDate;
        BigDecimal totalMonthlyRevenue;
        List<SeatOrder> seatOrders;

        if (location.equals("All")) {
            seatOrders = entityManager.createQuery(
                            "SELECT so FROM SeatOrder so JOIN FETCH so.seatOrderDetails sod WHERE so.orderDate >= :startOfDay AND so.orderDate <= :endOfDay",
                            SeatOrder.class)
                    .setParameter("startOfDay", utilStartOfDay)
                    .setParameter("endOfDay", utilEndOfDay)
                    .getResultList();

            orderDetails = seatOrders.stream()
                    .flatMap(so -> so.getSeatOrderDetails().stream())
                    .collect(Collectors.toList());

            totalDailyRevenue = entityManager.createQuery(
                            "SELECT CAST(SUM(sod.quantity * sod.price) AS java.math.BigDecimal) FROM SeatOrderDetail sod WHERE sod.seatOrder.orderDate >= :startOfDay AND sod.seatOrder.orderDate <= :endOfDay",
                            BigDecimal.class)
                    .setParameter("startOfDay", utilStartOfDay)
                    .setParameter("endOfDay", utilEndOfDay)
                    .getSingleResult();

            monthlyRevenueUpToSelectedDate = entityManager.createQuery("SELECT CAST(SUM(sod.quantity * sod.price) AS java.math.BigDecimal) FROM SeatOrderDetail sod WHERE sod.seatOrder.orderDate >= :startOfMonth AND sod.seatOrder.orderDate <= :endOfDay",
                            BigDecimal.class)
                    .setParameter("startOfMonth", Date.from(date.withDayOfMonth(1).atStartOfDay(ZoneId.systemDefault()).toInstant()))
                    .setParameter("endOfDay", utilEndOfDay)
                    .getSingleResult();

            totalMonthlyRevenue = entityManager.createQuery(
                            "SELECT CAST(SUM(sod.quantity * sod.price) AS java.math.BigDecimal) FROM SeatOrderDetail sod WHERE sod.seatOrder.orderDate >= :startOfMonth AND sod.seatOrder.orderDate <= :endOfMonth",
                            BigDecimal.class)
                    .setParameter("startOfMonth", Date.from(date.withDayOfMonth(1).atStartOfDay(ZoneId.systemDefault()).toInstant()))
                    .setParameter("endOfMonth", utilEndOfMonth)
                    .getSingleResult();
        } else {
            seatOrders = entityManager.createQuery(
                            "SELECT so FROM SeatOrder so JOIN FETCH so.seatOrderDetails sod WHERE so.orderDate >= :startOfDay AND so.orderDate <= :endOfDay AND sod.cinemaName = :location",
                            SeatOrder.class)
                    .setParameter("startOfDay", utilStartOfDay)
                    .setParameter("endOfDay", utilEndOfDay)
                    .setParameter("location", location)
                    .getResultList();

            orderDetails = seatOrders.stream()
                    .flatMap(so -> so.getSeatOrderDetails().stream())
                    .collect(Collectors.toList());

            totalDailyRevenue = entityManager.createQuery(
                            "SELECT CAST(SUM(sod.quantity * sod.price) AS java.math.BigDecimal) FROM SeatOrderDetail sod WHERE sod.seatOrder.orderDate >= :startOfDay AND sod.seatOrder.orderDate <= :endOfDay AND sod.cinemaName = :location",
                            BigDecimal.class)
                    .setParameter("startOfDay", utilStartOfDay)
                    .setParameter("endOfDay", utilEndOfDay)
                    .setParameter("location", location)
                    .getSingleResult();

            monthlyRevenueUpToSelectedDate = entityManager.createQuery(
                            "SELECT CAST(SUM(sod.quantity * sod.price) AS java.math.BigDecimal) FROM SeatOrderDetail sod WHERE sod.seatOrder.orderDate >= :startOfMonth AND sod.seatOrder.orderDate <= :endOfDay AND sod.cinemaName = :location",
                            BigDecimal.class)
                    .setParameter("startOfMonth", Date.from(date.withDayOfMonth(1).atStartOfDay(ZoneId.systemDefault()).toInstant()))
                    .setParameter("endOfDay", utilEndOfDay)
                    .setParameter("location", location)
                    .getSingleResult();

            totalMonthlyRevenue = entityManager.createQuery("SELECT CAST(SUM(sod.quantity * sod.price) AS java.math.BigDecimal) FROM SeatOrderDetail sod WHERE sod.seatOrder.orderDate >= :startOfMonth AND sod.seatOrder.orderDate <= :endOfMonth AND sod.cinemaName = :location",
                            BigDecimal.class)
                    .setParameter("startOfMonth", Date.from(date.withDayOfMonth(1).atStartOfDay(ZoneId.systemDefault()).toInstant()))
                    .setParameter("endOfMonth", utilEndOfMonth)
                    .setParameter("location", location)
                    .getSingleResult();
        }

        BigDecimal total = orderDetails.stream()
                .map(detail -> BigDecimal.valueOf(detail.getQuantity()).multiply(BigDecimal.valueOf(detail.getPrice())))
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        model.addAttribute("total", total);
        model.addAttribute("totalDailyRevenue", totalDailyRevenue);
        model.addAttribute("monthlyRevenueUpToSelectedDate", monthlyRevenueUpToSelectedDate);
        model.addAttribute("totalMonthlyRevenue", totalMonthlyRevenue);

        List<DetailWithMovieInfoModel> detailsWithMovieInfo = seatOrders.stream()
                .map(so -> {
                    DetailWithMovieInfoModel model1 = new DetailWithMovieInfoModel();
                    model1.setOrderId(so.getId());
                    model1.setOrderDate(so.getOrderDate());
                    model1.setUserId(so.getUserId().toString());
                    model1.setPrice(BigDecimal.valueOf(so.getTotalPrice()));
                    model1.setMovieName(so.getSeatOrderDetails().stream()
                            .map(sod -> movieRepository.findById(sod.getMovieId())
                                    .orElse(null))
                            .filter(movie -> movie != null)
                            .map(movie -> movie.getName())
                            .distinct()
                            .collect(Collectors.joining(", ")));
                    model1.setDetailId(so.getSeatOrderDetails().isEmpty() ? null : so.getSeatOrderDetails().get(0).getId());
                    return model1;
                })
                .collect(Collectors.toList());

        model.addAttribute("detailsWithMovieInfo", detailsWithMovieInfo);

        return "admin/daily-revenue";
    }


    @GetMapping("/details/{orderId}")
    public String getDetails(@PathVariable("orderId") UUID orderId, Model model) {
        SeatOrder seatOrder = entityManager.find(SeatOrder.class, orderId);

        if (seatOrder == null || seatOrder.getSeatOrderDetails().isEmpty()) {
            return "error/not-found";
        }
        UUID movieId = seatOrder.getSeatOrderDetails().get(0).getMovieId();
        Movie movie = movieService.getMovieById(movieId)
                .orElseThrow(() -> new IllegalArgumentException("Invalid movie Id:" + movieId));

        model.addAttribute("movie", movie);

        model.addAttribute("soLuong", seatOrder.getTotalQuantitySold());
        model.addAttribute("tongTien", seatOrder.getTotalPrice());
        model.addAttribute("tenPhim", movieRepository.findById(seatOrder.getSeatOrderDetails().get(0).getMovieId())
                .orElse(null)
                .getName());



        model.addAttribute("orderDate", seatOrder.getOrderDate().toString());
        model.addAttribute("roomNumber", seatOrder.getSeatOrderDetails().get(0).getRoomId());
        model.addAttribute("cinemaName", seatOrder.getSeatOrderDetails().get(0).getCinemaName());
        model.addAttribute("seatNumbers", seatOrder.getSeatOrderDetails().stream()
                .map(SeatOrderDetail::getSeatCode) // Adjusted to getSeatCode
                .collect(Collectors.joining(", ")));

        model.addAttribute("details", seatOrder.getSeatOrderDetails());

        return "admin/details";
    }
}