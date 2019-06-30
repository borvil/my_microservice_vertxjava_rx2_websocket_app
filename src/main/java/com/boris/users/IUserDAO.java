package com.boris.users;

import java.util.List;

public interface IUserDAO {

    List<User> getUserList();

    User findUserById(String id);

    void add (User user);

    void remove(String id);

    void update(User user);

}
