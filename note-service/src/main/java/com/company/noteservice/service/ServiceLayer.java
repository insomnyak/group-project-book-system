package com.company.noteservice.service;

import com.company.noteservice.dao.NoteDao;
import com.company.noteservice.model.Note;
import com.company.noteservice.util.MapClasses;
import com.company.queue.shared.viewmodel.NoteViewModel;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

@Component
public class ServiceLayer {

    NoteDao noteDao;

    @Autowired
    public ServiceLayer(NoteDao noteDao) {
        this.noteDao = noteDao;
    }

    public NoteViewModel save(NoteViewModel nvm) {
        Note note = (new MapClasses<>(nvm, Note.class)).mapFirstToSecond(false);
        noteDao.add(note);
        nvm.setNoteId(note.getNoteId());
        return nvm;
    }

    public void update(NoteViewModel nvm) {
        Note note = (new MapClasses<>(nvm, Note.class)).mapFirstToSecond(false);
        noteDao.update(nvm.getNoteId(), nvm);
    }

    public void remove(Integer noteId) {
        noteDao.delete(noteId);
    }

    public void removeByBookId(Integer bookId) {
        noteDao.deleteByBookId(bookId);
    }

    public NoteViewModel find(Integer noteId) {
        Note note = noteDao.find(noteId);
        return (new MapClasses<>(note, NoteViewModel.class)).mapFirstToSecond(false);
    }

    public List<NoteViewModel> findByBookId(Integer bookId) {
        List<Note> notes = noteDao.findByBookId(bookId);
        List<NoteViewModel> noteViewModels = new ArrayList<>();
        notes.forEach(note -> noteViewModels
                .add((new MapClasses<>(note, NoteViewModel.class)).mapFirstToSecond(false)));
        return noteViewModels;
    }

    public List<NoteViewModel> findAll() {
        List<Note> notes = noteDao.findAll();
        List<NoteViewModel> noteViewModels = new ArrayList<>();
        notes.forEach(note -> noteViewModels
                .add((new MapClasses<>(note, NoteViewModel.class)).mapFirstToSecond(false)));
        return noteViewModels;
    }
}
