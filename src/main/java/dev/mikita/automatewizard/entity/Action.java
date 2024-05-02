package dev.mikita.automatewizard.entity;

import com.fasterxml.jackson.databind.JsonNode;
import dev.mikita.automatewizard.util.JsonNodeStringConverter;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.util.List;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "aw_action")
public class Action {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(name = "name", nullable = false)
    private String name;

    @Column(name = "label", nullable = false)
    private String label;

    @Column(name = "description", nullable = false)
    private String description;

    @Column(name = "consumes", nullable = false, length = 4000)
    @Convert(converter = JsonNodeStringConverter.class)
    private JsonNode consumes;

    @Column(name = "produces", nullable = false, length = 4000)
    @Convert(converter = JsonNodeStringConverter.class)
    private JsonNode produces;

    @ManyToOne(optional = false)
    @JoinColumn(name = "plugin_id")
    private Plugin plugin;

    @OneToMany(mappedBy = "action", fetch = FetchType.LAZY)
    private List<Task> tasks;

    @PreRemove
    private void preRemove() {
        for (Task t : tasks) {
            t.setAction(null);
            if (t.getScenario().getState() != ScenarioState.INACTIVE) {
                t.getScenario().setState(ScenarioState.INACTIVE);
            }
        }
    }
}