package org.example.workflowtracker;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@SpringBootApplication
@Controller
public class WorkflowtrackerApplication {

    public static void main(String[] args) {
        SpringApplication.run(WorkflowtrackerApplication.class, args);
    }

    @GetMapping("/")
    public String helloWorld(){
        return "index";
    }

}
