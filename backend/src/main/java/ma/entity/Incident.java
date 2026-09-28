package ma.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;
import org.springframework.data.elasticsearch.annotations.Document;
import org.springframework.data.elasticsearch.annotations.Field;
import org.springframework.data.elasticsearch.annotations.FieldType;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "incidents")
@Document(indexName = "incidents") // Pour Elasticsearch
@EntityListeners(ma.listener.IncidentEntityListener.class)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Incident {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @org.springframework.data.annotation.Id // Pour Elasticsearch
    private Long id;

    @Column(nullable = false, length = 50)
    @Field(type = FieldType.Keyword)
    private String incidentType;

    @Column(nullable = false, length = 20)
    @Field(type = FieldType.Keyword)
    private String status;

    @Column(nullable = false, columnDefinition = "TEXT")
    @Field(type = FieldType.Text)
    private String description;

    @Column(length = 100)
    @Field(type = FieldType.Keyword)
    private String referenceFacture;

    @Column(length = 100)
    @Field(type = FieldType.Keyword)
    private String clientId;

    @Column
    @Field(type = FieldType.Double)
    private Double montantErreur;

    @Column(length = 255)
    @Field(type = FieldType.Text)
    private String sourceFile;

    @Column(nullable = false)
    @Field(type = FieldType.Integer)
    private Integer lineNumber;

    @Column(length = 20)
    @Field(type = FieldType.Keyword)
    private String priority;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "assigned_to_id")
    @org.springframework.data.annotation.Transient // Exclure d'Elasticsearch
    private User assignedTo;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "created_by_id", nullable = false)
    @org.springframework.data.annotation.Transient
    private User createdBy;

    @CreationTimestamp
    @Column(nullable = false, updatable = false)
    @Field(type = FieldType.Date)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(nullable = false)
    @Field(type = FieldType.Date)
    private LocalDateTime updatedAt;

    @Column
    @Field(type = FieldType.Date)
    private LocalDateTime resolvedAt;

    @Column(nullable = false)
    @Field(type = FieldType.Boolean)
    private Boolean deleted = false;

    @OneToMany(mappedBy = "incident", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @org.springframework.data.annotation.Transient
    @Builder.Default
    private List<Solution> solutions = new ArrayList<>();

    @OneToMany(mappedBy = "incident", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @org.springframework.data.annotation.Transient
    @Builder.Default
    private List<AuditLog> auditLogs = new ArrayList<>();
}