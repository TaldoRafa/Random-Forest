package dev.rafarg;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

public class Main {
    public static void main(String[] args) {
        try {
            DecisionTree tree = new DecisionTree();
            tree.trainWithExcelData("Entropia_Decision_Tree.xlsx");

            Map<String, String> newCase = new HashMap<>();
            newCase.put("Idade", "Jovem");
            newCase.put("PressaoArterial", "Normal");
            newCase.put("Colesterol", "Alto");

            String prediction = tree.classify(newCase);
            System.out.println("Classificação prevista de alto risco: " + prediction);

        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
