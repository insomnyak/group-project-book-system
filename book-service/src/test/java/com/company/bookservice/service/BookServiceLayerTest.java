package com.company.bookservice.service;

import com.company.bookservice.dao.BookDao;
import com.company.bookservice.dao.BookDaoJdbcTemplateImpl;
import com.company.bookservice.model.Book;
import com.company.bookservice.util.feign.NoteServiceClient;
import com.company.bookservice.viewmodel.BookViewModel;
import com.company.queue.shared.viewmodel.NoteViewModel;
import org.junit.Before;
import org.junit.Test;
import org.springframework.amqp.rabbit.AsyncRabbitTemplate;

import java.util.ArrayList;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeoutException;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.*;


public class BookServiceLayerTest {

    public static final String EXCHANGE = "note-exchange";
    public static final String ROUTING_KEY_ADD = "note.add.book.service";
    public static final String ROUTING_KEY_UPDATE = "note.update.book.service";
    public static final String ROUTING_KEY_DELETE = "note.delete.book.service";
    public static final String ROUTING_KEY_DELETE_BY_BOOK_ID = "note.deleteByBookId.book.service";

    BookServiceLayer bookServiceLayer;

    BookDao bookDao;
    NoteServiceLayer nsl;

    @Before
    public void setUp() throws Exception {

        setUpBookDaoMock();
        setUpNoteServiceLayerMock();

        bookServiceLayer = new BookServiceLayer(bookDao, nsl);
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

    private void setUpNoteServiceLayerMock(){

        nsl = mock(NoteServiceLayer.class);

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
        doReturn(noteViewModel).when(nsl).saveNote(noteViewModel1);

        // mock getNotesByBook
        doReturn(noteViewModelList).when(nsl).findAllNotesByBookId(1);

        // mock get all
        doReturn(noteViewModelList).when(nsl).findAllNotes();

        NoteViewModel updateNote = new NoteViewModel();
        updateNote.setNoteId(200);
        updateNote.setBookId(2);
        updateNote.setNote("The apprentice successful turned himself into the wind.");

        List<NoteViewModel> updateNoteList = new ArrayList<>();
        updateNoteList.add(updateNote);

        // mock update
        doNothing().when(nsl).updateNote(updateNote);
        doReturn(updateNoteList).when(nsl).findAllNotesByBookId(2);

        // mock delete
        doNothing().when(nsl).removeNotesByBookId(3);

        // mock trying to get notes from a book that does not exist
        doReturn(new ArrayList<NoteViewModel>()).when(nsl).findAllNotesByBookId(3);
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

        bvm = bookServiceLayer.saveBook(bvm);

        BookViewModel bvm2 = bookServiceLayer.findBook(bvm.getBookId());

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

        bookServiceLayer.updateBook(bookUpdate.getBookId(), bvmUpdate);

        BookViewModel bvmAfterUpdate = bookServiceLayer.findBook(bvmUpdate.getBookId());

        assertEquals(bvmAfterUpdate, bvmUpdate);
    }

    @Test(expected = NoSuchElementException.class)
    public void deleteBookViewModel() {

        bookServiceLayer.removeBook(3);

        BookViewModel bvm = bookServiceLayer.findBook(3);

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

        bookServiceLayer.updateBook(3, bvm);
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

        bookServiceLayer.saveBook(bvm);
    }
}
