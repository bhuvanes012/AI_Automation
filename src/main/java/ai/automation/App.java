package ai.automation;

import ai.automation.utils.Helper;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
@SpringBootApplication
public class App {
    public static void main(String[] args) throws IOException {
        SpringApplication.run(App.class,args);
        CodeGenerator.codeGen();
       // Helper.createFile("output/test.txt", "hello");
    }
    public static String llmCall(String query){
        String apiKey = "";

        String systemMessage = Helper.readFile("systemMessage.txt");
        String prompt = systemMessage.replace("{testSteps}",query);
        System.out.println(prompt);
        String result ="";
        String jsonBody = """
                {
                  "model": "gpt-5.4-mini",
                  "input": "%s"
                }
                """.formatted(
                          prompt
                         .replace("\\", "\\\\")
                         .replace("\"", "\\\"")
                         .replace("\n", "\\n")
                         .replace("\r", "\\r")
                         .replace("\t", "\\t")
                );
        try {
            HttpClient client = HttpClient.newHttpClient();

            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create("https://api.openai.com/v1/responses"))
                    .header("Content-Type", "application/json")
                    .header("Authorization", "Bearer " + apiKey)
                    .POST(HttpRequest.BodyPublishers.ofString(jsonBody))
                    .build();

            HttpResponse<String> response =
                    client.send(request, HttpResponse.BodyHandlers.ofString());
            JsonObject jsonObject = JsonParser.parseString(response.body()).getAsJsonObject();
            JsonArray output = jsonObject.get("output").getAsJsonArray();
            for(JsonElement out : output){
                JsonObject outputDetails= out.getAsJsonObject();
                JsonArray contentArray = outputDetails.get("content").getAsJsonArray();
                for(JsonElement content : contentArray){
                    JsonObject contentDetails= content.getAsJsonObject();
                    result = contentDetails.get("text").getAsString();
                }
            }
            System.out.println("Status Code: " + response.statusCode());
            System.out.println(result);

        } catch (Exception e) {
            e.printStackTrace();
        }
        return result;
    }
}
