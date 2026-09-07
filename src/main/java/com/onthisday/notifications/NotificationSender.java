package com.onthisday.notifications;

public interface NotificationSender {

  NotificationDeliveryResult send(String recipientToken, NotificationMessage message);
}
