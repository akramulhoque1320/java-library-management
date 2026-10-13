package com.ahnaf.library;

public class Member {
    public static final int MAX_BOOKS = 3;

    private final int id;
    private final String name;

    public Member(int id, String name) {
        this.id = id;
        this.name = name;
    }

    public int getId() { return id; }
    public String getName() { return name; }

    @Override
    public String toString() {
        return String.format("#%-4d %s", id, name);
    }
}
