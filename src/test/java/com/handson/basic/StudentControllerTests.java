package com.handson.basic;


import com.fasterxml.jackson.databind.ObjectMapper;
import com.handson.basic.DTO.StudentIn;
import com.handson.basic.model.Student;
import com.handson.basic.repo.StudentRepository;
import com.handson.basic.service.AWSService;
import com.handson.basic.service.SmsService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;


import java.time.LocalDate;


import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;


/**
 * End-to-end tests for {@code StudentsController}.
 * Security filters are disabled, so no JWT header is required.
 */
@SpringBootTest(
        classes = BasicApplication.class,
        webEnvironment = SpringBootTest.WebEnvironment.MOCK,
        properties = "jwt.secret=test-secret"
)
@AutoConfigureMockMvc(addFilters = false)   // bypass JWT/security filters
@ActiveProfiles("test")                     // use in-memory H2 profile
class StudentsControllerTests {


    @Autowired MockMvc mvc;
    @Autowired StudentRepository repo;
    @Autowired ObjectMapper om;


    @MockBean SmsService smsService;        // stubbed externals
    @MockBean AWSService awsService;


    private Student saved;


    @BeforeEach
    void initData() {
        repo.deleteAll();
        saved = Student.StudentBuilder.aStudent()
                .fullname("Ada Lovelace")
                .birthDate(LocalDate.of(2000, 1, 1))
                .satScore(700)
                .graduationScore(95.0)
                .phone("0501234567")
                .build();
        saved = repo.save(saved);
    }


    /* ---------- happy path ---------- */


    @Test
    void getStudentById_returnsDetails() throws Exception {
        mvc.perform(get("/api/students/{id}", saved.getId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.fullname").value("Ada Lovelace"));
    }


    @Test
    void createStudent_persistsAndReturnsEntity() throws Exception {
        StudentIn in = new StudentIn();
        in.setFullname("Grace Hopper");
        in.setBirthDate(LocalDate.of(1999, 12, 31));
        in.setSatScore(650);
        in.setGraduationScore(90.0);
        in.setPhone("0507654321");


        String body = mvc.perform(post("/api/students")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(om.writeValueAsString(in)))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();


        Long newId = om.readTree(body).get("id").asLong();
        assertThat(repo.findById(newId)).isPresent();
    }


    @Test
    void getHighSatStudents_filtersCorrectly() throws Exception {
        mvc.perform(get("/api/students/highSat").param("sat", "650"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].fullname").value("Ada Lovelace"));
    }


    /* ---------- new negative-path test ---------- */


    @Test
    void getStudentById_notFound_returns404ErrorPayload() throws Exception {
        long nonexistentId = 9999L;


        mvc.perform(get("/api/students/{id}", nonexistentId))
                .andExpect(status().isNotFound())
                // payload created by GlobalExceptionHandler.ErrorResponse
                .andExpect(jsonPath("$.status").value(404))
                .andExpect(jsonPath("$.error").value("Not Found"))
                .andExpect(jsonPath("$.message").value("User not found: " + nonexistentId));
    }
}
