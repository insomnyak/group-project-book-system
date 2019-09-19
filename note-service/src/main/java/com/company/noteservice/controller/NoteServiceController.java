package com.company.noteservice.controller;

import com.company.noteservice.dao.NoteDao;
import com.company.noteservice.model.Note;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RefreshScope
public class NoteServiceController {

    @Autowired
    NoteDao noteDao;

    @RequestMapping(value = "/notes", method = RequestMethod.POST)
    Note createNote(Note note) {
        return noteDao.add(note);
    }

    @RequestMapping(value = "/notes/{id}", method = RequestMethod.GET)
    Note getNote(Integer noteId) {
        return noteDao.find(noteId);
    }

    @RequestMapping(value = "/notes/book/{book_id}", method = RequestMethod.GET)
    List<Note> getNotesByBookId(Integer bookId) {
        return noteDao.findByBookId(bookId);
    }

    @RequestMapping(value = "/notes", method = RequestMethod.GET)
    List<Note> getAllNotes() {
        return noteDao.findAll();
    }

    @RequestMapping(value = "/notes/{id}", method = RequestMethod.PUT)
    void updateNote(Note note) {
        noteDao.update(note);
    }

    @RequestMapping(value = "/notes/{id}", method = RequestMethod.DELETE)
    void deleteNote(Integer noteId) {
        noteDao.delete(noteId);
    }

}
