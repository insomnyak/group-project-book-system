package com.company.bookservice.dao;

import com.company.bookservice.model.Book;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;


import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

@RunWith(SpringJUnit4ClassRunner.class)
@SpringBootTest
public class BookDaoJdbcTemplateImplTest {

    @Autowired
    protected BookDao bookDao;

    @Before
    public void setUp() throws Exception {

        List<Book> bookList = bookDao.getAllBooks();
        bookList.stream().forEach(book -> bookDao.deleteBook(book.getBookId()));

    }

    @After
    public void tearDown() throws Exception {

    }

    @Test
    public void addGetDeleteBook(){

        Book book = new Book();
        book.setTitle("It");
        book.setAuthor("Stephen King");

        book = bookDao.addBook(book);

        Book book1 = bookDao.getBookById(book.getBookId());

        assertEquals(book,book1);

        bookDao.deleteBook(book.getBookId());

        book1 = bookDao.getBookById(book.getBookId());

        assertNull(book1);

    }

    public void getAllBooks(){

        Book book = new Book();
        book.setTitle("It");
        book.setAuthor("Stephen King");

        book = bookDao.addBook(book);

        book = new Book();
        book.setTitle("One FLew Over the Cuckoo's Nest");
        book.setAuthor("Ken Kesey");

        book = bookDao.addBook(book);

        List<Book> bookList = bookDao.getAllBooks();

        assertEquals(2,bookList.size());

    }

    public void updateBook(){

        Book book = new Book();
        book.setTitle("It");
        book.setAuthor("Stephen King");

        book = bookDao.addBook(book);

        book.setTitle("UPDATE");
        book.setAuthor("UPDATE");

        bookDao.updateBook(book);

        Book book1 = bookDao.getBookById(book.getBookId());

        assertEquals(book,book1);

    }
}
