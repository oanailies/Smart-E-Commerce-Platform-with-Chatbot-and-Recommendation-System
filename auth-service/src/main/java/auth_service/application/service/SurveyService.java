package auth_service.application.service;

import auth_service.application.dto.SurveyData;
import org.springframework.stereotype.Service;

import java.io.*;
import java.nio.file.*;
import java.util.List;

@Service
public class SurveyService {

    private final String CSV_FILE_PATH = "src/main/resources/financial_data.csv";

    public void saveSurveyData(SurveyData data) {
        try {
            boolean fileExists = Files.exists(Paths.get(CSV_FILE_PATH));

            try (BufferedWriter writer = new BufferedWriter(new FileWriter(CSV_FILE_PATH, true))) {
                if (!fileExists) {
                    writer.write(getCSVHeader());
                    writer.newLine();
                }

                writer.write(convertToCSVRow(data));
                writer.newLine();
            }
        } catch (IOException e) {
            throw new RuntimeException("Failed to save survey data to CSV", e);
        }
    }

    private String getCSVHeader() {
        return String.join(",", List.of(
                "ClientId", "Name", "Gender", "Age", "County", "SelfEmployed", "Occupation",
                "IncomeRange", "BudgetRange", "PreferredProductTypes", "FavoriteBrands", "FashionStyle",
                "SkinType", "PurchaseFrequency", "DiscountSensitivity", "PreferredPaymentMethod",
                "DeviceUsed", "SocialMediaInfluence", "PreferredRewards"
        ));
    }

    private String convertToCSVRow(SurveyData data) {
        return String.join(",", List.of(
                quote(data.getClientId()),
                quote(data.getName()),
                quote(data.getGender()),
                quote(String.valueOf(data.getAge())),
                quote(data.getCounty()),
                quote(String.valueOf(data.isSelfEmployed())),
                quote(data.getOccupation()),
                quote(String.valueOf(data.getIncomeRange())),
                quote(String.valueOf(data.getBudgetRange())),
                quote(String.join(";", data.getPreferredProductTypes())),
                quote(String.join(";", data.getFavoriteBrands())),
                quote(String.join(";", data.getFashionStyle())),
                quote(data.getSkinType()),
                quote(data.getPurchaseFrequency()),
                quote(data.getDiscountSensitivity()),
                quote(data.getPreferredPaymentMethod()),
                quote(data.getDeviceUsed()),
                quote(data.getSocialMediaInfluence()),
                quote(data.getPreferredRewards())
        ));
    }

    private String quote(String value) {
        return "\"" + value.replace("\"", "\"\"") + "\"";
    }
}
