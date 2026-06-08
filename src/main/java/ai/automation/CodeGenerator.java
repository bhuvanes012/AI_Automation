package ai.automation;

import ai.automation.model.LocatorDetails;
import ai.automation.utils.SeleniumUtils;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.github.bonigarcia.wdm.WebDriverManager;
import org.openqa.selenium.By;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.chrome.ChromeDriver;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class CodeGenerator {
    public static WebElement lastClickElement;


    public static List<LocatorDetails> steps = new ArrayList<>();

    public static void codeGen() {
        WebDriverManager.chromedriver().setup();
        WebDriver driver = new ChromeDriver();
        driver.get("https://demo.automationtesting.in/Register.html");
        injectScript(driver);

        while (true) {

            try {

                Thread.sleep(500);

                // getStoredEvents(DriverFactory.getDriver());

                WebElement element = getLastClickedElement(driver);

                if (element != null) {

                    System.out.println(element.getTagName());

                    if (lastClickElement == null || (!lastClickElement.equals(element))) {

                        System.out.println("New click happened: " + element.getTagName());

                        lastClickElement = element;

                        SeleniumUtils.highlightElement(driver, element);

                        String elementText = "";

                        List<WebElement> childElements =
                                element.findElements(By.xpath("./*"));

                        if (childElements.size() > 1) {
                            elementText = childElements.get(0)
                                    .getAttribute("textContent");
                        } else {
                            elementText = element.getAttribute("textContent");
                        }

                        String splitedLabel =
                                beforeFirstSpecial(elementText);

                        List<String> elementXpathList =
                                (List<String>) ((JavascriptExecutor) driver)
                                        .executeScript(
                                                getXPathJs(),
                                                element,
                                                splitedLabel.trim()
                                        );

                        System.out.println(
                                "Xpath ->>>> "
                                        + Arrays.toString(
                                        elementXpathList.toArray()
                                )
                        );

                        for (String xpath : elementXpathList) {
                            System.out.println(xpath);
                        }

                        LocatorDetails elementDetail =
                                new LocatorDetails();

                        elementDetail.setLisOfXpath(elementXpathList);
                        elementDetail.setElementHTML(
                                element.getAttribute("outerHTML")
                        );
                        elementDetail.setElementXpath("");
                        elementDetail.setElementLabel(
                                elementText.trim()
                        );
                        elementDetail.setApplicationURL(
                                driver.getCurrentUrl()
                        );

                        if (steps.size() == 0) {

                            LocatorDetails applicationLoad =
                                    new LocatorDetails();

                            applicationLoad.setElementHTML(
                                    element.getAttribute("outerHTML")
                            );

                            applicationLoad.setElementXpath("");

                            applicationLoad.setElementLabel(
                                    "Load the Application URL:\n"
                                            + driver.getCurrentUrl()
                            );

                            applicationLoad.setApplicationURL(
                                    driver.getCurrentUrl()
                            );

                            applicationLoad.setAction(
                                    "Load the application URL: "
                                            + driver.getCurrentUrl()
                            );

                            steps.add(applicationLoad);
                        }

                        steps.add(elementDetail);


                    } else {

                        System.out.println("No changes");

                        if (element.getTagName().toLowerCase().trim()
                                .equalsIgnoreCase("input")) {

                            String value =
                                    element.getAttribute("value");

                            steps.get(steps.size() - 1).setElementInputValue(value);
                        }
                    }
                }

            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }


    public static void injectScript(WebDriver driver) {

        String jsCode = """
                window._lastClickedElement = null;
                
                document.addEventListener('click', function(e) {
                
                    let el = e.target;
                
                    const eventData = {
                        action: 'click',
                        tag: el.tagName,
                        id: el.id,
                        xpath: el,
                        timestamp: new Date().toISOString()
                    };
                
                    if (!window._clickEvents) {
                        window._clickEvents = [];
                    }
                
                    if (el.tagName === 'SELECT') {
                
                        const selectedOption = el.options[el.selectedIndex];
                
                        eventData.selectedOption = {
                            text: selectedOption.text,
                            value: selectedOption.value,
                            id: selectedOption.id
                        };
                
                        console.log(el.selectedIndex);
                
                        if (el.selectedIndex !== 0) {
                            el = el.options[el.selectedIndex];
                        }
                    }
                
                    window._clickEvents.push(eventData);
                
                    window._lastClickedElement = el;
                
                    console.log('PW_EVENT::' + JSON.stringify(eventData));
                
                }, true);
                """;


        String mouseHighlight = "let highlightedElement = null;\n" +
                "\n" +
                "document.addEventListener(\"mouseover\", (event) => {\n" +
                "\n" +
                "    if (highlightedElement) {\n" +
                "        highlightedElement.style.outline = \"\";\n" +
                "    }\n" +
                "\n" +
                "    highlightedElement = event.target;\n" +
                "    highlightedElement.style.outline = \"3px solid red\";\n" +
                "});";
        JavascriptExecutor js = (JavascriptExecutor) driver;
        js.executeScript(jsCode);
        js.executeScript(mouseHighlight);
    }


    public static String getXPathJs() {

        String script =
                "var el = arguments[0];" +
                        "var txt = arguments[1];" +
                        "var result = [];" +

                        // Add xpath only if unique
                        "function addXpath(xp) {" +
                        "   if(!xp || xp.trim() === '') return;" +
                        "   try {" +
                        "       var nodes = document.evaluate(" +
                        "           xp," +
                        "           document," +
                        "           null," +
                        "           XPathResult.ORDERED_NODE_SNAPSHOT_TYPE," +
                        "           null" +
                        "       );" +
                        "       if(nodes.snapshotLength === 1) {" +
                        "           var found = nodes.snapshotItem(0);" +
                        "           if(found === el && result.indexOf(xp) === -1) {" +
                        "               result.push(xp);" +
                        "           }" +
                        "       }" +
                        "   } catch(e) {}" +
                        "}" +

                        // Get actual index inside xpath result
                        "function getXpathIndex(baseXpath, tag, element) {" +
                        "   try {" +
                        "       var searchXpath = baseXpath + '//' + tag;" +
                        "       var nodes = document.evaluate(" +
                        "           searchXpath," +
                        "           document," +
                        "           null," +
                        "           XPathResult.ORDERED_NODE_SNAPSHOT_TYPE," +
                        "           null" +
                        "       );" +
                        "       for(var i = 0; ; i++) {" +
                        "           if(nodes.snapshotItem(i) === element) {" +
                        "               return i + 1;" +
                        "           }" +
                        "       }" +
                        "   } catch(e) {}" +
                        "   return 1;" +
                        "}" +

                        "var tag = el.tagName.toLowerCase();" +

                        // 1. ELEMENT ID
                        "if(el.id && el.id.trim() !== '') {" +
                        "   addXpath('//*[@id=\\''+ el.id + '\\']');" +
                        "}" +

                        // 2. ATTRIBUTE BASED XPATHS
                        "function addAttrXpath(attr) {" +
                        "   var val = el.getAttribute(attr);" +
                        "   if(val && val.trim() !== '') {" +
                        "       addXpath('//' + tag + '[@' + attr + '=\\''+ val + '\\']');" +
                        "       addXpath('//' + tag + '[contains(@' + attr + ',\\'' + val + '\\')]');" +

                        "       if(attr === 'class') {" +
                        "           var firstClass = val.split(' ')[0];" +
                        "           if(firstClass.trim() !== '') {" +
                        "               addXpath('//' + tag + '[contains(@class,\\'' + firstClass + '\\')]');" +
                        "           }" +
                        "       }" +
                        "   }" +
                        "}" +

                        "addAttrXpath('placeholder');" +
                        "addAttrXpath('name');" +
                        "addAttrXpath('role');" +
                        "addAttrXpath('class');" +
                        "addAttrXpath('title');" +
                        "addAttrXpath('type');" +
                        "addAttrXpath('value');" +
                        "addAttrXpath('for');" +

                        // 3. PARENT IDS
                        "var current = el.parentElement;" +
                        "while(current) {" +
                        "   if(current.id && current.id.trim() !== '') {" +
                        "       var base = '//*[@id=\\''+ current.id + '\\']';" +

                        "       if(txt && txt.trim() !== '') {" +
                        "           addXpath(base + '//' + tag + '[contains(text(),\\''+ txt + '\\')]');" +
                        "           addXpath(base + '//*[contains(text(),\\''+ txt + '\\')]');" +
                        "       } else {" +
                        "           var idx = getXpathIndex(base, tag, el);" +
                        "           addXpath('(' + base + '//' + tag + ')[' + idx + ']');" +
                        "       }" +
                        "   }" +
                        "   current = current.parentElement;" +
                        "}" +

                        // 4. TEXT BASED
                        "if(txt && txt.trim() !== '') {" +
                        "   addXpath('//' + tag + '[contains(text(),\\''+ txt + '\\')]');" +
                        "}" +

                        // 5. ABSOLUTE INDEX
                        "var all = document.getElementsByTagName(tag);" +
                        "for(var j = 0; j < all.length; j++) {" +
                        "   if(all[j] === el) {" +
                        "       addXpath('(//' + tag + ')[' + (j + 1) + ']');" +
                        "       break;" +
                        "   }" +
                        "}" +

                        "return result;";

        return script;
    }

    private static WebElement getLastClickedElement(WebDriver driver) {

        try {

            boolean isLastClickedElementAvailable = (boolean) ((JavascriptExecutor) driver)
                    .executeScript(
                            "return typeof window._lastClickedElement !== 'undefined' && "
                                    + "window._lastClickedElement !== null;"
                    );

            if (isLastClickedElementAvailable) {

                return (WebElement) ((JavascriptExecutor) driver)
                        .executeScript("return window._lastClickedElement;");

            } else {

                injectScript(driver);
            }

        } catch (Exception e) {

            return lastClickElement;
        }

        return lastClickElement;
    }

    public static String beforeFirstSpecial(String input) {

        if (input == null || input.isEmpty()) {
            return input;
        }

        // Match alphanumeric characters from the start of the string
        java.util.regex.Matcher matcher = java.util.regex.Pattern
                .compile("^[A-Za-z0-9]*")
                .matcher(input);

        if (matcher.find()) {
            return matcher.group();
        }

        // Should never happen because the regex always matches at least ""
        return "";
    }
}
