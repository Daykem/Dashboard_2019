package fr.dashboard.server;

import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class RouteController {

    @GetMapping("/")
    public String index(ModelMap model) {
        model.addAttribute("dashboard", "name");
        return "index";
    }

    @GetMapping("/dashboard")
    public String dashboard(ModelMap model) {
        model.addAttribute("dashboard", "name");
        return "dashboard";
    }

    @GetMapping("/create")
    public String create(ModelMap model) {
        model.addAttribute("dashboard", "name");
        return "create";
    }

    @GetMapping("/whereip")
    public String whereip(ModelMap model) {
        model.addAttribute("dashboard", "name");
        return "whereip";
    }

    @GetMapping("/tw")
    public String tw(ModelMap model) {
        model.addAttribute("dashboard", "name");
        return "tw";
    }

    @GetMapping("/tmp")
    public String tmp(ModelMap model) {
        model.addAttribute("dashboard", "name");
        return "tmp";
    }

    @GetMapping("/music")
    public String music(ModelMap model) {
        model.addAttribute("dashboard", "name");
        return "music";
    }

    @GetMapping("/rand")
    public String rand(ModelMap model) {
        model.addAttribute("dashboard", "name");
        return "rand";
    }

    @GetMapping("/modifsub")
    public String modifsub(ModelMap model) {
        model.addAttribute("dashboard", "name");
        return "modifsub";
    }

    @GetMapping("/twitch")
    public String twitch(ModelMap model) {
        model.addAttribute("dashboard", "name");
        return "twitch";
    }

    @GetMapping("/sub")
    public String sub(ModelMap model) {
        model.addAttribute("dashboard", "name");
        return "sub";
    }

    @GetMapping("/userr")
    public String userr(ModelMap model) {
        model.addAttribute("dashboard", "name");
        return "userr";
    }

    @GetMapping("/cine")
    public String cine(ModelMap model) {
        model.addAttribute("dashboard", "name");
        return "cine";
    }

    @GetMapping("/map")
    public String map(ModelMap model) {
        model.addAttribute("dashboard", "name");
        return "map";
    }

    @GetMapping("/notif")
    public String notif(ModelMap model) {
        model.addAttribute("dashboard", "name");
        return "notif";
    }

    @GetMapping("/ytb")
    public String ytb(ModelMap model) {
        model.addAttribute("dashboard", "name");
        return "ytb";
    }

    @GetMapping("/gith")
    public String gith(ModelMap model) {
        model.addAttribute("dashboard", "name");
        return "gith";
    }

    @GetMapping("/reddit")
    public String reddit(ModelMap model) {
        model.addAttribute("dashboard", "name");
        return "reddit";
    }
}