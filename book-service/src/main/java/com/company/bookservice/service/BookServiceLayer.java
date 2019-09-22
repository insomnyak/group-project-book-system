package com.company.bookservice.service;

import com.company.bookservice.dao.BookDao;
import com.company.bookservice.exception.QueueRequestTimeoutException;
import com.company.bookservice.model.Book;
import com.company.bookservice.util.MapClasses;
import com.company.bookservice.util.feign.NoteServiceClient;
import com.company.bookservice.viewmodel.BookViewModel;
import com.company.queue.shared.viewmodel.NoteViewModel;
import org.springframework.amqp.rabbit.AsyncRabbitTemplate;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Primary;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.stream.Collectors;

@Component @Primary
public class BookServiceLayer {

    public static final Long TIMEOUT = 1L;
    public static final TimeUnit TIMEOUT_UNIT = TimeUnit.SECONDS;

    private BookDao bookDao;
    private NoteServiceLayer nsl;

    @Autowired
    public BookServiceLayer(BookDao bookDao, NoteServiceLayer nsl) {
        this.bookDao = bookDao;
        this.nsl = nsl;
    }

    @Transactional
    public BookViewModel saveBook(BookViewModel bookViewModel) {

        // mapClasses is getting all Book View Model fields and adding to Book object
        Book book = (new MapClasses<>(bookViewModel, Book.class))
                .mapFirstToSecond(false, false);
        book = bookDao.addBook(book);

        if (bookViewModel.getNotes() != null) {
            /* if NoteViewModel list is not empty then we call the feign service (NoteServiceClient) to retrieve
         all NoteViewModel to add the notes to the NoteDAO and then setting the ID on the return note*/
            List<CompletableFuture<NoteViewModel>> listCf = new ArrayList<>();
            if (bookViewModel != null && !bookViewModel.getNotes().isEmpty()) {
                Book finalBook = book;
                bookViewModel.getNotes().stream().forEach(nvm -> {
                    if (nvm == null || nvm.getNote() == null || nvm.getNote().trim().length() == 0) {
                        throw new IllegalArgumentException("Please provide content for the note.");
                    }
                    nvm.setBookId(finalBook.getBookId());
                    CompletableFuture<NoteViewModel> cf = CompletableFuture.supplyAsync(System::nanoTime)
                            .thenApply(start -> {
                                NoteViewModel nvm2 = nsl.saveNote(nvm);
                                return nvm2;
                            }).thenApply(noteViewModel -> {
                                nvm.setNoteId(noteViewModel.getNoteId());
                                return nvm;
                            });
                    listCf.add(cf);
                });
                CompletableFuture<Void> allFutures =
                        CompletableFuture.allOf(listCf.toArray(new CompletableFuture[listCf.size()]));
                try {
                    allFutures.get(TIMEOUT, TIMEOUT_UNIT);
                } catch (InterruptedException | ExecutionException e) {
                    throw new RuntimeException(e.getCause() + " | " + e.getMessage());
                } catch (TimeoutException e) {
                    throw new QueueRequestTimeoutException("The request timed out while waiting for fulfillment. " +
                            "Your new book has been placed in a queue and will be added shortly.");
                }
            }
        } else {
            bookViewModel.setNotes(new ArrayList<>());
        }

        bookViewModel.setBookId(book.getBookId());

        return bookViewModel;
    }

    private BookViewModel buildBookViewModel(Book book) {

        BookViewModel bookViewModel = (new MapClasses<>(book, BookViewModel.class))
                .mapFirstToSecond(false, false);

        List<NoteViewModel> noteViewModelList = nsl.findAllNotesByBookId(book.getBookId());
        noteViewModelList = noteViewModelList == null ? new ArrayList<>() : noteViewModelList;
        bookViewModel.setNotes(noteViewModelList);

        return bookViewModel;
    }

