package com.ahnaf.library;

import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDate;

/** Dependency-free tests: run with `java -cp out com.ahnaf.library.LibraryTest`. */
public class LibraryTest {
    private static final LocalDate TODAY = LocalDate.of(2026, 10, 1);
    private static int passed = 0;

    public static void main(String[] args) throws Exception {
        test("add and find books", () -> {
            Library lib = new Library();
            lib.addBook("1", "Clean Code", "Robert Martin", 2008);
            check(lib.getBook("1").getTitle().equals("Clean Code"), "book should be found by ISBN");
            expectError(() -> lib.addBook("1", "Duplicate", "X", 2000));
        });

        test("search by title or author, ignoring case", () -> {
            Library lib = new Library();
            lib.addBook("1", "Clean Code", "Robert Martin", 2008);
            lib.addBook("2", "Effective Java", "Joshua Bloch", 2018);
            check(lib.search("java").size() == 1, "title search");
            check(lib.search("MARTIN").size() == 1, "author search");
            check(lib.search("python").isEmpty(), "no match");
        });

        test("borrow and return on time", () -> {
            Library lib = new Library();
            lib.addBook("1", "Clean Code", "Robert Martin", 2008);
            Member m = lib.addMember("Ahnaf");
            Book b = lib.borrowBook("1", m.getId(), TODAY);
            check(b.getDueDate().equals(TODAY.plusDays(14)), "due in 14 days");
            expectError(() -> lib.borrowBook("1", m.getId(), TODAY));
            check(lib.returnBook("1", TODAY.plusDays(14)) == 0, "no fine on due date");
            check(lib.getBook("1").isAvailable(), "book back on shelf");
            expectError(() -> lib.returnBook("1", TODAY));
        });

        test("late return is fined", () -> {
            Library lib = new Library();
            lib.addBook("1", "Clean Code", "Robert Martin", 2008);
            Member m = lib.addMember("Ahnaf");
            lib.borrowBook("1", m.getId(), TODAY);
            check(lib.getOverdueBooks(TODAY.plusDays(17)).size() == 1, "overdue after due date");
            check(lib.returnBook("1", TODAY.plusDays(17)) == 30, "3 days late = 30 taka");
        });

        test("member borrowing limit", () -> {
            Library lib = new Library();
            Member m = lib.addMember("Ahnaf");
            for (int i = 1; i <= 4; i++) {
                lib.addBook(String.valueOf(i), "Book " + i, "Author", 2020);
            }
            for (int i = 1; i <= 3; i++) {
                lib.borrowBook(String.valueOf(i), m.getId(), TODAY);
            }
            expectError(() -> lib.borrowBook("4", m.getId(), TODAY));
        });

        test("cannot remove a borrowed book", () -> {
            Library lib = new Library();
            lib.addBook("1", "Clean Code", "Robert Martin", 2008);
            lib.borrowBook("1", lib.addMember("Ahnaf").getId(), TODAY);
            expectError(() -> lib.removeBook("1"));
        });

        test("save and load round trip", () -> {
            Library lib = new Library();
            lib.addBook("1", "Clean Code", "Robert Martin", 2008);
            lib.addBook("2", "Effective Java", "Joshua Bloch", 2018);
            Member m = lib.addMember("Ahnaf");
            lib.borrowBook("2", m.getId(), TODAY);

            Path file = Files.createTempFile("library", ".dat");
            try {
                lib.save(file);
                Library loaded = Library.load(file);
                check(loaded.getBooks().size() == 2, "books restored");
                check(loaded.getMember(1).getName().equals("Ahnaf"), "member restored");
                check(!loaded.getBook("2").isAvailable(), "loan restored");
                check(loaded.getBook("2").getDueDate().equals(TODAY.plusDays(14)), "due date restored");
                check(loaded.addMember("Rahim").getId() == 2, "member ids continue");
            } finally {
                Files.deleteIfExists(file);
            }
        });

        System.out.println("\nAll " + passed + " tests passed.");
    }

    // ---- tiny test helpers ----

    interface TestBody {
        void run() throws Exception;
    }

    private static void test(String name, TestBody body) throws Exception {
        body.run();
        passed++;
        System.out.println("PASS  " + name);
    }

    private static void check(boolean condition, String message) {
        if (!condition) {
            throw new AssertionError("FAIL: " + message);
        }
    }

    private static void expectError(Runnable action) {
        try {
            action.run();
        } catch (LibraryException expected) {
            return;
        }
        throw new AssertionError("FAIL: expected a LibraryException");
    }
}
