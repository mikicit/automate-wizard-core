package dev.mikita.automatewizard.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "aw_installed_plugin")
public class InstalledPlugin {
    @EmbeddedId
    private InstalledPluginId id;

    @ManyToOne(optional = false)
    @MapsId("user_id")
    private User user;

    @ManyToOne(optional = false)
    @MapsId("plugin_id")
    private Plugin plugin;
}