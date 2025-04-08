package ServiceTest;

import com.library.dto.BookDTO;
import com.library.exception.BookServiceException;
import com.library.mapper.BookMapper;
import com.library.entity.Author;
import com.library.entity.Book;
import com.library.entity.Publisher;
import com.library.repository.AuthorDAO;
import com.library.repository.impl.BookRepositoryImpl;
import com.library.service.impl.BookServiceImpl;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.MockedConstruction;
import org.mockito.junit.MockitoJUnitRunner;

import java.sql.SQLException;
import java.util.*;

import static org.junit.Assert.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.Mockito.*;

@RunWith(MockitoJUnitRunner.class)
public class BookServiceImplTest {

    @Mock
    private BookRepositoryImpl bookRepositoryImpl;
    @Mock
    private BookMapper bookMapper;

    private BookServiceImpl bookServiceImpl;

    private Book testBook;
    private BookDTO testBookDTO;

    @Before
    public void setUp() {
        bookServiceImpl = BookServiceImpl.forTest(bookRepositoryImpl, bookMapper);

        testBook = new Book();
        testBook.setId(1);
        testBook.setTitle("Test Book");
        testBook.setPublishedDate(new String());

        testBookDTO = new BookDTO();
        testBookDTO.setId(1);
        testBookDTO.setTitle("Test Book");
        testBookDTO.setPublishedDate(testBook.getPublishedDate());

        when(bookMapper.toDTO(any(Book.class))).thenReturn(testBookDTO);
    }

    @Test
    public void getAllBooks_Success() throws SQLException {
        when(bookRepositoryImpl.getAll()).thenReturn(Collections.singletonList(testBook));

        List<BookDTO> result = bookServiceImpl.getAllBooks();

        assertEquals(1, result.size());
        assertEquals("Test Book", result.get(0).getTitle());
    }

    @Test(expected = BookServiceException.class)
    public void getAllBooks_Exception() throws SQLException {
        when(bookRepositoryImpl.getAll()).thenThrow(new SQLException("DB error"));
        bookServiceImpl.getAllBooks();
    }

    @Test
    public void getBookById_Success() throws SQLException {
        when(bookRepositoryImpl.getById(1)).thenReturn(Optional.of(testBook));

        BookDTO result = bookServiceImpl.getBookById(1);

        assertEquals(1, result.getId());
        assertEquals("Test Book", result.getTitle());
    }

    @Test(expected = BookServiceException.class)
    public void getBookById_NotFound() throws SQLException {
        when(bookRepositoryImpl.getById(anyInt())).thenReturn(Optional.empty());
        bookServiceImpl.getBookById(1);
    }

    @Test
    public void addBook_Success() throws SQLException {
        BookDTO inputDTO = new BookDTO();
        inputDTO.setTitle("New Book");
        inputDTO.setPublisherId(1);
        inputDTO.setAuthorIds(Set.of(1, 2));

        Author author1 = createTestAuthor(1);
        Author author2 = createTestAuthor(2);
        Publisher publisher = createTestPublisher(1);

        try (MockedConstruction<AuthorDAO> mockedAuthorDAO = mockConstruction(
                AuthorDAO.class,
                (mock, context) -> {
                    when(mock.getById(1)).thenReturn(Optional.of(author1));
                    when(mock.getById(2)).thenReturn(Optional.of(author2));
                }
        )) {
            Book expectedBook = new Book();
            expectedBook.setTitle("New Book");
            expectedBook.setPublisher(publisher);

            when(bookMapper.toModel(any(BookDTO.class))).thenReturn(expectedBook);

            bookServiceImpl.addBook(inputDTO);

            ArgumentCaptor<Book> bookCaptor = ArgumentCaptor.forClass(Book.class);
            verify(bookRepositoryImpl).create(bookCaptor.capture());

            Book savedBook = bookCaptor.getValue();
            assertEquals("New Book", savedBook.getTitle());
            assertEquals(2, savedBook.getAuthors().size());
        }
    }

