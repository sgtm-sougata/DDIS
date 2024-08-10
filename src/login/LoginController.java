/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package login;

import UserHome.GlioblastomaClinilcalDataParse;
import UserHome.UserInfo;
import java.io.IOException;
import java.io.Reader;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.sql.SQLException;
import java.util.Iterator;
import java.util.ResourceBundle;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVRecord;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;
import root.Crypto;
import root.DatabaseQuery;
import root.SystemLog;

/**
 *
 * @author Administrator
 */
public class LoginController implements Initializable {
    public static String uname="",utype="",u_id="";
    @FXML
    private TextField userid;
    @FXML
    private Button login;
    @FXML
    private PasswordField pass;
    
    @FXML
    private void loginAction(ActionEvent event) throws Exception{
        String uid="", password="",loginStatus="";
        int status=0;
        uid = userid.getText();
        password = new Crypto().encryptString(pass.getText());
        if(uid.equalsIgnoreCase("") || password.equalsIgnoreCase("")){
            Alert msg = new Alert(AlertType.INFORMATION);
            msg.setTitle("Login Satus");
            msg.setContentText("User ID or password is can not be empty!");
            msg.show();        
        }
        loginStatus=new UserInfo().login(uid,password);
        JSONParser parser = new JSONParser();
        JSONObject jsObj = (JSONObject)parser.parse(loginStatus);
        status = jsObj.size();
       if(status==0){
            Alert msg = new Alert(AlertType.INFORMATION);
             msg.setTitle("Login Satus");
             msg.setContentText("User ID or password is wrong!");
             msg.show();
        }
        else{
            JSONArray jsArray = new JSONArray();
            JSONObject jsObject = new JSONObject();
            jsArray = (JSONArray)jsObj.get("0");
            jsObject = (JSONObject)jsArray.get(0);
            uname = (String) jsObject.get("name");
            utype = (String) jsObject.get("utype");
            u_id = (String) jsObject.get("uid");
            ((Node)event.getSource()).getScene().getWindow().hide();
            if(utype.equalsIgnoreCase("U")){
                loadUserHome("/UserHome/Userhome.fxml");
            }
            else if(utype.equalsIgnoreCase("A"))
                loadUserHome("/AdministratorHome/Administratorhome.fxml");
            else{
                Alert msg = new Alert(AlertType.ERROR);
                msg.setTitle("Error Log");
                msg.setContentText("SK");
                msg.show();            
            }
            new SystemLog().otherLog(u_id, "Login successfully");
            //System.out.println(jsObject.toString());  //Print all the elements
        }
    }
    
    @Override
    public void initialize(URL url, ResourceBundle rb) {
        // TODO
        //insertIntocsvjsonattributemap();
        //insertMoleculeCatalog();
        //groupJSON("{\"attr1\":[\"val1\",\"table1\"],\"attr11\":[\"val11\",\"table1\"],\"attr3\":[\"val3\",\"table3\"],\"attr111\":[\"val11\",\"table1\"],\"attr2\":[\"val2\",\"table2\"],\"attr33\":[\"val2\",\"table3\"]}");
    }   
    
