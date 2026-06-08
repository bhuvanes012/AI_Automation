package ai.automation.controllers;


import ai.automation.App;
import ai.automation.CodeOverrider;
import ai.automation.model.LocatorDetails;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.ArrayList;
import java.util.HashMap;


@RestController
@RequestMapping("/api")
public class GenerateCodeController {

    @PostMapping("/generateLLMCode")
    public String generateLLMCode(
            @RequestBody ArrayList<LocatorDetails> locatorDetail) {

        StringBuilder result = new StringBuilder();

        int stepNo = 0;

        for (LocatorDetails locatorDetails : locatorDetail) {

            String action = locatorDetails.getAction();

            if (locatorDetails.getCustomPrompt() != null) {
                action = locatorDetails.getCustomPrompt();
            }

            result.append(
                    "\nStep: " + (++stepNo) + "\n"
                            + "Label: " + locatorDetails.getElementLabel() + "\n"
                            + "Xpath: " + locatorDetails.getElementXpath() + "\n"
                            + "Action: " + action + "\n"
                            + "Application URL: "
                            + locatorDetails.getApplicationURL() + "\n"
            );

            if (locatorDetails.getElementInputValue() != null
                    && !locatorDetails.getElementInputValue().isEmpty()) {

                result.append(
                        "Input Value: "
                                + locatorDetails.getElementInputValue()
                                + "\n");
            }
        }


        System.out.println("************************");
        System.out.println("*** Waiting for LLM response ***");
        System.out.println("************************");

        ///String response = chatService.chat(promptDetails);

        System.out.println(result.toString());
        return App.llmCall(result.toString());
    }

    @PostMapping(value = "/applyCode", consumes = "application/json")
    public ResponseEntity<?> applyCode(
            @RequestBody String rawJson) {

        // rawJson contains the exact JSON string sent
        // from the JavaScript frontend.

        System.out.println("Received raw JSON:");
        System.out.println(rawJson);

        CodeOverrider.overrideCode(rawJson);

        HashMap<String, Object> response = new HashMap<>();

        response.put("status", "ok");
        response.put("message", "JSON received as string");
        response.put("received", rawJson);

        return ResponseEntity.ok(response);
    }
}