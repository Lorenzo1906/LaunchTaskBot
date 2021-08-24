package com.lorenzo.LaunchTaskBot.data.model;

import javax.persistence.*;

@Entity
@Table
public class Action {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private long id;

    @Column
    private String name;

    @Column
    private String service;

    @Column
    private String url;

    @ManyToOne
    private Project project;

    @ManyToOne
    private Channel channel;
}
