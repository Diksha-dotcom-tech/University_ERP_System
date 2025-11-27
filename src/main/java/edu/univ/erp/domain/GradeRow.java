package edu.univ.erp.domain;

public class GradeRow {
    public class GradeUtil {

        public static String getLetterGrade(Double finalScore) {
            if (finalScore == null) return "";
            if (finalScore >= 90) return "A+";
            if (finalScore >= 80) return "A";
            if (finalScore >= 70) return "B";
            if (finalScore >= 60) return "C";
            if (finalScore >= 50) return "D";
            if (finalScore >= 40) return "E";
            return "F";
        }

        public static int getGradePoint(String letter) {
            return switch (letter) {
                case "A+" -> 10;
                case "A" -> 9;
                case "B" -> 8;
                case "C" -> 7;
                case "D" -> 6;
                case "E" -> 5;
                default -> 0;
            };
        }
    }


    private int enrollmentId;
    private int studentId;
    private String rollNo;
    private String studentName;

    private Double quizScore;
    private Double midtermScore;
    private Double endsemScore;
    private Double finalScore;
    private String finalGradeText;

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

    public String getFinalGradeText() { return finalGradeText; }
    public void setFinalGradeText(String finalGradeText) { this.finalGradeText = finalGradeText; }
}