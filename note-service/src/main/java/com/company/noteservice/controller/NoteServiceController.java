package com.company.noteservice.controller;

import com.company.noteservice.dao.NoteDao;
import com.company.noteservice.model.Note;
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

    @RequestMapping(value = "/notes", method = RequestMethod.POST)
    Note createNote(@RequestBody Note note) {
        return noteDao.add(note);
    }

    @RequestMapping(value = "/notes/{id}", method = RequestMethod.GET)
    Note getNote(@PathVariable(name = "id") Integer noteId) {
        return noteDao.find(noteId);
    }

    @RequestMapping(value = "/notes/book/{book_id}", method = RequestMethod.GET)
    List<Note> getNotesByBookId(@PathVariable(name = "book_id") Integer bookId) {
        return noteDao.findByBookId(bookId);
    }

    @RequestMapping(value = "/notes", method = RequestMethod.GET)
    List<Note> getAllNotes() {
        return noteDao.findAll();
    }

    @RequestMapping(value = "/notes/{id}", method = RequestMethod.PUT)
    void updateNote(@PathVariable Integer id, @RequestBody Note note) {
        noteDao.update(id, note);
    }

    @RequestMapping(value = "/notes/{id}", method = RequestMethod.DELETE)
    void deleteNote(@PathVariable(name = "id") Integer noteId) {
        noteDao.delete(noteId);
    }

    @RequestMapping(value = "/notes/book/{id}", method = RequestMethod.DELETE)
    @ResponseStatus(HttpStatus.NO_CONTENT)
    void deleteNoteByBookId(@PathVariable(name = "id") Integer id){
        noteDao.deleteByBookId(id);
    }

}
