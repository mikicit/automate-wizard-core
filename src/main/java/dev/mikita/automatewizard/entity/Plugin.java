package dev.mikita.automatewizard.entity;

import jakarta.persistence.*;
import lombok.Data;
import java.util.UUID;

@Entity
@Data
@Table(name = "aw_plugin")
public class Plugin {
    @Id
    private String id;

    @Column(name = "name", nullable = false, length = 64, unique = true)
    private String name;

    @ManyToOne(optional = false)
    @JoinColumn(name = "author_id")
    private User author;

    @Column(name = "description", nullable = false, length = 1024)
    private String description;

    public Plugin() {
        id = UUID.randomUUID().toString();
    }
}
