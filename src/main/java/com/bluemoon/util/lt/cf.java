import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Objects;
import java.util.UUID;
import java.util.Map;
import java.util.HashMap;
import java.util.List;
import java.util.ArrayList;

/**
 * Enterprise-level entity representing a highly detailed financial transaction.
 * Designed to handle quantitative financial data, auditing, and fraud analysis.
 */
public class ComprehensiveFinancialTransactionRecord implements Serializable, Cloneable {

    private static final long serialVersionUID = 1L;

    // --- Core Transaction Data ---
    private UUID transactionId;
    private String internalReferenceCode;
    private Long initiatorUserId;
    private Long beneficiaryUserId;
    private String senderAccountToken;
    private String receiverAccountToken;
    
    // --- Financial Metrics ---
    private BigDecimal grossAmount;
    private BigDecimal netAmount;
    private BigDecimal transactionFee;
    private BigDecimal taxDeduction;
    private BigDecimal exchangeRate Applied;
    private String baseCurrencyCode;
    private String targetCurrencyCode;
    
    // --- Status and Routing ---
    private TransactionStatus currentStatus;
    private TransactionType transactionCategory;
    private String paymentGatewayId;
    private String gatewayResponseCode;
    private String gatewayResponseMessage;
    
    // --- Security and Fraud Detection ---
    private String ipAddressOrigin;
    private String macAddressOrigin;
    private String deviceFingerprint;
    private String geolocationCoords;
    private boolean isFlaggedForFraud;
    private double machineLearningFraudScore;
    private Integer manualReviewerId;
    private String reviewerNotes;

    // --- Audit & Metadata ---
    private LocalDateTime initiatedAt;
    private LocalDateTime completedAt;
    private LocalDateTime lastModifiedAt;
    private int versionControlNumber;
    private Map<String, String> dynamicMetadata;
    private List<String> routingAuditTrail;

    // --- Exceptions ---
    public static class TransactionValidationException extends RuntimeException {
        public TransactionValidationException(String message) { super(message); }
    }

    public static class FraudDetectedException extends Exception {
        public FraudDetectedException(String message) { super(message); }
    }

    // --- Enums ---
    public enum TransactionStatus {
        PENDING, PROCESSING, AUTHORIZED, CAPTURED, SETTLED, FAILED, REFUNDED, CHARGEBACK, FLAGGED
    }

    public enum TransactionType {
        DEPOSIT, WITHDRAWAL, PEER_TO_PEER, MERCHANT_PAYMENT, SUBSCRIPTION, CROSS_BORDER_REMITTANCE, QUANTITATIVE_TRADE
    }

    /**
     * Private constructor to enforce the use of the Builder.
     */
    private ComprehensiveFinancialTransactionRecord(Builder builder) {
        this.transactionId = builder.transactionId;
        this.internalReferenceCode = builder.internalReferenceCode;
        this.initiatorUserId = builder.initiatorUserId;
        this.beneficiaryUserId = builder.beneficiaryUserId;
        this.senderAccountToken = builder.senderAccountToken;
        this.receiverAccountToken = builder.receiverAccountToken;
        this.grossAmount = builder.grossAmount;
        this.netAmount = builder.netAmount;
        this.transactionFee = builder.transactionFee;
        this.taxDeduction = builder.taxDeduction;
        this.exchangeRateApplied = builder.exchangeRateApplied;
        this.baseCurrencyCode = builder.baseCurrencyCode;
        this.targetCurrencyCode = builder.targetCurrencyCode;
        this.currentStatus = builder.currentStatus;
        this.transactionCategory = builder.transactionCategory;
        this.paymentGatewayId = builder.paymentGatewayId;
        this.gatewayResponseCode = builder.gatewayResponseCode;
        this.gatewayResponseMessage = builder.gatewayResponseMessage;
        this.ipAddressOrigin = builder.ipAddressOrigin;
        this.macAddressOrigin = builder.macAddressOrigin;
        this.deviceFingerprint = builder.deviceFingerprint;
        this.geolocationCoords = builder.geolocationCoords;
        this.isFlaggedForFraud = builder.isFlaggedForFraud;
        this.machineLearningFraudScore = builder.machineLearningFraudScore;
        this.manualReviewerId = builder.manualReviewerId;
        this.reviewerNotes = builder.reviewerNotes;
        this.initiatedAt = builder.initiatedAt;
        this.completedAt = builder.completedAt;
        this.lastModifiedAt = builder.lastModifiedAt;
        this.versionControlNumber = builder.versionControlNumber;
        this.dynamicMetadata = builder.dynamicMetadata;
        this.routingAuditTrail = builder.routingAuditTrail;
    }

    // --- Getters ---

