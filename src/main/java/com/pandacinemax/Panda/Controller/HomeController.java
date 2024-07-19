package com.pandacinemax.Panda.Controller;

import com.pandacinemax.Panda.Service.MovieService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
public class HomeController {
    @Autowired
    private MovieService movieService;
    @GetMapping
    public String showMovieList(Model model) {
        model.addAttribute("movies", movieService.getAllMovies());
        return "/movies/User/movie-list";
    }
}