    public List<BookViewModel> findAllBooks(){
        // 1. get all books
        List<Book> bookList = bookDao.getAllBooks();
        // 2. get all notes
        List<NoteViewModel> noteViewModelList = nsl.findAllNotes();
        // 3. instantiate bookViewModeList
        List<BookViewModel> bookViewModelList = new ArrayList<>();

        // 4. for each book in bookList
        for(Book book : bookList){
            // i. create bvm and set each book property
            BookViewModel bvm = (new MapClasses<>(book, BookViewModel.class))
                    .mapFirstToSecond(false, false);
            // ii. select notes where nvm.getBookId == book.getBookId >> and set bvm notes list accordingly
            bvm.setNotes(noteViewModelList.stream().filter(nvm -> nvm.getBookId().equals(book.getBookId()))
                    .collect(Collectors.toList()));
            // iii. add bvm to bookViewModelList
            bookViewModelList.add(bvm);
            //BookViewModel bvm = buildBookViewModel(b);
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
        nsl.removeNotesByBookId(bookId);

        Book book = bookDao.getBookById(bookId);

        if(book == null)
            throw new NoSuchElementException(String.format("No Book with id %s found", bookId));
        else
            bookDao.deleteBook(bookId);
    }

    @Transactional
    public void updateBook(Integer bookId, BookViewModel bookViewModel) {
        if (bookViewModel.getBookId() == null) {
            bookViewModel.setBookId(bookId);
        }
        if (!bookId.equals(bookViewModel.getBookId())) {
            throw new IllegalArgumentException("Id in the path must match the Id in the Book object");
        }

        // mapClasses is getting all Book View Model fields and adding to Book object
        Book book = (new MapClasses<>(bookViewModel, Book.class))
                .mapFirstToSecond(false, false);

        Book bookCheck = bookDao.getBookById(bookId);
        if(bookCheck == null) {
            throw new NoSuchElementException(String.format("No Book with id %s found", bookId));
        } else {
            bookDao.updateBook(book);
        }
        List<NoteViewModel> newNotes = bookViewModel.getNotes();
        if (newNotes == null || newNotes.isEmpty()) {
            nsl.removeNotesByBookId(bookId);
        } else {
            // update/delete/add notes

            /*
            1. get the currentNotes
            2. filter currentNotes for ones not present in newNotes >> notesToDelete
            3. delete notesToDelete
            4. for newNotes
                - if id is present:
                    i. check that id is in currentNotes >> inCurrentNotes
                    ii. if id is found >> update
                    iii. if id is not found >> create new note and update id
                - if id is not present:
                    i. create new note and update id
             */

            // 1. get currentNotes
            List<NoteViewModel> currentNotes = nsl.findAllNotesByBookId(bookId);

            // 2. filter currentNotes for ones not present in newNotes >> notesToDelete
            List<NoteViewModel> notesToDelete = currentNotes.stream().filter(nvm -> {
                for (NoteViewModel newNote : newNotes) {
                    if (nvm.getNoteId().equals(newNote.getNoteId())) return false;
                }
                return true;
            }).collect(Collectors.toList());

            // 3. delete notesToDelete
            notesToDelete.forEach(nvm -> nsl.removeNote(nvm.getNoteId()));

            // 4. for newNotes
            for (NoteViewModel newNote : newNotes) {
                if (newNote == null) continue;
                // - if id is present:
                if (newNote.getNoteId() != null) {
                    // i. check that id is in currentNotes
                    boolean inCurrentNotes = currentNotes.stream()
                            .anyMatch(nvm -> nvm.getNoteId().equals(newNote.getNoteId()));
                    // ii. if id is found >> update
                    if (inCurrentNotes) {
                        nsl.updateNote(newNote);
                    } else {
                        // iii. if id is not found >> create new note and update id
                        if (newNote.getNote() == null || newNote.getNote().trim().length() == 0) {
                            throw new IllegalArgumentException("Please provide content for the note.");
                        }
                        newNote.setBookId(bookId);
                        NoteViewModel createdNote = nsl.saveNote(newNote);
                        newNote.setNoteId(createdNote.getNoteId());
                    }
                } else {
                    // - if id is not present:
                    // i. create new note and update id
                    newNote.setBookId(bookId);
                    NoteViewModel createdNote = nsl.saveNote(newNote);
                    newNote.setNoteId(createdNote.getNoteId());
                }
            }
        }
    }
}
