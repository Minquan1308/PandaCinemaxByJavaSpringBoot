package com.pandacinemax.Panda.Model;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;

@Setter
@Getter
@RequiredArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "cinemas")
public class Cinema {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @NotBlank(message = "Tên là bắt buộc")
//    @Pattern(regexp = "^[a-zA-Z]+$", message = "Tên chỉ được chứa các chữ cái")
    private String cinemaName;
    private String location;

//    @ManyToMany(mappedBy = "cinemas")
//    private Set<Movie> movies = new HashSet<>();
public Long getId() {
    return id;
}

    public void setId(Long id) {
        this.id = id;
    }

    public String getName() {
        return cinemaName;
    }

    public void setName(String name) {
        this.cinemaName = name;
    }

    public String getLocation() {
        return location;
    }

    public void setLocation(String location) {
        this.location = location;
    }
}