    @Test(expected = BookServiceException.class)
    public void addBook_SQLException() throws SQLException {
        BookDTO inputDTO = new BookDTO();
        inputDTO.setTitle("New Book");
        inputDTO.setPublisherId(1);
        inputDTO.setAuthorIds(Collections.emptySet());

        Book expectedBook = new Book();
        expectedBook.setTitle("New Book");

        when(bookMapper.toModel(inputDTO)).thenReturn(expectedBook);
        doThrow(new SQLException("DB error")).when(bookRepositoryImpl).create(any(Book.class));

        bookServiceImpl.addBook(inputDTO);
    }

    @Test
    public void updateBook_Success() throws SQLException {
        Book existingBook = new Book();
        existingBook.setId(1);
        existingBook.setTitle("Old Title");
        existingBook.setPublisher(new Publisher());

        when(bookRepositoryImpl.getById(1)).thenReturn(Optional.of(existingBook));

        BookDTO updateDTO = new BookDTO();
        updateDTO.setTitle("Updated Title");
        updateDTO.setPublisherId(2); // Добавлено
        updateDTO.setAuthorIds(Set.of(3)); // Добавлено

        bookServiceImpl.updateBook(1, updateDTO);

        verify(bookRepositoryImpl).update(existingBook);
        assertEquals("Updated Title", existingBook.getTitle());
        assertEquals(2, existingBook.getPublisher().getId()); // Проверка publisher
    }

    @Test(expected = BookServiceException.class)
    public void updateBook_NotFound() throws SQLException {
        BookDTO updateDTO = new BookDTO();
        updateDTO.setTitle("Updated Title");
        updateDTO.setPublisherId(1); // Добавляем publisherId
        updateDTO.setAuthorIds(Set.of(1));

        when(bookRepositoryImpl.getById(1)).thenReturn(Optional.empty());

        bookServiceImpl.updateBook(1, updateDTO);
    }

    @Test(expected = BookServiceException.class)
    public void updateBook_SQLException() throws SQLException {
        Book existingBook = new Book();
        existingBook.setId(1);
        existingBook.setTitle("Old Title");
        existingBook.setPublisher(new Publisher()); // Добавлено

        when(bookRepositoryImpl.getById(1)).thenReturn(Optional.of(existingBook));
        doThrow(new SQLException()).when(bookRepositoryImpl).update(any(Book.class));

        BookDTO updateDTO = new BookDTO();
        updateDTO.setTitle("Updated Title");
        updateDTO.setPublisherId(2);

        bookServiceImpl.updateBook(1, updateDTO);
    }

    @Test
    public void deleteBook_Success() throws SQLException {
        bookServiceImpl.deleteBook(1);
        verify(bookRepositoryImpl).delete(1);
    }

    @Test(expected = BookServiceException.class)
    public void deleteBook_SQLException() throws SQLException {
        doThrow(new SQLException()).when(bookRepositoryImpl).delete(1);
        bookServiceImpl.deleteBook(1);
    }

    @Test(expected = IllegalArgumentException.class)
    public void addBook_WithoutTitle_ThrowsException() {
        BookDTO invalidDTO = new BookDTO();
        invalidDTO.setPublisherId(1);
        invalidDTO.setAuthorIds(Set.of(1));

        bookServiceImpl.addBook(invalidDTO);
    }

    @Test(expected = IllegalArgumentException.class)
    public void updateBook_WithoutPublisher_ThrowsException() {
        BookDTO invalidDTO = new BookDTO();
        invalidDTO.setTitle("Title");
        invalidDTO.setAuthorIds(Collections.emptySet());

        bookServiceImpl.updateBook(1, invalidDTO);
    }

    private Author createTestAuthor(int id) {
        Author author = new Author();
        author.setId(id);
        author.setName("Test Name");
        author.setSurname("Test Surname");
        author.setCountry("Test Country");
        author.setBooks(Set.of(testBook));
        return author;
    }

    private Publisher createTestPublisher(int id) {
        Publisher publisher = new Publisher();
        publisher.setId(id);
        publisher.setName("Test Publisher");
        publisher.setBooks(List.of(testBook));
        return publisher;
    }
}

