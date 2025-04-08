package ServiceTest;

import com.library.dto.PublisherDTO;
import com.library.exception.PublisherServiceException;
import com.library.mapper.PublisherMapper;
import com.library.entity.Publisher;
import com.library.repository.impl.PublisherRepositoryImpl;
import com.library.service.impl.PublisherServiceImpl;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import java.sql.SQLException;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.junit.Assert.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.Mockito.*;

@RunWith(MockitoJUnitRunner.class)
public class PublisherServiceImplTest {

    @Mock
    private PublisherRepositoryImpl publisherRepositoryImpl;
    @Mock
    private PublisherMapper publisherMapper;

    private PublisherServiceImpl publisherServiceImpl;

    private Publisher testPublisher;
    private PublisherDTO testPublisherDTO;

    @Before
    public void setUp() {
        publisherServiceImpl = PublisherServiceImpl.forTest(publisherRepositoryImpl, publisherMapper);

        testPublisher = new Publisher();
        testPublisher.setId(1);
        testPublisher.setName("Test Publisher");

        testPublisherDTO = new PublisherDTO();
        testPublisherDTO.setId(1);
        testPublisherDTO.setName("Test Publisher");

        when(publisherMapper.toDTO(any(Publisher.class))).thenReturn(testPublisherDTO);
    }

    @Test
    public void getAllPublishers_Success() throws SQLException {
        when(publisherRepositoryImpl.getAll()).thenReturn(Collections.singletonList(testPublisher));

        List<PublisherDTO> result = publisherServiceImpl.getAllPublishers();

        assertEquals(1, result.size());
        assertEquals("Test Publisher", result.get(0).getName());
    }

    @Test(expected = PublisherServiceException.class)
    public void getAllPublishers_Exception() throws SQLException {
        when(publisherRepositoryImpl.getAll()).thenThrow(new SQLException("DB error"));
        publisherServiceImpl.getAllPublishers();
    }

    @Test
    public void getPublisherById_Success() throws SQLException {
        when(publisherRepositoryImpl.getById(1)).thenReturn(Optional.of(testPublisher));

        PublisherDTO result = publisherServiceImpl.getPublisherById(1);

        assertEquals(1, result.getId());
        assertEquals("Test Publisher", result.getName());
    }

    @Test(expected = PublisherServiceException.class)
    public void getPublisherById_NotFound() throws SQLException {
        when(publisherRepositoryImpl.getById(anyInt())).thenReturn(Optional.empty());
        publisherServiceImpl.getPublisherById(1);
    }

    @Test
    public void addPublisher_Success() throws SQLException {
        PublisherDTO inputDTO = new PublisherDTO();
        inputDTO.setName("New Publisher");

        Publisher expectedPublisher = new Publisher();
        expectedPublisher.setName("New Publisher");

        when(publisherMapper.toModel(inputDTO)).thenReturn(expectedPublisher);

        publisherServiceImpl.addPublisher(inputDTO);

        ArgumentCaptor<Publisher> captor = ArgumentCaptor.forClass(Publisher.class);
        verify(publisherRepositoryImpl).create(captor.capture());

        Publisher savedPublisher = captor.getValue();
        assertEquals("New Publisher", savedPublisher.getName());
    }

    @Test(expected = PublisherServiceException.class)
    public void addPublisher_SQLException() throws SQLException {
        PublisherDTO inputDTO = new PublisherDTO();
        inputDTO.setName("New Publisher");

        Publisher expectedPublisher = new Publisher();
        expectedPublisher.setName("New Publisher");

        when(publisherMapper.toModel(inputDTO)).thenReturn(expectedPublisher);
        doThrow(new SQLException("DB error")).when(publisherRepositoryImpl).create(expectedPublisher);

        publisherServiceImpl.addPublisher(inputDTO);
    }

    @Test
    public void updatePublisher_Success() throws SQLException {
        Publisher existingPublisher = new Publisher();
        existingPublisher.setId(1);
        existingPublisher.setName("Old Publisher");

        when(publisherRepositoryImpl.getById(1)).thenReturn(Optional.of(existingPublisher));

        PublisherDTO updateDTO = new PublisherDTO();
        updateDTO.setName("Updated Publisher");
        updateDTO.setBookIds(List.of(1, 2));

        publisherServiceImpl.updatePublisher(1, updateDTO);

        verify(publisherRepositoryImpl).update(existingPublisher);
        assertEquals("Updated Publisher", existingPublisher.getName());
        assertEquals(2, existingPublisher.getBooks().size());
    }

    @Test(expected = PublisherServiceException.class)
    public void updatePublisher_NotFound() throws SQLException {
        when(publisherRepositoryImpl.getById(1)).thenReturn(Optional.empty());
        publisherServiceImpl.updatePublisher(1, new PublisherDTO());
    }

    @Test(expected = PublisherServiceException.class)
    public void updatePublisher_SQLException() throws SQLException {
        Publisher existingPublisher = new Publisher();
        existingPublisher.setId(1);
        existingPublisher.setName("Old Publisher");

        when(publisherRepositoryImpl.getById(1)).thenReturn(Optional.of(existingPublisher));
        doThrow(new SQLException()).when(publisherRepositoryImpl).update(existingPublisher);

        PublisherDTO updateDTO = new PublisherDTO();
        updateDTO.setName("Updated Publisher");
        updateDTO.setBookIds(Collections.emptyList());

        publisherServiceImpl.updatePublisher(1, updateDTO);
    }

    @Test
    public void deletePublisher_Success() throws SQLException {
        publisherServiceImpl.deletePublisher(1);
        verify(publisherRepositoryImpl).delete(1);
    }

    @Test(expected = PublisherServiceException.class)
    public void deletePublisher_SQLException() throws SQLException {
        doThrow(new SQLException()).when(publisherRepositoryImpl).delete(1);
        publisherServiceImpl.deletePublisher(1);
    }
}

