package com.example.EmployeeManager.model;

import jakarta.persistence.*;

import java.util.UUID;

@Entity
@Table(name = "task")
public class Task {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    @Column(nullable = false, updatable = false)
    private Long id;

    @Column(nullable = false, updatable = false)
    private UUID uuid;

    @Column(name = "title")
    private String title;

    @Column(name = "description", columnDefinition = "TEXT")
    private String description;

    @Column(name = "task_status")
    private TaskStatus status;

    public Task() {
    }

    public Task(String title, String description, TaskStatus status) {
        this.title = title;
        this.description = description;
        this.status = status;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public UUID getUuid() {
        return uuid;
    }

    public void setUuid(UUID uuid) {
        this.uuid = uuid;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public TaskStatus getStatus() {
        return status;
    }

    public void setStatus(TaskStatus status) {
        this.status = status;
    }

    public static Builder builder(){
        return new Builder();
    }
    @PrePersist
    public void prePersist(){
        this.uuid = UUID.randomUUID();
    }


    @Override
    public String toString() {
        return "Task{" +
                "id=" + id +
                ", uuid=" + uuid +
                ", title='" + title + '\'' +
                ", description='" + description + '\'' +
                ", status=" + status +
                '}';
    }

    public static class Builder{

        private String title;
        private String description;
        private TaskStatus status;

        public Builder(){}

        public Builder withTitle(String title){
            this.title = title;
            return this;
        }

        public Builder withDescription(String description){
            this.description = description;
            return this;
        }

        public Builder withStatus(TaskStatus status){
            this.status = status;
            return this;
        }

        public Task build(){
            return new Task(title, description, status);
        }

    }


}
