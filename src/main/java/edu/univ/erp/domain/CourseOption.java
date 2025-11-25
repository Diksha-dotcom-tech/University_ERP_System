package edu.univ.erp.domain;

public class CourseOption {
    private int courseId;
    private String code;
    private String title;

    public CourseOption(int courseId, String code, String title) {
        this.courseId = courseId;
        this.code = code;
        this.title = title;
    }

    public int getCourseId() { return courseId; }
    public String getCode() { return code; }
    public String getTitle() { return title; }

    @Override
    public String toString() {
        return code + " - " + title;
    }
}

