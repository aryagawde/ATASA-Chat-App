package com.chat.chatapp.user;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
 @RequiredArgsConstructor
public class UserService {

    private final UserRepository repository;

    // To save a user
    public void saveUser(User user) {
        user.setStatus(Status.ONLINE);
        repository.save(user);
    }

    // Disconnect the user
    public void disconnect(User user) {
        // Retrieve user details
        var storedUser = repository.findById(user.getNickName())
                        .orElse(null);
        // Make modifications to user details in the repo
        if (storedUser != null) {
            storedUser.setStatus(Status.OFFLINE);
            repository.save(storedUser);
        }
    }

    public List<User> findConnectedUsers() {
        return repository.findAllByStatus(Status.ONLINE);
    }
}
