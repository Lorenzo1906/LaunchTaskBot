package com.lorenzo.LaunchTaskBot.data.model;

import javax.persistence.*;

@Entity
@Table
public class Project {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private long id;

    @Column
    private String name;

    @Column
    private String slackChannel;

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getSlackChannel() {
        return slackChannel;
    }

    public void setSlackChannel(String slackChannel) {
        this.slackChannel = slackChannel;
    }
}
