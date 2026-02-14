package com.github.UsefulHands.reception.features.reservation;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

@Repository
public interface ReservationRepository extends JpaRepository<ReservationEntity, Long> {

    @Query("SELECT COUNT(r) > 0 FROM ReservationEntity r WHERE r.room.id = :roomId " +
            "AND r.isDeleted = false " +
            "AND r.status NOT IN ('CANCELLED', 'NO_SHOW', 'CHECKED_OUT') " +
            "AND (:checkIn < r.checkOutDate AND :checkOut > r.checkInDate)")
    boolean existsOverlappingReservations(
            @Param("roomId") Long roomId,
            @Param("checkIn") LocalDate checkIn,
            @Param("checkOut") LocalDate checkOut);

    @Query("SELECT COUNT(r) > 0 FROM ReservationEntity r WHERE r.room.id = :roomId " +
            "AND r.id != :currentResId " +
            "AND r.isDeleted = false " +
            "AND r.status NOT IN ('CANCELLED', 'NO_SHOW', 'CHECKED_OUT') " +
            "AND (:checkIn < r.checkOutDate AND :checkOut > r.checkInDate)")
    boolean existsOverlappingReservationsExcludingSelf(
            @Param("roomId") Long roomId,
            @Param("checkIn") LocalDate checkIn,
            @Param("checkOut") LocalDate checkOut,
            @Param("currentResId") Long currentResId);

    List<ReservationEntity> findByRoomIdOrderByCheckInDateDesc(Long roomId);

    @Query("SELECT r FROM ReservationEntity r WHERE r.room.id = :roomId " +
            "AND r.isDeleted = false " +
            "AND r.status NOT IN ('CANCELLED', 'NO_SHOW', 'CHECKED_OUT') " +
            "AND (:checkIn < r.checkOutDate AND :checkOut > r.checkInDate)")
    List<ReservationEntity> findOverlappingReservations(
            @Param("roomId") Long roomId,
            @Param("checkIn") LocalDate checkIn,
            @Param("checkOut") LocalDate checkOut);

    @Query("SELECT r FROM ReservationEntity r " +
            "WHERE r.checkInDate <= :end AND r.checkOutDate >= :start " +
            "AND r.isDeleted = false " +
            "AND r.status NOT IN ('CANCELLED', 'NO_SHOW')")
    List<ReservationEntity> findAllByDateRange(
            @Param("start") LocalDate start,
            @Param("end") LocalDate end);
}