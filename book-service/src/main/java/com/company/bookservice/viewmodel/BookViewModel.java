package com.company.bookservice.viewmodel;

import java.util.List;
import java.util.Objects;

public class BookViewModel {

    private Integer bookId;

    private String title;
    private String author;
    private List<NoteViewModel> noteViewModelList;

    public Integer getBookId() {
        return bookId;
    }

    public void setBookId(Integer bookId) {
        this.bookId = bookId;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getAuthor() {
        return author;
    }

    public void setAuthor(String author) {
        this.author = author;
    }

    public List<NoteViewModel> getNoteViewModelList() {
        return noteViewModelList;
    }

    public void setNoteViewModelList(List<NoteViewModel> noteViewModelList) {
        this.noteViewModelList = noteViewModelList;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        BookViewModel that = (BookViewModel) o;
        return Objects.equals(getBookId(), that.getBookId()) &&
                getTitle().equals(that.getTitle()) &&
                getAuthor().equals(that.getAuthor()) &&
                Objects.equals(getNoteViewModelList(), that.getNoteViewModelList());
    }

    @Override
    public int hashCode() {
        return Objects.hash(getBookId(), getTitle(), getAuthor(), getNoteViewModelList());
    }
}
