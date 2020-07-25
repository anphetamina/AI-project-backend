package it.polito.ai.backend;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import it.polito.ai.backend.dtos.ConfigurationDTO;
import it.polito.ai.backend.dtos.VirtualMachineDTO;
import it.polito.ai.backend.entities.SystemImage;
import it.polito.ai.backend.entities.VirtualMachineStatus;
import it.polito.ai.backend.services.team.CourseNotFoundException;
import it.polito.ai.backend.services.vm.VirtualMachineNotFoundException;
import it.polito.ai.backend.services.vm.VirtualMachineService;
import it.polito.ai.backend.services.vm.VirtualMachineServiceConflictException;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import java.util.HashMap;
import java.util.Map;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
@SpringBootTest
@AutoConfigureMockMvc(addFilters = false)
public class VirtualMachineServiceIntegrationTests {

    @MockBean
    VirtualMachineService virtualMachineService;

    @Autowired
    private MockMvc mockMvc;
    @Autowired
    private ObjectMapper objectMapper;

    @Test
    void createVirtualMachine() throws Exception {

        String courseId = "c0";
        Long teamId = 1L;
        String studentId = "student";
        Long vmId = 1L;

        VirtualMachineDTO virtualMachineDTO = VirtualMachineDTO.builder()
                .id(vmId)
                .num_vcpu(2)
                .disk_space(1000)
                .ram(4)
                .status(VirtualMachineStatus.OFF)
                .build();

        long id = virtualMachineDTO.getId();
        int numVcpu = virtualMachineDTO.getNum_vcpu();
        int diskSpace = virtualMachineDTO.getDisk_space();
        int ram = virtualMachineDTO.getRam();
        VirtualMachineStatus status = virtualMachineDTO.getStatus();

        Mockito.when(virtualMachineService.createVirtualMachine(courseId, teamId, studentId, virtualMachineDTO)).thenReturn(virtualMachineDTO);
        String json = objectMapper.writeValueAsString(virtualMachineDTO);
        MvcResult mvcResult = mockMvc.perform(post("/API/courses/"+courseId+"/teams/"+teamId+"/virtual-machines")
                .contentType(MediaType.APPLICATION_JSON)
                .content(json))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(id))
                .andExpect(jsonPath("$.num_vcpu").value(numVcpu))
                .andExpect(jsonPath("$.disk_space").value(diskSpace))
                .andExpect(jsonPath("$.ram").value(ram))
                .andExpect(jsonPath("$.status").value(status.toString()))
                .andExpect(jsonPath("$._links.self.href").value("http://localhost/API/courses/"+courseId+"/teams/"+teamId+"/virtual-machines/"+vmId))
                .andExpect(jsonPath("$._links.model.href").value("http://localhost/API/courses/"+courseId+"/model"))
                .andExpect(jsonPath("$._links.usedBy.href").value("http://localhost/API/courses/"+courseId+"/teams/"+teamId))
                .andExpect(jsonPath("$._links.ownedBy.href").value("http://localhost/API/courses/"+courseId+"/teams/"+teamId+"/virtual-machines/"+vmId+"/owners"))
                .andDo(print())
                .andReturn();
        // String contentResult = mvcResult.getResponse().getContentAsString();
        // Long id = JsonPath.parse(contentResult).read("$.id", Long.class);

