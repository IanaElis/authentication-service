package com.alex.project.repositories;

import com.alex.project.entiies.AdminMessage;
import io.quarkus.hibernate.orm.panache.PanacheRepository;
import jakarta.enterprise.context.ApplicationScoped;

import java.time.LocalDateTime;
import java.util.List;

@ApplicationScoped
public class AdminMessageRepository implements PanacheRepository<AdminMessage> {

    public List<AdminMessage> findAllActive() {
        return list("resolved = false or resolved = true order by createdAt desc");
    }

    public long deleteOlderThan(LocalDateTime cutoff) {
        return delete("createdAt < ?1", cutoff);
    }
}
