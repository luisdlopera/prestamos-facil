package com.prestamosfacil.infrastructure.adapter.out.notification.email;

public class EmailTemplateDTO {

    private final String title;
    private final String subtitle;
    private final String content;
    private final String footer;
    private final ActionDto action;

    public EmailTemplateDTO(String title, String subtitle, String content, String footer, ActionDto action) {
        this.title = title;
        this.subtitle = subtitle;
        this.content = content;
        this.footer = footer;
        this.action = action;
    }

    public String getTitle() { return title; }
    public String getSubtitle() { return subtitle; }
    public String getContent() { return content; }
    public String getFooter() { return footer; }
    public ActionDto getAction() { return action; }

    public static class ActionDto {
        private final String title;
        private final String url;

        public ActionDto(String title, String url) {
            this.title = title;
            this.url = url;
        }

        public String getTitle() { return title; }
        public String getUrl() { return url; }
    }
}
