package com.ahnaf.library;

import java.time.LocalDate;

public class Book {
    private final String isbn;
    private final String title;
    private final String author;
    private final int year;
    private Integer borrowerId; // null when the book is on the shelf
    private LocalDate dueDate;

    public Book(String isbn, String title, String author, int year) {
        this.isbn = isbn;
        this.title = title;
        this.author = author;
        this.year = year;
    }

    public String getIsbn() { return isbn; }
    public String getTitle() { return title; }
    public String getAuthor() { return author; }
    public int getYear() { return year; }
    public Integer getBorrowerId() { return borrowerId; }
    public LocalDate getDueDate() { return dueDate; }

    public boolean isAvailable() {
        return borrowerId == null;
    }

    public boolean isOverdue(LocalDate today) {
        return !isAvailable() && today.isAfter(dueDate);
    }

    void checkOut(int memberId, LocalDate dueDate) {
        this.borrowerId = memberId;
        this.dueDate = dueDate;
    }

    void checkIn() {
        this.borrowerId = null;
        this.dueDate = null;
    }

    @Override
    public String toString() {
        String status = isAvailable() ? "available" : "borrowed by member #" + borrowerId + ", due " + dueDate;
        return String.format("%-14s %-32s %-22s %4d  (%s)", isbn, title, author, year, status);
    }
}
