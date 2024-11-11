package dev.rafarg;

import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import java.io.FileInputStream;
import java.io.IOException;
import java.util.*;

public class DecisionTree {
    private Node root;
    private List<String> attributes;

    public DecisionTree() {
        this.root = new Node();
        this.attributes = new ArrayList<>();
    }

    private double calculateEntropy(List<ObjectData> data) {
        if (data.isEmpty()) return 0;
        
        Map<String, Integer> classCounts = new HashMap<>();
        for (ObjectData obj : data) {
            classCounts.merge(obj.classification, 1, Integer::sum);
        }

        double entropy = 0;
        int totalSize = data.size();
        for (int count : classCounts.values()) {
            double probability = (double) count / totalSize;
            entropy -= probability * Math.log(probability) / Math.log(2);
        }
        return entropy;
    }

    private double calculateInformationGain(List<ObjectData> data, String attribute) {
        double entropyParent = calculateEntropy(data);
        Map<String, List<ObjectData>> splitData = new HashMap<>();

        for (ObjectData obj : data) {
            String value = obj.attributes.get(attribute);
            splitData.computeIfAbsent(value, k -> new ArrayList<>()).add(obj);
        }

        double entropyChildren = 0;
        int totalSize = data.size();
        for (List<ObjectData> subset : splitData.values()) {
            double probability = (double) subset.size() / totalSize;
            entropyChildren += probability * calculateEntropy(subset);
        }

        return entropyParent - entropyChildren;
    }

    private String findBestAttribute(List<ObjectData> data, List<String> availableAttributes) {
        double maxGain = -1;
        String bestAttribute = null;

        for (String attribute : availableAttributes) {
            double gain = calculateInformationGain(data, attribute);
            if (gain > maxGain) {
                maxGain = gain;
                bestAttribute = attribute;
            }
        }

        return bestAttribute;
    }

    private Node buildTree(List<ObjectData> data, List<String> availableAttributes) {
        Node node = new Node();

        boolean sameClass = true;
        String firstClass = data.get(0).classification;
        for (ObjectData obj : data) {
            if (!obj.classification.equals(firstClass)) {
                sameClass = false;
                break;
            }
        }

        if (sameClass || availableAttributes.isEmpty()) {
            node.isLeaf = true;
            node.prediction = getMajorityClass(data);
            return node;
        }

        String bestAttribute = findBestAttribute(data, availableAttributes);
        node.attribute = bestAttribute;

        Map<String, List<ObjectData>> splitData = new HashMap<>();
        for (ObjectData obj : data) {
            String value = obj.attributes.get(bestAttribute);
            splitData.computeIfAbsent(value, k -> new ArrayList<>()).add(obj);
        }

        List<String> remainingAttributes = new ArrayList<>(availableAttributes);
        remainingAttributes.remove(bestAttribute);

        for (Map.Entry<String, List<ObjectData>> entry : splitData.entrySet()) {
            Node child = buildTree(entry.getValue(), remainingAttributes);
            node.children.put(entry.getKey(), child);
        }
        return node;
    }

    private String getMajorityClass(List<ObjectData> data) {
        Map<String, Integer> classCounts = new HashMap<>();
        for (ObjectData obj : data) {
            classCounts.merge(obj.classification, 1, Integer::sum);
        }

        return Collections.max(classCounts.entrySet(), Map.Entry.comparingByValue()).getKey();
    }

    public String classify(Map<String, String> attributes) {
        return classifyRecursive(root, attributes);
    }

    private String classifyRecursive(Node node, Map<String, String> attributes) {
        if (node.isLeaf) return node.prediction;

        String value = attributes.get(node.attribute);
        Node child = node.children.get(value);
        
        if (child == null) return node.prediction;

        return classifyRecursive(child, attributes);
    }

    public void trainWithExcelData(String filePath) throws IOException {
        List<ObjectData> data = readExcelData(filePath);
        attributes = new ArrayList<>(data.get(0).attributes.keySet());
        root = buildTree(data, attributes);
    }

    private List<ObjectData> readExcelData(String filePath) throws IOException {
        List<ObjectData> data = new ArrayList<>();
        
        try (FileInputStream fis = new FileInputStream(filePath);
             Workbook workbook = new XSSFWorkbook(fis)) {
            
            Sheet sheet = workbook.getSheetAt(0);
            Row headerRow = sheet.getRow(0);

            List<String> headers = new ArrayList<>();
            for (Cell cell : headerRow) {
                headers.add(cell.getStringCellValue());
            }

            for (Row row : sheet) {
                if (row.getRowNum() == 0) continue;
                
                Map<String, String> attributes = new HashMap<>();
                String classification = null;
                
                for (int i = 0; i < headers.size(); i++) {
                    String value = row.getCell(i).getStringCellValue();
                    if (i < headers.size() - 1) {
                        attributes.put(headers.get(i), value);
                    } else {
                        classification = value;
                    }
                }
                
                data.add(new ObjectData(attributes, classification));
            }
        }
        
        return data;
    }
}