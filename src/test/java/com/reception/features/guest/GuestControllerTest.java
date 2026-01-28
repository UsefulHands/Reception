package com.reception.features.guest;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.reception.common.exception.ConflictException;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;

@SpringBootTest
@ActiveProfiles("test")
@AutoConfigureMockMvc(addFilters = false)
public class GuestControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private GuestService guestService;

    // Jackson ObjectMapper'ı Spring'den almak daha iyidir (Konfigürasyonları kaçırmaz)
    @Autowired
    private ObjectMapper objectMapper;

    @Test
    public void createGuest_ShouldReturnCreated() throws Exception {
        // 1. Hazırlık: Service artık DTO alıp DTO dönüyor
        GuestDto guestDto = GuestDto.builder()
                .firstName("Hasan")
                .lastName("Vibe")
                .email("hasan@vibe.com")
                .identificationNumber("12345678901")
                .build();

        // Mockito: Service DTO alacak ve geriye (ID'si atanmış gibi) DTO dönecek
        when(guestService.createGuest(any(GuestDto.class))).thenReturn(guestDto);

        // 2. Aksiyon ve Kontrol
        mockMvc.perform(post("/api/v1/guests")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(guestDto)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.firstName").value("Hasan"))
                .andExpect(jsonPath("$.lastName").value("Vibe"));
    }

    @Test
    public void createGuest_EmptyFirstName_ShouldReturnBadRequest() throws Exception {
        GuestDto invalidDto = GuestDto.builder()
                .firstName("") // Boş isim @NotBlank'e takılmalı
                .lastName("Vibe")
                .identificationNumber("12345678901")
                .build();

        mockMvc.perform(post("/api/v1/guests")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalidDto)))
                .andExpect(status().isBadRequest());
        // Not: Burada service'e hiç gitmez, Controller'daki @Valid bunu durdurur.
    }

    @Test
    public void createGuest_DuplicateId_ShouldReturnConflict() throws Exception {
        GuestDto dto = GuestDto.builder()
                .firstName("Hasan")
                .lastName("Vibe")
                .identificationNumber("12345678901")
                .build();

        // Service katmanı ConflictException fırlatıyor
        when(guestService.createGuest(any(GuestDto.class)))
                .thenThrow(new ConflictException("Someone else has this id number"));

        mockMvc.perform(post("/api/v1/guests")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isConflict()); // 409
    }
}
