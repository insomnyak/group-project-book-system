package com.company.bookservice.util.feign;

import com.company.bookservice.viewmodel.NoteViewModel;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

import java.util.List;

@FeignClient(name="note-service")
public interface NoteServiceClient {

    @RequestMapping(value = "/notes", method = RequestMethod.POST)
    NoteViewModel createNote(NoteViewModel nvm);

    @RequestMapping(value = "/notes/{id}", method = RequestMethod.GET)
    NoteViewModel getNote(Integer noteId);

    @RequestMapping(value = "/notes/book/{book_id}", method = RequestMethod.GET)
    List<NoteViewModel> getNotesByBookId(Integer bookId);

    @RequestMapping(value = "/notes", method = RequestMethod.GET)
    List<NoteViewModel> getAllNotes();

    @RequestMapping(value = "/notes/{id}", method = RequestMethod.PUT)
    void updateNote(NoteViewModel nvm);

    @RequestMapping(value = "/notes/{id}", method = RequestMethod.DELETE)
    void deleteNote(Integer noteId);

}
