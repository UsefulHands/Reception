package com.github.UsefulHands.reception.features.admin;

import com.github.UsefulHands.reception.features.guest.GuestEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface AdminRepository extends JpaRepository<AdminEntity, Long> {

    Optional<AdminEntity> findByUserId(Long userId);
    Optional<AdminEntity> findById(Long id);
    @Query("SELECT a FROM AdminEntity a JOIN a.user u WHERE u.isDeleted = false")
    List<AdminEntity> findAllActiveAdmins();
}