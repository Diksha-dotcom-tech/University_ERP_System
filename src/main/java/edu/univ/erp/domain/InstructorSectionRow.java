package edu.univ.erp.domain;

/**
 * Data Transfer Object for displaying instructor's sections.
 * CORRECTED: Includes setters to allow DAOs to populate the fields.
 */
public class InstructorSectionRow {
    private int sectionId;
    private String courseCode;
    private String courseTitle;
    private String dayOfWeek;
    private String timeRange;
    private String room;
    private int capacity;
    private int enrolledCount;
    private String semester;
    private int year;

    // ======================================
    // GETTERS (Required for UI/Service Layer)
    // ======================================

    public int getSectionId() { return sectionId; }
    public String getCourseCode() { return courseCode; }
    public String getCourseTitle() { return courseTitle; }
    public String getDayOfWeek() { return dayOfWeek; }
    public String getTimeRange() { return timeRange; }
    public String getRoom() { return room; }
    public int getCapacity() { return capacity; }
    public int getEnrolledCount() { return enrolledCount; }
    public String getSemester() { return semester; }
    public int getYear() { return year; }

    // ======================================
    // SETTERS (Required for DAO Layer - FIX)
    // ======================================

    public void setSectionId(int sectionId) {
        this.sectionId = sectionId;
    }

    public void setCourseCode(String courseCode) {
        this.courseCode = courseCode;
    }

    public void setCourseTitle(String courseTitle) {
        this.courseTitle = courseTitle;
    }

    public void setDayOfWeek(String dayOfWeek) {
        this.dayOfWeek = dayOfWeek;
    }

    public void setTimeRange(String timeRange) {
        this.timeRange = timeRange;
    }

    public void setRoom(String room) {
        this.room = room;
    }

    public void setCapacity(int capacity) { // This setter is also often used by the DAO
        this.capacity = capacity;
    }

    public void setEnrolledCount(int enrolledCount) {
        this.enrolledCount = enrolledCount;
    }

    public void setSemester(String semester) {
        this.semester = semester;
    }

    public void setYear(int year) {
        this.year = year;
    }
}