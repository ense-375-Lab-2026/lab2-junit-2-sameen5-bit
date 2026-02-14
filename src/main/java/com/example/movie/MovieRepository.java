package com.example.movie;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

/**
 *provides data access operations for movies using JDBC.
 *uses SQLite and supports in-memory or file-based databases.
 *
 * IMPORTANT NOTE:
 *if you use "jdbc:sqlite::memory:", SQLite creates a NEW empty database per connection.
 *so we keep ONE connection open for the lifetime of this repository.
 */
public class MovieRepository {

    /**JDBC connection URL (e.g. "jdbc:sqlite:movies.db" or "jdbc:sqlite::memory:"). */
    private final String url;

    /**single shared connection (critical for in-memory SQLite). */
    private final Connection conn;

    /**
     *creates a new repository that connects to the database specified by {@code url}.
     *call {@link #createTable()} to ensure the movies table exists.
     * @param url the JDBC URL for the database connection
     */
    public MovieRepository(String url) {
        this.url = url;
        try {
            this.conn = DriverManager.getConnection(url);
        } catch (SQLException e) {
            throw new RuntimeException("Unable to obtain database connection", e);
        }
    }

    //close the repository connection (useful in tests with @AfterEach).
    public void close() {
        try {
            conn.close();
        } catch (SQLException e) {
            throw new RuntimeException("Failed to close database connection", e);
        }
    }

    //creates the movies table if it does not already exist.
    public void createTable() {
        String sql = "CREATE TABLE IF NOT EXISTS movies (" +
                "id INTEGER PRIMARY KEY AUTOINCREMENT," +
                "title TEXT NOT NULL," +
                "genres TEXT NOT NULL," +
                "year INTEGER NOT NULL" +
                ");";
        try (Statement stmt = conn.createStatement()) {
            stmt.execute(sql);
        } catch (SQLException e) {
            throw new RuntimeException("Failed to create movies table", e);
        }
    }

    //inserts a movie into the database.
    public void addMovie(Movie movie) {
        String sql = "INSERT INTO movies(title, genres, year) VALUES(?,?,?)";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, movie.getTitle());
            ps.setString(2, String.join(",", movie.getGenres()));
            ps.setInt(3, movie.getYear());
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException("Failed to add movie", e);
        }
    }

    //removes all rows from the movies table.
    public void clear() {
        String sql = "DELETE FROM movies";
        try (Statement stmt = conn.createStatement()) {
            stmt.executeUpdate(sql);
        } catch (SQLException e) {
            //if table doesn't exist, ignore (same behavior as your original)
        }
    }

    //deletes a movie by its title.
    public void deleteMovie(String title) {
        String sql = "DELETE FROM movies WHERE title = ?";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, title);
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException("Failed to delete movie", e);
        }
    }

    //returns all movies in the database.
    public List<Movie> getAllMovies() {
        String sql = "SELECT title, genres, year FROM movies";
        List<Movie> movies = new ArrayList<>();

        try (Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {

            while (rs.next()) {
                String title = rs.getString("title");
                String genres = rs.getString("genres");
                int year = rs.getInt("year");

                List<String> genreList = Arrays.stream(genres.split(","))
                        .map(String::trim)
                        .collect(Collectors.toList());

                movies.add(new Movie(title, genreList, year));
            }

        } catch (SQLException e) {
            throw new RuntimeException("Failed to fetch movies", e);
        }

        return movies;
    }

    //retrieves all movies that belong to the given genre (case-insensitive exact match).
    public List<Movie> getMoviesByGenre(String genre) {
        String sql = "SELECT title, genres, year FROM movies";
        List<Movie> result = new ArrayList<>();

        try (Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {

            while (rs.next()) {
                String title = rs.getString("title");
                String genres = rs.getString("genres");
                int year = rs.getInt("year");

                List<String> genreList = Arrays.stream(genres.split(","))
                        .map(String::trim)
                        .collect(Collectors.toList());

                boolean matches = genreList.stream().anyMatch(g -> g.equalsIgnoreCase(genre));
                if (matches) {
                    result.add(new Movie(title, genreList, year));
                }
            }

        } catch (SQLException e) {
            throw new RuntimeException("Failed to fetch movies by genre", e);
        }

        return result;
    }

    //loads movies from a CSV file into the database.
    public void loadMoviesFromCSV(String csvPath) {
        File file = new File(csvPath);
        if (!file.exists()) {
            throw new IllegalArgumentException("CSV file not found: " + csvPath);
        }

        try (BufferedReader br = new BufferedReader(new FileReader(file))) {
            br.readLine(); // skip header

            String line;
            while ((line = br.readLine()) != null) {
                String trimmed = line.trim();
                if (trimmed.isEmpty()) continue;

                String[] parts = parseCsvLine(trimmed);
                if (parts.length >= 3) {
                    String title = parts[0];

                    List<String> genres = Arrays.stream(parts[1].split(","))
                            .map(String::trim)
                            .collect(Collectors.toList());

                    int year = Integer.parseInt(parts[2].trim());

                    addMovie(new Movie(title, genres, year));
                }
            }
        } catch (IOException e) {
            throw new RuntimeException("Failed to load movies from CSV", e);
        }
    }

    //splits one CSV line into tokens. Supports quoted fields with commas.
    private String[] parseCsvLine(String line) {
        List<String> tokens = new ArrayList<>();
        boolean inQuotes = false;
        StringBuilder sb = new StringBuilder();

        for (int i = 0; i < line.length(); i++) {
            char c = line.charAt(i);

            if (c == '"') {
                inQuotes = !inQuotes;
            } else if (c == ',' && !inQuotes) {
                tokens.add(stripQuotes(sb.toString()));
                sb.setLength(0);
            } else {
                sb.append(c);
            }
        }

        tokens.add(stripQuotes(sb.toString()));
        return tokens.toArray(new String[0]);
    }

    //removes surrounding quotes, if present.
    private String stripQuotes(String token) {
        String trimmed = token.trim();
        if (trimmed.startsWith("\"") && trimmed.endsWith("\"") && trimmed.length() >= 2) {
            return trimmed.substring(1, trimmed.length() - 1);
        }
        return trimmed;
    }
}
