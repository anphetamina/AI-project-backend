package it.polito.ai.backend;

import com.fasterxml.jackson.databind.ObjectMapper;
import it.polito.ai.backend.dtos.VirtualMachineDTO;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

import javax.transaction.Transactional;

import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@Transactional
public class VirtualMachineServiceIntegrationAndSecurityTests {

    private MockMvc mockMvc;
    @Autowired
    private ObjectMapper objectMapper;
    @Autowired
    private WebApplicationContext context;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders
                .webAppContextSetup(context)
                .apply(springSecurity())
                .build();
    }

    @Test
    @WithMockUser(username = "s0@studenti.polito.it", roles = "STUDENT")
    void createVirtualMachine() throws Exception {
        Long teamId = 1L;
        String studentId = "s0";
        Long modelId = 3L;

        VirtualMachineDTO virtualMachineDTO = VirtualMachineDTO.builder()
                .num_vcpu(2)
                .disk_space(500)
                .ram(4)
                .studentId(studentId)
                .teamId(teamId)
                .modelId(modelId)
                .build();

        String request = objectMapper.writeValueAsString(virtualMachineDTO);
        mockMvc.perform(post("/API/virtual-machines")
                .contentType(MediaType.APPLICATION_JSON)
                .content(request))
                .andExpect(status().isCreated())
                .andDo(print());
    }

    @Test
    @WithMockUser(username = "wrong", roles = "STUDENT")
    void createVirtualMachine_wrongUsername() throws Exception {
        String courseId = "c0";
        Long teamId = 1L;
        String studentId = "student";
        Long vmId = 1L;
        Long modelId = 1L;

        VirtualMachineDTO virtualMachineDTO = VirtualMachineDTO.builder()
                .id(vmId)
                .num_vcpu(2)
                .disk_space(1000)
                .ram(4)
                .studentId(studentId)
                .teamId(teamId)
                .modelId(modelId)
                .build();

        String request = objectMapper.writeValueAsString(virtualMachineDTO);
        mockMvc.perform(post("/API/virtual-machines")
                .contentType(MediaType.APPLICATION_JSON)
                .content(request))
                .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(username = "")
    void createVirtualMachine_noUsernameAndNoRoles() throws Exception {
        String courseId = "c0";
        Long teamId = 1L;
        String studentId = "student";
        Long vmId = 1L;
        Long modelId = 1L;

        VirtualMachineDTO virtualMachineDTO = VirtualMachineDTO.builder()
                .id(vmId)
                .num_vcpu(2)
                .disk_space(1000)
                .ram(4)
                .studentId(studentId)
                .teamId(teamId)
                .modelId(modelId)
                .build();

        String request = objectMapper.writeValueAsString(virtualMachineDTO);
        mockMvc.perform(post("/API/virtual-machines")
                .contentType(MediaType.APPLICATION_JSON)
                .content(request))
                .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(roles = "STUDENT", username = "")
    void createVirtualMachine_noUsername() throws Exception {
        String courseId = "c0";
        Long teamId = 1L;
        String studentId = "student";
        Long vmId = 1L;
        Long modelId = 1L;

        VirtualMachineDTO virtualMachineDTO = VirtualMachineDTO.builder()
                .id(vmId)
                .num_vcpu(2)
                .disk_space(1000)
                .ram(4)
                .studentId(studentId)
                .teamId(teamId)
                .modelId(modelId)
                .build();

        String request = objectMapper.writeValueAsString(virtualMachineDTO);

        mockMvc.perform(post("/API/virtual-machines")
                .contentType(MediaType.APPLICATION_JSON)
                .content(request))
                .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(roles = "TEACHER")
    void createVirtualMachine_wrongRoles() throws Exception {
        String courseId = "c0";
        Long teamId = 1L;
        String studentId = "student";
        Long vmId = 1L;
        Long modelId = 1L;

        VirtualMachineDTO virtualMachineDTO = VirtualMachineDTO.builder()
                .id(vmId)
                .num_vcpu(2)
                .disk_space(1000)
                .ram(4)
                .studentId(studentId)
                .teamId(teamId)
                .modelId(modelId)
                .build();

        String request = objectMapper.writeValueAsString(virtualMachineDTO);

        mockMvc.perform(post("/API/virtual-machines")
                .contentType(MediaType.APPLICATION_JSON)
                .content(request))
                .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(username = "d0@polito.it", roles = "TEACHER")
    void getConfiguration() throws Exception {
        mockMvc.perform(get("/API/configurations/2"))
        .andExpect(status().isOk())
        .andDo(print());
    }
}