    public void loadUserHome(String fxmlPath) throws IOException{
        Parent root = FXMLLoader.load(getClass().getResource(fxmlPath));
        Scene scene = new Scene(root);
        Stage stage = new Stage(StageStyle.DECORATED);
        stage.setTitle("CHAVIRO -DICOM De-identification System");
        stage.setScene(scene);
        stage.show();        
    }
 
    
/*    
public void insertIntocsvjsonattributemap(){
        try{
                Reader reader = Files.newBufferedReader(Paths.get("C:/Users/SURAJIT/Downloads/INTELHOPEMapping.csv"),StandardCharsets.UTF_8);
                CSVParser csvParser = new CSVParser(reader, CSVFormat.DEFAULT.withFirstRecordAsHeader().withIgnoreHeaderCase().withTrim());
                try{
                    for(CSVRecord csvRecords : csvParser){
                        String IncludeingCHAVI = csvRecords.get("IncludeInCHAVI").trim();
                        String FieldType = csvRecords.get("FieldType").trim();
                        if(IncludeingCHAVI.equalsIgnoreCase("Y") && FieldType.equalsIgnoreCase("A")){
                            String redCapFieldname = csvRecords.get("RedCapFieldName").trim();
                            String chaviTablename = csvRecords.get("CHAVITableName").trim();
                            String chaviFieldname = csvRecords.get("CHAVIFieldName");
                            String parms[] = new String[4];
                            parms[0] = "TMC20190004";
                            parms[1] = chaviTablename;
                            parms[2] = chaviFieldname;
                            parms[3] = redCapFieldname;
                            DatabaseQuery db = new DatabaseQuery();
                            db.insertData("insert into csvjsonattributemap(projectid,tablename,chaviattribute,csvattribute) values(?,?,?,?)", parms, 4);
                            System.out.println(parms[0]+" : "+redCapFieldname+" : "+chaviTablename+" : "+chaviFieldname);
                        }
                    }
                }catch(Exception e){System.out.println("Err32462: "+e.toString());}
        }catch(Exception ex){System.out.println("Error34503: "+ex.toString());}
}   
  */  
  
/*public void insertMoleculeCatalog(){
        try{
                Reader reader = Files.newBufferedReader(Paths.get("C:/Users/SURAJIT/Downloads/cancer_gene_census.csv"),StandardCharsets.UTF_8);
                CSVParser csvParser = new CSVParser(reader, CSVFormat.DEFAULT.withFirstRecordAsHeader().withIgnoreHeaderCase().withTrim());
                try{
                    for(CSVRecord csvRecords : csvParser){
    
                            String geneSymbol = csvRecords.get("GeneSymbol").trim();
                            String geneName = csvRecords.get("Name").trim();
                            String tumorType = csvRecords.get("TumourTypes").trim();
                            String parms[] = new String[4];
                            parms[0] = geneSymbol;
                            parms[1] = "GENE";
                            parms[2] = geneName;
                            parms[3] = tumorType;
                            DatabaseQuery db = new DatabaseQuery();
                            db.insertData("insert into moleculescatalog(symbol,moleculetype,moleculename,tyumortype) values(?,?,?,?)", parms, 4);
                            System.out.println("GENE : "+geneSymbol+" : "+geneName+" : "+tumorType);
                        
                    }
                }catch(Exception e){System.out.println("Err522: "+e.toString());}
        }catch(Exception ex){System.out.println("Error0934: "+ex.toString());}
}   */  
    
/*public void groupJSON(String json){
    JSONParser parser = new JSONParser();
    JSONObject output = new JSONObject();
    try{
        output = (JSONObject) parser.parse(json);    
    }catch(Exception ex){}
    JSONObject groups = new JSONObject();
    JSONObject rows = new JSONObject();
    JSONArray jsArr = new JSONArray();
    //System.out.println(output);
    for(Iterator iterator = output.keySet().iterator();iterator.hasNext();){
        String key = (String) iterator.next();
        JSONArray js = (JSONArray) output.get(key);
        
        //System.out.println(js.get(0)+" : "+js);
        String tableName = (String) js.get(1);
            JSONObject jsObj = new JSONObject();
            jsObj.put(key, (String) js.get(0));
            //System.out.println("GG: "+rows.get(tableName));
        if(rows.get(tableName)!=null){
            JSONArray ss1 = (JSONArray) rows.get(tableName);            
            ss1.add(jsObj);
            //System.out.println("SS1: "+ss1);
            rows.put(tableName,ss1);
        }     
        if(rows.get(tableName)==null || rows.get(tableName).equals("")){
            JSONArray ss = new JSONArray();
            ss.add(jsObj);
            rows.put(tableName,ss);
        }
        
        //System.out.println(jsArr);
    }
    jsArr.add(rows);
    System.out.println(jsArr);
}  */
    
}
