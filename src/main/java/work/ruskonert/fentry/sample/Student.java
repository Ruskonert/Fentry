package work.ruskonert.fentry.sample;

import work.ruskonert.fentry.Fentry;

import java.util.ArrayList;
import java.util.List;

/**
 * This class generated with Java Language.
 * It can be check the complication about other platform(Kotlin).
 * @see work.ruskonert.fentry.sample.School
 */

@SuppressWarnings("unused")
public class Student extends Fentry<Student>
{

    private List<String> description = new ArrayList<>();
    public List<String> getDescription() {
        return description;
    }
    public void setDescription(List<String> description) {
        this.description = description;
    }

    private double score = 100.0;
    public double getScore() {
        return score;
    }
    public void setScore(double score) {
        this.score = score;
    }

    public int getGrade() {
        return grade;
    }
    public void setGrade(int grade) {
        this.grade = grade;
    }

    public String getName() {
        return name;
    }
    public void setName(String name) {
        this.name = name;
    }

    private int grade = 1;
    private String name = "The friend's name";
    public Student() {

    }
}
