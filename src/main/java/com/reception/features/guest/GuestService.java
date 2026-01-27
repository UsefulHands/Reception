package com.reception.features.guest;

import com.reception.common.exception.BaseException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class GuestService {

    private final GuestRepository guestRepository;

    public List<GuestEntity> getAllGuests() {
        log.info("Tüm misafir listesi getiriliyor...");
        return guestRepository.findAll();
    }

    public GuestEntity createGuest(GuestEntity guest) {
        guestRepository.findByIdentificationNumber(guest.getIdentificationNumber())
                .ifPresent(existingGuest -> {
                    log.error("Kayıt hatası: {} TC numaralı misafir zaten mevcut.", guest.getIdentificationNumber());
                    throw new BaseException("Bu kimlik numarası ile zaten bir kayıt bulunmaktadır.");
                });

        log.info("Yeni misafir kaydediliyor: {} {}", guest.getFirstName(), guest.getLastName());
        return guestRepository.save(guest);
    }
}
