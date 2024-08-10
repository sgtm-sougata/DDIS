/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package UserHome;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.Reader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import login.LoginController;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVRecord;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import root.GenerateSystemUID;
import root.GetConfig;
import root.ManageUser;
import root.MySqlConnect;
import root.SystemLog;

/**
 *
 * @author SURAJIT
 */
public class GlioblastomaClinilcalDataParse {

    Connection con = null;
    PreparedStatement pst = null;
    ResultSet rs = null;
    SystemLog sysLog;
    ManageUser mu;
    String projectID="",filePath="";
    String uid="";
    String log="";
    public GlioblastomaClinilcalDataParse(String projectID, String filePath){
        this.projectID = projectID;
        this.filePath = filePath;
        sysLog = new SystemLog();        
        String userid = LoginController.u_id;
        String utype = LoginController.utype;
        String username = LoginController.uname;
        uid = userid;
        mu = new ManageUser(userid,utype);
        
        try{
                //C:/Users/SURAJIT/Downloads/RadGlioHighA_DATA_2019-12-10_0653.csv
                Reader reader = Files.newBufferedReader(Paths.get(filePath),StandardCharsets.UTF_8);
                CSVParser csvParser = new CSVParser(reader, CSVFormat.DEFAULT.withFirstRecordAsHeader().withIgnoreHeaderCase().withTrim());
                List<String> headerName = csvParser.getHeaderNames();
                Map<String, String> patientIDMap = mu.getDeidenfitiedPatientIDMap();
                List<Map<String,List<String>>> attributeMap = mu.getChaviAttribute(projectID);
                //System.out.println(attributeMap.get(0).get("mr_number"));
                //System.out.println(mu.getMoleculeList().contains("MGMT"));
                List<String> moleculeList = mu.getMoleculeList();                
                int n = headerName.size();
                int k = 0;   
                JSONArray patientInfojsArray = new JSONArray();
                JSONArray diagnosisjsArray = new JSONArray();
                JSONArray stagejsArray = new JSONArray();
                JSONArray treatmentIntentjsArray = new JSONArray();
                JSONArray leafjsArray = new JSONArray();
                try{
                    GenerateSystemUID sysUID = new GenerateSystemUID();
                    JSONObject allPatientInfojsObj = new JSONObject();
                    allPatientInfojsObj.put("project", projectID);
                    for(CSVRecord csvRecords : csvParser){
                        //System.out.println("-------------------------------Start---------------------------------");
                        JSONObject patientInfoJSObj = new JSONObject();
                        JSONObject diagnosisJSObj = new JSONObject();
                        JSONObject stageJSObj = new JSONObject();
                        JSONObject leafJSObj = new JSONObject();
                        JSONObject treatmentIntentJSObj = new JSONObject();
                        JSONArray geneArr = new JSONArray();
                        while(k<n){      
                            if(!csvRecords.get(headerName.get(k)).isEmpty() && patientIDMap.get(csvRecords.get("mr_number")) != null){
                                String csvAttributeGeneric = headerName.get(k).split("__")[0];
                                List<String> mappedAttribute = attributeMap.get(0).get(csvAttributeGeneric);
                                String validData = "";
                                validData = (mappedAttribute==null)? moleculeList.contains(csvAttributeGeneric.toUpperCase())?csvAttributeGeneric.toUpperCase()+" : "+csvRecords.get(headerName.get(k)):null:mappedAttribute+" : "+csvRecords.get(headerName.get(k));
                                if(validData != null && mappedAttribute != null){
                                        int dayDifference = 0;                                       
                                        String chaviAttributeName = mappedAttribute.get(1);
                                        String tableName = mappedAttribute.get(0);
                                        String csvRecord = csvRecords.get(headerName.get(k));
                                        if(isDMYFormat(csvRecord) || isYMDFormat(csvRecord)){
                                            dayDifference = sysUID.getDayDiffrence(patientIDMap.get(csvRecords.get("mr_number")));
                                            //System.out.println("Day Difference: "+dayDifference);
                                        }
                                        String value = isDMYFormat(csvRecord)?sysUID.yyyyMMddTODate(sysUID.deidentifiedDate(dmyTOymd(csvRecord).replaceAll("\\-+","").replaceAll("\\/+", ""),dayDifference)):isYMDFormat(csvRecord)?sysUID.yyyyMMddTODate(sysUID.deidentifiedDate(csvRecord.replaceAll("\\-+","").replaceAll("\\/+", ""),dayDifference)):csvRecord;
                                        String chaviValue = getConfigureValue(chaviAttributeName,value).isEmpty()?value:getConfigureValue(chaviAttributeName,value);                                       
                                    if(mappedAttribute.get(0).equals("patientinfo"))
                                        patientInfoJSObj.put(chaviAttributeName, chaviValue);
                                    else if(mappedAttribute.get(0).equals("diagnosis"))
                                        diagnosisJSObj.put(chaviAttributeName,chaviValue);
                                    /*else if(mappedAttribute.get(0).equals("stageinformation"))
                                        stageJSObj.put(chaviAttributeName,chaviValue);*/ //There is not stage in Glioblastoma.
                                    else{
                                        JSONArray js = new JSONArray();
                                        js.add(chaviValue);
                                        js.add(tableName);
                                        leafJSObj.put(chaviAttributeName, js);
                                        
                                    }
                                }
                                if(validData != null && mappedAttribute == null){
                                    //System.out.println(mappedAttribute+" Here: "+validData
                                    JSONObject geneObj = new JSONObject();
                                    String geneCode = validData.split(":")[0].trim();
                                    String mutation_profile = validData.split(":")[1].trim();
                                    String mutation_result = getConfigureValue("expression_result",mutation_profile).isEmpty()?mutation_profile:getConfigureValue("expression_result",mutation_profile);
                                    //leafJSObj.put("expression_profile",geneArr.add(expression_result));
                                    geneObj.put("gene_tested", geneCode); //Key name should be same as the attributename of expressionprofile table.
                                    geneObj.put("expression_result",mutation_result);//Key name should be same as the attributename of expressionprofile table.
                                    geneArr.add(geneObj);
                                    //geneArr.add(expression_result);
                                    leafJSObj.put("expressionprofile",geneArr);
                                    //System.out.println("Gene: "+leafJSObj);
                                }
                                
                            }
                            k++;
                        }
                        if(patientInfoJSObj.size()>0){
                            //System.out.println("Table : "+table);
                            String patientid=csvRecords.get("mr_number");
                            //patientInfoJSObj.put("mr_number", patientid);
                            patientInfoJSObj.put("dcmpatientid", patientIDMap.get(patientid)); //key name should match with the attribute of patientinfo table in CHAVI databank
                            patientInfojsArray.add(patientInfoJSObj);
                            allPatientInfojsObj.put("patientinfo",patientInfojsArray);
                        }
                        if(diagnosisJSObj.size()>0){
                            //System.out.println(table);
                            String patientid=csvRecords.get("mr_number");
                            //diagnosisJSObj.put("mr_number", patientid);
                            diagnosisJSObj.put("dcmpatientid", patientIDMap.get(patientid));
                            diagnosisJSObj.put("objectid",csvRecords.get("redcap_repeat_instance"));
                            diagnosisjsArray.add(diagnosisJSObj);
                            allPatientInfojsObj.put("diagnosis",diagnosisjsArray);
                            
                            //Default stageinformation get from configuration.
                            //stageJSObj.put("mr_number", patientid);
                            stageJSObj.put("dcmpatientid", patientIDMap.get(patientid));
                            stageJSObj.put("overallstage", getConfigureValue("stage"));
                            stagejsArray.add(stageJSObj);
                            allPatientInfojsObj.put("stageinformation",stagejsArray);
                            
                            //Set default treatmentintent value
                            //treatmentIntentJSObj.put("mr_number", patientid);
                            treatmentIntentJSObj.put("dcmpatientid", patientIDMap.get(patientid));
                            treatmentIntentJSObj.put("treatmentintentvalue",getConfigureValue("treatmentintent"));
                            treatmentIntentjsArray.add(treatmentIntentJSObj);                            
                            allPatientInfojsObj.put("treatmentintent",treatmentIntentjsArray);                            
                        }
                        /*if(stageJSObj.size()>0){
                            //System.out.println(table);
                            String patientid = csvRecords.get("mr_number");
                            stageJSObj.put("sysPatientID", patientIDMap.get(patientid));
                            stageJSObj.put("objectid",csvRecords.get("redcap_repeat_instance"));
                            stagejsArray.add(stageJSObj);
                            allPatientInfojsObj.put("stageinformation", stagejsArray);
                        }*/
                        if(leafJSObj.size()>0){
                            //System.out.println(leafJSObj);
                            String patientid = csvRecords.get("mr_number");
                            //leafJSObj.put("mr_number", patientid);
                            leafJSObj.put("dcmpatientid", patientIDMap.get(patientid));
                            leafJSObj.put("objectid",csvRecords.get("redcap_repeat_instance"));
                            leafjsArray.add(leafJSObj);
                            allPatientInfojsObj.put("leafTable", leafjsArray);
                        }
                        System.out.println("EE: "+leafjsArray);
                        k=0;
                        //System.out.println("----------------------------------End------------------------------------");
                    }
                    if(log.length()>0)
                        sysLog.tempLog(uid, log);
                    else
                        writeJSONFile(System.getProperty("user.dir")+GetConfig.deidentifiedDCMSource,"Globlastoma_PatientEHR_"+projectID+"_"+System.currentTimeMillis()+".json",allPatientInfojsObj.toJSONString());
                    //System.out.println(allPatientInfojsObj);
                }catch(Exception ex){System.out.println("Err2340: "+ex.toString());}
        }catch(Exception ex){System.out.println("Err5425: "+ex.toString());}
        //System.out.println(mu.getChaviAttribute("TMC20190001"));
    }
    
