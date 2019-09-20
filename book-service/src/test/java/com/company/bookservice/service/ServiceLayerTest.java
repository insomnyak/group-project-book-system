package com.company.bookservice.service;

import com.company.bookservice.dao.BookDao;
import com.company.bookservice.dao.BookDaoJdbcTemplateImpl;
import com.company.bookservice.model.Book;
import com.company.bookservice.util.feign.NoteServiceClient;
import com.company.bookservice.viewmodel.BookViewModel;
import com.company.bookservice.viewmodel.NoteViewModel;
import org.junit.Before;
import org.junit.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.NoSuchElementException;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.*;


public class ServiceLayerTest {

    ServiceLayer serviceLayer;

    BookDao bookDao;

    NoteServiceClient noteServiceClient;

    @Before
    public void setUp() throws Exception {

        setUpBookDaoMock();
        setUpNoteServiceMock();

        serviceLayer = new ServiceLayer(bookDao, noteServiceClient);
    }

    private void setUpBookDaoMock(){

        bookDao = mock(BookDaoJdbcTemplateImpl.class);

        Book book = new Book();
        book.setBookId(1);
        book.setTitle("Catcher in the Rye");
        book.setAuthor("J.D Salinger");

        Book book1 = new Book();
        book1.setTitle("Catcher in the Rye");
        book1.setAuthor("J.D Salinger");

        List<Book> bookList = new ArrayList<>();
        bookList.add(book);

        // mock addBook
        doReturn(book).when(bookDao).addBook(book1);

        // mock getBook
        doReturn(book).when(bookDao).getBookById(1);

        // mock getAll
        doReturn(bookList).when(bookDao).getAllBooks();

        // Update mock data
        Book bookUpdate = new Book();
        bookUpdate.setBookId(2);
        bookUpdate.setTitle("The Alchemist");
        bookUpdate.setAuthor("Paulo Coelho");

        // mock update
        doNothing().when(bookDao).updateBook(bookUpdate);
        doReturn(bookUpdate).when(bookDao).getBookById(2);

        // mock delete
        doNothing().when(bookDao).deleteBook(3);
        doReturn(null).when(bookDao).getBookById(3);
    }

    private void setUpNoteServiceMock(){

        noteServiceClient = mock(NoteServiceClient.class);

        NoteViewModel noteViewModel = new NoteViewModel();
        noteViewModel.setNoteId(100);
        noteViewModel.setBookId(1);
        noteViewModel.setNote("A best-selling novel about post-war alienation told by angst-ridden teen Holden Caulfield.");

        NoteViewModel noteViewModel1 = new NoteViewModel();
        noteViewModel1.setBookId(1);
        noteViewModel1.setNote("A best-selling novel about post-war alienation told by angst-ridden teen Holden Caulfield.");

        List<NoteViewModel> noteViewModelList = new ArrayList<>();
        noteViewModelList.add(noteViewModel);

        // mock add
        doReturn(noteViewModel).when(noteServiceClient).createNote(noteViewModel1);

        // mock get
        doReturn(noteViewModel).when(noteServiceClient).getNote(100);

        // mock get all
        doReturn(noteViewModelList).when(noteServiceClient).getAllNotes();

        // mock get notes by book
        doReturn(noteViewModelList).when(noteServiceClient).getNotesByBookId(1);

        NoteViewModel updateNote = new NoteViewModel();
        updateNote.setNoteId(200);
        updateNote.setBookId(2);
        updateNote.setNote("The apprentice successful turned himself into the wind.");

        List<NoteViewModel> updateNoteList = new ArrayList<>();
        updateNoteList.add(updateNote);

        // mock update
        doNothing().when(noteServiceClient).updateNote(200, updateNote);
        doReturn(updateNote).when(noteServiceClient).getNote(200);
        doReturn(updateNoteList).when(noteServiceClient).getNotesByBookId(2);

        // mock delete
        doNothing().when(noteServiceClient).deleteNote(300);
        doReturn(null).when(noteServiceClient).getNote(300);

        // mock trying to get notes from a book that does not exist
        doReturn(new ArrayList<NoteViewModel>()).when(noteServiceClient).getNotesByBookId(3);
    }

