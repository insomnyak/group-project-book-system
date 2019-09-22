package com.company.bookservice.service;

import com.company.bookservice.util.feign.NoteServiceClient;
import com.company.queue.shared.viewmodel.NoteViewModel;
import org.springframework.amqp.rabbit.AsyncRabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

@Component
public class NoteServiceLayer {

    public static final String EXCHANGE = "note-exchange";
    public static final String ROUTING_KEY_ADD = "note.add.book.service";
    public static final String ROUTING_KEY_UPDATE = "note.update.book.service";
    public static final String ROUTING_KEY_DELETE = "note.delete.book.service";
    public static final String ROUTING_KEY_DELETE_BY_BOOK_ID = "note.deleteByBookId.book.service";
    public static final Long TIMEOUT = 8L;
    public static final TimeUnit TIMEOUT_UNIT = TimeUnit.SECONDS;

    private AsyncRabbitTemplate rabbitTemplate;
    private NoteServiceClient noteServiceClient;

    @Autowired
    public NoteServiceLayer(AsyncRabbitTemplate rabbitTemplate,
                            NoteServiceClient noteServiceClient) {
        this.rabbitTemplate = rabbitTemplate;
        this.noteServiceClient = noteServiceClient;
    }

    @Transactional
    NoteViewModel saveNote(NoteViewModel nvm) {
        try {
            return (NoteViewModel) rabbitTemplate.convertSendAndReceiveAsType(EXCHANGE, ROUTING_KEY_ADD, nvm,
                    ParameterizedTypeReference.forType(NoteViewModel.class)).get(TIMEOUT, TIMEOUT_UNIT);
        } catch (InterruptedException | TimeoutException | ExecutionException e) {
            throw new RuntimeException(e.getCause() + " \n " + e.getMessage());
        }
        //return noteServiceClient.createNote(nvm);
    }

    List<NoteViewModel> findAllNotes() {
        return noteServiceClient.getAllNotes();
    }

    void updateNote(NoteViewModel nvm) {
        rabbitTemplate.convertSendAndReceive(EXCHANGE, ROUTING_KEY_UPDATE, nvm);
        //noteServiceClient.updateNote(nvm.getNoteId(), nvm);
    }

    void removeNote(Integer noteId) {
        rabbitTemplate.convertSendAndReceive(EXCHANGE, ROUTING_KEY_DELETE, noteId);
        //noteServiceClient.deleteNote(noteId);
    }

    void removeNotesByBookId(Integer bookId) {
        rabbitTemplate.convertSendAndReceive(EXCHANGE, ROUTING_KEY_DELETE_BY_BOOK_ID, bookId);
        //noteServiceClient.deleteNotesByBookId(bookId);
    }

    List<NoteViewModel> findAllNotesByBookId(Integer bookId) {
        return noteServiceClient.getNotesByBookId(bookId);
    }
}
