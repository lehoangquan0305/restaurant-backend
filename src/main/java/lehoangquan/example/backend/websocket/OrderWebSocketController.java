package lehoangquan.example.backend.websocket;

import lehoangquan.example.backend.model.OrderEntity;
import lehoangquan.example.backend.model.Reservation;

import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Controller;

@Controller
public class OrderWebSocketController {

    private final SimpMessagingTemplate messagingTemplate;

    public OrderWebSocketController(SimpMessagingTemplate messagingTemplate) {
        this.messagingTemplate = messagingTemplate;
    }

    public void broadcastOrderUpdate(OrderEntity saved) {
        messagingTemplate.convertAndSend("/topic/orders", saved);
    }

    public void broadcastOrderUpdate(Reservation saved) {
    messagingTemplate.convertAndSend("/topic/reservations", saved);
}

}
