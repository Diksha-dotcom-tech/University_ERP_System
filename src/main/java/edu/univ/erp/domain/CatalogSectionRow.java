package edu.univ.erp.domain;

import java.sql.Time;
import java.time.LocalDateTime;

/**
 * A unified data object representing one row in the course catalog.
 * This class is used by CatalogDao, StudentService, and StudentDashboardFrame.
 */
public class CatalogSectionRow {

    private int sectionId;
    private int courseId;
    private String courseCode;
    private String courseTitle;
    private int instructorId;
    private String instructorName;

    private String dayOfWeek;
    private Time startTime;
    private Time endTime;
    private String room;

    private int capacity;
    private int enrolled;      // computed by query
    private int seatsLeft;     // computed by query

    private String semester;
    private int year;
    private int credits;

    // NEW: registration + drop deadlines
    private LocalDateTime registrationDeadline;
    private LocalDateTime dropDeadline;

    // ---------------------------
    // Getters & Setters
    // ---------------------------

    public int getSectionId() { return sectionId; }
    public void setSectionId(int sectionId) { this.sectionId = sectionId; }

    public int getCourseId() { return courseId; }
    public void setCourseId(int courseId) { this.courseId = courseId; }

    public String getCourseCode() { return courseCode; }
    public void setCourseCode(String courseCode) { this.courseCode = courseCode; }

    public String getCourseTitle() { return courseTitle; }
    public void setCourseTitle(String courseTitle) { this.courseTitle = courseTitle; }

    public int getInstructorId() { return instructorId; }
    public void setInstructorId(int instructorId) { this.instructorId = instructorId; }

    public String getInstructorName() { return instructorName; }
    public void setInstructorName(String instructorName) { this.instructorName = instructorName; }

    public String getDayOfWeek() { return dayOfWeek; }
    public void setDayOfWeek(String dayOfWeek) { this.dayOfWeek = dayOfWeek; }

    public Time getStartTime() { return startTime; }
    public void setStartTime(Time startTime) { this.startTime = startTime; }

    public Time getEndTime() { return endTime; }
    public void setEndTime(Time endTime) { this.endTime = endTime; }

    public String getRoom() { return room; }
    public void setRoom(String room) { this.room = room; }

    public int getCapacity() { return capacity; }
    public void setCapacity(int capacity) { this.capacity = capacity; }

    public int getEnrolled() { return enrolled; }
    public void setEnrolled(int enrolled) { this.enrolled = enrolled; }

    public int getSeatsLeft() { return seatsLeft; }
    public void setSeatsLeft(int seatsLeft) { this.seatsLeft = seatsLeft; }

    public String getSemester() { return semester; }
    public void setSemester(String semester) { this.semester = semester; }

    public int getYear() { return year; }
    public void setYear(int year) { this.year = year; }

    public LocalDateTime getRegistrationDeadline() { return registrationDeadline; }
    public void setRegistrationDeadline(LocalDateTime registrationDeadline) { this.registrationDeadline = registrationDeadline; }

    public LocalDateTime getDropDeadline() { return dropDeadline; }
    public void setDropDeadline(LocalDateTime dropDeadline) { this.dropDeadline = dropDeadline; }

    // ---------------------------
    // Derived helpers (used by UI)
    // ---------------------------

    /** UI helper: "09:00 - 10:30" */
    public String getTimeRange() {
        if (startTime == null || endTime == null) return "";
        return startTime.toString() + " - " + endTime.toString();
    }

    public Object getCredits() {return credits;
    }
    public void setCredits(int credits) { this.credits = credits; }

}