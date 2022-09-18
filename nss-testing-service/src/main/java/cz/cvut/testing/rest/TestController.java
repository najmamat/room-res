package cz.cvut.testing.rest;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class TestController {
    @GetMapping("/awesome")
    public ResponseEntity<String> getAwesomeMessage() {
        return ResponseEntity.ok("Awesome message");
    }
}
