package com.coach.service;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import io.github.cdimascio.dotenv.Dotenv;
import com.coach.repository.TaskRepository;
import com.coach.model.Task;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.ArrayList;
import java.util.List;

public class AiService {
    private static final String API_URL = "https://generativelanguage.googleapis.com/v1beta/models/gemini-3-flash-preview:generateContent?key=";
    private final String apiKey;
    private final HttpClient httpClient;
    private final TaskRepository taskRepository;
    private final com.coach.repository.UserRepository userRepository;
    private final List<JsonObject> chatHistory;
    private final JsonArray tools;

    public AiService() {
        Dotenv dotenv = Dotenv.configure().ignoreIfMissing().load();
        String key = dotenv.get("GEMINI_API_KEY");
        this.apiKey = (key != null && !key.isEmpty()) ? key : System.getenv("GEMINI_API_KEY");
        this.httpClient = HttpClient.newHttpClient();
        this.taskRepository = new TaskRepository();
        this.userRepository = new com.coach.repository.UserRepository();
        this.chatHistory = new ArrayList<>();
        this.tools = defineTools();
    }

    private JsonArray defineTools() {
        String toolsJson = """
        [{
          "functionDeclarations": [
            {
              "name": "getTasks",
              "description": "Fetch all tasks for the current user. Returns a list of tasks with IDs, titles, status, and deadlines."
            },
            {
              "name": "createTask",
              "description": "Create a new task.",
              "parameters": {
                "type": "OBJECT",
                "properties": {
                  "title": {"type": "STRING"},
                  "description": {"type": "STRING"},
                  "category": {"type": "STRING"},
                  "priority": {"type": "INTEGER", "description": "1=High, 2=Medium, 3=Low"}
                },
                "required": ["title"]
              }
            },
            {
              "name": "updateTask",
              "description": "Update an existing task status, title, etc.",
              "parameters": {
                "type": "OBJECT",
                "properties": {
                  "id": {"type": "INTEGER"},
                  "title": {"type": "STRING"},
                  "description": {"type": "STRING"},
                  "category": {"type": "STRING"},
                  "priority": {"type": "INTEGER"},
                  "status": {"type": "STRING", "description": "TODO, IN_PROGRESS, DONE"}
                },
                "required": ["id"]
              }
            },
            {
              "name": "deleteTask",
              "description": "Delete a task by its ID.",
              "parameters": {
                "type": "OBJECT",
                "properties": {
                  "id": {"type": "INTEGER"}
                },
                "required": ["id"]
              }
            },
            {
              "name": "bulkCreateTasks",
              "description": "Create multiple tasks at once.",
              "parameters": {
                "type": "OBJECT",
                "properties": {
                  "tasks": {
                    "type": "ARRAY",
                    "items": {
                      "type": "OBJECT",
                      "properties": {
                        "title": {"type": "STRING"},
                        "description": {"type": "STRING"},
                        "category": {"type": "STRING"},
                        "priority": {"type": "INTEGER"}
                      },
                      "required": ["title"]
                    }
                  }
                },
                "required": ["tasks"]
              }
            }
          ]
        }]
        """;
        return JsonParser.parseString(toolsJson).getAsJsonArray();
    }

