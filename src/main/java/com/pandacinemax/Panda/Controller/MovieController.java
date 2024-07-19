package com.pandacinemax.Panda.Controller;

import com.pandacinemax.Panda.Model.Movie;
import com.pandacinemax.Panda.Model.SeatOrderDetail;
import com.pandacinemax.Panda.Repository.MovieRepository;
import com.pandacinemax.Panda.Repository.OrderDetailRepository;
import com.pandacinemax.Panda.Service.CategoryService;
import com.pandacinemax.Panda.Service.MovieService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.repository.query.Param;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import jakarta.validation.Valid;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Controller
@RequestMapping("/movies")
public class MovieController {

    private static String UPLOAD_DIR = "src/main/resources/static/images/";

    @Autowired
    private MovieService movieService;
    @Autowired
    private MovieRepository movieRepository;

    @Autowired
    private CategoryService categoryService;
    @Autowired
    private OrderDetailRepository detailRepository;

    // Display a list of all movies

    @GetMapping
    public String showMovieList(Model model) {
        model.addAttribute("movies", movieService.getAllMovies());
        return "/movies/User/movie-list";
    }
    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping("/Admin/Index")
    public String showMovieListadmin(Model model) {
        model.addAttribute("movies", movieService.getAllMovies());
        return "/movies/Admin/list-movieadmin";
    }

    // For adding a new movie
    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping("/add")
    public String showAddForm(Model model) {
        model.addAttribute("movie", new Movie());
        model.addAttribute("categories", categoryService.getAllCategories());
        return "/movies/Admin/add-movie";
    }




        public String showSearchForm(Model model) {
            // Truyền danh sách các category vào để hiển thị trong dropdown
            model.addAttribute("categories", categoryService.getAllCategories() ); // Thay yourCategoryList bằng danh sách category thật của bạn
            return "layout_new"; // Tên của file thymeleaf template chứa form search
        }


    @GetMapping("/search")
    public String searchMovies(

            @RequestParam(name = "keyword", required = false, defaultValue = "") String keyword,
            Model model) {
        LocalDate date = LocalDate.now();

        // Xử lý logic tìm kiếm ở đây, ví dụ lấy danh sách phim theo categoryId và keyword
        List<Movie> movies = new ArrayList();
        List<Movie> movie = movieRepository.searchMovies(keyword);
        for (Movie item : movie)
        {
            if(item.getEndDate().equals(date) ||item.getEndDate().isAfter(date))
            {
                movies.add(item);

            }
        }

        // Truyền danh sách phim tìm được vào model để hiển thị
        model.addAttribute("movies", movies);

        // Truyền lại danh sách category để hiển thị lại form search
        model.addAttribute("categories", categoryService); // Thay yourCategoryList bằng danh sách category thật của bạn

        return "movies/User/search-result"; // Tên của file thymeleaf template chứa kết quả tìm kiếm
    }

    @PreAuthorize("hasRole('ADMIN')")
    @PostMapping("/add")
    public String addMovie(@Valid Movie movie, BindingResult result, @RequestParam("imageFile") MultipartFile imageFile, Model model) {
        if (result.hasErrors()) {
            model.addAttribute("categories", categoryService.getAllCategories());
            return "/movies/Admin/add-movie";
        }
        long daysDifference = ChronoUnit.DAYS.between(movie.getStartDate(), movie.getEndDate());
        if (daysDifference < 10) {
            // model.addAttribute("dateError", "The difference between the start date and end date must be at least 10 days.");
            model.addAttribute("categories", categoryService.getAllCategories());
            return "/movies/Admin/add-movie";
        }
        if (movie.getPrice()<30000) {
            // model.addAttribute("dateError", "The difference between the start date and end date must be at least 10 days.");
            model.addAttribute("categories", categoryService.getAllCategories());
            return "/movies/Admin/add-movie";
        }
        if (movie.getDurationMinutes()<60) {
            // model.addAttribute("dateError", "The difference between the start date and end date must be at least 10 days.");
            model.addAttribute("categories", categoryService.getAllCategories());
            return "/movies/Admin/add-movie";
        }
        if (!imageFile.isEmpty()) {
            try {
                System.out.println("Received file: " + imageFile.getOriginalFilename()); // Debug line
                String fileName = saveImage(imageFile);
                System.out.println("Saved file as: " + fileName); // Debug line
                movie.setImage(fileName);
            } catch (IOException e) {
                e.printStackTrace();
                model.addAttribute("imageError", e.getMessage());
                model.addAttribute("categories", categoryService.getAllCategories());
                return "/movies/Admin/add-movie";
            }
        } else {
            System.out.println("No file received"); // Debug line
        }
        // Lấy danh sách các rạp đã chọn và gán cho movie
//        Set<Cinema> selectedCinemas = new HashSet<>(cinemaService.getCinemasByIds(cinemaIds));
//        movie.setCinemas(selectedCinemas);


        movieService.addMovie(movie);
        return "redirect:/movies";
    }




