package com.pandacinemax.Panda.Controller;


import com.pandacinemax.Panda.Model.Category;
import com.pandacinemax.Panda.Model.Movie;
import com.pandacinemax.Panda.Model.SeatOrderDetail;
import com.pandacinemax.Panda.Repository.OrderDetailRepository;
import com.pandacinemax.Panda.Service.CategoryService;
import com.pandacinemax.Panda.Service.MovieService;
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
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Controller
@RequiredArgsConstructor
public class CategoryController {
    @Autowired
    private final CategoryService categoryService;
    @Autowired
    private final MovieService movieService;
    @Autowired
    private final OrderDetailRepository detailRepository;
    @GetMapping("/categories/add")
    public String showAddForm(Model model) {
        model.addAttribute("category", new Category());
        return "/Category/add-category";
    }
    @PostMapping("/categories/add")
    public String addCategory(@Valid Category category, BindingResult result) {
        if (result.hasErrors()) {
            return "/Category/add-category";
        }
        categoryService.addCategory(category);
        return "redirect:/categories";
    }
    // Hiển thị danh sách danh mục
    @GetMapping("/categories")
    public String listCategories(Model model) {
        List<Category> categories = categoryService.getAllCategories();
        model.addAttribute("categories", categories);
        return "/Category/categories-list";
    }
    // GET request to show category edit form
    @GetMapping("/categories/edit/{id}")
    public String showUpdateForm(@PathVariable("id") Long id, Model model) {
        Category category = categoryService.getCategoryById(id)
                .orElseThrow(() -> new IllegalArgumentException("Invalid category Id:"
                        + id));
        model.addAttribute("category", category);
        return "/Category/update-category";
    }
    // POST request to update category
    @PostMapping("/categories/update/{id}")
    public String updateCategory(@PathVariable("id") Long id, @Valid Category category,
                                 BindingResult result, Model model) {
        if (result.hasErrors()) {
            category.setId(id);
            return "/Category/update-category";
        }
        categoryService.updateCategory(category);
        model.addAttribute("categories", categoryService.getAllCategories());
        return "redirect:/categories";
    }
    // GET request for deleting category
    @GetMapping("/categories/delete/{id}")
    public String deleteCategory(@PathVariable("id") Long id, RedirectAttributes redirectAttributes) {
        List<SeatOrderDetail> list = new ArrayList<>();
        for (SeatOrderDetail item : list) {
            Optional<Movie> movie = movieService.getMovieById(item.getMovieId());
            if (movie.isPresent() && movie.get().getCategory().getId() == id) {
                redirectAttributes.addFlashAttribute("errorMessage", "This category is linked to existing movies.");
                return "redirect:/categories";
            }
        }

        try {
            categoryService.deleteCategoryById(id);

            return "redirect:/categories";
            //redirectAttributes.addFlashAttribute("successMessage", "Category deleted successfully.");
        } catch (DataIntegrityViolationException ex) {
            return "redirect:/categories";
            //redirectAttributes.addFlashAttribute("errorMessage", "Cannot delete this category as it is linked to existing movies.");
        }


    }

}
