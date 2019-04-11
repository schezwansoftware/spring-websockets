package com.schezwansoftware.springwebsockets.web.websocket.dto;

public class NotificationsDTO {

    private String message;

    private String title;

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    @Override
    public String toString() {
        return "NotificationsDTO{" +
            "message='" + message + '\'' +
            ", title='" + title + '\'' +
            '}';
    }
}
