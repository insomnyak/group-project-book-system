package com.company.noteservice.controller;

import com.company.noteservice.servicelayer.ServiceLayer;
import com.company.noteservice.viewmodel.NoteViewModel;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.List;

@RestControllerAdvice
@RefreshScope
public class ControllerExceptionHandler {

    @Autowired
    ServiceLayer sl;

    @RequestMapping(value = "/notes", method = RequestMethod.POST)
    NoteViewModel createNote(NoteViewModel nvm) {
        return sl.add(nvm);
    }

    @RequestMapping(value = "/notes/{id}", method = RequestMethod.GET)
    NoteViewModel getNote(Integer noteId) {
        return sl.get(noteId);
    }

    @RequestMapping(value = "/notes/book/{book_id}", method = RequestMethod.GET)
    List<NoteViewModel> getNotesByBookId(Integer bookId) {
        return sl.getAllByBookId(bookId);
    }

    @RequestMapping(value = "/notes", method = RequestMethod.GET)
    List<NoteViewModel> getAllNotes() {
        return sl.getAll();
    }

    @RequestMapping(value = "/notes/{id}", method = RequestMethod.PUT)
    void updateNote(NoteViewModel nvm) {
        sl.update(nvm);
    }

    @RequestMapping(value = "/notes/{id}", method = RequestMethod.DELETE)
    void deleteNote(Integer noteId) {
        sl.delete(noteId);
    }
}
