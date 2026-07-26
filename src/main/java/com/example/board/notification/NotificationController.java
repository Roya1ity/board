package com.example.board.notification;

import com.example.board.auth.CustomUserDetails;
import com.example.board.notification.dto.NotificationResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/notify")
public class NotificationController {

    private final NotificationService notificationService;

    public record UnreadCountResponse(long count) {}

    @GetMapping("/list")
    public Page<NotificationResponse> list(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @PageableDefault(size = 10, sort = "createdAt", direction = Sort.Direction.DESC) Pageable page
            ) {

        return notificationService.getNotifications(userDetails.getId(),page);

    }

    @PutMapping("/{id}/read")
    public ResponseEntity<Void> read(
            @PathVariable Long id,
            @AuthenticationPrincipal CustomUserDetails userDetails
    ) {
        notificationService.read(id,userDetails.getId());
        return ResponseEntity.ok().build();
    }

    @GetMapping("/unreads")
    public UnreadCountResponse unreadCount(
            @AuthenticationPrincipal CustomUserDetails userDetails
    ) {
        return new UnreadCountResponse(notificationService.unreadCount(userDetails.getId()));
    }
}
