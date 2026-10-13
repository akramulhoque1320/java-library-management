package com.ahnaf.library;

import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDate;
import java.util.Collection;
import java.util.Scanner;

public class Main {
    private static final Path DATA_FILE = Paths.get("library.dat");

    private final Scanner in = new Scanner(System.in);
    private final Library library;

    private Main(Library library) {
        this.library = library;
    }

    public static void main(String[] args) throws IOException {
        Library library = Library.load(DATA_FILE);
        if (library.getBooks().isEmpty() && library.getMembers().isEmpty()) {
            seed(library);
        }
        new Main(library).run();
    }

    private static void seed(Library library) {
        library.addBook("978-0132350884", "Clean Code", "Robert C. Martin", 2008);
        library.addBook("978-0134685991", "Effective Java", "Joshua Bloch", 2018);
        library.addBook("978-0596009205", "Head First Java", "Kathy Sierra", 2005);
        library.addBook("978-1593279288", "Python Crash Course", "Eric Matthes", 2019);
        library.addMember("Ahnaf");
        library.addMember("Rahim");
    }

    private void run() throws IOException {
        System.out.println("=== Library Management System ===");
        while (true) {
            System.out.println();
            System.out.println("1) List books        2) Add book        3) Remove book");
            System.out.println("4) Search books      5) List members    6) Add member");
            System.out.println("7) Borrow book       8) Return book     9) Overdue books");
            System.out.println("0) Save and exit");
            String choice = prompt("Choose");
            try {
                switch (choice) {
                    case "1" -> print(library.getBooks(), "No books yet.");
                    case "2" -> {
                        Book b = library.addBook(prompt("ISBN"), prompt("Title"), prompt("Author"),
                                Integer.parseInt(prompt("Year")));
                        System.out.println("Added: " + b.getTitle());
                    }
                    case "3" -> {
                        library.removeBook(prompt("ISBN"));
                        System.out.println("Book removed.");
                    }
                    case "4" -> print(library.search(prompt("Title or author")), "No matching books.");
                    case "5" -> {
                        for (Member m : library.getMembers()) {
                            System.out.println(m + "  (" + library.getBooksBorrowedBy(m.getId()).size() + " books)");
                        }
                    }
                    case "6" -> System.out.println("Added member " + library.addMember(prompt("Name")));
                    case "7" -> {
                        Book b = library.borrowBook(prompt("ISBN"), Integer.parseInt(prompt("Member id")), LocalDate.now());
                        System.out.println("Borrowed \"" + b.getTitle() + "\". Due back on " + b.getDueDate() + ".");
                    }
                    case "8" -> {
                        int fine = library.returnBook(prompt("ISBN"), LocalDate.now());
                        System.out.println(fine > 0 ? "Returned late. Fine: " + fine + " taka." : "Returned on time. Thank you!");
                    }
                    case "9" -> print(library.getOverdueBooks(LocalDate.now()), "No overdue books.");
                    case "0" -> {
                        library.save(DATA_FILE);
                        System.out.println("Saved to " + DATA_FILE + ". Goodbye!");
                        return;
                    }
                    default -> System.out.println("Please choose 0-9.");
                }
            } catch (LibraryException e) {
                System.out.println("Error: " + e.getMessage());
            } catch (NumberFormatException e) {
                System.out.println("Error: please enter a number.");
            }
        }
    }

    private String prompt(String label) {
        System.out.print(label + ": ");
        return in.hasNextLine() ? in.nextLine().trim() : "0";
    }

    private static void print(Collection<Book> books, String emptyMessage) {
        if (books.isEmpty()) {
            System.out.println(emptyMessage);
        }
        books.forEach(System.out::println);
    }
}
