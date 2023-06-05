package com.sergiostefanizzi.accountmicroservice.repository.model;

import com.fasterxml.jackson.annotation.JsonBackReference;
import jakarta.persistence.*;
import jakarta.validation.constraints.*;


import java.sql.Timestamp;
import java.time.LocalDate;

@Entity
@Table(name = "Account")
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
    private LocalDate birthdate;
    @Column(name="gender", length = 6)
    @Enumerated(EnumType.STRING)
    private Gender gender;
    @Column(name="password", nullable = false)
    @NotBlank
    @Size(max = 255)
    @Pattern(regexp = "^(?=.*[A-Za-z])(?=.*\\d)(?=.*[@$!%*#?&])[A-Za-z\\d@$!%*#?&]{8,}$")
    private String password;
    @Column(name="validation_code", length = 36)
    @Size(min = 36)
    private String validationCode;
    @Column(name = "is_admin", nullable = false)
    @NotNull
    private Boolean isAdmin;
    @Column(name="validated_at")
    @PastOrPresent
    private Timestamp validatedAt;
    @Column(name="updated_at")
    @PastOrPresent
    private Timestamp updatedAt;
    @Column(name="deleted_at")
    @PastOrPresent
    private Timestamp deletedAt;
    @Column(name = "Version")
    @Version
    private Long version;


    public AccountJpa(){}

    public AccountJpa(String email, String name, String surname, LocalDate birthdate, Gender gender, String password) {
        this.email = email;
        this.name = name;
        this.surname = surname;
        this.birthdate = birthdate;
        this.gender = gender;
        this.password = password;
        this.isAdmin = false;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getSurname() {
        return surname;
    }

    public void setSurname(String surname) {
        this.surname = surname;
    }

    public LocalDate getBirthdate() {
        return birthdate;
    }

    public void setBirthdate(LocalDate birthdate) {
        this.birthdate = birthdate;
    }

    public Gender getGender() {
        return gender;
    }

    public void setGender(Gender gender) {
        this.gender = gender;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getValidationCode() {
        return validationCode;
    }

    public void setValidationCode(String validationCode) {
        this.validationCode = validationCode;
    }

    public Timestamp getValidatedAt() {
        return validatedAt;
    }

    public void setValidatedAt(Timestamp validatedAt) {
        this.validatedAt = validatedAt;
    }

    public Timestamp getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(Timestamp updatedAt) {
        this.updatedAt = updatedAt;
    }

    public Timestamp getDeletedAt() {
        return deletedAt;
    }

    public void setDeletedAt(Timestamp deletedAt) {
        this.deletedAt = deletedAt;
    }

    public Long getVersion() {
        return version;
    }

    public void setVersion(Long version) {
        this.version = version;
    }

    public Boolean getAdmin() {
        return isAdmin;
    }

    public void setAdmin(Boolean admin) {
        isAdmin = admin;
    }
}
