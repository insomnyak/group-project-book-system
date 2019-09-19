package com.company.bookservice.viewmodel;

import java.util.Objects;

public class NoteViewModel {

    private Integer noteId;
    private Integer bookId;
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
