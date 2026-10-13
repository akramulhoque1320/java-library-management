package com.ahnaf.library;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class Library {
    public static final int LOAN_DAYS = 14;
    public static final int FINE_PER_DAY = 10; // taka

    private final Map<String, Book> books = new LinkedHashMap<>();
    private final Map<Integer, Member> members = new LinkedHashMap<>();
    private int nextMemberId = 1;

    // ---- Books ----

    public Book addBook(String isbn, String title, String author, int year) {
        isbn = clean(isbn);
        if (isbn.isEmpty() || clean(title).isEmpty()) {
            throw new LibraryException("ISBN and title are required.");
        }
        if (books.containsKey(isbn)) {
            throw new LibraryException("A book with ISBN " + isbn + " already exists.");
        }
        Book book = new Book(isbn, clean(title), clean(author), year);
        books.put(isbn, book);
        return book;
    }

    public void removeBook(String isbn) {
        Book book = getBook(isbn);
        if (!book.isAvailable()) {
            throw new LibraryException("Cannot remove \"" + book.getTitle() + "\" while it is borrowed.");
        }
        books.remove(book.getIsbn());
    }

    public Book getBook(String isbn) {
        Book book = books.get(clean(isbn));
        if (book == null) {
            throw new LibraryException("No book with ISBN " + isbn + ".");
        }
        return book;
    }

    public Collection<Book> getBooks() {
        return books.values();
    }

    public List<Book> search(String query) {
        String q = query.toLowerCase(Locale.ROOT).trim();
        List<Book> result = new ArrayList<>();
        for (Book book : books.values()) {
            if (book.getTitle().toLowerCase(Locale.ROOT).contains(q)
                    || book.getAuthor().toLowerCase(Locale.ROOT).contains(q)) {
                result.add(book);
            }
        }
        return result;
    }

    public List<Book> getOverdueBooks(LocalDate today) {
        List<Book> result = new ArrayList<>();
        for (Book book : books.values()) {
            if (book.isOverdue(today)) {
                result.add(book);
            }
        }
        return result;
    }

    // ---- Members ----

    public Member addMember(String name) {
        name = clean(name);
        if (name.isEmpty()) {
            throw new LibraryException("Member name is required.");
        }
        Member member = new Member(nextMemberId++, name);
        members.put(member.getId(), member);
        return member;
    }

    public Member getMember(int id) {
        Member member = members.get(id);
        if (member == null) {
            throw new LibraryException("No member with id " + id + ".");
        }
        return member;
    }

    public Collection<Member> getMembers() {
        return members.values();
    }

    public List<Book> getBooksBorrowedBy(int memberId) {
        List<Book> result = new ArrayList<>();
        for (Book book : books.values()) {
            if (Integer.valueOf(memberId).equals(book.getBorrowerId())) {
                result.add(book);
            }
        }
        return result;
    }

    // ---- Loans ----

    public Book borrowBook(String isbn, int memberId, LocalDate today) {
        Book book = getBook(isbn);
        Member member = getMember(memberId);
        if (!book.isAvailable()) {
            throw new LibraryException("\"" + book.getTitle() + "\" is already borrowed.");
        }
        if (getBooksBorrowedBy(memberId).size() >= Member.MAX_BOOKS) {
            throw new LibraryException(member.getName() + " already has " + Member.MAX_BOOKS + " books.");
        }
        book.checkOut(memberId, today.plusDays(LOAN_DAYS));
        return book;
    }

    /** Returns the book and gives back the late fine in taka (0 if on time). */
    public int returnBook(String isbn, LocalDate today) {
        Book book = getBook(isbn);
        if (book.isAvailable()) {
            throw new LibraryException("\"" + book.getTitle() + "\" is not borrowed.");
        }
        long daysLate = Math.max(0, today.toEpochDay() - book.getDueDate().toEpochDay());
        book.checkIn();
        return (int) daysLate * FINE_PER_DAY;
    }

    // ---- Persistence (simple tab-separated text file) ----

    public void save(Path file) throws IOException {
        List<String> lines = new ArrayList<>();
        for (Member m : members.values()) {
            lines.add(String.join("\t", "MEMBER", String.valueOf(m.getId()), m.getName()));
        }
        for (Book b : books.values()) {
            lines.add(String.join("\t", "BOOK", b.getIsbn(), b.getTitle(), b.getAuthor(),
                    String.valueOf(b.getYear()),
                    b.getBorrowerId() == null ? "" : String.valueOf(b.getBorrowerId()),
                    b.getDueDate() == null ? "" : b.getDueDate().toString()));
        }
        Files.write(file, lines, StandardCharsets.UTF_8);
    }

    public static Library load(Path file) throws IOException {
        Library library = new Library();
        if (!Files.exists(file)) {
            return library;
        }
        for (String line : Files.readAllLines(file, StandardCharsets.UTF_8)) {
            String[] p = line.split("\t", -1);
            if (p[0].equals("MEMBER")) {
                int id = Integer.parseInt(p[1]);
                library.members.put(id, new Member(id, p[2]));
                library.nextMemberId = Math.max(library.nextMemberId, id + 1);
            } else if (p[0].equals("BOOK")) {
                Book book = new Book(p[1], p[2], p[3], Integer.parseInt(p[4]));
                if (!p[5].isEmpty()) {
                    book.checkOut(Integer.parseInt(p[5]), LocalDate.parse(p[6]));
                }
                library.books.put(book.getIsbn(), book);
            }
        }
        return library;
    }

    /** Trims input and removes tabs/newlines so it cannot break the save file. */
    private static String clean(String s) {
        return s == null ? "" : s.replaceAll("[\\t\\r\\n]", " ").trim();
    }
}
