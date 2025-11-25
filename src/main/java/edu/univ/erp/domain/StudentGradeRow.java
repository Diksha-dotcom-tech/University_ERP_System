package edu.univ.erp.domain;

public class StudentGradeRow {

    private String courseCode;
    private String courseTitle;
    private String semester;
    private int year;

    private Double quizScore;
    private Double midtermScore;
    private Double endsemScore;
    private Double finalScore;
    private String finalGradeText;

    public String getCourseCode() { return courseCode; }
    public void setCourseCode(String courseCode) { this.courseCode = courseCode; }

    public String getCourseTitle() { return courseTitle; }
    public void setCourseTitle(String courseTitle) { this.courseTitle = courseTitle; }

    public String getSemester() { return semester; }
    public void setSemester(String semester) { this.semester = semester; }

    public int getYear() { return year; }
    public void setYear(int year) { this.year = year; }

    public Double getQuizScore() { return quizScore; }
    public void setQuizScore(Double quizScore) { this.quizScore = quizScore; }

    public Double getMidtermScore() { return midtermScore; }
    public void setMidtermScore(Double midtermScore) { this.midtermScore = midtermScore; }

    public Double getEndsemScore() { return endsemScore; }
    public void setEndsemScore(Double endsemScore) { this.endsemScore = endsemScore; }

    public Double getFinalScore() { return finalScore; }
    public void setFinalScore(Double finalScore) { this.finalScore = finalScore; }

    public String getFinalGradeText() { return finalGradeText; }
    public void setFinalGradeText(String finalGradeText) { this.finalGradeText = finalGradeText; }
}

