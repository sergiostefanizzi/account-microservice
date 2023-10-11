package com.sergiostefanizzi.accountmicroservice.model;

import com.fasterxml.jackson.annotation.JsonBackReference;
import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import lombok.*;


import java.sql.Timestamp;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "Account")
@Getter
@Setter
@RequiredArgsConstructor
@NoArgsConstructor(force = true)
public class AccountJpa {
    public enum Gender {
        MALE,FEMALE
    }
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    @Column(name="id")
    private Long id;
    @Column(name="email", unique = true, nullable = false, length = 320)
    @NotBlank
    @Email
    @Size(max = 320)
    @NonNull
    private String email;
    @Column(name="name", length = 50)
    @Size(max = 50)
    @Pattern(regexp = "^[a-zA-Z]+$")
    private String name;
    @Column(name="surname", length = 50)
    @Size(max = 50)
    @Pattern(regexp = "^[a-zA-Z]+$")
    private String surname;
    @Column(name="birthdate", nullable = false)
    @NotNull
    @Past
    @NonNull
    private LocalDate birthdate;
    @Column(name="gender", length = 6)
    @Enumerated(EnumType.STRING)
    @NonNull
    private Gender gender;
    @Column(name="password", nullable = false)
    @NotBlank
    @Size(max = 255)
    @Pattern(regexp = "^(?=.*[A-Za-z])(?=.*\\d)(?=.*[@$!%*#?&])[A-Za-z\\d@$!%*#?&]{8,}$")
    @NonNull
    private String password;
    @Column(name="validation_code", length = 36)
    @Size(min = 36)
    private String validationCode;
    @Column(name = "is_admin", nullable = false, columnDefinition = "boolean default false")
    @NotNull
    private Boolean isAdmin = false;
    @Column(name="validated_at")
    @PastOrPresent
    private LocalDateTime validatedAt;
    @Column(name="updated_at")
    @PastOrPresent
    private LocalDateTime updatedAt;
    @Column(name="deleted_at")
    @PastOrPresent
    private LocalDateTime deletedAt;
    @Column(name = "Version")
    @Version
    private Long version;
}
