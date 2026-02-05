package com.github.UsefulHands.reception.features.room;

import com.github.UsefulHands.reception.common.entity.BaseEntity;
import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.util.*;

@Entity
@Table(name = "rooms")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = true)
public class RoomEntity extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String roomNumber;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private RoomType type;

    @ElementCollection(targetClass = BedType.class)
    @CollectionTable(name = "room_bed_types", joinColumns = @JoinColumn(name = "room_id"))
    @Enumerated(EnumType.STRING)
    @Column(name = "bed_type")
    @Builder.Default
    private Set<BedType> bedTypes = new HashSet<>();

    @Column(nullable = false)
    private int beds;

    @Column(nullable = false)
    private int maxGuests;

    private Double areaSqm;

    @Enumerated(EnumType.STRING)
    private ViewType view;

    @Column(columnDefinition = "TEXT")
    private String description;

    @Column(nullable = false)
    private BigDecimal price;

    @Builder.Default
    private boolean available = true;

    @Builder.Default
    private boolean smokingAllowed = false;

    private int floor;

    @ElementCollection(targetClass = Amenity.class, fetch = FetchType.EAGER)
    @CollectionTable(name = "room_amenities", joinColumns = @JoinColumn(name = "room_id"))
    @Enumerated(EnumType.STRING)
    @Column(name = "amenity")
    @Builder.Default
    private Set<Amenity> amenities = new HashSet<>();

    // IMAGES
    @ElementCollection
    @CollectionTable(name = "room_images", joinColumns = @JoinColumn(name = "room_id"))
    @Column(name = "image_url")
    @Builder.Default
    private List<String> images = new ArrayList<>();
}
