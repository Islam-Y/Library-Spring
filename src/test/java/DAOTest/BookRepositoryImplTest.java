package DAOTest;


import static org.assertj.core.api.Assertions.assertThat;

import java.sql.Connection;
import java.sql.SQLException;
import java.time.Duration;
import java.time.LocalDate;
import java.util.Collections;
import java.util.HashSet;
import java.util.Optional;

import javax.sql.DataSource;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import org.flywaydb.core.Flyway;
import org.junit.jupiter.api.*;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.containers.wait.strategy.Wait;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import com.library.entity.Author;
import com.library.entity.Book;
import com.library.entity.Publisher;
import com.library.repository.AuthorDAO;
import com.library.repository.impl.BookRepositoryImpl;
import com.library.repository.impl.PublisherRepositoryImpl;

@Testcontainers
class BookRepositoryImplTest {

    @Container
    private static PostgreSQLContainer<?> postgres =
            new PostgreSQLContainer<>("postgres:14")
                    .withDatabaseName("test")
                    .withUsername("test")
                    .withPassword("test")
                    .waitingFor(Wait.forLogMessage(".*database system is ready to accept connections.*\\n", 2))
                    .withStartupTimeout(Duration.ofSeconds(60));

    private static DataSource dataSource;
    private BookRepositoryImpl bookRepositoryImpl;
    private AuthorDAO authorDAO;
    private PublisherRepositoryImpl publisherRepositoryImpl;

    @BeforeAll
    static void setup() {
        postgres.start();

        // Настройка HikariCP
        HikariConfig config = new HikariConfig();
        config.setJdbcUrl(postgres.getJdbcUrl());
        config.setUsername(postgres.getUsername());
        config.setPassword(postgres.getPassword());
        config.setDriverClassName(postgres.getDriverClassName());
        config.setMaximumPoolSize(2);
        config.setConnectionTimeout(3000);

        dataSource = new HikariDataSource(config);

        // Настройка Flyway без clean()
        Flyway flyway = Flyway.configure()
                .dataSource(dataSource)
                .schemas("public")
                .locations("filesystem:src/main/resources/db/migration")
                .baselineOnMigrate(true)
                .load();

        flyway.migrate();
    }

    @BeforeEach
    void init() {
        this.bookRepositoryImpl = BookRepositoryImpl.forTests(dataSource);
        this.authorDAO = AuthorDAO.forTests(dataSource);
        this.publisherRepositoryImpl = PublisherRepositoryImpl.forTests(dataSource);
    }

    @AfterEach
    void tearDown() throws SQLException {
        try (Connection conn = dataSource.getConnection()) {
            conn.prepareStatement("DELETE FROM book_author").executeUpdate();
            conn.prepareStatement("DELETE FROM books").executeUpdate();
            conn.prepareStatement("DELETE FROM authors").executeUpdate();
            conn.prepareStatement("DELETE FROM publishers").executeUpdate();
        }
    }

    @Test
    void shouldCreateAndRetrieveBook() throws SQLException {
        Publisher publisher = new Publisher();
        publisher.setName("Test Publisher");
        publisherRepositoryImpl.create(publisher);

        Book book = new Book();
        book.setTitle("1984");
        book.setPublishedDate(LocalDate.now().toString());
        book.setGenre("Антиутопия");
        book.setPublisher(publisher);

        bookRepositoryImpl.create(book);

        Optional<Book> found = bookRepositoryImpl.getById(book.getId());
        assertThat(found).isPresent();

        Book retrievedBook = found.get();
        assertThat(retrievedBook.getTitle()).isEqualTo("1984");
        assertThat(retrievedBook.getGenre()).isEqualTo("Антиутопия");
        assertThat(retrievedBook.getPublisher()).isNotNull();
    }

    @Test
    void shouldUpdateBook() throws SQLException {
        Publisher publisher = new Publisher();
        publisher.setName("Test Publisher");
        publisherRepositoryImpl.create(publisher);

        Book book = new Book();
        book.setTitle("Старик и море");
        book.setPublisher(publisher);
        bookRepositoryImpl.create(book);

        book.setTitle("Старик и море (обновлённое)");
        book.setGenre("Роман");
        bookRepositoryImpl.update(book);

        Optional<Book> updated = bookRepositoryImpl.getById(book.getId());
        assertThat(updated).isPresent();
        assertThat(updated.get().getTitle()).isEqualTo("Старик и море (обновлённое)");
    }

    @Test
    void shouldDeleteBook() throws SQLException {
        Publisher publisher = new Publisher();
        publisher.setName("Test Publisher");
        publisherRepositoryImpl.create(publisher);

        Book book = new Book();
        book.setTitle("Мастер и Маргарита");
        book.setPublisher(publisher);
        bookRepositoryImpl.create(book);

        bookRepositoryImpl.delete(book.getId());

        assertThat(bookRepositoryImpl.getById(book.getId())).isEmpty();
    }

    @Test
    void shouldHandleAuthorRelations() throws SQLException {
        Publisher publisher = new Publisher();
        publisher.setName("Test Publisher");
        publisherRepositoryImpl.create(publisher);

        Author author = new Author();
        author.setName("Джордж");
        author.setSurname("Оруэлл");
        authorDAO.create(author);

        Book book = new Book();
        book.setTitle("1984");
        book.setPublisher(publisher);
        book.setAuthors(new HashSet<>(Collections.singleton(author)));
        bookRepositoryImpl.create(book);

        Book retrieved = bookRepositoryImpl.getById(book.getId()).orElseThrow();
        assertThat(retrieved.getAuthors())
                .hasSize(1)
                .extracting(Author::getSurname)
                .containsExactly("Оруэлл");
    }

    @Test
    void shouldHandlePublisherRelation() throws SQLException {
        Publisher publisher = new Publisher();
        publisher.setName("Эксмо");
        publisherRepositoryImpl.create(publisher);

        Book book = new Book();
        book.setTitle("Преступление и наказание");
        book.setPublisher(publisher);
        bookRepositoryImpl.create(book);

        Book retrieved = bookRepositoryImpl.getById(book.getId()).orElseThrow();
        assertThat(retrieved.getPublisher())
                .isNotNull()
                .extracting(Publisher::getName)
                .isEqualTo("Эксмо");
    }
}