package it.polito.ai.backend;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.jayway.jsonpath.JsonPath;
import it.polito.ai.backend.dtos.ConfigurationDTO;
import it.polito.ai.backend.dtos.VirtualMachineDTO;
import it.polito.ai.backend.entities.SystemImage;
import it.polito.ai.backend.entities.VirtualMachineStatus;
import it.polito.ai.backend.services.team.CourseNotFoundException;
import it.polito.ai.backend.services.vm.VirtualMachineNotFoundException;
import it.polito.ai.backend.services.vm.VirtualMachineService;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
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

    static String courseId;
    static String studentId;
    static Long teamId;
    static Long vmId;
    static SystemImage os;
    static VirtualMachineDTO virtualMachineDTO;
    static ConfigurationDTO configurationDTO;


    @BeforeAll
    static void beforeAll() {
        studentId = "student";
        courseId = "course";
        teamId = 1L;
        vmId = 1L;
        os = SystemImage.WINDOWS_10;
        virtualMachineDTO = VirtualMachineDTO.builder()
                .id(1L)
                .num_vcpu(2)
                .disk_space(1000)
                .ram(4)
                .status(VirtualMachineStatus.OFF)
                .build();
        configurationDTO = ConfigurationDTO.builder()
                .id(1L)
                .min_vcpu(2)
                .min_disk(1000)
                .min_ram(4)
                .max_vcpu(12)
                .max_disk(2000)
                .max_ram(16)
                .max_on(4)
                .tot(20)
                .build();

    }

    @Test
    void createVirtualMachine() {
        try {

            long id = virtualMachineDTO.getId();
            int numVcpu = virtualMachineDTO.getNum_vcpu();
            int diskSpace = virtualMachineDTO.getDisk_space();
            int ram = virtualMachineDTO.getRam();
            VirtualMachineStatus status = virtualMachineDTO.getStatus();

            Mockito.when(virtualMachineService.createVirtualMachine(courseId, teamId, studentId, numVcpu, diskSpace, ram)).thenReturn(virtualMachineDTO);

            Map<String, Object> body = new HashMap<>();
            body.put("numVcpu", numVcpu);
            body.put("diskSpace", diskSpace);
            body.put("ram", ram);
            body.put("studentId", studentId);
            String json = objectMapper.writeValueAsString(body);
            MvcResult mvcResult = mockMvc.perform(post("/API/courses/"+courseId+"/teams/"+teamId+"/virtual-machines")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(json))
                    .andExpect(status().is2xxSuccessful())
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

            Mockito.when(virtualMachineService.createVirtualMachine(courseId, teamId, studentId, numVcpu, diskSpace, ram)).thenThrow(CourseNotFoundException.class);
            mockMvc.perform(post("/API/courses/"+courseId+"/teams/"+teamId+"/virtual-machines")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(json))
                    .andExpect(status().isNotFound())
                    .andDo(print());

        } catch (Exception exception) {
            exception.printStackTrace();
        }
    }

    @Test
    void createVirtualMachine_invalidPathVariables() {
        try {

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


        } catch (Exception exception) {
            exception.printStackTrace();
        }
    }

    @Test
    void createVirtualMachine_invalidRequestBody() {
        try {

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


        } catch (Exception exception) {
            exception.printStackTrace();
        }
    }

    @Test
    void shareOwnership_invalidRequestBody() {
        try {

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

            /*Map<String, String> body3 = new HashMap<>();
            body3.put("name", "ok");
            body3.put("min", "1");
            body3.put("max", "2");
            body3.put("enabled", "erer");
            objectMapper.configure(DeserializationFeature.ACCEPT_EMPTY_STRING_AS_NULL_OBJECT, true);
            String json3 = objectMapper.writeValueAsString(body3);
            mockMvc.perform(put("/API/courses/"+courseId+"/setCourse")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(json3))
                    .andExpect(status().isBadRequest())
                    .andDo(print());*/

        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    @Test
    void getVirtualMachine() {

        try {
            Mockito.when(virtualMachineService.getVirtualMachine(Mockito.anyString(), Mockito.anyLong(), Mockito.anyLong())).thenThrow(VirtualMachineNotFoundException.class);
            mockMvc.perform(get("/API/courses/"+courseId+"/teams/"+teamId+"/virtual-machines/"+vmId))
                    .andExpect(status().isNotFound())
            .andDo(print());
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
