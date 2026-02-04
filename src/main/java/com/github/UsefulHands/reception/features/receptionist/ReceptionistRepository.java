package com.github.UsefulHands.reception.features.receptionist;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.Optional;
import java.util.List;

@Repository
public interface ReceptionistRepository extends JpaRepository<ReceptionistEntity, Long> {

    Optional<ReceptionistEntity> findByUserId(Long id);
    Optional<ReceptionistEntity> findById(Long id);
}