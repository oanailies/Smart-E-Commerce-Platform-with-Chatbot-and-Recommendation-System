package auth_service.application.dto;

import java.util.List;

public class SurveyData {
    private String clientId;
    private String name;
    private String gender;
    private Integer age;
    private String county;
    private boolean isSelfEmployed;
    private String occupation;
    private Double incomeRange;
    private Double budgetRange;
    private List<String> preferredProductTypes;
    private List<String> favoriteBrands;
    private List<String> fashionStyle;
    private String skinType;
    private String purchaseFrequency;
    private String discountSensitivity;
    private String preferredPaymentMethod;
    private String deviceUsed;
    private String socialMediaInfluence;
    private String preferredRewards;

    public String getClientId() {
        return clientId;
    }

    public void setClientId(String clientId) {
        this.clientId = clientId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getGender() {
        return gender;
    }

    public void setGender(String gender) {
        this.gender = gender;
    }

    public Integer getAge() {
        return age;
    }

    public void setAge(Integer age) {
        this.age = age;
    }

    public String getCounty() {
        return county;
    }

    public void setCounty(String county) {
        this.county = county;
    }

    public boolean isSelfEmployed() {
        return isSelfEmployed;
    }

    public void setSelfEmployed(boolean selfEmployed) {
        isSelfEmployed = selfEmployed;
    }

    public String getOccupation() {
        return occupation;
    }

    public void setOccupation(String occupation) {
        this.occupation = occupation;
    }

    public Double getIncomeRange() {
        return incomeRange;
    }

    public void setIncomeRange(Double incomeRange) {
        this.incomeRange = incomeRange;
    }

    public Double getBudgetRange() {
        return budgetRange;
    }

    public void setBudgetRange(Double budgetRange) {
        this.budgetRange = budgetRange;
    }

    public List<String> getPreferredProductTypes() {
        return preferredProductTypes;
    }

    public void setPreferredProductTypes(List<String> preferredProductTypes) {
        this.preferredProductTypes = preferredProductTypes;
    }

    public List<String> getFavoriteBrands() {
        return favoriteBrands;
    }

    public void setFavoriteBrands(List<String> favoriteBrands) {
        this.favoriteBrands = favoriteBrands;
    }

    public List<String> getFashionStyle() {
        return fashionStyle;
    }

    public void setFashionStyle(List<String> fashionStyle) {
        this.fashionStyle = fashionStyle;
    }

    public String getSkinType() {
        return skinType;
    }

    public void setSkinType(String skinType) {
        this.skinType = skinType;
    }

    public String getPurchaseFrequency() {
        return purchaseFrequency;
    }

    public void setPurchaseFrequency(String purchaseFrequency) {
        this.purchaseFrequency = purchaseFrequency;
    }

    public String getDiscountSensitivity() {
        return discountSensitivity;
    }

    public void setDiscountSensitivity(String discountSensitivity) {
        this.discountSensitivity = discountSensitivity;
    }

    public String getPreferredPaymentMethod() {
        return preferredPaymentMethod;
    }

    public void setPreferredPaymentMethod(String preferredPaymentMethod) {
        this.preferredPaymentMethod = preferredPaymentMethod;
    }

    public String getDeviceUsed() {
        return deviceUsed;
    }

    public void setDeviceUsed(String deviceUsed) {
        this.deviceUsed = deviceUsed;
    }

    public String getSocialMediaInfluence() {
        return socialMediaInfluence;
    }

    public void setSocialMediaInfluence(String socialMediaInfluence) {
        this.socialMediaInfluence = socialMediaInfluence;
    }

    public String getPreferredRewards() {
        return preferredRewards;
    }

    public void setPreferredRewards(String preferredRewards) {
        this.preferredRewards = preferredRewards;
    }
}
