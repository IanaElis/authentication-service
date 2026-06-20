package com.alex.project.entiies;

import io.quarkus.hibernate.orm.panache.PanacheEntityBase;
import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "admin_messages")
public class AdminMessage extends PanacheEntityBase {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    public Long id;

    @Column(nullable = false)
    public long senderUserId;

    @Column(nullable = false, columnDefinition = "TEXT")
    public String content;

    @Column(nullable = false)
    public LocalDateTime createdAt = LocalDateTime.now();

    @Column(nullable = false)
    public boolean resolved = false;
}
