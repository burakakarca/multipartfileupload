package com.platonics.demo.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.platonics.demo.model.ErrorDTO;
import com.platonics.demo.model.ResponseValidateFile;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.*;

@Service
public class FileServiceImpl implements FileService {
    private final static String COMMA = ",";
    private final static String DOUBLE_QUOTES = "\"";
    private final static Integer ERROR_CODE_REQUIRED_FILE = 1;
    private final static Integer ERROR_CODE_NOT_ALLOWED_VALUE = 2;
    @Override
    public ResponseValidateFile validateFile(MultipartFile file) throws Exception {
        ResponseValidateFile responseValidateFile = new ResponseValidateFile();
        List<Map<String, String>> lineList = convertCsvToList(file);
        List<ErrorDTO> errorList = validateInput(lineList);
        responseValidateFile.setErrorList(errorList);
        return responseValidateFile;
    }

    private List<Map<String, String>> convertCsvToList(MultipartFile file) {
        List<Map<String, String>> dataList = new ArrayList<>();
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(file.getInputStream()))) {
            String[] labels = reader.readLine().split(COMMA);
            String line;
            while ((line = reader.readLine()) != null) {
                if (line.startsWith(DOUBLE_QUOTES)) {
                    Map<String, String> row = new HashMap<>();
                    String[] values = line.split(DOUBLE_QUOTES);
                    row.put(labels[0], values[1]);
                    String restOfTheLine = values[2];
                    addLineToList(row, dataList, labels, restOfTheLine, 1);
                } else {
                    Map<String, String> row = new HashMap<>();
                    addLineToList(row, dataList, labels, line, 0);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return dataList;
    }

    private void addLineToList(Map<String, String> row, List<Map<String, String>> csvData, String[] labels, String line, int beginningValue) {
        String[] values = line.split(COMMA);
        for (int i = beginningValue; i < labels.length; i++) {
            row.put(labels[i], values[i]);
        }
        csvData.add(row);
    }

    private List<ErrorDTO> validateInput(List<Map<String, String>> lineList) throws IOException {
        ObjectMapper objectMapper = new ObjectMapper();
        JsonNode surveyJsonNode = objectMapper.readTree(FileServiceImpl.class.getResource("/survey.json"));
        List<ErrorDTO> errorList = new ArrayList<>();
        int lineNumber = 0;
        for (Map<String, String> line : lineList) {
            lineNumber++;
            JsonNode elements = surveyJsonNode.get("pages").get(0).get("elements");
            validateLine(elements, line, lineNumber, errorList);
        }
        return errorList;
    }

    private void validateLine(JsonNode elements, Map<String, String> line, int lineNumber, List<ErrorDTO> errorList) {
        for (JsonNode element : elements) {
            boolean containsVisibleIf = element.has("visibleIf");
            if (containsVisibleIf && !isVisible(element, line)) {
                continue;
            }
            boolean required = element.get("isRequired").asBoolean();
            String name = element.get("name").asText();
            if (required && (line.get(name) == null || line.get(name).isEmpty())) {
                addToErrorList(errorList, ERROR_CODE_REQUIRED_FILE, "Error on " + lineNumber + ".Line : " + name + " is required!");
            }
            if (element.has("choices")) {
                List<String> choices = getChoices(element);
                if (line.get(name) != null && !line.get(name).isEmpty() && !choices.contains(line.get(name))) {
                    addToErrorList(errorList, ERROR_CODE_NOT_ALLOWED_VALUE, "Error on " + lineNumber + ".Line : Not allowed value for " + name + ". Allowed values : " + choices);
                }
            }
        }
    }

    private void addToErrorList(List<ErrorDTO> errorList, Integer errorCode, String errorMessage) {
        ErrorDTO errorDTO = new ErrorDTO();
        errorDTO.setErrorCode(errorCode);
        errorDTO.setErrorMessage(errorMessage);
        errorList.add(errorDTO);
    }

    private boolean isVisible(JsonNode element, Map<String, String> row) {
        String visibleIf = element.get("visibleIf").asText().trim().replace("'", "").replace("{", "").replace("}", "");
        String[] visibleConditions = visibleIf.split("=");
        return row.get(visibleConditions[0].trim()) != null && row.get(visibleConditions[0].trim()).equals(visibleConditions[1].trim());
    }

    private List<String> getChoices(JsonNode element) {
        List<String> choices = new ArrayList<>();
        for (JsonNode choice : element.get("choices")) {
            if (choice.has("value")) {
                choices.add(choice.get("value").asText());
            } else {
                choices.add(choice.asText());
            }
        }
        return choices;
    }
}