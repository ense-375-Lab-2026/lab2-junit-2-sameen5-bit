package com.example.movie;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import java.nio.file.Paths;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 *unit tests for the {@link MovieRepository}.  These tests use an
 *in‑memory SQLite database so that no external files are created.
 */
public class MovieRepositoryTest {
    private MovieRepository repository;

    @BeforeEach
    void setUp() {
        //use in‑memory database for isolation
        repository = new MovieRepository("jdbc:sqlite::memory:");
        repository.createTable();
    }

    @Test
    void testAddAndGetAllMovies() {
        Movie movie = new Movie("Test Movie", List.of("Drama"), 2020);
        repository.addMovie(movie);
        List<Movie> movies = repository.getAllMovies();
        assertEquals(1, movies.size(), "Expected one movie in the database");
        assertEquals("Test Movie", movies.get(0).getTitle());
        assertEquals(2020, movies.get(0).getYear());
        assertTrue(movies.get(0).getGenres().contains("Drama"));
    }

    @Test
    void testDeleteMovie() {
        repository.addMovie(new Movie("Movie1", List.of("Action"), 2000));
        repository.addMovie(new Movie("Movie2", List.of("Drama"), 2001));
        repository.deleteMovie("Movie1");
        List<Movie> remaining = repository.getAllMovies();
        assertEquals(1, remaining.size());
        assertEquals("Movie2", remaining.get(0).getTitle());
    }

    @Test
    void testGetMoviesByGenre() {
        repository.addMovie(new Movie("Movie1", List.of("Action", "Comedy"), 2000));
        repository.addMovie(new Movie("Movie2", List.of("Drama"), 2001));
        List<Movie> action = repository.getMoviesByGenre("Action");
        assertEquals(1, action.size());
        assertEquals("Movie1", action.get(0).getTitle());
        List<Movie> drama = repository.getMoviesByGenre("Drama");
        assertEquals(1, drama.size());
        assertEquals("Movie2", drama.get(0).getTitle());
    }

    @Test
    void testLoadMoviesFromCSV() {
        // Copy the CSV from resources and load into the repository
        String csvPath = Paths.get("src", "main", "resources", "movies.csv").toString();
        repository.loadMoviesFromCSV(csvPath);
        List<Movie> movies = repository.getAllMovies();
        assertFalse(movies.isEmpty(), "Imported movies should not be empty");
        // check that a known movie exists
        boolean found = movies.stream().anyMatch(m -> m.getTitle().equals("Mad Max: Fury Road"));
        assertTrue(found, "Mad Max: Fury Road should be present after import");
    }
}