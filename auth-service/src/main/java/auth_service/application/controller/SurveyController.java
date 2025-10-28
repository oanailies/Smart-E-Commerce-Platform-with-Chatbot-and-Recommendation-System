package auth_service.application.controller;

import auth_service.application.dto.SurveyData;
import auth_service.application.service.SurveyService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;

@RestController
@RequestMapping("/survey")
public class SurveyController {

    @Autowired
    private SurveyService surveyService;

    @PostMapping("/submit")
    public ResponseEntity<String> submitSurvey(@RequestBody SurveyData surveyData) {
        surveyService.saveSurveyData(surveyData);
        return ResponseEntity.ok("Survey data saved successfully.");
    }

    @GetMapping("/json")
    public ResponseEntity<List<Map<String, Object>>> getSurveyDataAsJson() {
        try {
            Path csvPath = Paths.get("src/main/resources/financial_data.csv");
            if (!Files.exists(csvPath)) {
                return ResponseEntity.status(404).build();
            }

            List<String> lines = Files.readAllLines(csvPath, StandardCharsets.UTF_8);
            if (lines.isEmpty()) {
                return ResponseEntity.ok(Collections.emptyList());
            }

            String[] rawHeaders = lines.get(0).split(",");
            List<String> headers = Arrays.stream(rawHeaders)
                    .map(h -> h.trim().replace("\"", ""))
                    .toList();

            List<Map<String, Object>> jsonData = new ArrayList<>();

            for (int i = 1; i < lines.size(); i++) {
                String line = lines.get(i).trim();
                if (line.isEmpty()) continue;

                String[] values = line.split(",", -1);
                Map<String, Object> row = new LinkedHashMap<>();

                for (int j = 0; j < headers.size(); j++) {
                    String value = j < values.length ? values[j].replace("\"", "").trim() : "";
                    if (value.equalsIgnoreCase("null") || value.equals("-") || value.isEmpty()) {
                        value = "";
                    }

                    if (headers.get(j).equals("PreferredProductTypes") ||
                            headers.get(j).equals("FavoriteBrands") ||
                            headers.get(j).equals("FashionStyle")) {
                        row.put(headers.get(j), value.isEmpty() ? new ArrayList<>() : Arrays.asList(value.split(";")));
                    } else {
                        row.put(headers.get(j), value);
                    }
                }

                jsonData.add(row);
            }

            return ResponseEntity.ok(jsonData);

        } catch (IOException e) {
            return ResponseEntity.internalServerError().build();
        }
    }
}
