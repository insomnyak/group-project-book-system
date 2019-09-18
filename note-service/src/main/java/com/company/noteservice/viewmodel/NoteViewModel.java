package com.company.noteservice.viewmodel;

import javax.validation.constraints.Min;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import java.util.Objects;

public class NoteViewModel {

    @Min(value = 1)
    private Integer noteId;

    @NotNull
    @Min(value = 1)
    private Integer bookId;

    @Size(max = 255)
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
        if (!(o instanceof NoteViewModel)) return false;
        NoteViewModel that = (NoteViewModel) o;
        return Objects.equals(getNoteId(), that.getNoteId()) &&
                getBookId().equals(that.getBookId()) &&
                Objects.equals(getNote(), that.getNote());
    }

    @Override
    public int hashCode() {
        return Objects.hash(getNoteId(), getBookId(), getNote());
    }
}
