package com.company.bookservice.viewmodel;

import javax.validation.Valid;
import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.Size;
import java.util.List;
import java.util.Objects;

public class BookViewModel {

    private Integer bookId;

    @NotEmpty(message = "Please supply a title for the book")
    @Size(min=1, max=50, message = "Please supply a valid book title")
    private String title;

    @NotEmpty(message = "Please supply an author for the book")
    @Size(min=1, max=50, message = "Please supply a valid author name")
    private String author;

    @Valid
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
