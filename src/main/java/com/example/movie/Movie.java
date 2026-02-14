package com.example.movie;

import java.util.List;

/**
 *a simple value object representing a movie.  Each movie has a title,
 *a list of genres, and the year the movie was released.
 */
public class Movie {
    private String title;
    private List<String> genres;
    private int year;

    /**
     *constructs a new Movie instance.
     *
     *@param title the title of the movie
     *@param genres the list of genres associated with the movie
     *@param year the year the movie was released
     */
    public Movie(String title, List<String> genres, int year) {
        this.title = title;
        this.genres = genres;
        this.year = year;
    }

    /**
     *returns the movie's title.
     *@return the title
     */
    public String getTitle() {
        return title;
    }

    /**
     *sets the movie's title.
     * @param title the new title
     */
    public void setTitle(String title) {
        this.title = title;
    }

    /**
     *returns the list of genres.
     * @return the genres
     */
    public List<String> getGenres() {
        return genres;
    }

    /**
     *sets the list of genres.
     * @param genres the new genres
     */
    public void setGenres(List<String> genres) {
        this.genres = genres;
    }

    /**
     *returns the year the movie was released.
     * @return the release year
     */
    public int getYear() {
        return year;
    }

    /**
     *sets the release year.
     * @param year the new release year
     */
    public void setYear(int year) {
        this.year = year;
    }

    @Override
    public String toString() {
        return "Movie{" +
                "title='" + title + '\'' +
                ", genres=" + genres +
                ", year=" + year +
                '}';
    }
}