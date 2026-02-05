package com.github.UsefulHands.reception.features.receptionist;

import com.github.UsefulHands.reception.features.admin.AdminEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import java.util.Optional;
import java.util.List;

@Repository
public interface ReceptionistRepository extends JpaRepository<ReceptionistEntity, Long> {

    Optional<ReceptionistEntity> findByUserId(Long id);
    Optional<ReceptionistEntity> findById(Long id);
    @Query("SELECT r FROM ReceptionistEntity r JOIN r.user u WHERE u.isDeleted = false")
    List<ReceptionistEntity> findAllActiveReceptionists();
}