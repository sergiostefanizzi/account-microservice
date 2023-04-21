package com.sergiostefanizzi.accountmicroservice.repository.model;

import com.fasterxml.jackson.annotation.JsonBackReference;
import com.fasterxml.jackson.annotation.JsonManagedReference;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PastOrPresent;

import java.sql.Timestamp;


@Entity
@Table(name = "Admin")
public class AdminJpa {
    @Id
    @Column(name = "id")
    private Long id;
    @Column(name = "created_at", nullable = false)
    @NotNull
    @PastOrPresent
    private Timestamp createdAt;
    @Column(name = "updated_at")
    @PastOrPresent
    private Timestamp updatedAt;
    @Column(name = "deleted_at")
    @PastOrPresent
    private Timestamp deletedAt;
    @Column(name = "version", nullable = false)
    @Version
    private Long version;

    @OneToOne
    @PrimaryKeyJoinColumn(name = "id", referencedColumnName = "id")
    @JsonManagedReference
    private AccountJpa account;

    public AdminJpa(){}

    public AdminJpa(Timestamp createdAt, Timestamp updatedAt, AccountJpa account) {
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
        this.account = account;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Timestamp getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Timestamp createdAt) {
        this.createdAt = createdAt;
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

    public AccountJpa getAccount() {
        return account;
    }

    public void setAccount(AccountJpa account) {
        this.account = account;
    }
}
