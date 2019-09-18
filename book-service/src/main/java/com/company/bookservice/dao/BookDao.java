package com.company.bookservice.dao;


import com.company.bookservice.model.Book;

import java.util.List;

public interface BookDao {

    Book getBookById (Integer bookId);

    List<Book> getAllBooks();

    Book addBook(Book book);

    void deleteBook(Integer bookId);

    void updateBook(Book book);

}
