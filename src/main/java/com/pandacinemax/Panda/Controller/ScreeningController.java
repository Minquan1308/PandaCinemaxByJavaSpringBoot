package com.pandacinemax.Panda.Controller;

import com.pandacinemax.Panda.Model.*;
import com.pandacinemax.Panda.Repository.*;
import com.pandacinemax.Panda.Service.CinemaService;
import com.pandacinemax.Panda.Service.MovieService;
import com.pandacinemax.Panda.Service.RoomService;
import com.pandacinemax.Panda.Service.ScreeningService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import jakarta.validation.Valid;

import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

@Controller
@RequestMapping("/screenings")
public class ScreeningController {

    @Autowired
    private ScreeningService screeningService;
    @Autowired
    private MovieService movieService;
    @Autowired
    private CinemaService cinemaService;
    @Autowired
    private RoomService roomService;
    @Autowired
    private CinemaRepository cinemaRepository;
    @Autowired
    private MovieRepository movieRepository;
    @Autowired
    private ScreeningRepository screeningRepository;
    @Autowired
    private SeatRepository seatRepository;
    @Autowired
    private RoomRepository roomRepository;
    @Autowired
    private OrderDetailRepository detailRepository;

    @GetMapping("/cinemas/{cinemaId}/rooms")
    @ResponseBody
    public ResponseEntity<List<Room>> getRoomsByCinemaId(@PathVariable Long cinemaId) {
        List<Room> rooms = roomRepository.findByCinemaId(cinemaId);
        return ResponseEntity.ok().body(rooms);
    }

    @GetMapping
    public String showScreeningList(Model model) {
        List<Screening> screenings = screeningService.getAllScreenings();
        model.addAttribute("screenings", screenings);
        return "/screenings/screening-list";
    }

    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping("/add")
    public String showAddForm(Model model) {
        model.addAttribute("screening", new Screening());
        model.addAttribute("movies", movieService.getAllMovies());
        model.addAttribute("cinemas", cinemaService.getAllCinemas());
        model.addAttribute("rooms", roomService.getAllRooms());
        return "/screenings/add-screening";
    }

    @PreAuthorize("hasRole('ADMIN')")
    @PostMapping("/add")
    public String addScreening(@Valid Screening screening, BindingResult result, Model model) {
        if (result.hasErrors()) {
            model.addAttribute("screening", screening);
            model.addAttribute("movies", movieService.getAllMovies());
            model.addAttribute("cinemas", cinemaService.getAllCinemas());
            model.addAttribute("rooms", roomService.getAllRooms());
            return "/screenings/add-screening";
        }


        if (screening.getMovie().getStartDate().isAfter(screening.getDate())
            || screening.getDate().isAfter(screening.getMovie().getEndDate() ))
        {

            result.rejectValue("date", "error.screening", "Date error");
        }

        screening.setEndTime(calculateEndTime(screening.getTime(), screening.getMovie().getDurationMinutes()));

        List<Screening> screenings = screeningService.getAllScreenings().stream()
                .filter(s -> s.getDate().equals(screening.getDate()) )
                .collect(Collectors.toList());
        screenings.sort(Comparator.comparing(Screening::getEndTime).reversed());




        // return minutesDifference <= 30;
        for (Screening existingScreening : screenings) {
            if (existingScreening.getRoom().getId() == screening.getRoom().getId()) {
                LocalTime time1 = LocalTime.parse(screening.getTime()); // giờ chieu cua lich dang them
                LocalTime time2 = LocalTime.parse(existingScreening.getEndTime()); // giờ chiếu của lịch mới nhat trong cùng phòng
                long minutesDifference = Math.abs(ChronoUnit.MINUTES.between(time1, time2));
                if(minutesDifference<30&& time1.isBefore(time2))
                {
                    result.rejectValue("time", "error.screening", "The interval between showtimes must be greater than or equal to 30 minutes");
                }
                if (existingScreening.getTime().equals(screening.getTime())) {
                    // Thêm lỗi vào BindingResult nếu thời gian bắt đầu trùng lặp
                    result.rejectValue("time", "error.screening", "Start time conflicts with an existing screening in the same room.");
                }

            } else {
                if (existingScreening.getCinema().getId() == screening.getCinema().getId()) {
                    if (existingScreening.getMovie().getId() == screening.getMovie().getId() && existingScreening.getTime().equals(screening.getTime())) {
                        result.rejectValue("time", "error.screening", "Movie screenings are scheduled at this time ${screening.getTime()}");
                    }
                }
            }

        }

        // Kiểm tra nếu có lỗi trong BindingResult sau khi kiểm tra điều kiện
        if (result.hasErrors()) {
            model.addAttribute("screening", screening);
            model.addAttribute("movies", movieService.getAllMovies());
            model.addAttribute("cinemas", cinemaService.getAllCinemas());
            model.addAttribute("rooms", roomService.getAllRooms());
            return "/screenings/add-screening";
        }

        screeningService.addScreening(screening);
        return "redirect:/screenings";
    }


