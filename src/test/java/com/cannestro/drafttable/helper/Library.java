package com.cannestro.drafttable.helper;

import lombok.AllArgsConstructor;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;

import java.util.ArrayList;
import java.util.List;


@AllArgsConstructor
public class Library {

    private final List<Book> books;
    private final List<Book> archivedBooks;


    public Library() {
        this(new ArrayList<>(), new ArrayList<>());
    }

    public static Library defaultLibrary() {
        List<Book> bookList = List.of(
                new Book("Later", "C. U. Dude"),
                new Book("Later", "C. U. Dude"),
                new Book("Masterpiece", "No Name")
        );
        return new Library(new ArrayList<>(bookList), new ArrayList<>());
    }

    public void add(Book book) {
        books.add(book);
    }

    public void addSomething() {
        books.add(new Book("Something", "Someone"));
    }

    public void archiveSomething() {
        if (!books.isEmpty())
            archivedBooks.add(books.remove(0));
    }

    public int numberOfBooks() {
        return books.size();
    }

    public int archiveSize() {
        return books.size();
    }

    @Override
    public String toString() {
        return ToStringBuilder.reflectionToString(this, ToStringStyle.JSON_STYLE);
    }

}
