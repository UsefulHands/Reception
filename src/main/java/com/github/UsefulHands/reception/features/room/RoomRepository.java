package com.github.UsefulHands.reception.features.room;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

@Repository
public interface RoomRepository extends JpaRepository<RoomEntity, Long> {

    Optional<RoomEntity> findByRoomNumber(String roomNumber);

    @Query("SELECT r FROM RoomEntity r WHERE r.isDeleted = false")
    List<RoomEntity> findAllActiveRooms();

    @Query("SELECT r FROM RoomEntity r WHERE r.available = true AND r.isDeleted = false")
    List<RoomEntity> findByAvailableTrueAndIsDeletedFalse();
}
