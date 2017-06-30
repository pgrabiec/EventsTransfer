package hello;

import org.springframework.social.connect.ConnectionRepository;
import org.springframework.social.google.api.Google;
import org.springframework.social.google.api.calendar.CalendarOperations;
import org.springframework.social.google.api.calendar.Event;
import org.springframework.social.google.api.calendar.EventPage;
import org.springframework.social.google.api.plus.Person;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/google")
public class GoogleController {

    private Google google;
    private ConnectionRepository connectionRepository;

    public GoogleController(Google google, ConnectionRepository connectionRepository) {
        this.google = google;
        this.connectionRepository = connectionRepository;
    }


    @GetMapping
    public String helloGoogle(Model model) {
        if (connectionRepository.findPrimaryConnection(Google.class) == null) {
            return "redirect:/connect/google";
        }
        Person user = google.plusOperations().getGoogleProfile();

        System.out.println(google.isAuthorized());

        System.out.println(": " + user.getGivenName() + " : " + user.getEmailAddresses() + " : " + user.getFamilyName());
        model.addAttribute("googleProfile", user);

        CalendarOperations operations = google.calendarOperations();

        EventPage page = operations.eventListQuery("primary").getPage();

        for (Event event : page.getItems()) {
            System.out.println("Google event: " + event.getSummary() + " " + event.getStart().getDateTime().toString());
        }

        return "google";
    }


}