        Mockito.when(virtualMachineService.createVirtualMachine(courseId, teamId, studentId, virtualMachineDTO)).thenThrow(CourseNotFoundException.class);
        mockMvc.perform(post("/API/courses/"+courseId+"/teams/"+teamId+"/virtual-machines")
                .contentType(MediaType.APPLICATION_JSON)
                .content(json))
                .andExpect(status().isNotFound())
                .andDo(print());
    }

    @Test
    void createVirtualMachine_invalidPathVariables() throws Exception {
        String courseId = "course";
        Long teamId = null;

        mockMvc.perform(post("/API/courses/" + courseId + "/teams/" + teamId + "/virtual-machines"))
                .andExpect(status().isBadRequest());
        courseId = "  ";
        mockMvc.perform(post("/API/courses/" + courseId + "/teams/" + teamId + "/virtual-machines"))
                .andExpect(status().isBadRequest());
        courseId = null;
        mockMvc.perform(post("/API/courses/" + courseId + "/teams/" + teamId + "/virtual-machines"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void createVirtualMachine_invalidRequestBody() throws Exception {
        String courseId = "c0";
        Long teamId = 1L;
        String studentId = "s0";
        int numVcpu = 0;
        int diskSpace = 1000;
        int ram = 4;

        Map<String, Object> body = new HashMap<>();
        body.put("numVcpu", numVcpu);
        body.put("diskSpace", diskSpace);
        body.put("ram", ram);
        body.put("studentId", studentId);
        String json = objectMapper.writeValueAsString(body);
        mockMvc.perform(post("/API/courses/" + courseId + "/teams/" + teamId + "/virtual-machines"))
                .andExpect(status().isBadRequest());
        mockMvc.perform(post("/API/courses/"+courseId+"/teams/"+teamId+"/virtual-machines")
                .contentType(MediaType.APPLICATION_JSON)
                .content(json))
                .andExpect(status().isBadRequest());
        body.put("numVcpu", 3);
        body.put("diskSpace", 0);
        mockMvc.perform(post("/API/courses/"+courseId+"/teams/"+teamId+"/virtual-machines")
                .contentType(MediaType.APPLICATION_JSON)
                .content(json))
                .andExpect(status().isBadRequest());

        VirtualMachineDTO virtualMachineDTO = VirtualMachineDTO.builder()
                .id(1L)
                .num_vcpu(2)
                .disk_space(1000)
                .ram(4)
                .status(VirtualMachineStatus.OFF)
                .build();

        String body2 = objectMapper.writeValueAsString(virtualMachineDTO);
        Mockito.when(virtualMachineService.createVirtualMachine(Mockito.anyString(), Mockito.anyLong(), Mockito.anyString(), Mockito.any(VirtualMachineDTO.class))).thenThrow(VirtualMachineServiceConflictException.class);
        mockMvc.perform(post("/API/courses/"+courseId+"/teams/"+teamId+"/virtual-machines")
                .contentType(MediaType.APPLICATION_JSON)
                .content(body2))
                .andExpect(status().isConflict());

    }

    @Test
    void shareOwnership_invalidRequestBody() throws Exception {
        String courseId = "c0";
        long teamId = 1L;
        long vmId = 1L;
        Map<String, String> body = new HashMap<>();
        body.put("studentId", "  ");
        objectMapper.configure(DeserializationFeature.ACCEPT_EMPTY_STRING_AS_NULL_OBJECT, true);
        String json = objectMapper.writeValueAsString(body);
        mockMvc.perform(post("/API/courses/"+courseId+"/teams/"+teamId+"/virtual-machines/"+vmId+"/owners")
                .contentType(MediaType.APPLICATION_JSON)
                .content(json))
                .andExpect(status().isBadRequest())
                .andDo(print());

        Map<String, String> body2 = new HashMap<>();
        body2.put("studentId", null);
        objectMapper.configure(DeserializationFeature.ACCEPT_EMPTY_STRING_AS_NULL_OBJECT, true);
        String json2 = objectMapper.writeValueAsString(body2);
        mockMvc.perform(post("/API/courses/"+courseId+"/teams/"+teamId+"/virtual-machines/"+vmId+"/owners")
                .contentType(MediaType.APPLICATION_JSON)
                .content(json2))
                .andExpect(status().isBadRequest())
                .andDo(print());

    }

    @Test
    void getVirtualMachine() throws Exception {

        String courseId = "c0";
        long teamId = 1L;

        Mockito.when(virtualMachineService.getVirtualMachine(Mockito.anyString(), Mockito.anyLong(), Mockito.anyLong())).thenThrow(VirtualMachineNotFoundException.class);
        mockMvc.perform(get("/API/courses/"+courseId+"/teams/"+teamId+"/virtual-machines/"+9999))
                .andExpect(status().isNotFound())
                .andDo(print());
    }

    @Test
    void deleteVirtualMachine() throws Exception {

        String courseId = "c0";
        Long teamId = 1L;
        Long vmId = 1L;

        Mockito.when(virtualMachineService.deleteVirtualMachine(courseId, teamId, vmId)).thenReturn(true);
        mockMvc.perform(delete("/API/courses/"+courseId+"/teams/"+teamId+"/virtual-machines/"+vmId))
                .andExpect(status().isOk())
                .andDo(print());
    }

    @Test
    void updateVirtualMachine() throws Exception {

        String courseId = "c0";
        Long teamId = 1L;
        Long vmId = 1L;

        VirtualMachineDTO virtualMachineDTO = VirtualMachineDTO.builder()
                .id(1L)
                .num_vcpu(2)
                .disk_space(1000)
                .ram(4)
                .status(VirtualMachineStatus.OFF)
                .build();

        Mockito.when(virtualMachineService.updateVirtualMachine(courseId, teamId, vmId, virtualMachineDTO)).thenReturn(virtualMachineDTO);
        String body = objectMapper.writeValueAsString(virtualMachineDTO);
        mockMvc.perform(put("/API/courses/"+courseId+"/teams/"+teamId+"/virtual-machines/"+vmId)
                .contentType(MediaType.APPLICATION_JSON)
                .content(body))
                .andExpect(status().isOk())
                .andDo(print());
    }
}
