package hello;

import com.google.api.client.googleapis.auth.oauth2.GoogleCredential;
import com.google.api.client.googleapis.javanet.GoogleNetHttpTransport;
import com.google.api.client.http.HttpTransport;
import com.google.api.client.json.JsonFactory;
import com.google.api.client.json.jackson2.JacksonFactory;
import com.google.api.client.util.DateTime;
import com.google.api.services.calendar.Calendar;
import com.google.api.services.calendar.model.EventDateTime;
import org.springframework.social.connect.ConnectionRepository;
import org.springframework.social.facebook.api.*;
import org.springframework.social.facebook.api.Event;
import org.springframework.social.google.api.Google;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.bind.annotation.*;

import static java.lang.System.currentTimeMillis;

import java.io.IOException;
import java.security.GeneralSecurityException;
import java.util.Date;


@Controller
@RequestMapping("/")
public class HelloController {

    private Google google;
    private Facebook facebook;
    private ConnectionRepository connectionRepository;

    private static final JsonFactory JSON_FACTORY =
            JacksonFactory.getDefaultInstance();

    /** Global instance of the HTTP transport. */
    private static HttpTransport HTTP_TRANSPORT;


    private static final String APPLICATION_NAME =
            "Google Calendar API Java Quickstart";

    public HelloController(Google google,Facebook facebook, ConnectionRepository connectionRepository) {
        this.google = google;
        this.facebook = facebook;
        this.connectionRepository = connectionRepository;
    }

    private PagedList<Event> events = null;
    private Checked checked = new Checked();

    @GetMapping
    public String helloFacebook(Model model) {
        if (connectionRepository.findPrimaryConnection(Facebook.class) == null) {
            return "redirect:/connect/facebook";
        }

        if (connectionRepository.findPrimaryConnection(Google.class) == null) {
            return "redirect:/connect/google";
        }

        String [] fields = { "id" , "name" , "email",  "first_name", "last_name" };
        model.addAttribute("facebookProfile", facebook.fetchObject("me", User.class, fields));

        MultiValueMap<String, String> parameters = new LinkedMultiValueMap<String, String>();
        Date since = new Date(currentTimeMillis() - 2*(1000L * 60L * 60L * 24L * 7L));
        parameters.set("since", since.toString());
        String[] fieldE = { "name" , "description" , "start_time","end_time"};
        this.events = facebook.fetchConnections("me","events",Event.class,parameters,fieldE);

        model.addAttribute("events",events);
        model.addAttribute("checked",checked);

        return "hello";
    }

    @PostMapping("/submit")
    public String submit(@ModelAttribute(value= "checked") Checked checked) {

        if (connectionRepository.findPrimaryConnection(Facebook.class) == null) {
            return "redirect:/connect/facebook";
        }

        if (connectionRepository.findPrimaryConnection(Google.class) == null) {
            return "redirect:/connect/google";
        }

        try {
            HTTP_TRANSPORT = GoogleNetHttpTransport.newTrustedTransport();
        } catch (GeneralSecurityException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        GoogleCredential credential = new GoogleCredential().setAccessToken(google.getAccessToken());
        Calendar service = new com.google.api.services.calendar.Calendar.Builder(
                HTTP_TRANSPORT, JSON_FACTORY, credential)
                .setApplicationName(APPLICATION_NAME)
                .build();


        if (checked.getEvents() != null) {
            for (Integer b : checked.getEvents()) {
                Event event = events.get(b);

                com.google.api.services.calendar.model.Event eventToAdd = new com.google.api.services.calendar.model.Event()
                        .setSummary(event.getName())
                        .setDescription(event.getDescription());

                DateTime startDateTime = new DateTime(event.getStartTime());
                EventDateTime start = new EventDateTime().setDateTime(startDateTime);
                eventToAdd.setStart(start);

                DateTime endDateTime;
                if(event.getEndTime()!= null) {
                    endDateTime = new DateTime(event.getEndTime());
                }
                else{
                    endDateTime = startDateTime;
                }
                EventDateTime end = new EventDateTime().setDateTime(endDateTime);
                eventToAdd.setEnd(end);

                String calendarId = "primary";

                try {
                    service.events().insert(calendarId, eventToAdd).execute();
                } catch (IOException e) {
                    e.printStackTrace();
                }
                System.out.println("Added: "+event.getName());
            }
        }

        return "submit";
    }

}

