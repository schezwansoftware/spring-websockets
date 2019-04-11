package com.schezwansoftware.springwebsockets.web.websocket;

import com.schezwansoftware.springwebsockets.web.websocket.dto.NotificationsDTO;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.messaging.handler.annotation.SendTo;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Controller;

@Controller
public class NotificationsController {

    private static final Logger log = LoggerFactory.getLogger(NotificationsController.class);

    private final SimpMessagingTemplate messagingTemplate ;

    public NotificationsController(SimpMessagingTemplate messagingTemplate) {
        this.messagingTemplate = messagingTemplate;
    }


    @MessageMapping("/topic/notifications")
    @SendTo("/queue/notify")
    public NotificationsDTO sendNotifications(@Payload NotificationsDTO notificationsDTO, String principal) {
        log.debug("Sending user notifications data {}", notificationsDTO);
        messagingTemplate.convertAndSendToUser(principal,"/queue/notify", notificationsDTO);
        return notificationsDTO;
    }
}
