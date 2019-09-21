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
import java.util.stream.Collectors;

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
        book = bookDao.addBook(book);

        /* if NoteViewModel list is not empty then we call the feign service (NoteServiceClient) to retrieve
         all NoteViewModel to add the notes to the NoteDAO and then setting the ID on the return note*/
        if (bookViewModel != null && !bookViewModel.getNotes().isEmpty()) {
            Book finalBook = book;
            bookViewModel.getNotes().stream().forEach(nvm -> {
                if (nvm == null || nvm.getNote() == null || nvm.getNote().trim().length() == 0) {
                    throw new IllegalArgumentException("Please provide content for the note.");
                }
                nvm.setBookId(finalBook.getBookId());
                NoteViewModel nvm2 = saveNote(nvm);
                nvm.setNoteId(nvm2.getNoteId());
            });
        }


        bookViewModel.setBookId(book.getBookId());

        return bookViewModel;
    }

    private BookViewModel buildBookViewModel(Book book) {

        BookViewModel bookViewModel = (new MapClasses<>(book, BookViewModel.class))
                .mapFirstToSecond(false, false);

        List<NoteViewModel> noteViewModelList = findAllNotesByBookId(book.getBookId());
        bookViewModel.setNotes(noteViewModelList);

        return bookViewModel;
    }

    public List<BookViewModel> findAllBooks(){
        // 1. get all books
        List<Book> bookList = bookDao.getAllBooks();
        // 2. get all notes
        List<NoteViewModel> noteViewModelList = findAllNotes();
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
        removeNotesByBookId(bookId);

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
            removeNotesByBookId(bookId);
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
            List<NoteViewModel> currentNotes = findAllNotesByBookId(bookId);

            // 2. filter currentNotes for ones not present in newNotes >> notesToDelete
            List<NoteViewModel> notesToDelete = currentNotes.stream().filter(nvm -> {
                for (NoteViewModel newNote : newNotes) {
                    if (nvm.getNoteId().equals(newNote.getNoteId())) return false;
                }
                return true;
            }).collect(Collectors.toList());

            // 3. delete notesToDelete
            notesToDelete.forEach(nvm -> removeNote(nvm.getNoteId()));

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
                        updateNote(newNote);
                    } else {
                        // iii. if id is not found >> create new note and update id
                        if (newNote.getNote() == null || newNote.getNote().trim().length() == 0) {
                            throw new IllegalArgumentException("Please provide content for the note.");
                        }
                        newNote.setBookId(bookId);
                        NoteViewModel createdNote = saveNote(newNote);
                        newNote.setNoteId(createdNote.getNoteId());
                    }
                } else {
                    // - if id is not present:
                    // i. create new note and update id
                    newNote.setBookId(bookId);
                    NoteViewModel createdNote = saveNote(newNote);
                    newNote.setNoteId(createdNote.getNoteId());
                }
            }
        }
    }

    @Transactional
    private NoteViewModel saveNote(NoteViewModel nvm) {
        return noteServiceClient.createNote(nvm);
    }

    private List<NoteViewModel> findAllNotes() {
        return noteServiceClient.getAllNotes();
    }

    private void updateNote(NoteViewModel nvm) {
        noteServiceClient.updateNote(nvm.getNoteId(), nvm);
    }

    private void removeNote(Integer noteId) {
        noteServiceClient.deleteNote(noteId);
    }

    private void removeNotesByBookId(Integer bookId) {
        noteServiceClient.deleteNotesByBookId(bookId);
    }

    private List<NoteViewModel> findAllNotesByBookId(Integer bookId) {
        return noteServiceClient.getNotesByBookId(bookId);
    }
}
