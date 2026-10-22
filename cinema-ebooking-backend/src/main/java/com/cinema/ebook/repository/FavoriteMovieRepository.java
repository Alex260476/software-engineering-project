package com.cinema.ebook.repository;

import com.cinema.ebook.model.FavoriteMovie;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface FavoriteMovieRepository extends JpaRepository<FavoriteMovie, String> {

    List<FavoriteMovie> findByUserIdAndIsActive(String userId, boolean isActive);

    Optional<FavoriteMovie> findByUserIdAndMovieId(String userId, String movieId);

    @Query("SELECT COUNT(fm) FROM FavoriteMovie fm WHERE fm.movie.movieId = :movieId AND fm.isActive = true")
    long countFavoritesByMovieId(@Param("movieId") String movieId);

    @Query("SELECT fm FROM FavoriteMovie fm WHERE fm.user.userId = :userId AND fm.isActive = true ORDER BY fm.favoritedAt DESC")
    List<FavoriteMovie> findUserFavoritesOrderByDate(@Param("userId") String userId);

    boolean existsByUserIdAndMovieIdAndIsActive(String userId, String movieId, boolean isActive);

    void deleteByUserIdAndMovieId(String userId, String movieId);
}
