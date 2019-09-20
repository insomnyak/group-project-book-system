package com.company.bookservice.util.feign;

import com.company.bookservice.viewmodel.NoteViewModel;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

import java.util.List;

@FeignClient(name="note-service")
public interface NoteServiceClient {

    @RequestMapping(value = "/notes", method = RequestMethod.POST)
    NoteViewModel createNote(@RequestBody NoteViewModel nvm);

    @RequestMapping(value = "/notes/{id}", method = RequestMethod.GET)
    NoteViewModel getNote(@PathVariable(value = "id") Integer noteId);

    @RequestMapping(value = "/notes/book/{book_id}", method = RequestMethod.GET)
    List<NoteViewModel> getNotesByBookId(@PathVariable(value = "book_id") Integer bookId);

    @RequestMapping(value = "/notes", method = RequestMethod.GET)
    List<NoteViewModel> getAllNotes();

    @RequestMapping(value = "/notes/{id}", method = RequestMethod.PUT)
    void updateNote(@PathVariable Integer id, @RequestBody NoteViewModel nvm);

    @RequestMapping(value = "/notes/{id}", method = RequestMethod.DELETE)
    void deleteNote(@PathVariable(value = "id") Integer noteId);

    @RequestMapping(value = "/notes/book/{book_id}", method = RequestMethod.DELETE)
    void deleteNotesByBookId(@PathVariable(value = "book_id") Integer bookId);

}
