package ServiceTest;

import com.library.service.impl.AuthorServiceImpl;
import com.library.service.impl.BookServiceImpl;
import com.library.service.Fabric;
import com.library.service.impl.PublisherServiceImpl;
import org.junit.Test;

import static org.junit.Assert.*;

public class FabricTest {

    @Test
    public void testGetAuthorServiceSingleton() {
        AuthorServiceImpl service1 = Fabric.getAuthorService();
        AuthorServiceImpl service2 = Fabric.getAuthorService();
        assertNotNull("AuthorServiceImpl не должен быть null", service1);
        assertSame("Должны возвращаться один и тот же экземпляр AuthorServiceImpl", service1, service2);
    }

    @Test
    public void testGetBookServiceSingleton() {
        BookServiceImpl service1 = Fabric.getBookService();
        BookServiceImpl service2 = Fabric.getBookService();
        assertNotNull("BookServiceImpl не должен быть null", service1);
        assertSame("Должны возвращаться один и тот же экземпляр BookServiceImpl", service1, service2);
    }

    @Test
    public void testGetPublisherServiceSingleton() {
        PublisherServiceImpl service1 = Fabric.getPublisherService();
        PublisherServiceImpl service2 = Fabric.getPublisherService();
        assertNotNull("PublisherServiceImpl не должен быть null", service1);
        assertSame("Должны возвращаться один и тот же экземпляр PublisherServiceImpl", service1, service2);
    }
}
