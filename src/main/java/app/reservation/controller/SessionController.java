package app.reservation.controller;

import java.util.List;

import javax.servlet.http.HttpSession;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.SessionAttributes;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import app.reservation.model.Person;
import app.reservation.model.Session;
import app.reservation.model.User;
import app.reservation.model.UserRoles;
import app.reservation.service.PersonService;
import app.reservation.service.SessionService;
import app.reservation.service.UserService;

@Controller
@RequestMapping("/session")
@SessionAttributes({ "update", "counselors" })
public class SessionController {

	@Autowired
	private SessionService sessionService;

	@Autowired
	private PersonService personService;
	
	@Autowired
	private UserService userService;

	@RequestMapping(value = { "/add" }, method = RequestMethod.GET)
	public String getSessionForm(@ModelAttribute("session") Session session, Model model) {
		List<Person> counselors = personService.findPersonByRoleName(UserRoles.ROLE_COUNSELOR);
		model.addAttribute("counselors", counselors);
		return "session";
	}

	@RequestMapping(value = "/add", method = RequestMethod.POST)
	public String addSession(@ModelAttribute("session") Session session, Model model, HttpSession httpSession) {
		//
		// =======
		// @RequestMapping(value ={"/addSession"}, method = RequestMethod.GET)
		// public String getSessionForm(@ModelAttribute("session") Session session,Model
		// model) {
		// List<Person>counselors=personService.findPersonByRoleName(UserRoles.ROLE_COUNSELOR);
		// model.addAttribute("counselors",counselors);
		// return "session";
		// }
		//
		// @RequestMapping(value ="/addsession", method = RequestMethod.POST)
		// public String addSession(@Valid @ModelAttribute("session") Session
		// session,BindingResult result,Model model, HttpSession httpSession) {
		//
		// if (result.hasErrors() ) {
		//
		// return "session";
		//
		// }
		// >>>>>>> master
		Person person = personService.findById(session.getCounselor().getId());
		session.setCounselor(person);

		System.out.println("+++++++++++" + person);

		sessionService.save(session);

		return "redirect:/session/sessionList";

	}

	@RequestMapping(value = "/updateSession/{id}", method = RequestMethod.GET)
	public String updateSession(@PathVariable Long id, @ModelAttribute("session") Session session, Model modle) {
		Session updateSession = sessionService.findOne(id);
		modle.addAttribute("mode", "EDIT_SESSION");
		modle.addAttribute("update", updateSession);
		List<Person> counselors = personService.findPersonByRoleName(UserRoles.ROLE_COUNSELOR);
		modle.addAttribute("counselors", counselors);
		return "session";
	}

	@RequestMapping(value = "/deletSession/{id}", method = RequestMethod.GET)
	public String deletSession(@PathVariable Long id, RedirectAttributes redirect) {

		sessionService.delete(id);
		return "redirect:/session/sessionList";

	}

	@RequestMapping(value = { "", "/", "/sessionList" }, method = RequestMethod.GET)

	public String getSessionList(Model model) {

		model.addAttribute("sessionList", sessionService.findAll());

		return "sessionList";
	}
	
	@RequestMapping(value="/counselor", method=RequestMethod.GET)
	public String getSessionByCounselor(Model model)
	{
		// Get current user
				Authentication authority = SecurityContextHolder.getContext().getAuthentication();
				String name = authority.getName();
				System.out.println("all the names........................" + name);
				User user = userService.findByUsername(name);
				Person person = user.getPerson();
				
				List<Session> sessionPerCouncelor=sessionService.getSesssionByCounselor(person);
				model.addAttribute("sessionList", sessionPerCouncelor);
				return "sessionList";
				
	}

}
