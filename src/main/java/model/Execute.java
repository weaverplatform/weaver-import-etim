package model;

import com.google.common.base.Charsets;
import com.google.common.io.Resources;
import com.jcabi.xml.XML;
import com.jcabi.xml.XMLDocument;
import com.sysunite.weaver.connector.neo4j.Neo4JConstants;
import com.sysunite.weaver.connector.neo4j.RelationProperty;
import com.sysunite.weaver.connector.neo4j.Transaction;
import com.sysunite.weaver.connector.neo4j.ValueProperty;
import org.apache.commons.io.FileUtils;
import org.neo4j.graphdb.GraphDatabaseService;
import org.neo4j.graphdb.factory.GraphDatabaseFactory;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringReader;
import java.net.URL;
import java.io.File;
import java.util.*;


/**
 * Created by char on 17/05/16.
 */
public class Execute {

    public static final String ALPHANUMERIC = "Alphanumeric";
    public static final String NUMERIC = "Numeric";
    public static final String LOGICAL = "Logical";
    public static final String RANGE = "Range";
    public static final String COORDINATE = "Coordinate";

    public static final String db = "/Users/char/Documents/Neo4j/UNITE1";

    public static final String fileLog = "/Users/char/Documents/sysunite/unite-0/logger";

    public static void main(String[] args) throws IOException {

//        Setup Neo4j
        GraphDatabaseFactory graphDatabaseFactory = new GraphDatabaseFactory();
        //Emptying the Neo4j to avoid crush due to already existing files in directory
        FileUtils.deleteDirectory(new File(db));
        File databaseDir = new File(db);
        GraphDatabaseService graphDb = graphDatabaseFactory.newEmbeddedDatabase(databaseDir);

        PrintWriter logger = new PrintWriter(fileLog + new Date().toString() + ".txt", "UTF-8");

        logger.println("Unite Data import Log " + new Date().toString());


        // Clear Database
        try {
            graphDb.execute("MATCH (n) DETACH DELETE n");
        } catch (Exception e) {
            e.printStackTrace();
        }

        //Open transaction with Neo4j
        Transaction tx = new Transaction(graphDb);



        // Read XML
        try {

            /*********************************
             *************Units***************
             *********************************/

            //Open the correct partitioned .xml
            URL url = Resources.getResource("UnitsUnite.xml");
            String text = Resources.toString(url, Charsets.UTF_8);

            XML xml = new XMLDocument(text);

            List<String> codeUnits = xml.xpath("//Unit/Code/text()");
            List<String> descriptionUnits = xml.xpath("//Description/text()");
            List<String> AbbreviationUnits = xml.xpath("//Abbreviation/text()");

            // Create Units, with Properties Code,UnitValue and its Abbreviation
            for (int i = 0; i < codeUnits.size(); i++) {
                ArrayList<ValueProperty> unitProperties = new ArrayList<ValueProperty>();
                unitProperties.add(new ValueProperty("Label", Neo4JConstants.WEAVER_LABEL, "Unit"));
                unitProperties.add(new ValueProperty(codeUnits.get(i), "UnitValue", descriptionUnits.get(i)));
                unitProperties.add(new ValueProperty(codeUnits.get(i), "Code", codeUnits.get(i)));
                unitProperties.add(new ValueProperty(codeUnits.get(i), "UnitValueAbbreviation", AbbreviationUnits.get(i)));
                tx.createIndividual(codeUnits.get(i), unitProperties, new ArrayList<>());
            }

            System.out.println("------        Unit Nodes Created        --------");

            logger.println("Unit Nodes Created: " + codeUnits.size());


            /*********************************
             ***************Type**************
             *********************************/

            //Statically create the 5 different  "Type" nodes available
            tx.createIndividual(ALPHANUMERIC,Arrays.asList(new ValueProperty("Label", Neo4JConstants.WEAVER_LABEL, "Type")),new ArrayList<>());
            tx.createIndividual(LOGICAL,Arrays.asList(new ValueProperty("Label", Neo4JConstants.WEAVER_LABEL, "Type")),new ArrayList<>());
            tx.createIndividual(NUMERIC,Arrays.asList(new ValueProperty("Label", Neo4JConstants.WEAVER_LABEL, "Type")),new ArrayList<>());
            tx.createIndividual(RANGE,Arrays.asList(new ValueProperty("Label", Neo4JConstants.WEAVER_LABEL, "Type")),new ArrayList<>());
            tx.createIndividual(COORDINATE,Arrays.asList(new ValueProperty("Label", Neo4JConstants.WEAVER_LABEL, "Type"),new ValueProperty(COORDINATE, "Type", COORDINATE)),new ArrayList<>());

            System.out.println("------        Type Nodes Created        --------");

            logger.println("Type Nodes Created: 5");

            /*********************************
             ***********ETIM Features*********
             *********************************/

            url = Resources.getResource("FeaturesUnite.xml");
            text = Resources.toString(url, Charsets.UTF_8);
            xml = new XMLDocument(text);
            List<String> codeFeatures = xml.xpath("//Feature/Code/text()");
            List<String> typeFeatures = xml.xpath("//Feature/Type/text()");
            List<String> descriptionFeatures = xml.xpath("//Description/text()");

            for (int i = 0; i < codeFeatures.size(); i++) {
                ArrayList<ValueProperty> efProperties = new ArrayList<ValueProperty>();
                efProperties.add(new ValueProperty("Label", Neo4JConstants.WEAVER_LABEL, "ETIM_Feature"));
                efProperties.add(new ValueProperty(codeFeatures.get(i), "Description", descriptionFeatures.get(i)));
                tx.createIndividual(codeFeatures.get(i) , efProperties, Arrays.asList(new RelationProperty(codeFeatures.get(i), "hasType", typeFeatures.get(i))));
            }
            System.out.println("------    ETIM Feature Nodes Created    --------");

            logger.println("ETIM Feature Nodes Created: " + codeFeatures.size());

            /*********************************
             ***********Actual Values*********
             *********************************/

            //Open the correct partitioned .xml
            url = Resources.getResource("ValuesUnite.xml");
            text = Resources.toString(url, Charsets.UTF_8);
            xml = new XMLDocument(text);
            List<String> descriptionValues = xml.xpath("//Description/text()");
            List<String> codeValues = xml.xpath("//Code/text()");

            //Create the "Actual Value" nodes, with Code and Value properties
            for (int i = 0; i < codeValues.size(); i++) {
                ArrayList<ValueProperty> ActualValuesproperties = new ArrayList<ValueProperty>();
                ActualValuesproperties.add(new ValueProperty("Label", Neo4JConstants.WEAVER_LABEL, "Actual_Value"));
                ActualValuesproperties.add(new ValueProperty(descriptionValues.get(i),"Value", descriptionValues.get(i)));
                ActualValuesproperties.add(new ValueProperty(descriptionValues.get(i),"Code", codeValues.get(i)));
                tx.createIndividual(codeValues.get(i) + "value", ActualValuesproperties, new ArrayList<>());


            }

            System.out.println("------    Actual Value Nodes Created    --------");

            logger.println("Actual Value Nodes Created: " + codeValues.size());

            /*********************************
             ***************Ports*************
             *********************************/

            //Statically create the different "Port" nodes available
            for (int ide = 0;ide<100;ide++){
                String tempStr = "p" + Integer.toString(ide);
                tx.createIndividual(tempStr,Arrays.asList(new ValueProperty("Label", Neo4JConstants.WEAVER_LABEL, "Port"),new ValueProperty(tempStr, "Value", Integer.toString(ide))),new ArrayList<>());
            }

            System.out.println("------        Port Nodes Created        --------");

            logger.println(" Port  Nodes Created: 100");

            /*********************************
             *************Features************
             *********************************/

            //Open ModellingClassesUnite.xml for reading the different Features
            url = Resources.getResource("ModellingClassesUnite.xml");
            text = Resources.toString(url, Charsets.UTF_8);
            DocumentBuilderFactory factoryFeats = DocumentBuilderFactory.newInstance();
            DocumentBuilder builderFeats = factoryFeats.newDocumentBuilder();
            Document documentFeats = builderFeats.parse(new InputSource(new StringReader(text)));
            NodeList feature_flowList = documentFeats.getElementsByTagName("ModellingClass");
            ArrayList<String> codePortPairs = new ArrayList<String>();
            // List containig Features
            ArrayList<String> featuresList = new ArrayList<String>();
            int featureCounter = 0;
            for (int i = 0; i < feature_flowList.getLength(); i++) {
                //childList holds the elements within <ModellingClass
                NodeList childList = feature_flowList.item(i).getChildNodes();
                for (int j = 0; j < childList.getLength(); j++) {
                    Node childNode = childList.item(j);
                    //When we find <Features>, go in
                    if (childNode.getNodeName().equals("Features")) {
                        //InnerchildList holds what's inside <Features> -> all the <Feature (NOT FeatureCode,UnitCode,OrderNumber,DimensionalDrawingCode,PortCode yet)
                        NodeList InnerchildList = childNode.getChildNodes();

                        for (int j2 = 0; j2 < InnerchildList.getLength(); j2++) { //Go through all Features
                            Node innerchildNode2 = InnerchildList.item(j2);
                            if (innerchildNode2.getNodeName().equals("Feature")) {//This is needed because some Nodes have irrelevant names
                                ArrayList<ValueProperty> valueProperties = new ArrayList<ValueProperty>();
                                NodeList innerchildList2 = innerchildNode2.getChildNodes();
                                String DDC = "No DrawingCode";
                                String UnitCode = "No UnitCode";
                                String featureName = null;
                                String PortC = null;

                                for (int s = 0; s < innerchildList2.getLength(); s++) { //Run within <Feature to find drawing code



                                    Node fNode = innerchildList2.item(s);

                                    if (fNode.getNodeName().equals("DimensionalDrawingCode")) {
                                        DDC = fNode.getTextContent();
                                    }
                                    if (fNode.getNodeName().equals("FeatureCode")) {
                                        featureName = fNode.getTextContent(); //This serves as the name of Feature, but its really the name of the ETIM Feature Node

                                    }
                                    if (fNode.getNodeName().equals("PortCode")) {
                                        PortC = "p" + fNode.getTextContent();
                                    }
                                    if (fNode.getNodeName().equals("UnitCode")) {
                                        UnitCode = fNode.getTextContent();
                                    }
                                }//End of searching the properties of a Feature

                                //Create Feature Node
                                valueProperties.add(new ValueProperty(featureName + "feature", "DrawingCode", DDC));
                                valueProperties.add(new ValueProperty(featureName + "feature", Neo4JConstants.WEAVER_LABEL, "Feature"));
                                if (!featuresList.contains(featureName)) {
//                                    System.out.println(featureName);
                                    tx.createIndividual(featureName + "feature", valueProperties, new ArrayList<>());
                                    featuresList.add(featureName);
                                    featureCounter ++;
                                    try{
                                        tx.createRelationProperty(new RelationProperty(featureName + "feature", "basedOn", featureName));
                                    }catch(org.neo4j.graphdb.MultipleFoundException MFex5){}
                                    if (!codePortPairs.contains(featureName + "feature" + PortC)) {
                                        try {
                                            tx.createRelationProperty(new RelationProperty(featureName + "feature", "hasPort", PortC));
                                            codePortPairs.add(featureName + "feature" + PortC);
                                        } catch (org.neo4j.graphdb.MultipleFoundException MFex) {}
                                    }
                                    if (UnitCode != "No UnitCode") { //Only create links to Features that have a UnitCode
                                        try { //Dont create multiple lines(or most precisely, DON'T BLOW UP)
                                            tx.createRelationProperty(new RelationProperty(featureName + "feature", "hasUnit", UnitCode));
                                        } catch (org.neo4j.graphdb.MultipleFoundException MFex1) {}
                                    }
                                }


                            }//End of specific Feature
                        }//End of All Features
                    }
                }//End of going through Modelling Classes
            }//End of going through ModellingClassesUnite.xml

            System.out.println("------      Feature Nodes Created       --------");

            logger.println("Feature Nodes Created: " + featureCounter);

            /*********************************
             *********Modelling Class*********
             ************** + ****************
             ********Enumerator Value*********
             *********************************/

            //Open ModellingClassesUnite.xml for reading
            url = Resources.getResource("ModellingClassesUnite.xml");
            text = Resources.toString(url, Charsets.UTF_8);
            DocumentBuilderFactory factoryMC = DocumentBuilderFactory.newInstance();
            DocumentBuilder builderMC = factoryMC.newDocumentBuilder();
            Document documentMC = builderMC.parse(new InputSource(new StringReader(text)));
            NodeList flowList = documentMC.getElementsByTagName("ModellingClass");
            ArrayList<String> vCodes = new ArrayList<String>();
            int modellingClassCounter = 0;
            int enumeratorCounter = 0;
            for (int i = 0; i < flowList.getLength(); i++) { //Run within <ModellingClasses>
                NodeList childList = feature_flowList.item(i).getChildNodes(); //childList holds the elements within <ModellingClass
                String mcCode = null;
                String mcVersion = null;
                String mcDesc = "No Description";
                ArrayList<ValueProperty> mcProperties = new ArrayList<ValueProperty>();
                ArrayList<String> mcFeatures = new ArrayList<String>();
                ArrayList<String> mcDrawingCodes = new ArrayList<String>();
                //A list of ValueCodes

                for (int j = 0; j < childList.getLength(); j++) { //Run within <ModellingClass
                    Node childNode = childList.item(j);
                    if (childNode.getNodeName().equals("Code")) {
                        mcCode = childNode.getTextContent();
                    }
                    if (childNode.getNodeName().equals("Version")) {
                        mcVersion = childNode.getTextContent();
                    }
                    if ("Translations".equals(childNode.getNodeName())) {
                        mcDesc = childNode.getTextContent().trim();
                    }
                    if (childNode.getNodeName().equals("Features")) {
                        //InnerchildList holds what's inside <Features> -> all the <Feature (NOT FeatureCode,UnitCode,OrderNumber,DimensionalDrawingCode,PortCode yet)
                        NodeList innerchildList = childNode.getChildNodes();
                        for (int j2 = 0; j2 < innerchildList.getLength(); j2++) { //Go through all Features
                            String fcode = "default fcode";
                            Node InnerchildNode2 = innerchildList.item(j2);
                            String fDrawingCode = "No_DC";
                            if (InnerchildNode2.getNodeName().equals("Feature")) {//This is needed because some Nodes have irrelevant names

//                                ArrayList<ValueProperty> Featproperties = new ArrayList<ValueProperty>();
                                NodeList innerchildList2 = InnerchildNode2.getChildNodes();
                                for (int s = 0; s < innerchildList2.getLength(); s++) { //Run within <Feature to find its code
                                    Node fNode = innerchildList2.item(s);

                                    if (fNode.getNodeName().equals("FeatureCode")) {
                                        mcFeatures.add(fNode.getTextContent()) ;
                                        fcode = fNode.getTextContent(); // Keep the Feature code
                                    }

                                    if (fNode.getNodeName().equals("DimensionalDrawingCode")) {
                                        mcDrawingCodes.add(fNode.getTextContent()) ;
                                        fDrawingCode = fNode.getTextContent();
                                    }

                                    if (fNode.getNodeName().equals("Values")) { // Find <Values>
                                        NodeList innerchildList3 = fNode.getChildNodes();
                                        for (int s1 = 0; s1 < innerchildList3.getLength(); s1++) {
                                            Node f2Node = innerchildList3.item(s1);
                                            if (f2Node.getNodeName().equals("Value")) {
                                                NodeList innerchildList4 = f2Node.getChildNodes();
                                                for (int s3 = 0; s3 < innerchildList4.getLength(); s3++) { //Run within <Value to find its code
                                                    Node f3Node = innerchildList4.item(s3);
                                                    if (f3Node.getNodeName().equals("ValueCode")) {
                                                        NodeList innerchildList5 = f3Node.getChildNodes();
                                                        if(!vCodes.contains(innerchildList5.item(0).getTextContent().trim())){ // if we havent created the Enumerator yet, create him along with the Feature that points to that Enumerator
                                                            vCodes.add(innerchildList5.item(0).getTextContent().trim());
                                                            try{
                                                                tx.createIndividual(innerchildList5.item(0).getTextContent().trim(),new ArrayList<>(), new ArrayList<>());
                                                                enumeratorCounter ++;
                                                                tx.createValueProperty(new ValueProperty(innerchildList5.item(0).getTextContent().trim(), Neo4JConstants.WEAVER_LABEL, "Enumerator_Value"));
                                                                tx.createRelationProperty(new RelationProperty(fcode + "feature", "hasEnumerator", innerchildList5.item(0).getTextContent().trim()));
                                                                tx.createRelationProperty(new RelationProperty(innerchildList5.item(0).getTextContent().trim(),"hasValue",innerchildList5.item(0).getTextContent().trim() + "value"));
                                                            }catch(org.neo4j.graphdb.MultipleFoundException MFex3){}

                                                        }else { // if the Enumerator exists just make the connection coming from Feature
                                                            try{
                                                                tx.createRelationProperty(new RelationProperty(fcode + "feature", "hasEnumerator", innerchildList5.item(0).getTextContent().trim()));
                                                            }catch(org.neo4j.graphdb.MultipleFoundException MFex3){}
                                                        }
                                                    }
                                                }//End of a <Value
                                            }
                                        }
                                    }
                                }//End of searching for the FeatureCode
                                if (fDrawingCode.equals("No_DC")){
                                    mcDrawingCodes.add("No_DC");
                                }
                            }//Checking for relevant names

                        }//End of All Features
                    }

                }//End of going through Modelling Classes

                //Create Modelling Class Node with Code and Version properties
                mcProperties.add(new ValueProperty(mcCode, "Code", mcCode));
                mcProperties.add(new ValueProperty(mcCode, "Version", mcVersion));
                mcProperties.add(new ValueProperty(mcCode, Neo4JConstants.WEAVER_LABEL, "Modelling_Class"));
                tx.createIndividual(mcCode, mcProperties, new ArrayList<>());

                modellingClassCounter ++;

                //Adding the description of the Modelling Class (if it exists)
                if (mcDesc != "No Description") {
                    tx.createValueProperty(new ValueProperty(mcCode,"Description",mcDesc));
                    logger.println("No description for Modelling CLass: " + mcCode);
                }
                for(int hf = 0; hf<mcFeatures.size();hf++){
                    try{ //Avoid duplicates(crashing)
//                        tx.createRelationProperty(new RelationProperty(mcCode, "hasFeature", mcFeatures.get(hf) + "feature"));
                        tx.createPropertyRelation(new RelationProperty(mcCode, "hasFeature", mcFeatures.get(hf) + "feature"),mcDrawingCodes.get(hf),"DrawingCode");
                    }catch(org.neo4j.graphdb.MultipleFoundException mFex3){
                        System.out.println("WTF");
                    }
                }

            }//End of going through ModellingClassesUnite.xml

            System.out.println("------    Modelling Class Nodes Created    --------");
            System.out.println("------    Enumerator Value Nodes Created   --------");

            logger.println("Modelling Class Nodes Created: " + modellingClassCounter);
            logger.println("Enumerator Value Nodes Created: " + enumeratorCounter);


            /*********************************
             *************ETIM Class**********
             *********************************/


//            Reading xml and parse it
            url = Resources.getResource("ClassesUnite.xml");
            text = Resources.toString(url, Charsets.UTF_8);

            DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
            DocumentBuilder builder = factory.newDocumentBuilder();
            Document document = builder.parse(new InputSource(new StringReader(text)));

            NodeList flowListClass = document.getElementsByTagName("Class");

            ArrayList<ETIMClass> etimClassArrayList = new ArrayList<ETIMClass>();

//            int etimClassCounter = 0;

            for (int i = 0; i < flowListClass.getLength(); i++) {
                NodeList childList = flowListClass.item(i).getChildNodes();
                ETIMClass etimClass = new ETIMClass();

                for (int j = 0; j < childList.getLength(); j++) {
                    Node childNode = childList.item(j);

                    if (childNode.getNodeType() == Node.ELEMENT_NODE){

//                        Getting code

                        if (childNode.getNodeName().equals("Code")) {
                            etimClass.setCode(childNode.getTextContent());
                        }

//                        Getting Version

                        if (childNode.getNodeName().equals("Version")) {
                            etimClass.setVersion(childNode.getTextContent());
                        }

//                        Getting Description

                        if (childNode.getNodeName().equals("Translations")) {
                            NodeList translationsList = childNode.getChildNodes();
                            for (int k = 0; k < translationsList.getLength(); k++) {
                                Node translation = translationsList.item(k);
                                if (translation.getNodeType() == Node.ELEMENT_NODE) {
                                    NodeList trans = translation.getChildNodes();
                                    for (int l = 0; l < trans.getLength(); l++) {
                                        Node description = trans.item(l);
                                        if (description.getNodeName().equals("Description")) {
                                            etimClass.setDescription(description.getTextContent());
                                        }
                                    }

                                }

                            }

                        } //ending ETIM Class node value population

//                        Getting modellingClasses

                        if (childNode.getNodeName().equals("ModellingClasses")) {
                            NodeList modellingClasses = childNode.getChildNodes();
                            for (int k = 0; k < modellingClasses.getLength(); k++) {
                                Node nodeModelling = modellingClasses.item(k);
                                if (nodeModelling.getNodeType() == Node.ELEMENT_NODE) {
                                    NodeList modelling = nodeModelling.getChildNodes();
                                    for (int l = 0; l < modelling.getLength(); l++) {
                                        Node modCode = modelling.item(l);
                                        if (modCode.getNodeType() == Node.ELEMENT_NODE) {

                                            /**
                                             * Create relation "extends" for Modeling Class and ETIM_Class
                                             */

                                            if (modCode.getNodeName().equals("Code")) {
                                                etimClass.setModellingClasse(modCode.getTextContent());
                                            }
                                        }
                                    }
                                }
                            }
                        } // ending relations with Modelling Class


//                        Getting features


                        if (childNode.getNodeName().equals("Features")) {
                            NodeList featuresClasses = childNode.getChildNodes();
                            ArrayList<String> valueArrayList = new ArrayList<String>();
                            ArrayList<String> featureArrayList = new ArrayList<String>();
                            for (int k = 0; k < featuresClasses.getLength(); k++) {
                                Node featureClass = featuresClasses.item(k);
                                if (featureClass.getNodeType() == Node.ELEMENT_NODE) {
                                    NodeList featureStuff = featureClass.getChildNodes();
                                    for (int l = 0; l < featureStuff.getLength(); l++) {
                                        Node featStuff = featureStuff.item(l);
                                        if (featStuff.getNodeType() == Node.ELEMENT_NODE) {

                                            /**
                                             * Get Values inside Features
                                             */


                                            if (featStuff.getNodeName().equals("Values")) {
                                                NodeList valuesList = featStuff.getChildNodes();
                                                for (int m = 0; m < valuesList.getLength(); m++) {
                                                    Node value = valuesList.item(m);
                                                    if (value.getNodeType() == Node.ELEMENT_NODE && value.getNodeName().equals("ValueCode")) {
                                                            valueArrayList.add(value.getTextContent().trim());
                                                    }
                                                }
                                            }

                                            /**
                                             * Get Feature Code inside features
                                             */

                                            if (featStuff.getNodeName().equals("FeatureCode")) {
//                                                System.out.println("Features");
//                                                System.out.println(featStuff.getTextContent());
                                                featureArrayList.add(featStuff.getTextContent());
                                            }
                                        }
                                    }
                                }
                            }
                            etimClass.setFeatures(featureArrayList);
                            etimClass.setValues(valueArrayList);
                        } //ending Feature Code
                    }
                }

                etimClassArrayList.add(etimClass);

            }


            /**
             * Create ETIM Class
             */



            for (ETIMClass etimClass:etimClassArrayList){
//                System.out.println(etimClass.toString());
                ArrayList<ValueProperty> etimClassProperty = new ArrayList<ValueProperty>();
                etimClassProperty.add(new ValueProperty("Label", Neo4JConstants.WEAVER_LABEL, "ETIM_Class"));
                etimClassProperty.add(new ValueProperty(etimClass.getCode(), "Version", etimClass.getVersion()));
                etimClassProperty.add(new ValueProperty(etimClass.getCode(), "Description", etimClass.getDescription() == null ? "NULL Description" : etimClass.getDescription()));

//                create ETIM Class
                tx.createIndividual(etimClass.getCode(), etimClassProperty, new ArrayList<>());
//                etimClassCounter ++;

//                create relations Modelling Class and ETIM Class
                if (etimClass.getModellingClasse() != null) {
                    try{ //Avoid duplicates(crashing)
                        tx.createRelationProperty(new RelationProperty(etimClass.getModellingClasse(), "extends", etimClass.getCode()));
                    }catch(org.neo4j.graphdb.MultipleFoundException MFex3){}
                }

//                create features relations
                if (etimClass.getFeatures() != null) {
                    for (String feature:etimClass.getFeatures()) {
                        try{ //Avoid duplicates(crashing)
                            tx.createRelationProperty(new RelationProperty(etimClass.getCode(), "hasFeature", feature + "feature"));
                        }catch(org.neo4j.graphdb.MultipleFoundException mfex3){}
                        // do not find this feature, so lets try to create
                        // just indicate that these feature does not exists on features file
                        catch (com.sysunite.weaver.connector.neo4j.IndividualNotFoundException infe){
                            ArrayList<ValueProperty> efProperties = new ArrayList<ValueProperty>();
                            efProperties.add(new ValueProperty(feature + "feature" , Neo4JConstants.WEAVER_LABEL, "Feature"));
                            efProperties.add(new ValueProperty(feature, "Description", "NULL Description"));
                            tx.createIndividual(feature + "feature" , efProperties, new ArrayList<>()); //FIXME something rare
                            try{ //Avoid duplicates(crashing)
                                tx.createRelationProperty(new RelationProperty(etimClass.getCode(), "hasFeature", feature + "feature"));
                            }catch(org.neo4j.graphdb.MultipleFoundException mfex3){}
//                            System.out.println(feature);
                        }

//                    create relations features and Enumerator Value
                        if (etimClass.getValues() != null) {
                            for (String value:etimClass.getValues()) {
                                try{ //Avoid duplicates(crashing)
                                    tx.createRelationProperty(new RelationProperty(feature, "hasEnumerator", value));
                                }catch(org.neo4j.graphdb.MultipleFoundException mfex3){}
                                // do not find this feature, so lets try to create
                                // just indicate that these feature does not exists on features file
                                catch (com.sysunite.weaver.connector.neo4j.IndividualNotFoundException infe){
                                    ArrayList<ValueProperty> enumeratorValues = new ArrayList<ValueProperty>();
                                    enumeratorValues.add(new ValueProperty("Label", Neo4JConstants.WEAVER_LABEL, "Enumerator_Value"));
                                    enumeratorValues.add(new ValueProperty(feature, "Description", "NULL Description"));
                                    tx.createIndividual(value , enumeratorValues, new ArrayList<>());
                                    try{ //Avoid duplicates(crashing)
                                        tx.createRelationProperty(new RelationProperty(feature, "hasFeature", value));
//                                        tx.createPropertyRelation(new RelationProperty(feature, "hasFeature", value),"LOL");
                                    }catch(org.neo4j.graphdb.MultipleFoundException mfex3){}
                                }
                            }
                        }
                    }

                }

            }

            System.out.println("------        ETIM Class Nodes Created        --------");
            logger.println("ETIM Class Nodes Created: " + etimClassArrayList.size());


        } catch (Exception ex) {
            ex.printStackTrace();
        }

        tx.commit();
        tx.close();

        logger.close();

        graphDb.shutdown();
    }
}
