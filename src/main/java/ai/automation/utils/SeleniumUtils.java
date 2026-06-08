package ai.automation.utils;

import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;

public class SeleniumUtils {


    public static void highlightElement(WebDriver driver, WebElement element) {

        try {

            JavascriptExecutor js = (JavascriptExecutor) driver;

            String originalStyle = element.getAttribute("style");

            js.executeScript(
                    "arguments[0].setAttribute('style', "
                            + "'border: 3px solid red; background: yellow;');",
                    element
            );

            Thread.sleep(1000);

            js.executeScript(
                    "arguments[0].setAttribute('style', arguments[1]);",
                    element,
                    originalStyle
            );

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
