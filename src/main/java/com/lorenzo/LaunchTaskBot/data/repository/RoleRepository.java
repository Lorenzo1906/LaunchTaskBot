package com.lorenzo.LaunchTaskBot.data.repository;

import com.lorenzo.LaunchTaskBot.data.model.Role;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;

import java.util.List;

public interface RoleRepository extends CrudRepository<Role, Long> {
    @Query("select r from Role r left join r.users users where users.username like ?1")
    List<Role> findByUsers_UsernameLike(String username);
}
