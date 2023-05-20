package at.hypercrawler.filterservice.domain.model;

public enum SupportedFileType {
    HTML(".html"), PDF(".pdf"), TXT(".txt");

    private final String format;

    SupportedFileType(String format) {
        this.format = format;
    }

    public String getFormat() {
        return format;
    }
}
