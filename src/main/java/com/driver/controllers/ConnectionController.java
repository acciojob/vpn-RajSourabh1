package com.driver.controllers;

import com.driver.model.User;
import com.driver.services.impl.ConnectionServiceImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/connection")
public class ConnectionController {
    @Autowired
    ConnectionServiceImpl connectionService;

    @PostMapping("/connect")
    public ResponseEntity<Void> connect(@RequestParam int userId, @RequestParam String countryName) throws Exception{
        //Connect the user to a vpn by considering the following priority order.
        //1. If the user is already connected to any service provider, throw "Already connected" exception.
        //2. Else if the countryName corresponds to the original country of the user, do nothing.
        //3. Else, the user should be subscribed under a serviceProvider having option to connect to given country.
            //If the connection can not be made (As user does not have a serviceProvider or serviceProvider does not have given country, throw "Unable to connect" exception.
            //Else, establish the connection where the maskedIp is "updatedCountryCode.serviceProviderId.userId" and return the updated user. If there are multiple options, connect to using the service provider having smallest id
        User user = connectionService.connect(userId, countryName);
        return new ResponseEntity<>(HttpStatus.OK);
    }

    @DeleteMapping("/disconnect")
    public ResponseEntity<Void> disconnect(@RequestParam int userId) throws Exception{
        //If the given user was not connected to a vpn, throw "Already disconnected" exception.
        //Else, disconnect from vpn, make masked Ip as null, update relevant attributes and return updated user.
        User user = connectionService.disconnect(userId);
        return new ResponseEntity<>(HttpStatus.OK);
    }

    @GetMapping("/communicate")
    public ResponseEntity<Void> communicate(@RequestParam int senderId, @RequestParam int receiverId) throws Exception{
        //Establish a connection between sender and receiver users
        //The sender should have the same country as the receiver (the country of the receiver can be virtual in case he is connected to vpn). We have to connect to the current country of receiver.
        //If the sender's original country does not match receiver's country, we need to connect them to a suitable vpn. The sender is initially not connected to any vpn. If there are multiple options, connect using the service provider having smallest id
        //If communication can not be established due to any reason, throw "Cannot establish communication" exception
        User updatedSender = connectionService.communicate(senderId, receiverId);
        return new ResponseEntity<>(HttpStatus.OK);
    }
}
