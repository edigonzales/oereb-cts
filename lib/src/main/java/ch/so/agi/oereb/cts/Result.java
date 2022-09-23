package ch.so.agi.oereb.cts;

import java.net.URI;
import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

//import com.fasterxml.jackson.annotation.JsonFormat;
//import com.fasterxml.jackson.annotation.JsonInclude;
//import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlCData;
//import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlElementWrapper;
//import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlProperty;
//import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlRootElement;

//@JsonInclude(JsonInclude.Include.NON_EMPTY)
//@JacksonXmlRootElement(localName = "result")
public class Result {
    protected String className; 
    
    protected String description;

    protected URI serviceEndpoint;

    protected boolean success = true;
    
    protected String message;

    protected URI request;
    
    protected Integer statusCode;
    
//    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd HH:mm:ss")
    protected Instant startTime;

//    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd HH:mm:ss")
    protected Instant endTime;

    protected double processingTimeSecs = -1;
    
    protected String resultFileLocation;

//    @JacksonXmlElementWrapper(localName = "checkResults")
//    @JacksonXmlProperty(localName = "check")
    protected List<Result> results = new ArrayList<Result>();

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
            //
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

    @Override
    public String toString() {
        return "Result [className=" + className + ", description=" + description + ", serviceEndpoint="
                + serviceEndpoint + ", success=" + success + ", message=" + message + ", request=" + request
                + ", statusCode=" + statusCode + ", startTime=" + startTime + ", endTime=" + endTime
                + ", processingTimeSecs=" + processingTimeSecs + ", resultFileLocation=" + resultFileLocation
                + ", results=" + results + "]";
    }    

}