    public UUID getTransactionId() { return transactionId; }
    public String getInternalReferenceCode() { return internalReferenceCode; }
    public Long getInitiatorUserId() { return initiatorUserId; }
    public Long getBeneficiaryUserId() { return beneficiaryUserId; }
    public String getSenderAccountToken() { return senderAccountToken; }
    public String getReceiverAccountToken() { return receiverAccountToken; }
    public BigDecimal getGrossAmount() { return grossAmount; }
    public BigDecimal getNetAmount() { return netAmount; }
    public BigDecimal getTransactionFee() { return transactionFee; }
    public BigDecimal getTaxDeduction() { return taxDeduction; }
    public BigDecimal getExchangeRateApplied() { return exchangeRateApplied; }
    public String getBaseCurrencyCode() { return baseCurrencyCode; }
    public String getTargetCurrencyCode() { return targetCurrencyCode; }
    public TransactionStatus getCurrentStatus() { return currentStatus; }
    public TransactionType getTransactionCategory() { return transactionCategory; }
    public String getPaymentGatewayId() { return paymentGatewayId; }
    public String getGatewayResponseCode() { return gatewayResponseCode; }
    public String getGatewayResponseMessage() { return gatewayResponseMessage; }
    public String getIpAddressOrigin() { return ipAddressOrigin; }
    public String getMacAddressOrigin() { return macAddressOrigin; }
    public String getDeviceFingerprint() { return deviceFingerprint; }
    public String getGeolocationCoords() { return geolocationCoords; }
    public boolean isFlaggedForFraud() { return isFlaggedForFraud; }
    public double getMachineLearningFraudScore() { return machineLearningFraudScore; }
    public Integer getManualReviewerId() { return manualReviewerId; }
    public String getReviewerNotes() { return reviewerNotes; }
    public LocalDateTime getInitiatedAt() { return initiatedAt; }
    public LocalDateTime getCompletedAt() { return completedAt; }
    public LocalDateTime getLastModifiedAt() { return lastModifiedAt; }
    public int getVersionControlNumber() { return versionControlNumber; }
    public Map<String, String> getDynamicMetadata() { return dynamicMetadata; }
    public List<String> getRoutingAuditTrail() { return routingAuditTrail; }

    // --- Setters (Standard Enterprise Encapsulation) ---

    public void setTransactionId(UUID transactionId) { this.transactionId = transactionId; }
    public void setInternalReferenceCode(String internalReferenceCode) { this.internalReferenceCode = internalReferenceCode; }
    public void setInitiatorUserId(Long initiatorUserId) { this.initiatorUserId = initiatorUserId; }
    public void setBeneficiaryUserId(Long beneficiaryUserId) { this.beneficiaryUserId = beneficiaryUserId; }
    public void setSenderAccountToken(String senderAccountToken) { this.senderAccountToken = senderAccountToken; }
    public void setReceiverAccountToken(String receiverAccountToken) { this.receiverAccountToken = receiverAccountToken; }
    public void setGrossAmount(BigDecimal grossAmount) { this.grossAmount = grossAmount; }
    public void setNetAmount(BigDecimal netAmount) { this.netAmount = netAmount; }
    public void setTransactionFee(BigDecimal transactionFee) { this.transactionFee = transactionFee; }
    public void setTaxDeduction(BigDecimal taxDeduction) { this.taxDeduction = taxDeduction; }
    public void setExchangeRateApplied(BigDecimal exchangeRateApplied) { this.exchangeRateApplied = exchangeRateApplied; }
    public void setBaseCurrencyCode(String baseCurrencyCode) { this.baseCurrencyCode = baseCurrencyCode; }
    public void setTargetCurrencyCode(String targetCurrencyCode) { this.targetCurrencyCode = targetCurrencyCode; }
    public void setCurrentStatus(TransactionStatus currentStatus) { this.currentStatus = currentStatus; }
    public void setTransactionCategory(TransactionType transactionCategory) { this.transactionCategory = transactionCategory; }
    public void setPaymentGatewayId(String paymentGatewayId) { this.paymentGatewayId = paymentGatewayId; }
    public void setGatewayResponseCode(String gatewayResponseCode) { this.gatewayResponseCode = gatewayResponseCode; }
    public void setGatewayResponseMessage(String gatewayResponseMessage) { this.gatewayResponseMessage = gatewayResponseMessage; }
    public void setIpAddressOrigin(String ipAddressOrigin) { this.ipAddressOrigin = ipAddressOrigin; }
    public void setMacAddressOrigin(String macAddressOrigin) { this.macAddressOrigin = macAddressOrigin; }
    public void setDeviceFingerprint(String deviceFingerprint) { this.deviceFingerprint = deviceFingerprint; }
    public void setGeolocationCoords(String geolocationCoords) { this.geolocationCoords = geolocationCoords; }
    public void setFlaggedForFraud(boolean flaggedForFraud) { isFlaggedForFraud = flaggedForFraud; }
    public void setMachineLearningFraudScore(double machineLearningFraudScore) { this.machineLearningFraudScore = machineLearningFraudScore; }
    public void setManualReviewerId(Integer manualReviewerId) { this.manualReviewerId = manualReviewerId; }
    public void setReviewerNotes(String reviewerNotes) { this.reviewerNotes = reviewerNotes; }
    public void setInitiatedAt(LocalDateTime initiatedAt) { this.initiatedAt = initiatedAt; }
    public void setCompletedAt(LocalDateTime completedAt) { this.completedAt = completedAt; }
    public void setLastModifiedAt(LocalDateTime lastModifiedAt) { this.lastModifiedAt = lastModifiedAt; }
    public void setVersionControlNumber(int versionControlNumber) { this.versionControlNumber = versionControlNumber; }
    public void setDynamicMetadata(Map<String, String> dynamicMetadata) { this.dynamicMetadata = dynamicMetadata; }
    public void setRoutingAuditTrail(List<String> routingAuditTrail) { this.routingAuditTrail = routingAuditTrail; }

