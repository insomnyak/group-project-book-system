package com.company.bookservice.service;

import com.company.bookservice.dao.BookDao;
import com.company.bookservice.model.Book;
import com.company.bookservice.util.MapClasses;
import com.company.bookservice.util.feign.NoteServiceClient;
import com.company.bookservice.viewmodel.BookViewModel;
import com.company.bookservice.viewmodel.NoteViewModel;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.NoSuchElementException;

@Component
public class ServiceLayer {

    BookDao bookDao;
    NoteServiceClient noteServiceClient;

    @Autowired
    public ServiceLayer(BookDao bookDao, NoteServiceClient noteServiceClient) {
        this.bookDao = bookDao;
        this.noteServiceClient = noteServiceClient;
    }

    @Transactional
    public BookViewModel saveBook(BookViewModel bookViewModel) {

        // mapClasses is getting all Book View Model fields and adding to Book object
        Book book = (new MapClasses<>(bookViewModel, Book.class))
                .mapFirstToSecond(false, false);

        /* if NoteViewModel list is not empty then we call the feign service (NoteServiceClient) to retrieve
         all NoteViewModel to add the notes to the NoteDAO and then setting the ID on the return note*/
        if (bookViewModel != null && !bookViewModel.getNoteViewModelList().isEmpty()) {
            bookViewModel.getNoteViewModelList().stream().forEach(note -> {
                NoteViewModel nvm = noteServiceClient.createNote(note);
                note.setNoteId(nvm.getNoteId());
            });
        }

        bookDao.addBook(book);
        bookViewModel.setBookId(book.getBookId());

        return bookViewModel;
    }

    private BookViewModel buildBookViewModel(Book book) {

        BookViewModel bookViewModel = (new MapClasses<>(book, BookViewModel.class))
                .mapFirstToSecond(false, false);

        List<NoteViewModel> noteViewModelList = noteServiceClient.getNotesByBookId(book.getBookId());
        bookViewModel.setNoteViewModelList(noteViewModelList);

        return bookViewModel;
    }

    public List<BookViewModel> findAllBooks(){

        List<Book> bookList = bookDao.getAllBooks();

        List<BookViewModel> bookViewModelList = new ArrayList<>();

        for(Book b : bookList){
            BookViewModel bvm = buildBookViewModel(b);
            bookViewModelList.add(bvm);
        }
        return bookViewModelList;
    }

    public BookViewModel findBook (Integer bookId) {

        Book book = bookDao.getBookById(bookId);

        if(book == null)
            throw new NoSuchElementException(String.format("No Book with id %s found", bookId));
        else
            return buildBookViewModel(book);
    }

    public void removeBook(Integer bookId) {

        Book book = bookDao.getBookById(bookId);

        if(book == null)
            throw new NoSuchElementException(String.format("No Book with id %s found", bookId));
        else
            bookDao.deleteBook(bookId);
    }


    public void updateBook(BookViewModel bookViewModel) {
        // mapClasses is getting all Book View Model fields and adding to Book object
        Book book = (new MapClasses<>(bookViewModel, Book.class))
                .mapFirstToSecond(false, false);

        bookDao.updateBook(book);

        if (bookViewModel != null && !bookViewModel.getNoteViewModelList().isEmpty()) {
            bookViewModel.getNoteViewModelList().stream().forEach(note -> {
                noteServiceClient.updateNote(note);
            });
        }
    }
}
