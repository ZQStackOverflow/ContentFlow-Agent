package com.example.model;

import java.util.List;

/**
 * 根因分析结果
 */
public class RootCauseAnalysisResult {
    private String faultService;
    private String rootCauseSummary;
    private String detailedAnalysis;
    private String codeLocation;
    private String suggestedFixType;
    private List<String> relatedLogEntries;
    private List<String> affectedServices;
    private ErrorType errorType;
    private SeverityLevel severityLevel;

    public enum ErrorType {
        NULL_POINTER_EXCEPTION,
        ARRAY_INDEX_OUT_OF_BOUNDS,
        DATABASE_DEADLOCK,
        UPSTREAM_TIMEOUT,
        BUSINESS_LOGIC_BUG,
        CONFIGURATION_ERROR,
        RESOURCE_EXHAUSTION,
        UNKNOWN
    }

    public enum SeverityLevel {
        CRITICAL, HIGH, MEDIUM, LOW
    }

    public RootCauseAnalysisResult() {
    }

    public String getFaultService() {
        return faultService;
    }

    public void setFaultService(String faultService) {
        this.faultService = faultService;
    }

    public String getRootCauseSummary() {
        return rootCauseSummary;
    }

    public void setRootCauseSummary(String rootCauseSummary) {
        this.rootCauseSummary = rootCauseSummary;
    }

    public String getDetailedAnalysis() {
        return detailedAnalysis;
    }

    public void setDetailedAnalysis(String detailedAnalysis) {
        this.detailedAnalysis = detailedAnalysis;
    }

    public String getCodeLocation() {
        return codeLocation;
    }

    public void setCodeLocation(String codeLocation) {
        this.codeLocation = codeLocation;
    }

    public String getSuggestedFixType() {
        return suggestedFixType;
    }

    public void setSuggestedFixType(String suggestedFixType) {
        this.suggestedFixType = suggestedFixType;
    }

    public List<String> getRelatedLogEntries() {
        return relatedLogEntries;
    }

    public void setRelatedLogEntries(List<String> relatedLogEntries) {
        this.relatedLogEntries = relatedLogEntries;
    }

    public List<String> getAffectedServices() {
        return affectedServices;
    }

    public void setAffectedServices(List<String> affectedServices) {
        this.affectedServices = affectedServices;
    }

    public ErrorType getErrorType() {
        return errorType;
    }

    public void setErrorType(ErrorType errorType) {
        this.errorType = errorType;
    }

    public SeverityLevel getSeverityLevel() {
        return severityLevel;
    }

    public void setSeverityLevel(SeverityLevel severityLevel) {
        this.severityLevel = severityLevel;
    }
}