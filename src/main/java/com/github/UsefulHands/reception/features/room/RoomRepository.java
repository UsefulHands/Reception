package com.github.UsefulHands.reception.features.room;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Optional;

@Repository
public interface RoomRepository extends JpaRepository<RoomEntity, Long> {
    Optional<RoomEntity> findByRoomNumber(String roomNumber);
    List<RoomEntity> findByAvailableTrue();
}
