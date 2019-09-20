package com.company.bookservice.viewmodel;

import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.Size;
import java.util.Objects;

public class NoteViewModel {

    private Integer noteId;

    private Integer bookId;

    @Size(max = 255)
    @NotEmpty(message = "Please supply content for the note")
    private String note;

    public Integer getNoteId() {
        return noteId;
    }

    public void setNoteId(Integer noteId) {
        this.noteId = noteId;
    }

    public Integer getBookId() {
        return bookId;
    }

    public void setBookId(Integer bookId) {
        this.bookId = bookId;
    }

    public String getNote() {
        return note;
    }

    public void setNote(String note) {
        this.note = note;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        NoteViewModel that = (NoteViewModel) o;
        return Objects.equals(getNoteId(), that.getNoteId()) &&
                getBookId().equals(that.getBookId()) &&
                getNote().equals(that.getNote());
    }

    @Override
    public int hashCode() {
        return Objects.hash(getNoteId(), getBookId(), getNote());
    }
}
