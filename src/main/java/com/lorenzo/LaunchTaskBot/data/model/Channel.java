package com.lorenzo.LaunchTaskBot.data.model;

import javax.persistence.*;
import java.util.List;

@Entity
@Table
public class Channel {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private long id;

    @Column
    private String name;

    @OneToOne(mappedBy = "channel")
    private Project project;

    @OneToMany(mappedBy = "channel")
    private List<Role> roles;

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
}
