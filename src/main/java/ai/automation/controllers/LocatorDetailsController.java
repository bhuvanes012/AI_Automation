package ai.automation.controllers;


import ai.automation.CodeGenerator;
import ai.automation.model.LocatorDetails;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.ArrayList;
import java.util.List;

/**
 * REST controller that returns a collection of {@link LocatorDetails}
 * in JSON format.
 */
@RestController
@RequestMapping("/api/locators")
public class LocatorDetailsController {

    /**
     * GET /api/locators
     *
     * Returns a static list of LocatorDetails objects.
     *
     * @return list of LocatorDetails serialized as JSON
     */
    @GetMapping
    public List<LocatorDetails> getAllLocators() {

        return CodeGenerator.steps;
    }

    @GetMapping("/clear")
    public String clearSteps() {

        CodeGenerator.steps.clear();

        return "All locator steps have been cleared.";
    }

    @PostMapping("/locatorInput")
    public String locatorDetails(
            ArrayList<LocatorDetails> locatorDetails) {

        return "All locator steps have been cleared.";
    }
}