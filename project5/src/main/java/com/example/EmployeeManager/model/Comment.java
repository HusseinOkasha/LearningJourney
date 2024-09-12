package com.example.EmployeeManager.model;

import jakarta.persistence.*;

import java.util.Objects;
import java.util.UUID;

@Entity
@Table(name = "comment", uniqueConstraints = {
        @UniqueConstraint(columnNames = "uuid")
})
public class Comment {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    @Column(name = "id", nullable = false, updatable = false)
    private Long id;

    @Column(name="body", nullable = false)
    private String body;

    @Column(name ="uuid", nullable = false, updatable = false)
    private UUID uuid;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "account_id")
    private Account createdBy;


    public Comment(){}

    private Comment(Builder builder){
        this.body = builder.body;
        this.createdBy = builder.createdBy;
        this.uuid = builder.uuid;
    }

    public static Builder builder(){
        return new Builder();
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getBody() {
        return body;
    }

    public void setBody(String body) {
        this.body = body;
    }

    public UUID getUuid() {
        return uuid;
    }

    public void setUuid(UUID uuid) {
        this.uuid = uuid;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Comment comment)) return false;
        return Objects.equals(id, comment.id) && Objects.equals(body, comment.body) && Objects.equals(uuid, comment.uuid) && Objects.equals(createdBy, comment.createdBy);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, body, uuid, createdBy);
    }

    @Override
    public String toString() {
        return "Comment{" +
                "id=" + id +
                ", body='" + body + '\'' +
                ", uuid=" + uuid +
                ", createdBy=" + createdBy +
                '}';
    }

    @PrePersist
    public void generateUuid() {
        if (uuid == null) {
            uuid = UUID.randomUUID();
        }
    }

    public Account getCreatedBy() {
        return createdBy;
    }

    public void setCreatedBy(Account createdBy) {
        this.createdBy = createdBy;
    }

    public static class Builder{

        private String body;
        private UUID uuid;
        private Account createdBy;

        public Builder(){}

        public Builder withBody(String body){
            this.body = body;
            return this;
        }
        public Builder withUuid(UUID uuid){
            this.uuid = uuid;
            return this;
        }
        public Builder withCreatedBy(Account createdBy){
            this.createdBy = createdBy;
            return this;
        }

        public Comment build(){
            return new Comment(this);
        }
    }


}
