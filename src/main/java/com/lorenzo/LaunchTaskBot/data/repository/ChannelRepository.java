package com.lorenzo.LaunchTaskBot.data.repository;

import com.lorenzo.LaunchTaskBot.data.model.Channel;
import org.springframework.data.repository.CrudRepository;

import java.util.List;

public interface ChannelRepository extends CrudRepository<Channel, Long> {
    List<Channel> findByNameEqualsIgnoreCase(String name);
}
