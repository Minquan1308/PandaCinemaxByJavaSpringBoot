package com.pandacinemax.Panda.Controller;

import com.pandacinemax.Panda.Model.Cinema;
import com.pandacinemax.Panda.Model.Room;
import com.pandacinemax.Panda.Model.SeatOrderDetail;
import com.pandacinemax.Panda.Repository.OrderDetailRepository;
import com.pandacinemax.Panda.Repository.RoomRepository;
import com.pandacinemax.Panda.Service.CinemaService;
import org.springframework.beans.factory.annotation.Autowired;
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
import java.util.List;
import java.util.Optional;


@Controller
@RequestMapping("/rooms")
public class RoomController {

    @Autowired
    private RoomRepository roomRepository;
    @Autowired
    private CinemaService cinemaService;
    @Autowired
    private OrderDetailRepository detailRepository;

    @GetMapping("/index")
    public String roomIndex(Model model) {
        List<Room> rooms = roomRepository.findAll();
        model.addAttribute("rooms", rooms);
        return "room/index";
    }

    @GetMapping("/details/{id}")
    public String roomDetails(@PathVariable("id") int id, Model model) {
        Optional<Room> room = roomRepository.findById(id);
        if (room.isPresent()) {
            model.addAttribute("room", room.get());
            return "room/details";
        } else {
            return "error";
        }
    }

    @GetMapping("/delete/{id}")
    public String roomDelete(@PathVariable("id") int id) {
        // Implement your logic here to check for bookings and delete room
        List <SeatOrderDetail> list = detailRepository.findAll();
        //Optional<Room> room = roomRepository.findById(id);
        for(SeatOrderDetail item : list)
        {
            if(item.getRoomId() == id )
            {
                return "redirect:/rooms/index";
            }
        }
        roomRepository.deleteById(id);
        return "redirect:/rooms/index";
    }

    @GetMapping("/add")
    public String addRoomForm(Model model) {
        model.addAttribute("cinemas", cinemaService.getAllCinemas());
        model.addAttribute("room", new Room());
        return "room/add";
    }

    @PostMapping("/add")
    public String addRoomSubmit(@ModelAttribute Room room) {
        if (room.getSoHang() >= 4 && room.getSoCot() >= 5) {
            roomRepository.save(room);
        }
        return "redirect:/rooms/index"; // Redirect to Cinema index
    }
}