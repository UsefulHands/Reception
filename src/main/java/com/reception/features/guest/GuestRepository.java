package com.reception.features.guest;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.Optional;

@Repository
public interface GuestRepository extends JpaRepository<GuestEntity, Long> {

    Optional<GuestEntity> findByIdentificationNumber(String idNumber);
}