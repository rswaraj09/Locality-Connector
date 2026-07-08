package com.example.localityconnector.controller;

import com.example.localityconnector.service.NotificationService;
import com.example.localityconnector.util.ApiResponse;
import com.example.localityconnector.util.SecurityUtils;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/notifications")
@RequiredArgsConstructor
@Tag(name = "Notifications", description = "In-app notification management")
@SecurityRequirement(name = "bearerAuth")
public class NotificationController {

    private final NotificationService notificationService;

    @Operation(summary = "List notifications for the logged-in user/business")
    @GetMapping
    public ResponseEntity<ApiResponse<Object>> getNotifications() {
        String entityId = SecurityUtils.getLoggedInEntityId();
        if (entityId == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(ApiResponse.fail("Not logged in"));
        }
        return ResponseEntity.ok(ApiResponse.ok(notificationService.getNotifications(entityId)));
    }

    @Operation(summary = "Get unread notification count")
    @GetMapping("/unread-count")
    public ResponseEntity<ApiResponse<Object>> getUnreadCount() {
        String entityId = SecurityUtils.getLoggedInEntityId();
        if (entityId == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(ApiResponse.fail("Not logged in"));
        }
        long count = notificationService.getUnreadCount(entityId);
        return ResponseEntity.ok(ApiResponse.ok(Map.of("count", count)));
    }

    @Operation(summary = "Mark a notification as read")
    @PutMapping("/{id}/read")
    public ResponseEntity<ApiResponse<Object>> markAsRead(@PathVariable String id) {
        notificationService.markAsRead(id);
        return ResponseEntity.ok(ApiResponse.ok(Map.of("message", "Notification marked as read")));
    }

    @Operation(summary = "Mark all notifications as read")
    @PutMapping("/read-all")
    public ResponseEntity<ApiResponse<Object>> markAllAsRead() {
        String entityId = SecurityUtils.getLoggedInEntityId();
        if (entityId == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(ApiResponse.fail("Not logged in"));
        }
        int count = notificationService.markAllAsRead(entityId);
        return ResponseEntity.ok(ApiResponse.ok(Map.of("message", "Marked " + count + " notifications as read")));
    }

    @Operation(summary = "Delete a specific notification")
    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Object>> deleteNotification(@PathVariable String id) {
        String entityId = SecurityUtils.getLoggedInEntityId();
        if (entityId == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(ApiResponse.fail("Not logged in"));
        }
        boolean deleted = notificationService.deleteNotification(id, entityId);
        if (!deleted) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(ApiResponse.fail("Notification not found or unauthorized"));
        }
        return ResponseEntity.ok(ApiResponse.ok(Map.of("message", "Notification deleted successfully")));
    }

    @Operation(summary = "Delete all notifications for the logged-in user/business")
    @DeleteMapping("/clear-all")
    public ResponseEntity<ApiResponse<Object>> clearAllNotifications() {
        String entityId = SecurityUtils.getLoggedInEntityId();
        if (entityId == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(ApiResponse.fail("Not logged in"));
        }
        int count = notificationService.deleteAllNotifications(entityId);
        return ResponseEntity.ok(ApiResponse.ok(Map.of("message", "Deleted " + count + " notifications")));
    }
}
