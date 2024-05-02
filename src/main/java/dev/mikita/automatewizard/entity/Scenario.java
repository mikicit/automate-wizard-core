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
@Table(name = "aw_scenario")
public class Scenario {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(name = "name", nullable = false)
    private String name;

    @ManyToOne(optional = false)
    @JoinColumn(name = "owner_id")
    private User owner;

    @Enumerated(EnumType.STRING)
    @Column(name = "state", nullable = false)
    private ScenarioState state;

    @Enumerated(EnumType.STRING)
    @Column(name = "run_type", nullable = false)
    private ScenarioRunType runType;

    @ManyToOne
    @JoinColumn(name = "trigger_id")
    private Trigger trigger;

    @Column(name = "trigger_payload", length = 4000)
    @Convert(converter = JsonNodeStringConverter.class)
    private JsonNode triggerPayload;

    @Column(name = "schedule")
    private String schedule;

    @OneToOne(mappedBy = "scenario", cascade = CascadeType.ALL, orphanRemoval = true)
    private Webhook webhook;

    @OrderColumn(name = "task_order")
    @OneToMany(mappedBy = "scenario", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    private List<Task> tasks;

    @OneToMany(mappedBy = "scenario", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    private List<ScenarioExecution> executions;
}