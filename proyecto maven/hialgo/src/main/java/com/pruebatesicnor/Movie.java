package com.pruebatesicnor;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)

public class Movie {
    public String imdbID;
    public String Title;
    public String Year;
    public String Poster;
    public int PersonalRating;

    public Movie() {
        // Constructor sin argumentos
    }

    public Movie(String imdbID, String Title, String Year, String Poster, int PersonalRating) {
        this.imdbID = imdbID;
        this.Title = Title;
        this.Year = Year;
        this.Poster = Poster;
        this.PersonalRating = PersonalRating;
    }
}
