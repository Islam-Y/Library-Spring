package ServletTest;

import static org.assertj.core.api.Assertions.assertThat;

import com.library.service.Fabric;
import com.library.service.impl.PublisherServiceImpl;
import com.library.controller.PublisherServlet;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.lang.reflect.Field;

@ExtendWith(MockitoExtension.class)
class PublisherServletInitTest {

    @Mock
    private PublisherServiceImpl mockPublisherServiceImpl;

    @BeforeEach
    void setUp() throws Exception {
        Field publisherServiceField = Fabric.class.getDeclaredField("publisherService");
        publisherServiceField.setAccessible(true);
        publisherServiceField.set(null, mockPublisherServiceImpl);
    }

    @Test
    void init_SetsUpServiceProperly() throws Exception {
        PublisherServlet servlet = new PublisherServlet();
        servlet.init();

        Field serviceField = PublisherServlet.class.getDeclaredField("publisherService");
        serviceField.setAccessible(true);
        PublisherServiceImpl service = (PublisherServiceImpl) serviceField.get(servlet);

        assertThat(service).isSameAs(mockPublisherServiceImpl).isNotNull();
    }

    @AfterEach
    void tearDown() throws Exception {
        Field publisherServiceField = Fabric.class.getDeclaredField("publisherService");
        publisherServiceField.setAccessible(true);
        publisherServiceField.set(null, null);
    }
}