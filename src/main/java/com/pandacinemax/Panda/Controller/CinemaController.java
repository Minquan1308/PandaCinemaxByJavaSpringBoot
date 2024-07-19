package com.pandacinemax.Panda.Controller;


import com.pandacinemax.Panda.Model.Cinema;
import com.pandacinemax.Panda.Model.Movie;
import com.pandacinemax.Panda.Model.Room;
import com.pandacinemax.Panda.Model.SeatOrderDetail;
import com.pandacinemax.Panda.Repository.CinemaRepository;
import com.pandacinemax.Panda.Repository.OrderDetailRepository;
import com.pandacinemax.Panda.Service.CinemaService;
import com.pandacinemax.Panda.Service.RoomService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Controller
@RequiredArgsConstructor
public class CinemaController {
    @Autowired
    private final CinemaService cinemaService;
    @Autowired
    private final CinemaRepository cinemaRepository;
    @Autowired
    private final RoomService roomService;
    @Autowired
    private final OrderDetailRepository detailRepository;
    @GetMapping("/cinemas/add")
    public String showAddForm(Model model) {
        model.addAttribute("cinema", new Cinema());
        return "/Cinema/add-cinema";
    }
    @PostMapping("/cinemas/add")
    public String addCinema(@Valid Cinema cinema, BindingResult result) {
        if (result.hasErrors()) {
            return "/Cinema/add-cinema";
        }
        cinemaService.addCinema(cinema);
        return "redirect:/cinemas";
    }
    // Hiển thị danh sách danh mục
    @GetMapping("/cinemas")
    public String listCinemas(Model model) {
        List<Cinema> cinemas = cinemaService.getAllCinemas();
        model.addAttribute("cinemas", cinemas);
        return "/Cinema/cinema-list";
    }
    // GET request to show category edit form
    @GetMapping("/cinemas/edit/{id}")
    public String showUpdateForm(@PathVariable("id") Long id, Model model) {
        List <SeatOrderDetail> list = detailRepository.findAll();
        Optional<Cinema> cinema = cinemaService.getCinemaById(id);
        for(SeatOrderDetail item : list)
        {
            if(item.getCinemaName().equals(cinema.get().getCinemaName()) )
            {
                return "redirect:/cinemas";
            }
        }
//        Cinema cinema = cinemaService.getCinemaById(id)
//                .orElseThrow(() -> new IllegalArgumentException("Invalid cinema Id:"
//                        + id));
        model.addAttribute("cinema", cinema);
        return "/Cinema/update-cinema";
    }
    // POST request to update category
    @PostMapping("/cinemas/update/{id}")
    public String updateCategory(@PathVariable("id") Long id, @Valid Cinema cinema,
                                 BindingResult result, Model model) {
        if (result.hasErrors()) {
            cinema.setId(id);
            return "/Cinema/update-cinema";
        }
        cinemaService.updateCinema(cinema);
        model.addAttribute("cinemas", cinemaService.getAllCinemas());
        return "redirect:/cinemas";
    }
    // GET request for deleting category
    @GetMapping("/cinemas/delete/{id}")
    public String deleteCinema(@PathVariable("id") Long id, Model model, RedirectAttributes redirectAttributes) {
        List<SeatOrderDetail> list = new ArrayList<>();
        Optional<Cinema> cinema = cinemaService.getCinemaById(id);
        for (SeatOrderDetail item : list) {

            if (item.getCinemaName().equals(cinema.get().getCinemaName())) {

                redirectAttributes.addFlashAttribute("errorMessage", "This category is linked to existing movies.");

                return "redirect:/cinemas";
            }
        }

        try {
            cinemaService.deleteCinemaById(id);

            return "redirect:/cinemas";

        } catch (DataIntegrityViolationException ex) {

            return "redirect:/cinemas";
            //redirectAttributes.addFlashAttribute("errorMessage", "Cannot delete this category as it is linked to existing movies.");
        }

//        cinemaService.deleteCinemaById(id);
//        model.addAttribute("cinemas", cinemaService.getAllCinemas());
//        return "redirect:/cinemas";

    }
    @GetMapping("/{cinemaId}/rooms")
    @ResponseBody
    public List<Room> getRoomsByCinema(@PathVariable Long cinemaId) {
        return roomService.findByCinemaId(cinemaId); // Thay thế bằng phương thức phù hợp của service
    }

}