    public void setUniformCSVValue(String String){
        
    }

    public void writeJSONFile(String folder,String fileName, String jsObj){
        try{
            File destFolder = new File(folder);
            if(! destFolder.exists())
                {
                    destFolder.mkdirs();
                } 
            FileWriter fr = new FileWriter(destFolder+"/"+fileName);
            BufferedWriter br = new BufferedWriter(fr);  
            br.write(jsObj);
            br.close();
            fr.close();  
        Alert msg = new Alert(Alert.AlertType.INFORMATION);
        msg.setTitle("CSV De-identification status");
        msg.setContentText("RadglioHigh (Glioblastoma) de-identified JSON file is created >>"+destFolder+"/Globlastoma_PatientEHR_"+projectID+"_(Timestamp)"+".json");
        msg.show();                     
        }catch(Exception ex){System.out.println("Er3454:"+ex.toString());}    
    }
    private String getConfigureValue(String attribute, String csvValue){
        String value="";
        JSONParser jsParser = new JSONParser();
        try{
            FileReader reader = new FileReader("RadglioASchemevalidation.json");
            Object obj = jsParser.parse(reader);
            JSONObject jsObj = (JSONObject) obj;
            //System.out.println(jsObj.size());
            JSONArray jsArray = (JSONArray) jsObj.get(attribute);
            JSONArray csvValues = (JSONArray) jsArray.get(0);
            JSONArray chaviValues = (JSONArray) jsArray.get(1);
            boolean flag = true;
            for(int i=0;i<csvValues.size();i++){
                if(csvValues.get(i).equals(csvValue)){
                    flag = false;
                    return chaviValues.get(i).toString();
                }
                if(!csvValues.get(i).equals(csvValue) && flag && (i==csvValues.size()-1)){
                    log += "\n------------------------------\n";
                    log+="AttributeName: "+ attribute+" \nExcepted input values from CSV: "+csvValues+"\nOutput values for CAHVI: "+chaviValues+"\nInput value: "+csvValue+"\n";
                    log+="------------------------------\n";
                    flag = false;
                }
            }
        }catch(Exception ex){return "";}
        return value;
    }
    private String getConfigureValue(String attribute){
        String value="";
        JSONParser jsParser = new JSONParser();
        try{
            FileReader reader = new FileReader("RadglioASchemevalidation.json");
            Object obj = jsParser.parse(reader);
            JSONObject jsObj = (JSONObject) obj;
            value = (String) jsObj.get(attribute);
        }catch(Exception ex){return "";}
        return value;
    }    
    public String dmyTOymd(String dmfFormateDate) throws java.text.ParseException{
        Date initDate = new SimpleDateFormat("dd-MM-yyyy").parse(dmfFormateDate);
        SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd");
        return formatter.format(initDate);    
    }
    public boolean isDMYFormat(String date){
        SimpleDateFormat dmy = new SimpleDateFormat("dd-MM-yyyy");
        dmy.setLenient(false);
        if(date==null)
            return false;
        try{
            Date dtdmy = dmy.parse(date);
            return true;
        }catch(Exception ex){return false;}
    }   
    public boolean isYMDFormat(String date){
    SimpleDateFormat ymd = new SimpleDateFormat("yyyy-MM-dd");
        ymd.setLenient(false);
        if(date==null)
            return false;
        try{
            Date dtymd = ymd.parse(date);
            return true;
        }catch(Exception ex){return false;}    
    }
}
