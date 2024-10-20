package com.chat.chatapp.user;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.messaging.handler.annotation.SendTo;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

import java.util.List;

@Controller
@RequiredArgsConstructor
public class UserController {
    private final UserService service;

    // Method to save the user
    @MessageMapping("/user.addUser")
    @SendTo("/user/topic") // forward and connect to all users - a new queue automatically created to send notifications
    public User addUser(@Payload User user) {
        service.saveUser(user);
        // In webSockets, we use annotation @Payload
        return user;
    }

    @MessageMapping("/user.disconnectUser")
    @SendTo("/user/topic")
    public User disconnect(
            @Payload User user
    ) {
        service.disconnect(user);
        return user;
    }

    @GetMapping("/users")
    public ResponseEntity<List<User>> findConnectedUsers() {
        return ResponseEntity.ok(service.findConnectedUsers());
    }


}