    // For editing a movie
    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping("/edit/{id}")
    public String showEditForm(@PathVariable UUID id, Model model) {
        List<SeatOrderDetail> list = detailRepository.findSeatOrderDetailsByMovieId(id);
        if(list.isEmpty())
        {
            Movie movie = movieService.getMovieById(id)
                    .orElseThrow(() -> new IllegalArgumentException("Invalid movie Id:" + id));
            model.addAttribute("movie", movie);
            model.addAttribute("categories", categoryService.getAllCategories());
            return "/movies/Admin/update-movie";
        }
        model.addAttribute("movies", movieService.getAllMovies());
        return "/movies/Admin/list-movieadmin";


    }

    // Process the form for updating a movie
    @PreAuthorize("hasRole('ADMIN')")
    @PostMapping("/update/{id}")
    public String updateMovie(@PathVariable UUID id, @Valid Movie movie, BindingResult result, @RequestParam("imageFile") MultipartFile imageFile, Model model) {
        if (result.hasErrors()) {
            movie.setId(id);
            return "/movies/Admin/update-movie";
        }
        long daysDifference = ChronoUnit.DAYS.between(movie.getStartDate(), movie.getEndDate());
        if (daysDifference < 10) {
            result.rejectValue("startDate", "error.movie", "The difference between the start date and end date must be at least 10 days.");
        }
        if (movie.getPrice()<30) {
            result.rejectValue("price", "error.movie", "Ticket price must be from 30.");
        }
        if (movie.getDurationMinutes()<60) {
            result.rejectValue("durationMinutes", "error.movie", "\n" + "Movie duration must be 60 minutes or more.");
        }
        if (result.hasErrors()) {
            movie.setId(id);
            model.addAttribute("categories", categoryService.getAllCategories());
            return "/movies/Admin/update-movie";
        }

        if (!imageFile.isEmpty()) {
            try {
                System.out.println("Received file: " + imageFile.getOriginalFilename()); // Debug line
                String fileName = saveImage(imageFile);
                System.out.println("Saved file as: " + fileName); // Debug line
                movie.setImage(fileName);
            } catch (IOException e) {
                e.printStackTrace();
                model.addAttribute("imageError", e.getMessage());
                model.addAttribute("categories", categoryService.getAllCategories());
                return "/movies/Admin/update-movie";
            }
        } else {
            System.out.println("No file received"); // Debug line
        }

        movieService.updateMovie(id, movie);
        return "redirect:/movies/Admin/Index";
    }

    // Handle request to delete a movie
    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping("/delete/{id}")
    public String deleteMovie(@PathVariable UUID id) {
        List<SeatOrderDetail> list = detailRepository.findSeatOrderDetailsByMovieId(id);
        if(list.isEmpty())
        {
            movieService.deleteMovie(id);
            return "redirect:/screenings";
        }
        movieService.deleteMovie(id);
        return "redirect:/movies/Admin/Index";
    }

    // View movie details
    @GetMapping("/{id}")
    public String viewMovie(@PathVariable UUID id, Model model) {
        Movie movie = movieService.getMovieById(id)
                .orElseThrow(() -> new IllegalArgumentException("Invalid movie Id:" + id));
        model.addAttribute("movie", movie);
        return "/movies/User/movie-display";
    }
    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping("/Admin/{id}")
    public String viewMovieAdmin(@PathVariable UUID id, Model model) {
        Movie movie = movieService.getMovieById(id)
                .orElseThrow(() -> new IllegalArgumentException("Invalid movie Id:" + id));
        model.addAttribute("movie", movie);
        return "/movies/Admin/movie-detail";
    }

    private String saveImage(MultipartFile imageFile) throws IOException {
        Path uploadPath = Paths.get(UPLOAD_DIR);

        // Tạo thư mục nếu nó chưa tồn tại
        if (!Files.exists(uploadPath)) {
            Files.createDirectories(uploadPath);
            System.out.println("Directory created at: " + uploadPath.toString()); // Debug line
        }

        Path path = uploadPath.resolve(imageFile.getOriginalFilename());

//        // Kiểm tra xem file đã tồn tại chưa
//        if (Files.exists(path)) {
//            throw new IOException("File already exists: " + path.toString());
//        }

        // Lưu file nếu nó chưa tồn tại
        byte[] bytes = imageFile.getBytes();
        Files.write(path, bytes);
        System.out.println("File saved at: " + path.toString()); // Debug line

        return imageFile.getOriginalFilename();
    }




}
