package com.company.noteservice.dao;


import com.company.noteservice.model.Note;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;

public interface NoteDao {
    Note add(Note note);
    Note find(Integer id);
    List<Note> findAll();
    void update(Note note);
    void delete(Integer id);
    void deleteByBookId(Integer bookId);

    List<Note> findByBookId(Integer bookId);

    Note mapRowToNote(ResultSet rs, int rowNum) throws SQLException;
}
