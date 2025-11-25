package edu.univ.erp.domain;

public class GradeRow {

    private int enrollmentId;
    private int studentId;
    private String rollNo;
    private String studentName;

    private Double quizScore;
    private Double midtermScore;
    private Double endsemScore;
    private Double finalScore;
    private String finalGradeText; // ADDED

    public int getEnrollmentId() { return enrollmentId; }
    public void setEnrollmentId(int enrollmentId) { this.enrollmentId = enrollmentId; }

    public int getStudentId() { return studentId; }
    public void setStudentId(int studentId) { this.studentId = studentId; }

    public String getRollNo() { return rollNo; }
    public void setRollNo(String rollNo) { this.rollNo = rollNo; }

    public String getStudentName() { return studentName; }
    public void setStudentName(String studentName) { this.studentName = studentName; }

    public Double getQuizScore() { return quizScore; }
    public void setQuizScore(Double quizScore) { this.quizScore = quizScore; }

    public Double getMidtermScore() { return midtermScore; }
    public void setMidtermScore(Double midtermScore) { this.midtermScore = midtermScore; }

    public Double getEndsemScore() { return endsemScore; }
    public void setEndsemScore(Double endsemScore) { this.endsemScore = endsemScore; }

    public Double getFinalScore() { return finalScore; }
    public void setFinalScore(Double finalScore) { this.finalScore = finalScore; }

    // ADDED
    public String getFinalGradeText() { return finalGradeText; }
    public void setFinalGradeText(String finalGradeText) { this.finalGradeText = finalGradeText; }
}