    @Test
    public void addGetBookViewModel() {

        Book book1 = new Book();
        book1.setTitle("Catcher in the Rye");
        book1.setAuthor("J.D Salinger");

        NoteViewModel noteViewModel1 = new NoteViewModel();
        noteViewModel1.setBookId(1);
        noteViewModel1.setNote("A best-selling novel about post-war alienation told by angst-ridden teen Holden Caulfield.");

        List<NoteViewModel> noteViewModelList = new ArrayList<>();
        noteViewModelList.add(noteViewModel1);

        BookViewModel bvm = new BookViewModel();
        bvm.setTitle("Catcher in the Rye");
        bvm.setAuthor("J.D Salinger");
        bvm.setNotes(noteViewModelList);

        bvm = serviceLayer.saveBook(bvm);

        BookViewModel bvm2 = serviceLayer.findBook(bvm.getBookId());

        assertEquals(bvm, bvm2);
    }

    @Test
    public void updateBookViewModel() {

        BookViewModel bvmUpdate = new BookViewModel();

        Book bookUpdate = new Book();
        bookUpdate.setBookId(2);
        bookUpdate.setTitle("The Alchemist");
        bookUpdate.setAuthor("Paulo Coelho");

        NoteViewModel updateNote = new NoteViewModel();
        updateNote.setNoteId(200);
        updateNote.setBookId(2);
        updateNote.setNote("The apprentice successful turned himself into the wind.");

        List<NoteViewModel> noteViewModelList = new ArrayList<>();
        noteViewModelList.add(updateNote);

        bvmUpdate.setBookId(bookUpdate.getBookId());
        bvmUpdate.setAuthor(bookUpdate.getAuthor());
        bvmUpdate.setTitle(bookUpdate.getTitle());
        bvmUpdate.setNotes(noteViewModelList);

        serviceLayer.updateBook(bookUpdate.getBookId(), bvmUpdate);

        BookViewModel bvmAfterUpdate = serviceLayer.findBook(bvmUpdate.getBookId());

        assertEquals(bvmAfterUpdate, bvmUpdate);
    }

    @Test(expected = NoSuchElementException.class)
    public void deleteBookViewModel() {

        serviceLayer.removeBook(3);

        BookViewModel bvm = serviceLayer.findBook(3);

    }

    @Test(expected = IllegalArgumentException.class)
    public void exceptionInconsistentId() {

        BookViewModel bvm = new BookViewModel();

        Book book = new Book();
        book.setBookId(2);
        book.setTitle("The Alchemist");
        book.setAuthor("Paulo Coelho");

        NoteViewModel nvm = new NoteViewModel();
        nvm.setNoteId(200);
        nvm.setBookId(2);
        nvm.setNote("The apprentice successful turned himself into the wind.");

        List<NoteViewModel> noteViewModelList = new ArrayList<>();
        noteViewModelList.add(nvm);

        bvm.setBookId(book.getBookId());
        bvm.setAuthor(book.getAuthor());
        bvm.setTitle(book.getTitle());
        bvm.setNotes(noteViewModelList);

        serviceLayer.updateBook(3, bvm);
    }

    @Test(expected = IllegalArgumentException.class)
    public void checkNoteDescription() {

        BookViewModel bvm = new BookViewModel();

        Book book = new Book();
        book.setBookId(1);
        book.setTitle("Catcher in the Rye");
        book.setAuthor("J.D Salinger");

        NoteViewModel nvm = new NoteViewModel();
        nvm.setNoteId(100);
        nvm.setBookId(1);

        List<NoteViewModel> noteViewModelList = new ArrayList<>();
        noteViewModelList.add(nvm);

        bvm.setBookId(book.getBookId());
        bvm.setAuthor(book.getAuthor());
        bvm.setTitle(book.getTitle());
        bvm.setNotes(noteViewModelList);

        serviceLayer.saveBook(bvm);
    }
}
