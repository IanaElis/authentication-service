package com.alex.project.entiies;

import io.quarkus.hibernate.orm.panache.PanacheEntityBase;
import io.smallrye.common.constraint.NotNull;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Size;


@Entity
@Table(name = "users")
public class User extends PanacheEntityBase {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int id;

    @Column(name = "username")
    @NotNull
    @Size(min = 3, max = 255)
    @NotEmpty
    @NotBlank
    private String  username;

    @Column(name = "password_hash")
    @NotNull
    @NotBlank
    @NotEmpty
    @Size(min = 8, max = 255)
    private String  password;

    @Column(name = "role")
    @NotNull
    @Enumerated(EnumType.STRING)
    private Role role;

    public User(int id, String username, String password, Role role) {
        this.id = id;
        this.username = username;
        this.password = password;
        this.role = role;
    }

    public User() {
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public @Size(min = 3, max = 255) @NotEmpty @NotBlank String getUsername() {
        return username;
    }

    public void setUsername(@Size(min = 3, max = 255) @NotEmpty @NotBlank String username) {
        this.username = username;
    }

    public @NotBlank @NotEmpty @Size(min = 8, max = 255) String getPassword() {
        return password;
    }

    public void setPassword(@NotBlank @NotEmpty @Size(min = 8, max = 255) String password) {
        this.password = password;
    }

    public @jakarta.validation.constraints.NotNull Role getRole() {
        return role;
    }

    public void setRole(@jakarta.validation.constraints.NotNull Role role) {
        this.role = role;
    }
}
