package com.company.noteservice.dao;

import com.company.noteservice.model.Note;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;

@Repository
public class NoteDaoJdbcTemplateImpl implements NoteDao {

    private final String INSERT_NOTE_SQL =
            "insert into note (book_id, note) values (?,?)";
    private final String UPDATE_NOTE_SQL =
            "update note set book_id = ?, note = ? where note_id = ?";
    private final String SELECT_NOTE_SQL =
            "select * from note where note_id = ?";
    private final String SELECT_ALL_NOTES_SQL =
            "select * from note";
    private final String DELETE_NOTE_SQL =
            "delete from note where note_id = ?";
    private final String SELECT_ALL_NOTES_BY_BOOK_ID_SQL =
            "select * from note where book_id = ?";

    private JdbcTemplate jdbcTemplate;

    @Autowired
    public NoteDaoJdbcTemplateImpl(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @Override
    @Transactional
    public Note add(Note note) {
        jdbcTemplate.update(INSERT_NOTE_SQL,
                note.getBookId(),
                note.getNote());
        int id = jdbcTemplate.queryForObject("select last_insert_id()", Integer.class);
        note.setNoteId(id);
        return note;
    }

    @Override
    public Note find(Integer id) {
        try {
            return jdbcTemplate.queryForObject(SELECT_NOTE_SQL, this::mapRowToNote, id);
        } catch (EmptyResultDataAccessException e) {
            return null;
        }
    }

    @Override
    public List<Note> findAll() {
        return jdbcTemplate.query(SELECT_ALL_NOTES_SQL, this::mapRowToNote);
    }

    @Override
    public void update(Note note) {
        jdbcTemplate.update(UPDATE_NOTE_SQL,
                note.getBookId(),
                note.getNote(),
                note.getNoteId());
    }

    @Override
    public void delete(Integer id) {
        jdbcTemplate.update(DELETE_NOTE_SQL, id);
    }

    @Override
    public List<Note> findByBookId(Integer bookId) {
        return jdbcTemplate.query(SELECT_ALL_NOTES_BY_BOOK_ID_SQL, this::mapRowToNote, bookId);
    }

    @Override
    public Note mapRowToNote(ResultSet rs, int rowNum) throws SQLException {
        Note note = new Note();
        note.setNoteId(rs.getInt("note_id"));
        note.setBookId(rs.getInt("book_id"));
        note.setNote(rs.getString("note"));
        return note;
    }
}
