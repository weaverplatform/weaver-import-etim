package model;

import java.util.ArrayList;

/**
 * Created by char on 23/05/16.
 *
 * Class to handle ETIM Classes
 *
 *
 */
public class ETIMClass {

    private String code;
    private String description;
    private String version;

    private ArrayList<String> features;
    private ArrayList<String> values;

    private String modellingClasse;

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getVersion() {
        return version;
    }

    public void setVersion(String version) {
        this.version = version;
    }

    public ArrayList<String> getFeatures() {
        return features;
    }

    public void setFeatures(ArrayList<String> features) {
        this.features = features;
    }

    public String getModellingClasse() {
        return modellingClasse;
    }

    public void setModellingClasse(String modellingClasse) {
        this.modellingClasse = modellingClasse;
    }

    public ArrayList<String> getValues() {
        return values;
    }

    public void setValues(ArrayList<String> values) {
        this.values = values;
    }

    @Override
    public String toString() {
        return "ETIMClass{" +
                "code='" + code + '\'' +
                ", description='" + description + '\'' +
                ", version='" + version + '\'' +
                ", features=" + features +
                ", values=" + values +
                ", modellingClasse='" + modellingClasse + '\'' +
                '}';
    }
}
