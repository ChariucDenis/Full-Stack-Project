package com.example.Securitate.ai;

import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/ai")
@RequiredArgsConstructor
@CrossOrigin(origins = {
        "http://localhost:3000",
        "http://localhost:5173",
        "https://<netlify-app>.netlify.app"
})
public class BookingAssistantController {

    private final BookingAssistantAgent agent;

    @PostMapping(
            value = "/chat",
            consumes = MediaType.APPLICATION_JSON_VALUE,
            produces = MediaType.APPLICATION_JSON_VALUE
    )
    public ChatResponseDTO chat(@RequestBody ChatRequestDTO req) {
        return agent.chat(
                req.sessionId() == null ? "anonymous" : req.sessionId(),
                req.message()
        );
    }


    public record ChatRequestDTO(String sessionId, String message) {}

    public record ActionDTO(String type, String to, String label) {}

    public record ChatResponseDTO(String reply, List<ActionDTO> actions) {}
}
