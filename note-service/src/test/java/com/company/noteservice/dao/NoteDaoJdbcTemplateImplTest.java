package com.company.noteservice.dao;

import com.company.noteservice.model.Note;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import java.util.List;

import static org.junit.Assert.*;

@RunWith(value = SpringJUnit4ClassRunner.class)
@SpringBootTest
public class NoteDaoJdbcTemplateImplTest {

    @Autowired
    private NoteDao noteDao;

    @Before
    public void setUp() throws Exception {
        List<Note> notes = noteDao.findAll();
        notes.forEach(note -> noteDao.delete(note.getNoteId()));
    }

    @After
    public void tearDown() throws Exception {
    }

    @Test
    public void addGetUpdateDelete() {
        Note note = new Note() {{
            setBookId(1);
            setNote("testing");
        }};
        noteDao.add(note);

        // get
        Note note2 = noteDao.find(note.getNoteId());
        assertEquals(note, note2);

        // update
        note.setNote("testing 2345");
        noteDao.update(100, note);
        note2 = noteDao.find(note.getNoteId());
        assertEquals(note, note2);

        // delete
        noteDao.delete(note.getNoteId());
        note2 = noteDao.find(note.getNoteId());
        assertNull(note2);
    }

    @Test
    public void findAll_findByBookId() {
        Note note = new Note() {{
            setBookId(1);
            setNote("testing");
        }};
        noteDao.add(note);

        note = new Note() {{
            setBookId(1);
            setNote("testing");
        }};
        noteDao.add(note);

        List<Note> notes = noteDao.findAll();
        assertEquals(notes.size(), 2);

        notes = noteDao.findByBookId(1);
        assertEquals(notes.size(), 2);

        notes = noteDao.findByBookId(2);
        assertEquals(notes.size(), 0);
    }
}