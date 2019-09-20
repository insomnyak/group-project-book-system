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

        /* if NoteViewModel list is not empty then we call the feign service (NoteServiceClient) to retrieve
         all NoteViewModel to add the notes to the NoteDAO and then setting the ID on the return note*/
        if (bookViewModel != null && !bookViewModel.getNoteViewModelList().isEmpty()) {
            bookViewModel.getNoteViewModelList().stream().forEach(nvm -> {
                if (nvm == null || nvm.getNote() == null || nvm.getNote().trim().length() == 0) {
                    throw new IllegalArgumentException("Please provide content for the note.");
                }
                NoteViewModel nvm2 = noteServiceClient.createNote(nvm);
                nvm.setNoteId(nvm2.getNoteId());
            });
        }

        book = bookDao.addBook(book);
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
        noteServiceClient.deleteNotesByBookId(bookId);

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

        bookDao.updateBook(book);
        List<NoteViewModel> notes = bookViewModel.getNoteViewModelList();
        if (notes == null || notes.isEmpty()) {
            noteServiceClient.deleteNotesByBookId(bookId);
        } else {
            // update, delete, add
            List<NoteViewModel> noteViewModelList = noteServiceClient.getNotesByBookId(bookId);

            // delete Notes that are not present in received BookViewModel
            List<NoteViewModel> oldNotesNotPresent = noteViewModelList.stream().filter(note -> {
                for (NoteViewModel n: notes) {
                    if (note.getBookId() == n.getBookId()) return true;
                }
                return false;
            }).collect(Collectors.toList());
            oldNotesNotPresent.forEach(n -> noteServiceClient.deleteNote(n.getNoteId()));

            // update/add all notes
            for (NoteViewModel n : notes) {
                if (n.getNote() == null || n.getNote().isEmpty()) {
                    continue;
                }
                n.setBookId(bookId);
                // add with NoteId is null
                if (n.getNoteId() == null) {
                    NoteViewModel newNote = noteServiceClient.createNote(n);
                    n.setNoteId(newNote.getNoteId());
                } else { // update note is NoteId is not null
                    NoteViewModel checkIfNoteExists = noteServiceClient.getNote(n.getNoteId());
                    if (checkIfNoteExists == null) {
                        NoteViewModel newNote = noteServiceClient.createNote(n);
                        n.setNoteId(newNote.getNoteId());
                    } else {
                        noteServiceClient.updateNote(n);
                    }
                }
            }





            bookViewModel.getNoteViewModelList().stream().forEach(note -> {
                noteServiceClient.updateNote(note);
            });
        }
    }

    @Transactional
    public NoteViewModel saveNote(NoteViewModel nvm) {
        String note = nvm.getNote();
        if (note == null || note.trim().length() == 0) {
            throw new IllegalArgumentException("Please provide a non-empty note.");
        }

        validateNote(nvm);

        return noteServiceClient.createNote(nvm);
    }

    public List<NoteViewModel> findAllNotes() {
        return noteServiceClient.getAllNotes();
    }

    public void updateNote(NoteViewModel nvm) {
        validateNote(nvm);
        noteServiceClient.updateNote(nvm);
    }

    public void removeNote(Integer noteId) {
        NoteViewModel nvm = noteServiceClient.getNote(noteId);
        if (nvm == null)
            throw new NoSuchElementException(String.format("No Note with id %s found", noteId));
        noteServiceClient.deleteNote(noteId);
    }

    public void validateNote(NoteViewModel nvm) {
        if (nvm.getBookId() == null)
            throw new IllegalArgumentException("Please provide a bookId");

        Book book = bookDao.getBookById(nvm.getBookId());
        if(book == null)
            throw new NoSuchElementException(String.format("No Book with id %s found", nvm.getBookId()));
    }
}
