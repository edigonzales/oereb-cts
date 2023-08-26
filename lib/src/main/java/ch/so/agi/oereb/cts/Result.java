package ch.so.agi.oereb.cts;

import java.io.Serializable;
import java.net.URI;
import java.time.Duration;
import java.time.Instant;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import ch.interlis.iom.IomObject;
import ch.interlis.iom_j.Iom_jObject;

public class Result implements Serializable {
    private static final String MODEL_NAME = "SO_AGI_OEREB_CTS_20230819";
    private static final String ILI_TOPIC = MODEL_NAME+".Results";
    private static final String TAG_PROBE_RESULT = MODEL_NAME + ".Results.ProbeResult";
    private static final String TAG_CHECK_RESULT = MODEL_NAME + ".CheckResult";

    private static final long serialVersionUID = 1L;

    protected String identifier;
    
    protected String className; 
    
    protected String description;

    protected URI serviceEndpoint;

    protected boolean success = true;
    
    protected String message;

    protected URI request;
    
    protected Integer statusCode;
    
    protected String testSuiteTime;
    
    protected Instant startTime;

    protected Instant endTime;

    protected double processingTimeSecs = -1;
    
    protected String resultFileLocation;

    protected List<Result> results = new ArrayList<Result>();

    public String getIdentifier() {
        return identifier;
    }

    public void setIdentifier(String identifier) {
        this.identifier = identifier;
    }

    public String getClassName() {
        return className;
    }

    public void setClassName(String className) {
        this.className = className;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public URI getServiceEndpoint() {
        return serviceEndpoint;
    }

    public void setServiceEndpoint(URI serviceEndpoint) {
        this.serviceEndpoint = serviceEndpoint;
    }

    public boolean isSuccess() {
        return success;
    }

    public void setSuccess(boolean success) {
        this.success = success;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public URI getRequest() {
        return request;
    }

    public void setRequest(URI request) {
        this.request = request;
    }

    public Integer getStatusCode() {
        return statusCode;
    }

    public void setStatusCode(int statusCode) {
        this.statusCode = statusCode;
    }

    public void setStatusCode(Integer statusCode) {
        this.statusCode = statusCode;
    }
    
    public String getTestSuiteTime() {
        return testSuiteTime;
    }

    public void setTestSuiteTime(String testSuiteTime) {
        this.testSuiteTime = testSuiteTime;
    }

    public Instant getStartTime() {
        return startTime;
    }

    public void setStartTime(Instant startTime) {
        this.startTime = startTime;
    }

    public Instant getEndTime() {
        return endTime;
    }

    public void setEndTime(Instant endTime) {
        this.endTime = endTime;
    }

    public double getProcessingTimeSecs() {
        return processingTimeSecs;
    }

    public void setProcessingTimeSecs(double processingTimeSecs) {
        this.processingTimeSecs = processingTimeSecs;
    }

    public String getResultFileLocation() {
        return resultFileLocation;
    }

    public void setResultFileLocation(String resultFileLocation) {
        this.resultFileLocation = resultFileLocation;
    }

    public List<Result> getResults() {
        return results;
    }

    public void setResults(List<Result> results) {
        this.results = results;
    }
    
    public void addResult(Result result) {
        this.results.add(result);
                
        if (!result.success) {
            this.success = false;
            //this.message = this.resultsFailed.get(0).getMessage();
        } 
    }
    
    public void start() {
        this.startTime = Instant.now();
    }
    
    public void stop() {
        this.endTime = Instant.now();
        
        this.processingTimeSecs = Duration.between(startTime, endTime).toMillis() / 1000.0;
    }
    
    
    public IomObject toIomObject() {
        DateTimeFormatter dtf = DateTimeFormatter.ofPattern("YYYY-MM-dd'T'HH:mm:ss.SSS");
        // Der gleiche Request erhält immer die gleiche TID pro Identifier. Zusätzlich wird die lesbare
        // Probe hinzugefügt.
        UUID uuid = UUID.nameUUIDFromBytes(this.request.toString().getBytes());
        Iom_jObject iomObj = new Iom_jObject(TAG_PROBE_RESULT, this.identifier + "." + this.className + "." + uuid);
        iomObj.setattrvalue("identifier", this.identifier);
        iomObj.setattrvalue("className", this.className);
        iomObj.setattrvalue("success", Boolean.valueOf(this.success).toString());
        iomObj.setattrvalue("serviceEndpoint", this.serviceEndpoint.toString());
        iomObj.setattrvalue("request", this.request.toString());
        iomObj.setattrvalue("testSuiteTime", this.testSuiteTime);
        iomObj.setattrvalue("startTime", this.startTime.atZone(ZoneId.systemDefault()).toLocalDateTime().format(dtf));
        iomObj.setattrvalue("endTime", this.endTime.atZone(ZoneId.systemDefault()).toLocalDateTime().format(dtf));
        iomObj.setattrvalue("processingTimeSecs", String.valueOf(this.processingTimeSecs));
        iomObj.setattrvalue("resultFileLocation", this.resultFileLocation);
        
        for (Result result : this.results) {
            Iom_jObject checkIomStruct = new Iom_jObject(TAG_CHECK_RESULT, null);
            checkIomStruct.setattrvalue("className", result.className);
            checkIomStruct.setattrvalue("description", result.description);
            checkIomStruct.setattrvalue("success", Boolean.valueOf(result.success).toString());
            if (result.message != null) checkIomStruct.setattrvalue("message", result.message);
            if (result.statusCode != null) checkIomStruct.setattrvalue("statusCode", String.valueOf(result.statusCode));
            checkIomStruct.setattrvalue("startTime", result.startTime.atZone(ZoneId.systemDefault()).toLocalDateTime().format(dtf));
            checkIomStruct.setattrvalue("endTime", result.endTime.atZone(ZoneId.systemDefault()).toLocalDateTime().format(dtf));
            checkIomStruct.setattrvalue("processingTimeSecs", String.valueOf(result.processingTimeSecs));            
            iomObj.addattrobj("checkResults", checkIomStruct);
        }
        
        return iomObj;
    }
    
    @Override
    public String toString() {
        return "Result [className=" + className + ", description=" + description + ", serviceEndpoint="
                + serviceEndpoint + ", success=" + success + ", message=" + message + ", request=" + request
                + ", statusCode=" + statusCode + ", startTime=" + startTime + ", endTime=" + endTime
                + ", processingTimeSecs=" + processingTimeSecs + ", resultFileLocation=" + resultFileLocation
                + ", results=" + results + "]";
    }    
}
