package com.example.movie;

import java.util.List;

/**
 *A high‑level API for generating movie recommendations.  This class
 *delegates to a {@link MovieRepository} to perform the underlying
 *queries.  In a real application the recommender could implement more
 *sophisticated logic; here we simply retrieve movies matching a
 *particular genre.
 */
public class MovieRecommender {
    private final MovieRepository repository;

    /**
     *constructs a new recommender backed by the given repository.
     *@param repository the repository to use for data access
     */
    public MovieRecommender(MovieRepository repository) {
        this.repository = repository;
    }

    /**
     *returns a list of movies that belong to the specified genre.
     *@param genre the genre to search for
     *@return a list of recommended movies
     */
    public List<Movie> recommendByGenre(String genre) {
        return repository.getMoviesByGenre(genre);
    }
}