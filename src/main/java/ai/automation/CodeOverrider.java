package ai.automation;

import ai.automation.utils.Helper;
import com.github.javaparser.StaticJavaParser;
import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.ImportDeclaration;
import com.github.javaparser.ast.body.*;
import com.github.javaparser.ast.expr.SimpleName;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import java.io.*;
import java.nio.channels.FileChannel;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.StandardOpenOption;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class CodeOverrider {
    public CodeOverrider(){

    }

    public static void overrideCode(String response) {

        try {

            String jsonResoponse = response;

            JsonObject jsonObject = JsonParser.parseString(jsonResoponse).getAsJsonObject();

            JsonArray pomArray = jsonObject.get("framework").getAsJsonObject().get("components").getAsJsonObject().get("POM").getAsJsonArray();

            JsonArray stepDefinitionArray = jsonObject.get("framework").getAsJsonObject().get("components").getAsJsonObject().get("StepDefinition").getAsJsonArray();

            JsonArray featureFile = jsonObject.get("framework").getAsJsonObject().get("components").getAsJsonObject().get("features").getAsJsonArray();

            constructPomClass(pomArray, "/Users/bhuvanes/Project/Mobile-testing_May31/src/test/java/psm_PageObjects/Salesforce_HomePageObjects/",true);
            constructPomClass(stepDefinitionArray, "/Users/bhuvanes/Project/Mobile-testing_May31/src/test/java/starter/stepdefinitions/Salesforce_StepDefinition/",false);
            constructFeatureFile(featureFile, "/Users/bhuvanes/Project/Mobile-testing_May31/src/test/resources/features/salesFre/");

        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private static void constructFeatureFile(JsonArray featuresArray, String featuresPath) {

        try {

            for (JsonElement pom : featuresArray) {

                JsonObject featureObject = pom.getAsJsonObject();

                String fileName = featureObject.get("file").getAsString();
                String scenarioName = featureObject.get("scenarioName").getAsString();
                String featureName = featureObject.get("featureName").getAsString();

                JsonArray steps = featureObject.get("featureSteps").getAsJsonArray();

                Boolean isFeatureFilePresent = Helper.isFeatureFilePresent(fileName,featuresPath);

                featuresPath = featuresPath + fileName;

                if (isFeatureFilePresent) {

                    File file = new File(featuresPath);

                    String fileContents = Files.readString(file.toPath(), StandardCharsets.UTF_8);

                    StringBuilder featureFileMsg = new StringBuilder();

                    featureFileMsg.append(fileContents).append("\n");
                    featureFileMsg.append("Scenario: ").append(scenarioName).append("\n");

                    for (JsonElement step : steps) {

                        String scenarioStep = step.getAsString();
                        featureFileMsg.append(scenarioStep).append("\n");
                    }

                    File filePath = new File(featuresPath);

                    Files.writeString(filePath.toPath(), featureFileMsg.toString(), StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING);

                    try (FileChannel channel = FileChannel.open(filePath.toPath(), StandardOpenOption.WRITE)) {

                        channel.force(true);
                    }

                } else {

                    StringBuilder featureFileMsg = new StringBuilder();

                    featureFileMsg.append("Feature: ").append(featureName).append("\n");

                    featureFileMsg.append("Scenario: ").append(scenarioName).append("\n");

                    for (JsonElement step : steps) {

                        String scenarioStep = step.getAsString();
                        featureFileMsg.append(scenarioStep).append("\n");
                    }

                    Helper.createFile(featuresPath, featureFileMsg.toString());
                }
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private static void constructPomClass(JsonArray pomArray, String packagePath, boolean isPOM) {

        try {

            for (JsonElement pom : pomArray) {

                JsonObject pomObject = pom.getAsJsonObject();

                String fileName = pomObject.get("file").getAsString();

                Boolean isPomFilePresent = Helper.isJavaFilePresent(fileName,packagePath);

                String pomPath = packagePath + fileName;

                if (isPomFilePresent) {

                    File filePath = new File(pomPath);

                    CompilationUnit cu = StaticJavaParser.parse(filePath);

                    updateImports(cu, pomObject);
                    updateVariables(cu, pomObject);
                    updateMethods(cu, pomObject);

                    Files.writeString(filePath.toPath(), cu.toString(), StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING);

                    try (FileChannel channel = FileChannel.open(filePath.toPath(), StandardOpenOption.WRITE)) {

                        channel.force(true);
                    }

                } else {

                    String pomJavaCode = constructJavaCode(pomObject,isPOM);

                    Helper.createFile(pomPath, pomJavaCode);
                }
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private static void updateMethods(CompilationUnit cu, JsonObject pomObject) {

        try {

            List<String> currentMethodNames = cu.findAll(MethodDeclaration.class).stream().map(MethodDeclaration::getNameAsString).collect(Collectors.toList());

            List<String> ctorNames = cu.findAll(ConstructorDeclaration.class).stream().map(ConstructorDeclaration::getNameAsString).collect(Collectors.toList());

            currentMethodNames = Stream.concat(currentMethodNames.stream(), ctorNames.stream()).collect(Collectors.toList());

            String className = pomObject.get("file").getAsString().replace(".java", "");

            JsonArray methods = pomObject.get("methods").getAsJsonArray();

            for (JsonElement method : methods) {

                JsonObject methodDetails = method.getAsJsonObject();

                String methodName = methodDetails.get("name").getAsString();

                String methodCode = methodDetails.get("code").getAsString();

                boolean isMethodPresent = false;

                isMethodPresent = methodExists(cu,className,methodCode);

                if (!isMethodPresent) {

                    System.out.println(methodCode);

                    BodyDeclaration<?> member = StaticJavaParser.parseBodyDeclaration(methodCode.trim());
                    cu.getClassByName(className).get().getMembers().add(member);
                }
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    private static boolean methodExists(CompilationUnit cu,
                                        String className,
                                        String methodCode) {

        BodyDeclaration<?> body =
                StaticJavaParser.parseBodyDeclaration(methodCode);

        Optional<ClassOrInterfaceDeclaration> clazz =
                cu.getClassByName(className);

        if (clazz.isEmpty()) {
            return false;
        }

        // Handle Methods
        if (body instanceof MethodDeclaration newMethod) {

            for (MethodDeclaration existingMethod : clazz.get().getMethods()) {

                if (!existingMethod.getNameAsString()
                        .equals(newMethod.getNameAsString())) {
                    continue;
                }

                if (existingMethod.getParameters().size()
                        != newMethod.getParameters().size()) {
                    continue;
                }

                boolean sameParameters = true;

                for (int i = 0; i < existingMethod.getParameters().size(); i++) {

                    String existingType =
                            existingMethod.getParameter(i).getType().asString();

                    String newType =
                            newMethod.getParameter(i).getType().asString();

                    if (!existingType.equals(newType)) {
                        sameParameters = false;
                        break;
                    }
                }

                if (sameParameters) {
                    return true;
                }
            }

            return false;
        }

        // Handle Constructors
        if (body instanceof ConstructorDeclaration newConstructor) {

            for (ConstructorDeclaration existingConstructor :
                    clazz.get().getConstructors()) {

                if (existingConstructor.getParameters().size()
                        != newConstructor.getParameters().size()) {
                    continue;
                }

                boolean sameParameters = true;

                for (int i = 0; i < existingConstructor.getParameters().size(); i++) {

                    String existingType =
                            existingConstructor.getParameter(i).getType().asString();

                    String newType =
                            newConstructor.getParameter(i).getType().asString();

                    if (!existingType.equals(newType)) {
                        sameParameters = false;
                        break;
                    }
                }

                if (sameParameters) {
                    return true;
                }
            }

            return false;
        }

        return false;
    }
    private static void updateVariables(CompilationUnit cu, JsonObject pomObject) {

        try {

            List<String> allVariables = cu.findAll(FieldDeclaration.class).stream().flatMap(fd -> fd.getVariables().stream()).map(VariableDeclarator::getName).map(SimpleName::asString).collect(Collectors.toList());

            String className = pomObject.get("file").getAsString().replace(".java", "");

            JsonArray variables = pomObject.get("variables").getAsJsonArray();

            for (JsonElement variableStatement : variables) {

                String variableState = variableStatement.getAsString() + "\n";

                boolean isVariablePresent = false;

                for (String currentVariable : allVariables) {

                    if (variableState.trim().contains(currentVariable.trim())) {

                        isVariablePresent = true;
                    }
                }

                if (!isVariablePresent) {

                    BodyDeclaration<?> member = StaticJavaParser.parseBodyDeclaration(variableStatement.getAsString());

                    cu.getClassByName(className).get().getMembers().addFirst(member);
                }
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private static void updateImports(CompilationUnit cu, JsonObject pomObject) {

        try {

            List<String> currentImports = cu.getImports().stream().map(ImportDeclaration::toString).collect(Collectors.toList());

            JsonArray imports = pomObject.get("imports").getAsJsonArray();

            for (JsonElement importStatement : imports) {

                String importState = importStatement.getAsString();

                boolean isImportPresent = false;

                for (String currentImport : currentImports) {

                    if (currentImport.contains(importState)) {
                        isImportPresent = true;
                    }
                }

                if (!isImportPresent) {
                    cu.addImport(importState);
                }
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private static String constructJavaCode(JsonObject pomObject, boolean isPOM) {

        String javaCode = "";

        try {

            javaCode += "package " + pomObject.get("package").getAsString() + ";\n";

            JsonArray imports = pomObject.get("imports").getAsJsonArray();

            for (JsonElement importStatement : imports) {

                String importState = importStatement.getAsString() ;
                if(!importState.toLowerCase().contains("import")){
                    importState = "import "+importState;
                }
                if(!importState.toLowerCase().contains(";")){
                    importState =  importState +";";
                }


                javaCode +=   importState+"\n";
            }
            if(isPOM) {
                javaCode += "\npublic class " + pomObject.get("file").getAsString().replace(".java", "") + "  extends PageObject{\n";
            }else{
                javaCode += "\npublic class " + pomObject.get("file").getAsString().replace(".java", "") + "  {\n";
            }

            JsonArray variables = pomObject.get("variables").getAsJsonArray();

            for (JsonElement variableStatement : variables) {

                String variableState = variableStatement.getAsString() + "\n";

                javaCode += variableState;
            }

            JsonArray methods = pomObject.get("methods").getAsJsonArray();

            for (JsonElement method : methods) {

                JsonObject methodDetails = method.getAsJsonObject();

                String methodCode = methodDetails.get("code").getAsString() + "\n";

                javaCode += methodCode;
            }

            javaCode += "\n}";

        } catch (Exception e) {
            e.printStackTrace();
        }

        return javaCode;
    }

    public static void main(String[] args) {
        overrideCode(readCode());
    }

    private static String readCode() {

        String filePath = "code.txt";
        String line;

        InputStream is = null;
        BufferedReader br = null;

        String message = "";

        try {

            is = CodeOverrider.class.getClassLoader().getResourceAsStream(filePath);

            if (is == null) {

                System.err.println("File not found in resources: " + filePath);

                return "";
            }

            br = new BufferedReader(new InputStreamReader(is, StandardCharsets.UTF_8));

            while ((line = br.readLine()) != null) {
                message += line;
            }

        } catch (IOException e) {

            System.err.println("Error reading file: " + e.getMessage());

        } finally {

            try {

                if (br != null) {
                    br.close();
                }

                if (is != null) {
                    is.close();
                }

            } catch (IOException e) {

                System.err.println("Error closing resources: " + e.getMessage());
            }
        }

        return message;
    }
}