package com.company.bookservice.controller;

import com.company.bookservice.service.ServiceLayer;
import com.company.bookservice.viewmodel.BookViewModel;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.CacheConfig;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.CachePut;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.List;

import static com.insomnyak.util.terminal.AnsiColor.*;

@RestController
@RequestMapping(value = "/books")
@CacheConfig(cacheNames = {"books"})
public class BookServiceController {

    @Autowired
    private ServiceLayer sl;

    @CachePut(key = "#result.getBookId()")
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public BookViewModel createBook(@RequestBody @Valid BookViewModel bvm) {
        System.out.println(String.format("%sSAVING BOOK AND ADDING TO CACHE%s", BRIGHT_RED, RESET));
        return sl.saveBook(bvm);
    }

    @Cacheable
    @GetMapping("/{id}")
    @ResponseStatus(HttpStatus.OK)
    public BookViewModel getBook(@PathVariable Integer id) {
        System.out.println(String.format("%sCACHING BOOKID%s %d", BRIGHT_RED, RESET, id));
        return sl.findBook(id);
    }

    @CacheEvict
    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteBook(@PathVariable Integer id) {
        sl.removeBook(id);
    }

    @CacheEvict(key = "#bvm.getBookId()")
    @PutMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void updateBook(@PathVariable Integer id, @RequestBody @Valid BookViewModel bvm) {
        sl.updateBook(id, bvm);
    }

    @GetMapping
    @ResponseStatus(HttpStatus.OK)
    public List<BookViewModel> findAllBooks() {
        return sl.findAllBooks();
    }

}
