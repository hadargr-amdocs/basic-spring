package com.handson.basic;

import com.handson.basic.model.Student;
import com.handson.basic.repo.StudentRepository;
import com.handson.basic.service.StudentService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.AssertionsForClassTypes.assertThatThrownBy;
import static org.mockito.Mockito.*;

/**
 * Unit-level tests: no Spring, no JPA, no H2.
 */
@ExtendWith(MockitoExtension.class)          // JUnit hooks up Mockito
class StudentServiceTest {

    /** Mock the repository dependency */
    @Mock StudentRepository repo;

    /** Class under test (CUT) with mocks injected */
    @InjectMocks StudentService service;

    Student ada;

    @BeforeEach
    void setUp() {
        ada = Student.StudentBuilder.aStudent()
                .id(1L)
                .fullname("Ada Lovelace")
                .satScore(700)
                .build();
    }

    /* ------------------------------------------------------------------
       happy-path: findById()
       ------------------------------------------------------------------ */
    @Test
    void findById_returnsStudentWhenPresent() {
        when(repo.findById(1L)).thenReturn(Optional.of(ada));

        Optional<Student> result = service.findById(1L);

        assertThat(result).containsSame(ada);
        verify(repo).findById(1L);           // repo called exactly once
        verifyNoMoreInteractions(repo);
    }

    /* ------------------------------------------------------------------
       business rule: SAT filter
       ------------------------------------------------------------------ */
    @Test
    void getStudentsWithHighSat_delegatesToRepo() {
        when(repo.findAllBySatScoreGreaterThan(650)).thenReturn(List.of(ada));

        List<Student> list = service.getStudentWithSatHigherThan(650);

        assertThat(list).hasSize(1).containsExactly(ada);
        verify(repo).findAllBySatScoreGreaterThan(650);
    }

    /* ------------------------------------------------------------------
       save() passes entity straight through
       ------------------------------------------------------------------ */
    @Test
    void save_persistsViaRepo() {
        when(repo.save(ada)).thenReturn(ada);

        Student saved = service.save(ada);

        assertThat(saved).isSameAs(ada);
        verify(repo).save(ada);
    }

    // Positive path: valid ID should return an existing Student
    @Test
    void findById_withValidId_returnsStudent() {
        when(repo.findById(1L)).thenReturn(Optional.of(ada));
        Optional<Student> result = service.findById(1L);
        assertThat(result).contains(ada);
    }

    // Positive path: requesting SAT scores above a threshold that exists should return a list
    @Test
    void getStudentWithSatHigherThan_validScore_returnsList() {
        when(repo.findAllBySatScoreGreaterThan(600)).thenReturn(List.of(ada));
        List<Student> result = service.getStudentWithSatHigherThan(600);
        assertThat(result).containsExactly(ada);
    }

    // Negative path: saving a Student with a null fullname should throw NullPointerException
    @Test
    void save_withNullFullname_throwsException() {
        Student broken = Student.StudentBuilder.aStudent()
                .id(2L)
                .fullname(null)
                .satScore(500)
                .build();

        // צפוי להיזרק NullPointerException
        assertThatThrownBy(() -> service.save(broken))
                .isInstanceOf(NullPointerException.class);
    }

    // Negative path: attempting to delete a null Student should throw NullPointerException
    @Test
    void delete_withNullStudent_throwsException() {
        assertThatThrownBy(() -> service.delete(null))
                .isInstanceOf(NullPointerException.class);
    }




}