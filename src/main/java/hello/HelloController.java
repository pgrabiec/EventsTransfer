package hello;

import org.springframework.social.connect.ConnectionRepository;
import org.springframework.social.facebook.api.*;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.bind.annotation.*;

import static java.lang.System.currentTimeMillis;
import static java.lang.System.setOut;

import java.util.Date;

@Controller
@RequestMapping("/")
public class HelloController {

    private Facebook facebook;
    private ConnectionRepository connectionRepository;

    public HelloController(Facebook facebook, ConnectionRepository connectionRepository) {
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

        String [] fields = { "id" , "name" , "email",  "first_name", "last_name" };
        model.addAttribute("facebookProfile", facebook.fetchObject("me", User.class, fields));

        MultiValueMap<String, String> parameters = new LinkedMultiValueMap<String, String>();
        Date since = new Date(currentTimeMillis() - 2*(1000L * 60L * 60L * 24L * 7L));
        parameters.set("since", since.toString());
        String[] fieldE = { "name" , "description" , "start_time"};
        this.events = facebook.fetchConnections("me","events",Event.class,parameters,fieldE);

        model.addAttribute("events",events);
        model.addAttribute("checked",checked);

        return "hello";
    }

    @PostMapping("/submit")
    public String submit(@ModelAttribute(value= "checked") Checked checked) {

            for (Integer b : checked.getEvents()) {
                System.out.println(events.get(b).getName());
            }
        return "submit";
    }

}