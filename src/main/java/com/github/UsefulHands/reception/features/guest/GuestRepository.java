package com.github.UsefulHands.reception.features.guest;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.Optional;
import java.util.List;

@Repository
public interface GuestRepository extends JpaRepository<GuestEntity, Long> {

    Optional<GuestEntity> findByUserId(Long userId);
}