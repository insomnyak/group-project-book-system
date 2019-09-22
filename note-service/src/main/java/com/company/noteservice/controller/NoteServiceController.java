package com.company.noteservice.controller;

import com.company.noteservice.dao.NoteDao;
import com.company.noteservice.model.Note;
import com.company.noteservice.service.ServiceLayer;
import com.company.queue.shared.viewmodel.NoteViewModel;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RefreshScope
public class NoteServiceController {

    @Autowired
    NoteDao noteDao;

    @Autowired
    ServiceLayer sl;

    @RequestMapping(value = "/notes", method = RequestMethod.POST)
    NoteViewModel createNote(@RequestBody NoteViewModel nvm) {
        return sl.save(nvm);
    }

    @RequestMapping(value = "/notes/{id}", method = RequestMethod.GET)
    NoteViewModel getNote(@PathVariable(name = "id") Integer noteId) {
        return sl.find(noteId);
    }

    @RequestMapping(value = "/notes/book/{book_id}", method = RequestMethod.GET)
    List<NoteViewModel> getNotesByBookId(@PathVariable(name = "book_id") Integer bookId) {
        return sl.findByBookId(bookId);
    }

    @RequestMapping(value = "/notes", method = RequestMethod.GET)
    List<NoteViewModel> getAllNotes() {
        return sl.findAll();
    }

    @RequestMapping(value = "/notes/{id}", method = RequestMethod.PUT)
    void updateNote(@PathVariable Integer id, @RequestBody NoteViewModel nvm) {
        sl.update(nvm);
    }

    @RequestMapping(value = "/notes/{id}", method = RequestMethod.DELETE)
    void deleteNote(@PathVariable(name = "id") Integer noteId) {
        sl.remove(noteId);
    }

    @RequestMapping(value = "/notes/book/{id}", method = RequestMethod.DELETE)
    @ResponseStatus(HttpStatus.NO_CONTENT)
    void deleteNoteByBookId(@PathVariable(name = "id") Integer id){
        sl.removeByBookId(id);
    }

}
