package com.prestamosfacil.domain.shared;

import lombok.Getter;
import java.util.List;

@Getter
public class PageResult<T> {

    private final List<T> content;
    private final int page;
    private final int perPage;
    private final long total;
    private final int totalPages;

    public PageResult(List<T> content, int page, int perPage, long total) {
        this.content = content;
        this.page = page;
        this.perPage = perPage;
        this.total = total;
        this.totalPages = perPage > 0 ? (int) Math.ceil((double) total / perPage) : 0;
    }

    public boolean hasNext() { return page < totalPages - 1; }
    public boolean hasPrevious() { return page > 0; }
}
