package cz.cvut.room.rest;

import cz.cvut.room.environment.Generator;
import cz.cvut.room.exception.NoPermissionException;
import cz.cvut.room.model.Room;
import cz.cvut.room.service.IRoomService;
import cz.cvut.room.util.EquipmentType;
import cz.cvut.room.util.RoomType;
import cz.cvut.room.util.wrappers.UserWrapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.mockito.internal.verification.Times;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;

import java.util.HashSet;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(MockitoExtension.class)
public class RoomControllerTest extends BaseControllerTestRunner{

    @Mock
    private IRoomService roomServiceMock;

    @InjectMocks
    private RoomController sut;

    @BeforeEach
    public void setUp() {
        super.setUp(sut);
    }

    private final UserWrapper adminUserWrapper = new UserWrapper("admin", true);
    private final UserWrapper nonAdminUserWrapper = new UserWrapper("admin", false);

    @Test
    public void adminAddRoomAddsRoomViaService() throws Exception {
        final Room toCreate = Generator.getTestRoom();
        mockMvc.perform(post("/").content(toJson(toCreate)).contentType(MediaType.APPLICATION_JSON_VALUE))
                .andExpect(status().isCreated());
        final ArgumentCaptor<Room> captor = ArgumentCaptor.forClass(Room.class);
        final ArgumentCaptor<UserWrapper> wrapperCaptor = ArgumentCaptor.forClass(UserWrapper.class);

        verify(roomServiceMock).addRoom(captor.capture(), wrapperCaptor.capture());

        assertEquals(toCreate.getCode(), captor.getValue().getCode());
        assertEquals(toCreate.getEquipment(), captor.getValue().getEquipment());
    }

    /**
     * Cannot implement successfully because the information about user is from filtered request above the controller layer.
     * So actually this use case should never even happen.
     * @throws Exception
     */
    @Test
    public void noAdminAddRoomAddsRoomViaServiceThrowsNoPermissionException() throws Exception {
        final Room toCreate = Generator.getTestRoom();
        mockMvc.perform(post("/").content(toJson(toCreate)).contentType(MediaType.APPLICATION_JSON_VALUE))
                .andExpect(status().isUnauthorized());
        final ArgumentCaptor<Room> captor = ArgumentCaptor.forClass(Room.class);
        final ArgumentCaptor<UserWrapper> wrapperCaptor = ArgumentCaptor.forClass(UserWrapper.class);

        Mockito.doThrow(NoPermissionException.class).when(roomServiceMock).addRoom(toCreate, nonAdminUserWrapper);

        verify(roomServiceMock).addRoom(captor.capture(), wrapperCaptor.capture());
        assertThrows(NoPermissionException.class, () -> roomServiceMock.addRoom(toCreate, nonAdminUserWrapper));

        verify(roomServiceMock).addRoom(captor.capture(), wrapperCaptor.capture());

        assertEquals(toCreate.getCode(), captor.getValue().getCode());
        assertEquals(toCreate.getEquipment(), captor.getValue().getEquipment());
        assertFalse(wrapperCaptor.capture().isAdmin());
    }

    @Test
    public void getRoomsReturnsAllRoomsWithOkStatus() throws Exception {
        mockMvc.perform(get("/"))
                .andExpect(status().isOk());

        Mockito.verify(roomServiceMock, times(1)).getRooms();
    }

    @Test
    public void getRoomReturnsRoomsWithOkStatus() throws Exception {
        final Integer roomId = 1;
        mockMvc.perform(get("/" + roomId))
                .andExpect(status().isOk());

        Mockito.verify(roomServiceMock, times(1)).find(roomId);
    }
}
