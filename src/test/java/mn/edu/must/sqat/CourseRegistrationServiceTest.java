package mn.edu.must.sqat;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.contains;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class CourseRegistrationServiceTest {
    @Mock StudentRepository repository;
    @Mock EmailSender emailSender;
    CourseRegistrationService service;

    private static final String STUDENT_ID = "S001";
    private static final String COURSE_ID = "CS101";
    private static final Student ACTIVE_STUDENT = new Student(STUDENT_ID, "student@must.edu.mn", true, List.of("CS100"));

    @BeforeEach
    void setUp() {
        service = new CourseRegistrationService(repository, emailSender);
    }

    private void stubEligibleStudent() {
        when(repository.findStudent(STUDENT_ID)).thenReturn(ACTIVE_STUDENT);
        when(repository.courseExists(COURSE_ID)).thenReturn(true);
        when(repository.prerequisites(COURSE_ID)).thenReturn(List.of("CS100"));
    }

    @Test
    void activeStudentWithCompletedPrerequisites_registersAndSendsConfirmation() {
        stubEligibleStudent();
        assertEquals("OK", service.register(STUDENT_ID, COURSE_ID));
        verify(repository).saveRegistration(STUDENT_ID, COURSE_ID);
        verify(emailSender).send(eq(ACTIVE_STUDENT.email()), eq("Бүртгэл баталгаажлаа"), contains(COURSE_ID));
    }

    @Test
    void missingStudent_returnsNoStudentAndDoesNotSaveOrEmail() {
        when(repository.findStudent(STUDENT_ID)).thenReturn(null);
        assertEquals("ERROR_NO_STUDENT", service.register(STUDENT_ID, COURSE_ID));
        verify(repository, never()).saveRegistration(anyString(), anyString());
        verifyNoInteractions(emailSender);
    }

    @Test
    void inactiveStudent_returnsInactiveAndDoesNotInteractWithEmail() {
        when(repository.findStudent(STUDENT_ID)).thenReturn(new Student(STUDENT_ID, "student@must.edu.mn", false, List.of()));
        assertEquals("ERROR_INACTIVE_STUDENT", service.register(STUDENT_ID, COURSE_ID));
        verifyNoInteractions(emailSender);
        verify(repository, never()).saveRegistration(anyString(), anyString());
    }

    @Test
    void missingPrerequisite_returnsPrerequisitesErrorWithoutSaving() {
        when(repository.findStudent(STUDENT_ID)).thenReturn(new Student(STUDENT_ID, "student@must.edu.mn", true, List.of()));
        when(repository.courseExists(COURSE_ID)).thenReturn(true);
        when(repository.prerequisites(COURSE_ID)).thenReturn(List.of("CS100"));
        assertEquals("ERROR_PREREQUISITES", service.register(STUDENT_ID, COURSE_ID));
        verify(repository, never()).saveRegistration(anyString(), anyString());
    }

    @Test
    void emailFailure_keepsRegistrationSuccessful() {
        stubEligibleStudent();
        doThrow(new RuntimeException("SMTP unavailable")).when(emailSender).send(anyString(), anyString(), anyString());
        assertEquals("OK", service.register(STUDENT_ID, COURSE_ID));
        verify(repository).saveRegistration(STUDENT_ID, COURSE_ID);
        verify(emailSender).send(anyString(), anyString(), anyString());
    }

    @Test
    void nullArguments_throwIllegalArgumentExceptionWithoutDependencyInteractions() {
        assertThrows(IllegalArgumentException.class, () -> service.register(null, COURSE_ID));
        assertThrows(IllegalArgumentException.class, () -> service.register(STUDENT_ID, null));
        verifyNoInteractions(repository, emailSender);
    }

    @Test
    void confirmationEmailBody_containsCourseCode() {
        stubEligibleStudent();
        service.register(STUDENT_ID, COURSE_ID);
        ArgumentCaptor<String> bodyCaptor = ArgumentCaptor.forClass(String.class);
        verify(emailSender).send(eq(ACTIVE_STUDENT.email()), anyString(), bodyCaptor.capture());
        assertTrue(bodyCaptor.getValue().contains(COURSE_ID));
    }

    @ParameterizedTest(name = "{0} returns {1}")
    @CsvSource({"missing, ERROR_NO_STUDENT", "inactive, ERROR_INACTIVE_STUDENT", "prerequisite, ERROR_PREREQUISITES"})
    void invalidRegistrationState_returnsExpectedErrorWithoutSaving(String condition, String expected) {
        // Stubs differ by CSV row, so mark branch-dependent ones lenient to preserve strict Mockito defaults.
        lenient().when(repository.findStudent(STUDENT_ID)).thenReturn(condition.equals("missing") ? null :
                new Student(STUDENT_ID, "student@must.edu.mn", !condition.equals("inactive"), List.of()));
        lenient().when(repository.courseExists(COURSE_ID)).thenReturn(true);
        lenient().when(repository.prerequisites(COURSE_ID)).thenReturn(List.of("CS100"));
        assertEquals(expected, service.register(STUDENT_ID, COURSE_ID));
        verify(repository, never()).saveRegistration(anyString(), anyString());
        verifyNoInteractions(emailSender);
    }
}
