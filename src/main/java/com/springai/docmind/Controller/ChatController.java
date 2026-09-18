package com.springai.docmind.Controller;

import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/chat")
@Tag(
        name = "Chat Management",
        description = "Endpoints related to chat"
)
public class ChatController {

    @PostMapping
    public ResponseEntity<String> chat(){
        return ResponseEntity.ok("This is testing config");
    }
}
