package com.lorenzo.LaunchTaskBot.data.model;

import javax.persistence.*;

@Entity
@Table
public class Audit {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private long id;

    @Column
    private String actionValue;

    @Column
    private String status;

    @ManyToOne
    private User user;

    @ManyToOne
    private Channel channel;

    @ManyToOne
    private Action action;

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public String getActionValue() {
        return actionValue;
    }

    public void setActionValue(String actionValue) {
        this.actionValue = actionValue;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }

    public Channel getChannel() {
        return channel;
    }

    public void setChannel(Channel channel) {
        this.channel = channel;
    }

    public Action getAction() {
        return action;
    }

    public void setAction(Action action) {
        this.action = action;
    }
}
