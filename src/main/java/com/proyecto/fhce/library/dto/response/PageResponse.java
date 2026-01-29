package com.proyecto.fhce.library.dto.response;

import java.util.List;

import org.springframework.data.domain.Page;

public class PageResponse<T> {
  private List<T> content;
  private Integer pageNumber;
  private Integer pageSize;
  private Long totalElements;
  private Integer totalPages;
  private Boolean last;
  private Boolean first;

  public PageResponse(Page<T> page) {
    this.content = page.getContent();
    this.pageNumber = page.getNumber();
    this.pageSize = page.getSize();
    this.totalElements = page.getTotalElements();
    this.totalPages = page.getTotalPages();
    this.last = page.isLast();
    this.first = page.isFirst();
  }
}