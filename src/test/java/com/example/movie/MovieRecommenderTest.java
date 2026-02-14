package com.example.movie;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests for the {@link MovieRecommender} class.  These tests verify that
 * recommendations correctly delegate to the repository and return
 * appropriate results.
 */
public class MovieRecommenderTest {
    private MovieRepository repository;
    private MovieRecommender recommender;

    @BeforeEach
    void setUp() {
        repository = new MovieRepository("jdbc:sqlite::memory:");
        repository.createTable();
        repository.addMovie(new Movie("Mad Max: Fury Road", List.of("Action", "Adventure", "Sci-Fi"), 2015));
        repository.addMovie(new Movie("Moonlight", List.of("Drama"), 2016));
        recommender = new MovieRecommender(repository);
    }

    @Test
    void testRecommendByGenreAction() {
        List<Movie> action = recommender.recommendByGenre("Action");
        assertEquals(1, action.size());
        assertEquals("Mad Max: Fury Road", action.get(0).getTitle());
    }

    @Test
    void testRecommendByGenreDrama() {
        List<Movie> drama = recommender.recommendByGenre("Drama");
        assertEquals(1, drama.size());
        assertEquals("Moonlight", drama.get(0).getTitle());
    }
}