    public String getAdvice(int userId, String prompt) {
        if (apiKey == null || apiKey.isEmpty()) {
            return "AI feature disabled: No Gemini API Key provided. Set GEMINI_API_KEY environment variable.";
        }

        JsonObject userContent = new JsonObject();
        userContent.addProperty("role", "user");
        JsonArray userParts = new JsonArray();
        JsonObject userPart = new JsonObject();
        userPart.addProperty("text", prompt);
        userParts.add(userPart);
        userContent.add("parts", userParts);
        chatHistory.add(userContent);

        int maxLoops = 5;
        try {
            while (maxLoops-- > 0) {
                JsonObject requestBody = new JsonObject();
                
                JsonObject sysInst = new JsonObject();
                JsonObject sysPart = new JsonObject();
                
                StringBuilder sysText = new StringBuilder("You are a helpful productivity coach. You can manage tasks for the user using the provided tools. Always summarize what was done briefly and warmly. If asked to do something, do it and confirm.");
                
                // Fetch user context for personalization
                com.coach.model.User user = userRepository.findById(userId);
                if (user != null) {
                    sysText.append("\nUser Context:");
                    if (user.getGoals() != null && !user.getGoals().isEmpty()) sysText.append("\n- Goals: ").append(user.getGoals());
                    if (user.getWorkRhythm() != null && !user.getWorkRhythm().isEmpty()) sysText.append("\n- Work Rhythm: ").append(user.getWorkRhythm());
                    if (user.getPreferences() != null && !user.getPreferences().isEmpty()) sysText.append("\n- Preferences: ").append(user.getPreferences());
                }
                
                sysPart.addProperty("text", sysText.toString());
                JsonArray sysParts = new JsonArray();
                sysParts.add(sysPart);
                sysInst.add("parts", sysParts);
                requestBody.add("systemInstruction", sysInst);

                JsonArray contents = new JsonArray();
                for (JsonObject msg : chatHistory) {
                    contents.add(msg);
                }
                requestBody.add("contents", contents);
                requestBody.add("tools", this.tools);

                HttpRequest request = HttpRequest.newBuilder()
                        .uri(new URI(API_URL + apiKey))
                        .header("Content-Type", "application/json")
                        .POST(HttpRequest.BodyPublishers.ofString(requestBody.toString()))
                        .build();

                HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());

                if (response.statusCode() != 200) {
                    return "Error from AI Service: " + response.statusCode() + " - " + response.body();
                }

                JsonObject jsonResponse = JsonParser.parseString(response.body()).getAsJsonObject();
                JsonArray candidates = jsonResponse.getAsJsonArray("candidates");
                if (candidates == null || candidates.isEmpty()) {
                    return "No response from AI.";
                }

                JsonObject content = candidates.get(0).getAsJsonObject().getAsJsonObject("content");
                JsonArray parts = content.getAsJsonArray("parts");
                
                // Track AI response
                chatHistory.add(content);

                JsonObject firstPart = parts.get(0).getAsJsonObject();

                if (firstPart.has("functionCall")) {
                    JsonObject functionCall = firstPart.getAsJsonObject("functionCall");
                    String functionName = functionCall.get("name").getAsString();
                    JsonObject args = functionCall.has("args") ? functionCall.getAsJsonObject("args") : new JsonObject();
                    
                    // The functionResponse role should be 'function' in v1beta
                    JsonObject functionResponsePart = handleFunctionCall(userId, functionName, args);

                    JsonObject functionRespContent = new JsonObject();
                    functionRespContent.addProperty("role", "function");
                    JsonArray frParts = new JsonArray();
                    frParts.add(functionResponsePart);
                    functionRespContent.add("parts", frParts);
                    chatHistory.add(functionRespContent);
                    
                } else if (firstPart.has("text")) {
                    return firstPart.get("text").getAsString();
                } else {
                    return "Unhandled response format.";
                }
            }
            return "Too many function calls. Stopping.";
        } catch (Exception e) {
            e.printStackTrace();
            return "Failed to communicate with AI Service.";
        }
    }

    private JsonObject handleFunctionCall(int userId, String name, JsonObject args) {
        JsonObject result = new JsonObject();
        try {
            switch (name) {
                case "getTasks":
                    List<Task> tasks = taskRepository.findByUserId(userId);
                    JsonArray tasksArray = new JsonArray();
                    for(Task t : tasks) {
                        JsonObject obj = new JsonObject();
                        obj.addProperty("id", t.getId());
                        obj.addProperty("title", t.getTitle());
                        obj.addProperty("description", t.getDescription());
                        obj.addProperty("status", t.getStatus());
                        obj.addProperty("priority", t.getPriority());
                        obj.addProperty("deadline", t.getDeadline() != null ? t.getDeadline().toString() : null);
                        tasksArray.add(obj);
                    }
                    result.add("tasks", tasksArray);
                    break;
                case "createTask":
                    Task newTask = new Task();
                    newTask.setUserId(userId);
                    newTask.setTitle(args.has("title") ? args.get("title").getAsString() : "New Task");
                    newTask.setDescription(args.has("description") ? args.get("description").getAsString() : "");
                    newTask.setCategory(args.has("category") ? args.get("category").getAsString() : "General");
                    newTask.setPriority(args.has("priority") ? args.get("priority").getAsInt() : 2);
                    newTask.setStatus("TODO");
                    Task saved = taskRepository.save(newTask);
                    if (saved != null) {
                        result.addProperty("status", "success");
                        result.addProperty("message", "Task created with ID " + saved.getId());
                    } else {
                        result.addProperty("status", "error");
                    }
                    break;
                case "updateTask":
                    int updateId = args.get("id").getAsInt();
                    List<Task> existing = taskRepository.findByUserId(userId);
                    Task taskToUpdate = existing.stream().filter(t -> t.getId() == updateId).findFirst().orElse(null);
                    if (taskToUpdate != null) {
                        if (args.has("title")) taskToUpdate.setTitle(args.get("title").getAsString());
                        if (args.has("description")) taskToUpdate.setDescription(args.get("description").getAsString());
                        if (args.has("category")) taskToUpdate.setCategory(args.get("category").getAsString());
                        if (args.has("priority")) taskToUpdate.setPriority(args.get("priority").getAsInt());
                        if (args.has("status")) taskToUpdate.setStatus(args.get("status").getAsString());
                        boolean updated = taskRepository.update(taskToUpdate);
                        result.addProperty("status", updated ? "success" : "error");
                    } else {
                        result.addProperty("status", "error");
                        result.addProperty("message", "Task not found");
                    }
                    break;
                case "deleteTask":
                    int delId = args.get("id").getAsInt();
                    boolean deleted = taskRepository.delete(delId, userId);
                    result.addProperty("status", deleted ? "success" : "error");
                    break;
                case "bulkCreateTasks":
                    JsonArray tasksToCreate = args.getAsJsonArray("tasks");
                    int count = 0;
                    for (int i = 0; i < tasksToCreate.size(); i++) {
                        JsonObject tObj = tasksToCreate.get(i).getAsJsonObject();
                        Task bt = new Task();
                        bt.setUserId(userId);
                        bt.setTitle(tObj.has("title") ? tObj.get("title").getAsString() : "Bulk Task");
                        bt.setDescription(tObj.has("description") ? tObj.get("description").getAsString() : "");
                        bt.setCategory(tObj.has("category") ? tObj.get("category").getAsString() : "General");
                        bt.setPriority(tObj.has("priority") ? tObj.get("priority").getAsInt() : 2);
                        bt.setStatus("TODO");
                        if (taskRepository.save(bt) != null) count++;
                    }
                    result.addProperty("status", "success");
                    result.addProperty("message", "Created " + count + " tasks successfully.");
                    break;
                default:
                    result.addProperty("status", "error");
                    result.addProperty("message", "Unknown function " + name);
            }
        } catch (Exception e) {
            result.addProperty("error", e.getMessage());
        }

        JsonObject responseObj = new JsonObject();
        JsonObject functionResponse = new JsonObject();
        functionResponse.addProperty("name", name);
        functionResponse.add("response", result);
        responseObj.add("functionResponse", functionResponse);
        return responseObj;
    }

    public String reformulateObjective(int userId, String objective) {
        String prompt = "You are a productivity coach. Reformulate this objective to be SMART (Specific, Measurable, Achievable, Relevant, Time-bound) and exciting: "
                + objective;
        return getAdvice(userId, prompt);
    }

    public String breakdownTask(int userId, String taskTitle, String taskDescription) {
        String prompt = "You are a productivity coach. Break down this task into 3 to 5 small, actionable steps:\n" +
                "Task: " + taskTitle + "\nDescription: " + taskDescription;
        return getAdvice(userId, prompt);
    }
}
