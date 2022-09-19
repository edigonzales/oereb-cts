package ch.so.agi.oereb.cts;

import java.util.ArrayList;
import java.util.List;

import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlElementWrapper;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlProperty;

public class Results {
    //@JacksonXmlElementWrapper(localName = "wrapperResults")
    @JacksonXmlProperty(localName = "probeResult")
    private List<Result> results = new ArrayList<>();

    public List<Result> getResults() {
        return results;
    }

    public void setResults(List<Result> results) {
        this.results = results;
    }
    
    public void addResult(Result result) {
        results.add(result);
    }
}
