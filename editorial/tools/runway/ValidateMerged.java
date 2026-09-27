import com.fasterxml.jackson.databind.ObjectMapper;
import com.onthisday.ingestion.*;
import com.onthisday.ingestion.quiz.*;
import java.nio.file.Path;
public class ValidateMerged {
  public static void main(String[] a) {
    var om = new ObjectMapper();
    var c = new CuratedContentReader(om).read(Path.of(a[0]));
    var r = new CuratedContentValidator().validate(c.eventsFile(), c.dailyEventsFile());
    r.errors().forEach(e -> System.out.println("EVENT ERROR " + e.path() + ": " + e.message()));
    r.warnings().forEach(w -> System.out.println("EVENT WARN " + w.path() + ": " + w.message()));
    var q = new QuizContentReader(om).read(Path.of(a[0], "quizzes"));
    var qr = new QuizContentValidator().validate(q);
    qr.errors().forEach(e -> System.out.println("QUIZ ERROR " + e.path() + ": " + e.message()));
    qr.warnings().forEach(w -> System.out.println("QUIZ WARN " + w.path() + ": " + w.message()));
    System.out.println("events valid=" + r.valid() + " quiz valid=" + qr.valid());
  }
}
