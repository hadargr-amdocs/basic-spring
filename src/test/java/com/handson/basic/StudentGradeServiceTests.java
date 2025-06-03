package com.handson.basic;

import com.handson.basic.model.StudentGrade;
import com.handson.basic.repo.StudentGradeRepository;
import com.handson.basic.service.StudentGradeService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class StudentGradeServiceTests {

    @Mock
    StudentGradeRepository repo;

    @InjectMocks
    StudentGradeService service;

    StudentGrade grade;

    @BeforeEach
    void setUp() {
        grade = new StudentGrade(1L, "Math", 95);
    }

    // ✅ Happy Path 1: save returns saved object
    @Test
    void save_validStudentGrade_returnsSaved() {
        when(repo.save(grade)).thenReturn(grade);

        StudentGrade result = service.save(grade);

        assertThat(result).isSameAs(grade);
        verify(repo).save(grade);
    }

    // ✅ Happy Path 2: findById returns existing object
    @Test
    void findById_existingId_returnsStudentGrade() {
        when(repo.findById(1L)).thenReturn(Optional.of(grade));

        Optional<StudentGrade> result = service.findById(1L);

        assertThat(result).contains(grade);
        verify(repo).findById(1L);
    }

    // ❌ Negative Path 1: save with null throws exception
    @Test
    void save_nullStudentGrade_throwsException() {
        assertThatThrownBy(() -> service.save(null))
                .isInstanceOf(NullPointerException.class);
    }

    // ❌ Negative Path 2: findById with non-existing ID returns empty
    @Test
    void findById_nonExistingId_returnsEmpty() {
        when(repo.findById(999L)).thenReturn(Optional.empty());

        Optional<StudentGrade> result = service.findById(999L);

        assertThat(result).isEmpty();
        verify(repo).findById(999L);
    }
}