    @GetMapping("/calculateEndTime") //hàm tính tóan thời gian kết thúc
    public String calculateEndTime(@RequestParam String startTime, @RequestParam int duration) {
        // Parse startTime string to LocalTime
        LocalTime startTimeParsed = LocalTime.parse(startTime, DateTimeFormatter.ofPattern("HH:mm"));

        // Calculate endTime by adding duration in minutes to startTime
        LocalTime endTimeParsed = startTimeParsed.plusMinutes(duration);

        // Format endTime as HH:mm string
        String endTime = endTimeParsed.format(DateTimeFormatter.ofPattern("HH:mm"));

        return endTime;
    }

    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping("/edit/{id}")
    public String showEditForm(@PathVariable int id, Model model) {

        LocalDate date = LocalDate.now();
        Screening screening = screeningService.getScreeningById(id);
        if(screening.getDate().isBefore(date))
        {
            return "redirect:/screenings";
        }
        model.addAttribute("screening", screening);
        model.addAttribute("movies", movieService.getAllMovies());
        model.addAttribute("cinemas", cinemaService.getAllCinemas());
        model.addAttribute("rooms", roomService.getAllRooms());
        return "/screenings/update-screening";
    }

    @PreAuthorize("hasRole('ADMIN')")
    @PostMapping("/update/{id}")
    public String updateScreening(@PathVariable int id, @Valid Screening screening, BindingResult result, Model model) {
        List<SeatOrderDetail> list = detailRepository.findSeatOrderDetailsByScreeningId(id);
        if(list.isEmpty())
        {
            if (screening.getDate().isBefore(screening.getMovie().getStartDate())
                    || screening.getDate().isAfter(screening.getMovie().getEndDate())) {

                result.rejectValue("date", "error.screening", "Date error");
            }


            screening.setEndTime(calculateEndTime(screening.getTime(), screening.getMovie().getDurationMinutes()));

            List<Screening> screenings = screeningService.getAllScreenings().stream()
                    .filter(s -> s.getDate().equals(screening.getDate()) && s.getRoom().getId() == (screening.getRoom().getId()))
                    .collect(Collectors.toList());
            screenings.sort(Comparator.comparing(Screening::getEndTime).reversed());

            for (Screening existingScreening : screenings) {
                if (existingScreening.getRoom().getId() == screening.getRoom().getId()) {
                    LocalTime time1 = LocalTime.parse(screening.getTime()); // giờ chieu cua lich dang them
                    LocalTime time2 = LocalTime.parse(existingScreening.getEndTime()); // giờ chiếu của lịch mới nhat trong cùng phòng
                    long minutesDifference = Math.abs(ChronoUnit.MINUTES.between(time1, time2));
                    if(minutesDifference<30)
                    {
                        result.rejectValue("time", "error.screening", "The interval between showtimes must be greater than or equal to 30 minutes");
                    }
                    if (existingScreening.getTime().equals(screening.getTime())) {
                        // Thêm lỗi vào BindingResult nếu thời gian bắt đầu trùng lặp
                        result.rejectValue("time", "error.screening", "Start time conflicts with an existing screening in the same room.");
                    }

                } else {
                    if (existingScreening.getCinema().getId() == screening.getCinema().getId()) {
                        if (existingScreening.getMovie().getId() == screening.getMovie().getId() && existingScreening.getTime().equals(screening.getTime())) {
                            result.rejectValue("time", "error.screening", "Movie screenings are scheduled at this time ${screening.getTime()}");
                        }
                    }
                }

            }

            // Kiểm tra nếu có lỗi trong BindingResult sau khi kiểm tra điều kiện
            if (result.hasErrors()) {
                model.addAttribute("screening", screening);
                model.addAttribute("movies", movieService.getAllMovies());
                model.addAttribute("cinemas", cinemaService.getAllCinemas());
                model.addAttribute("rooms", roomService.getAllRooms());
                return "/screenings/update-screening";
            }
            screening.setEndTime(calculateEndTime(screening.getTime(), screening.getMovie().getDurationMinutes()));
            screeningService.updateScreening(screening);
            return "redirect:/screenings";

        }
        LocalDate date = LocalDate.now();
        Optional<Screening> scr = Optional.ofNullable(screeningService.getScreeningById(id));

            if( scr.get().getCinema().equals(screening.getCinema()) && scr.get().getMovie().equals(screening.getMovie()) && screening.getDate().isAfter(date))
            {
                screening.setEndTime(calculateEndTime(screening.getTime(), screening.getMovie().getDurationMinutes()));
                screeningService.updateScreening(screening);
                return "redirect:/screenings";
            }
            else
            {
                model.addAttribute("screening", screening);
                model.addAttribute("movies", movieService.getAllMovies());
                model.addAttribute("cinemas", cinemaService.getAllCinemas());
                model.addAttribute("rooms", roomService.getAllRooms());
                return "/screenings/update-screening";

            }







    }

    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping("/delete/{id}")
    public String deleteScreening(@PathVariable int id) {
        List<SeatOrderDetail> list = detailRepository.findSeatOrderDetailsByScreeningId(id);
        if(list.isEmpty())
        {
            screeningService.deleteScreeningById(id);
            return "redirect:/screenings";
        }

        return "redirect:/screenings";
    }

