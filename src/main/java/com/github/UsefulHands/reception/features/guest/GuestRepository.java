package com.github.UsefulHands.reception.features.guest;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.util.Optional;
import java.util.List;

@Repository
public interface GuestRepository extends JpaRepository<GuestEntity, Long> {

    Optional<GuestEntity> findById(Long id);
    Optional<GuestEntity> findByUserId(Long id);
    @Query("SELECT g FROM GuestEntity g JOIN g.user u WHERE u.isDeleted = false")
    List<GuestEntity> findAllActiveGuests();

    @Query("SELECT g FROM GuestEntity g JOIN g.user u WHERE u.username = :username AND u.isDeleted = false")
    Optional<GuestEntity> findActiveGuestByUsername(@Param("username") String username);
}