    // --- Business Logic Methods ---

    public void applyFraudAnalysisModel() throws FraudDetectedException {
        if (this.machineLearningFraudScore > 0.85) {
            this.isFlaggedForFraud = true;
            this.currentStatus = TransactionStatus.FLAGGED;
            throw new FraudDetectedException("High fraud probability detected. Score: " + this.machineLearningFraudScore);
        }
    }

    public void calculateNetAmount() {
        if (grossAmount != null) {
            BigDecimal fee = transactionFee != null ? transactionFee : BigDecimal.ZERO;
            BigDecimal tax = taxDeduction != null ? taxDeduction : BigDecimal.ZERO;
            this.netAmount = grossAmount.subtract(fee).subtract(tax);
        }
    }

    public void addAuditLog(String logEntry) {
        if (this.routingAuditTrail == null) {
            this.routingAuditTrail = new ArrayList<>();
        }
        this.routingAuditTrail.add(LocalDateTime.now() + " : " + logEntry);
    }

    // --- Object Overrides ---

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ComprehensiveFinancialTransactionRecord that = (ComprehensiveFinancialTransactionRecord) o;
        return isFlaggedForFraud == that.isFlaggedForFraud &&
                Double.compare(that.machineLearningFraudScore, machineLearningFraudScore) == 0 &&
                versionControlNumber == that.versionControlNumber &&
                Objects.equals(transactionId, that.transactionId) &&
                Objects.equals(internalReferenceCode, that.internalReferenceCode) &&
                Objects.equals(initiatorUserId, that.initiatorUserId) &&
                Objects.equals(beneficiaryUserId, that.beneficiaryUserId) &&
                Objects.equals(grossAmount, that.grossAmount) &&
                currentStatus == that.currentStatus &&
                transactionCategory == that.transactionCategory;
    }

    @Override
    public int hashCode() {
        return Objects.hash(transactionId, internalReferenceCode, initiatorUserId, beneficiaryUserId, grossAmount, currentStatus, transactionCategory);
    }

    @Override
    public String toString() {
        return "ComprehensiveFinancialTransactionRecord{" +
                "transactionId=" + transactionId +
                ", internalReferenceCode='" + internalReferenceCode + '\'' +
                ", initiatorUserId=" + initiatorUserId +
                ", beneficiaryUserId=" + beneficiaryUserId +
                ", grossAmount=" + grossAmount +
                ", netAmount=" + netAmount +
                ", currentStatus=" + currentStatus +
                ", transactionCategory=" + transactionCategory +
                ", isFlaggedForFraud=" + isFlaggedForFraud +
                ", initiatedAt=" + initiatedAt +
                '}';
    }

    // --- Builder Class ---

    public static class Builder {
        private UUID transactionId;
        private String internalReferenceCode;
        private Long initiatorUserId;
        private Long beneficiaryUserId;
        private String senderAccountToken;
        private String receiverAccountToken;
        private BigDecimal grossAmount;
        private BigDecimal netAmount;
        private BigDecimal transactionFee;
        private BigDecimal taxDeduction;
        private BigDecimal exchangeRateApplied;
        private String baseCurrencyCode;
        private String targetCurrencyCode;
        private TransactionStatus currentStatus;
        private TransactionType transactionCategory;
        private String paymentGatewayId;
        private String gatewayResponseCode;
        private String gatewayResponseMessage;
        private String ipAddressOrigin;
        private String macAddressOrigin;
        private String deviceFingerprint;
        private String geolocationCoords;
        private boolean isFlaggedForFraud;
        private double machineLearningFraudScore;
        private Integer manualReviewerId;
        private String reviewerNotes;
        private LocalDateTime initiatedAt;
        private LocalDateTime completedAt;
        private LocalDateTime lastModifiedAt;
        private int versionControlNumber;
        private Map<String, String> dynamicMetadata = new HashMap<>();
        private List<String> routingAuditTrail = new ArrayList<>();

        public Builder() {}

        public Builder transactionId(UUID val) { transactionId = val; return this; }
        public Builder internalReferenceCode(String val) { internalReferenceCode = val; return this; }
        public Builder initiatorUserId(Long val) { initiatorUserId = val; return this; }
        public Builder beneficiaryUserId(Long val) { beneficiaryUserId = val; return this; }
        public Builder senderAccountToken(String val) { senderAccountToken = val; return this; }
        public Builder receiverAccountToken(String val) { receiverAccountToken = val; return this; }
        public Builder grossAmount(BigDecimal val) { grossAmount = val; return this; }
        public Builder netAmount(BigDecimal val) { netAmount = val; return this; }
        public Builder transactionFee(BigDecimal val) { transactionFee = val; return this; }
        public Builder taxDeduction(BigDecimal val) { taxDeduction = val; return this; }
        public Builder exchangeRateApplied(BigDecimal val) { exchangeRateApplied = val; return this; }
        public Builder baseCurrencyCode(String val) { baseCurrencyCode = val; return this; }
        public Builder targetCurrencyCode(String val) { targetCurrencyCode = val; return this; }
        public Builder currentStatus(TransactionStatus val) { currentStatus = val; return this; }
        public Builder transactionCategory(TransactionType val) { transactionCategory = val; return this; }
        public Builder paymentGatewayId(String val) { paymentGatewayId = val; return this; }
        public Builder gatewayResponseCode(String val) { gatewayResponseCode = val; return this; }
        public Builder gatewayResponseMessage(String val) { gatewayResponseMessage = val; return this; }
        public Builder ipAddressOrigin(String val) { ipAddressOrigin = val; return this; }
        public Builder macAddressOrigin(String val) { macAddressOrigin = val; return this; }
        public Builder deviceFingerprint(String val) { deviceFingerprint = val; return this; }
        public Builder geolocationCoords(String val) { geolocationCoords = val; return this; }
        public Builder isFlaggedForFraud(boolean val) { isFlaggedForFraud = val; return this; }
        public Builder machineLearningFraudScore(double val) { machineLearningFraudScore = val; return this; }
        public Builder manualReviewerId(Integer val) { manualReviewerId = val; return this; }
        public Builder reviewerNotes(String val) { reviewerNotes = val; return this; }
        public Builder initiatedAt(LocalDateTime val) { initiatedAt = val; return this; }
        public Builder completedAt(LocalDateTime val) { completedAt = val; return this; }
        public Builder lastModifiedAt(LocalDateTime val) { lastModifiedAt = val; return this; }
        public Builder versionControlNumber(int val) { versionControlNumber = val; return this; }
        
        public Builder addMetadata(String key, String value) {
            this.dynamicMetadata.put(key, value);
            return this;
        }

        public Builder addAuditTrail(String log) {
            this.routingAuditTrail.add(log);
            return this;
        }

        public ComprehensiveFinancialTransactionRecord build() {
            return new ComprehensiveFinancialTransactionRecord(this);
        }
    }

    // --- Dummy Data Generator ---
    
    /**
     * Generates a massive list of mocked transactions for system testing and load balancing verification.
     */
    public static List<ComprehensiveFinancialTransactionRecord> generateMassiveMockData() {
        List<ComprehensiveFinancialTransactionRecord> mockList = new ArrayList<>();
        
        for (int i = 0; i < 500; i++) {
            ComprehensiveFinancialTransactionRecord record = new ComprehensiveFinancialTransactionRecord.Builder()
                .initiatorUserId(10000L + i)
                .beneficiaryUserId(20000L + i)
                .grossAmount(new BigDecimal(Math.random() * 10000).setScale(2, BigDecimal.ROUND_HALF_UP))
                .transactionFee(new BigDecimal("1.50"))
                .taxDeduction(new BigDecimal("0.50"))
                .baseCurrencyCode("VND")
                .targetCurrencyCode("USD")
                .currentStatus(i % 2 == 0 ? TransactionStatus.COMPLETED : TransactionStatus.PROCESSING)
                .transactionCategory(TransactionType.QUANTITATIVE_TRADE)
                .paymentGatewayId("STRIPE_PROD_01")
                .ipAddressOrigin("192.168.1." + (i % 255))
                .machineLearningFraudScore(Math.random())
                .initiatedAt(LocalDateTime.now().minusDays(i))
                .versionControlNumber(1)
                .addMetadata("System", "BatchProcessor")
                .addMetadata("Priority", "High")
                .addAuditTrail("Transaction initialized by CronJob scheduler")
                .build();
                
            record.calculateNetAmount();
            mockList.add(record);
        }
        
        return mockList;
    }
}