    @GetMapping("/{id}")
    public String viewScreening(@PathVariable int id, Model model) {
        Screening screening = screeningService.getScreeningById(id);
        model.addAttribute("screening", screening);
        return "/screenings/screening-detail";
    }

    @GetMapping("/User/search")
    public String showScreeningSearchUser(@RequestParam(value = "cinemaId", required = false) Long cinemaId,
                                          @RequestParam(value = "date", required = false) @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate date,
                                          Model model) {
        LocalDate currentDate = LocalDate.now();
        LocalTime currentTime = LocalTime.now();

        // Lấy danh sách các rạp chiếu
        List<Cinema> cinemas = cinemaRepository.findAll();

        // Nếu không có cinemaId, thiết lập giá trị mặc định là ID của rạp chiếu đầu tiên trong danh sách
        if (cinemaId == null && !cinemas.isEmpty()) {
            cinemaId = cinemas.get(0).getId();
        }

        // Nếu không có ngày được chọn, mặc định là ngày hiện tại
        LocalDate selectedDate = (date != null) ? date : currentDate;

        // Chuẩn bị danh sách các ngày từ ngày hiện tại đến 6 ngày sau
        List<LocalDate> dateList = IntStream.range(0, 7)
                .mapToObj(currentDate::plusDays)
                .collect(Collectors.toList());

        // Định dạng ngày và thứ trong danh sách ngày để hiển thị trên giao diện
        DateTimeFormatter dayFormatter = DateTimeFormatter.ofPattern("dd");
        DateTimeFormatter weekDayFormatter = DateTimeFormatter.ofPattern("E");
        List<Map<String, String>> formattedDateList = dateList.stream()
                .map(localDate -> {
                    Map<String, String> dateMap = new HashMap<>();
                    dateMap.put("fullDate", localDate.format(DateTimeFormatter.ofPattern("yyyy-MM-dd")));
                    dateMap.put("day", localDate.format(dayFormatter));
                    dateMap.put("weekDay", localDate.format(weekDayFormatter));
                    return dateMap;
                })
                .collect(Collectors.toList());

        // Truyền các danh sách ngày và ngày đã chọn vào model
        model.addAttribute("formattedDateList", formattedDateList);
        model.addAttribute("selectedDate", selectedDate.format(DateTimeFormatter.ofPattern("yyyy-MM-dd")));

        // Lấy danh sách các lịch chiếu cho rạp và ngày đã chọn
        List<Screening> screenings = screeningRepository.findByCinemaIdAndDate(cinemaId, selectedDate);

        // Kiểm tra nếu ngày đã chọn là ngày hiện tại, thì chỉ lấy các lịch chiếu có thời gian bắt đầu sau thời gian hiện tại
        if (selectedDate.equals(currentDate)) {
            screenings = screenings.stream()
                    .filter(screening -> {
                        LocalTime screeningTime = LocalTime.parse(screening.getTime(), DateTimeFormatter.ofPattern("HH:mm"));
                        return screeningTime.isAfter(currentTime);
                    })
                    .collect(Collectors.toList());
        }

        // Nhóm các lịch chiếu theo phim
        Map<String, List<Screening>> screeningsByMovie = screenings.stream()
                .collect(Collectors.groupingBy(
                        screening -> screening.getMovie().getName()
                ));

        // Truyền danh sách lịch chiếu đã nhóm theo phim vào model
        model.addAttribute("screeningsByMovie", screeningsByMovie);
        model.addAttribute("cinemaId", cinemaId);

        // Truyền danh sách các rạp chiếu vào model để hiển thị trong giao diện
        model.addAttribute("cinemas", cinemas);

        return "/screenings/User/search";
    }
@GetMapping("/User/show")
public String showScreeningUser(@RequestParam("movieId") UUID movieId,
                                @RequestParam(value = "date", required = false) @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate date,
                                Model model) {
    LocalDate currentDate = LocalDate.now();
    LocalTime currentTime = LocalTime.now();

    // Chuẩn bị danh sách các ngày từ ngày hiện tại đến 6 ngày sau
    List<LocalDate> dateList = IntStream.range(0, 7)
            .mapToObj(currentDate::plusDays)
            .collect(Collectors.toList());

    // Nếu không có ngày được chọn, mặc định là ngày hiện tại
    LocalDate selectedDate = (date != null) ? date : currentDate;

    // Định dạng ngày và thứ trong danh sách ngày để hiển thị trên giao diện
    DateTimeFormatter dayFormatter = DateTimeFormatter.ofPattern("dd");
    DateTimeFormatter weekDayFormatter = DateTimeFormatter.ofPattern("E");
    List<Map<String, String>> formattedDateList = dateList.stream()
            .map(localDate -> {
                Map<String, String> dateMap = new HashMap<>();
                dateMap.put("fullDate", localDate.format(DateTimeFormatter.ofPattern("yyyy-MM-dd")));
                dateMap.put("day", localDate.format(dayFormatter));
                dateMap.put("weekDay", localDate.format(weekDayFormatter));
                return dateMap;
            })
            .collect(Collectors.toList());

    // Truyền các danh sách ngày và ngày đã chọn vào model
    model.addAttribute("formattedDateList", formattedDateList);
    model.addAttribute("selectedDate", selectedDate.format(DateTimeFormatter.ofPattern("yyyy-MM-dd")));

    // Lấy danh sách các lịch chiếu cho phim và ngày đã chọn
    List<Screening> screenings = screeningRepository.findByMovieIdAndDate(movieId, selectedDate);
    if (selectedDate.equals(currentDate)) {
        screenings = screenings.stream()
                .filter(screening -> {
                    LocalTime screeningTime = LocalTime.parse(screening.getTime(), DateTimeFormatter.ofPattern("HH:mm"));
                    return screeningTime.isAfter(currentTime);
                })
                .collect(Collectors.toList());
    }




    // Nhóm các lịch chiếu theo rạp và ngày
    Map<String, Map<String, List<Screening>>> screeningsByCinema = screenings.stream()
            .collect(Collectors.groupingBy(
                    screening -> screening.getCinema().getCinemaName(),
                    Collectors.groupingBy(screening -> screening.getCinema().getLocation())
            ));

    // Truyền danh sách lịch chiếu đã nhóm theo rạp vào model
    model.addAttribute("screeningsByCinema", screeningsByCinema);
    model.addAttribute("movieId", movieId);

    // Lấy tên phim từ repository và truyền vào model
    String movieName = movieRepository.findById(movieId)
            .map(movie -> movie.getName())
            .orElse("Unknown Movie");
    model.addAttribute("movieName", movieName);

    return "/screenings/User/show";
}



}