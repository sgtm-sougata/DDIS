/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package UserHome;

import static UserHome.UserIndexController.fileProcess;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Reader;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.ResourceBundle;
import java.util.logging.Level;
import java.util.logging.Logger;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.concurrent.Task;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.CheckBoxTreeItem;
import javafx.scene.control.ComboBox;
import javafx.scene.control.DatePicker;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressBar;
import javafx.scene.control.ProgressIndicator;
import javafx.scene.control.SplitPane;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.control.TreeItem;
import javafx.scene.control.TreeView;
import javafx.scene.control.cell.CheckBoxTreeCell;
import javafx.scene.text.Text;
import javafx.stage.DirectoryChooser;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import javax.swing.JOptionPane;
import login.LoginController;
import static login.LoginController.u_id;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVRecord;
import org.apache.commons.io.FileUtils;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;
import root.Crypto;
import root.DatabaseQuery;
import root.GenerateSystemUID;
import root.GetConfig;
import root.ManageUser;
import root.PreProcessDICOM;
import root.SystemLog;

/**
 * FXML Controller class
 *
 * @author Surajit Kundu
 */
public class UserhomeController implements Initializable {

    @FXML
    private Label userName;
    @FXML
    private Button passChange;
    @FXML
    private TextField oldPassword;
    @FXML
    private TextField newPassword;
    public TextArea statusLogBody;
    @FXML
    private Button refreshDrive;
    @FXML
    private Button moveFilebtn;
    @FXML
    private TreeView<String> dicomList;
    @FXML
    private SplitPane splitSpane;
    @FXML
    private TextField age;
    @FXML
    private ComboBox<String> gender;
    @FXML
    private TextField dia_anato_loc;
    @FXML
    private TextField dia_dis_site;
    @FXML
    private TextField dia_patho;
    @FXML
    private TextField treat_intent;
    @FXML
    private ComboBox<String> surg_done;
    @FXML
    private DatePicker surg_date;
    @FXML
    private ComboBox<String> chemo_given;
    @FXML
    private TextField chemo_type;
    @FXML
    private TextField chemo_regimn_used;
    @FXML
    private TextField chemo_cycle;
    @FXML
    private DatePicker chemo_start_date;
    @FXML
    private DatePicker chemo_end_date;
    @FXML
    private ComboBox<String> radio_given;
    @FXML
    private ComboBox<String> radio_ebrt_given;
    @FXML
    private DatePicker radio_ebrt_start;
    @FXML
    private DatePicker radio_ebrt_end;
    @FXML
    private ComboBox<String> hormn_given;
    @FXML
    private TextField hormn_type;
    @FXML
    private TextField hormn_reg_used;
    @FXML
    private DatePicker hormn_start_date;
    @FXML
    private DatePicker hormn_end_date;
    @FXML
    private ComboBox<String> targt_given;
    @FXML
    private TextField targt_type;
    @FXML
    private DatePicker targt_start_date;
    @FXML
    private DatePicker targt_end_date;
    @FXML
    private TextField targt_reg_used;
    @FXML
    private ComboBox<String> immuno_given;
    @FXML
    private TextField immuno_type;
    @FXML
    private TextField immuno_reg_used;
    @FXML
    private DatePicker immuno_start_date;
    @FXML
    private DatePicker immuno_end_date;
    @FXML
    private Button saveData;
    @FXML
    private Button previewData;
    @FXML
    private Button editData;
    @FXML
    private ComboBox<String> driveList;
    @FXML
    private Label selectedDriveName;
    
    @FXML
    private ComboBox<String> projectList;
    @FXML
    private Button uploadJSON;
    @FXML
    private Label JSONfileName;
    
    Crypto crypto;
    SystemLog sysLog;
    ManageUser mu;
    
    static PreProcessDICOM fileProcess;
    File selectedFile;
    File selectedCSVFile;
    File selectedJSONFile;
    String selectedProject;
    Map<String, String> projectlist;
    
    public static double progressStatus = 0.0;
    public static double unzipStatus = 0.0;
    public static int totalFile = 0;
    public static int totalDeidentified = 0;
    public static int totalAlreadyDeidentified = 0;
    
    public int fileSize=10;
    public static JSONObject jsObj=null;
   // public JSONObject CSVtoJSONObj=null;
    public JSONArray CSVtoJSONArray=null;
    
    ObservableList<String> gen = FXCollections.observableArrayList("Male","Female","Transgender");
    ObservableList<String> yes_no = FXCollections.observableArrayList("Yes","No");
    @FXML
    private TextField patId;
    @FXML
    private Button startDEdicombtn;
    @FXML
    private ProgressIndicator preProcessStatus;
    @FXML
    private ProgressIndicator deIdentificationStatus;
    @FXML
    private Text totalDeDICOM;
    @FXML
    private Text totalAlDeDICOM;
    @FXML
    private Label csvFileName;
    @FXML
    private Button loadList;
    @FXML
    private Button csvFile;
    @FXML
    private Button deidentifyCSV;
    @FXML
    private Button mapProject;
    
    private void refreshDriveAction(ActionEvent event){
        ObservableList<String> driveLists = FXCollections.observableArrayList(getDriveList());
        driveList.setItems(driveLists);    
    }
    
    @FXML
    private void moveFilebtnAction(ActionEvent event){
        //    
    StringBuilder pathBuilder = new StringBuilder();
    for (TreeItem<String> item = dicomList.getSelectionModel().getSelectedItem();
        item != null ; item = item.getParent()) {

        pathBuilder.insert(0, item.getValue());
        pathBuilder.insert(0, "/");
    }
    //printLog(String.valueOf(move(new File(System.getProperty("user.dir")+pathBuilder.toString()), new File("C:/"))));
    String selectedDrive=driveList.getValue();
    if(selectedDrive != null || !selectedDrive.equals("")){
        selectedDriveName.setText(driveList.getValue());
        try{
            FileUtils.moveDirectory(new File(System.getProperty("user.dir")+pathBuilder.toString()), new File(selectedDrive+pathBuilder.toString()));   
            setDeIdentifiedDIR(); 
            Alert msg = new Alert(AlertType.INFORMATION);
            msg.setTitle("File moved");            
            msg.setContentText("Files moved to "+selectedDrive+pathBuilder.toString());
            msg.show();
        }catch(Exception ex){System.out.println(ex.toString());}
    }
    }
    
    @FXML
    private void changePassAction(ActionEvent event) throws Exception{
        String oldPass="",newPass="";
        UserInfo uInfo= new UserInfo();
        oldPass = oldPassword.getText();
        newPass = newPassword.getText();
        Alert msg = new Alert(AlertType.INFORMATION);
        msg.setTitle("Status");
        if(uInfo.changePassword(oldPass, newPass, LoginController.u_id))
            msg.setContentText("Password is changed!");
        else
            msg.setContentText("Try again!");
        msg.show();
    }
    @FXML
    private void previewDataAction(ActionEvent event){  // Preview button is clicked
        
    }
    @FXML
    private void editDataAction(ActionEvent event){     // Edit button is clicked
    
    }

    /**
     * Initializes the controller class.
     */
    @Override
    public void initialize(URL url, ResourceBundle rb) {
        String username = LoginController.uname;
        String uid = LoginController.u_id;
        String utype = LoginController.utype;        
        crypto = new Crypto();
        sysLog = new SystemLog();
        mu = new ManageUser(uid,utype);
        
        splitSpane.setDividerPosition(0, 0.13);
        gender.setItems(gen);
        surg_done.setItems(yes_no);
        chemo_given.setItems(yes_no);
        radio_given.setItems(yes_no);
        radio_ebrt_given.setItems(yes_no);
        hormn_given.setItems(yes_no);
        targt_given.setItems(yes_no);
        immuno_given.setItems(yes_no);
        //imageType.setItems(image_type);
        ObservableList<String> driveLists = FXCollections.observableArrayList(getDriveList());
        driveList.setItems(driveLists);

        ObservableList<String> projectsList = FXCollections.observableArrayList();
        projectlist = mu.getProjectList();
       // System.out.println(projectlist);
        ArrayList<String> projectCode = new ArrayList<String>(projectlist.keySet());
        ArrayList<String> projectLabel = new ArrayList<String>(projectlist.values());
        projectsList.addAll(projectLabel);
        projectList.setItems(projectsList);         
        
        /* Tree view of de-identified DICOM directory start here*/
        userName.setText("Welcome, \n"+username);  
        clearTempDir();
        setDeIdentifiedDIR();
        /* End here */

    } 
    
    @FXML
    private void loadListAction(ActionEvent event){
        setDeIdentifiedDIR();
    }
    
    private void initdicomDeAction(ActionEvent event){
        /* If patinet EHR data should be taken through the form, set this function on action of "Start de-identification process" button*/ 
        splitSpane.setDividerPosition(0, 1);
        progressStatus = 0;
        unzipStatus = 0;
        totalDeidentified = 0;
        totalAlreadyDeidentified = 0;
        totalFile = 0;
        Alert msg = new Alert(AlertType.INFORMATION);
        msg.setTitle("Require Patient Information");
        msg.setContentText("Please provide some basic patient information in the given form before deidentification");
        msg.show();        
    }   
    @FXML
    private void uploadJSONAction(ActionEvent event) throws IOException, InterruptedException, ParseException{
        FileChooser fileChooser = new FileChooser();
        fileProcess = new PreProcessDICOM();
        fileProcess.JSONfilterFileExtension(fileChooser);
        selectedJSONFile = fileChooser.showOpenDialog(null); 
        if(fileProcess.isValidFile(selectedJSONFile)){
            JSONfileName.setText(selectedJSONFile.getName());
            JSONParser parser = new JSONParser();
            Object obj = parser.parse(new FileReader(selectedJSONFile.getAbsolutePath()));
            jsObj =  (JSONObject) obj;            
        }
        else
            JSONfileName.setText("Choose a valid JSON file");         
    }
    @FXML
    private void projectListAction(ActionEvent event){
        csvFile.setDisable(false);
        selectedProject = String.valueOf(projectlist.keySet().toArray()[projectList.getSelectionModel().getSelectedIndex()]);        
    }
    @FXML
    private void uploadCSVAction(ActionEvent event) throws IOException, InterruptedException{
        FileChooser fileChooser = new FileChooser();
        fileProcess = new PreProcessDICOM();
        fileProcess.CSVfilterFileExtension(fileChooser);
        selectedCSVFile = fileChooser.showOpenDialog(null);
        csvFileName.setText(selectedCSVFile.getName());        
    }    
    @FXML
    private void deidentifyCSVAction(ActionEvent event) throws IOException, InterruptedException{
        /*FileChooser fileChooser = new FileChooser();
        fileProcess = new PreProcessDICOM();
        fileProcess.CSVfilterFileExtension(fileChooser);
        selectedCSVFile = fileChooser.showOpenDialog(null);*/
        String err ="";
        JSONObject CSVtoJSONObj=null, diagnosisObj=null, stageObj=null;
        JSONArray diagnosisArray=null, stageArray=null;
        JSONArray surgeryArray=null, radiotherapyArray=null,chemoArray=null,targetedtherapyArray=null,hormonetherapyArray=null;
        JSONObject csvLog = new JSONObject();
        JSONArray csvPatientID = new JSONArray();
        String selectedProject = projectList.getValue().split(":")[1].trim().replaceAll("\\(","").replaceAll("\\)","");
        if(fileProcess.isValidFile(selectedCSVFile) && !selectedProject.equals("GENL")){
            //System.out.println(selectedCSVFile.getAbsolutePath()+" : "+selectedProject);
            new ProjectWiseDeidentification(selectedProject, selectedCSVFile.getAbsolutePath());
        }        
        if(fileProcess.isValidFile(selectedCSVFile) && selectedProject.equals("GENL")){
            csvFileName.setText(selectedCSVFile.getName() + " loading...");
            String csvPath = selectedCSVFile.getAbsolutePath();
            GenerateSystemUID sysUID = new GenerateSystemUID();
            int bb=0;
            try{
                Reader reader = Files.newBufferedReader(Paths.get(csvPath));
                CSVParser csvParser = new CSVParser(reader, CSVFormat.DEFAULT.withFirstRecordAsHeader().withIgnoreHeaderCase().withTrim());
               
                CSVtoJSONArray = new JSONArray();
                int i =0; 
                String sysPatientID = "";
                int dayDifference = 0;
                try{
                for(CSVRecord csvRecords : csvParser){  
                    bb++;
                    String redcap_repeat_instrument = csvRecords.get("redcap_repeat_instrument").trim();
                    String mr_number = csvRecords.get("mr_number").replace("MR/", "");                     
                    //if(csvPatientID.size()>0)
                        //prev_mr_number = String.valueOf(csvPatientID.get(0)).replace("MR/", "");
                    //String prev_mr_number = String.valueOf(CSVtoJSONObj.get("mr_number")).replace("MR/", "");                     
                    //System.out.println(i+" : "+redcap_repeat_instrument+" : "+mr_number);
                    i++;
                    String redcap_repeat_instance = csvRecords.get("redcap_repeat_instance");                                         

                    if(redcap_repeat_instrument.equals("")){                    
                        //System.out.println(mr_number);
                        CSVtoJSONObj = new JSONObject();

                        diagnosisArray = new JSONArray();
                        stageArray = new JSONArray();
                        surgeryArray = new JSONArray();
                        chemoArray = new JSONArray();
                        radiotherapyArray = new JSONArray();
                        targetedtherapyArray = new JSONArray();
                        hormonetherapyArray = new JSONArray();
                        Map <String, String> patientinfoMap= mu.getAttributeMap("patientinfo", selectedProject);
                        ArrayList<String> chaviAttributeList = new ArrayList(patientinfoMap.keySet());                        
                        
                        sysPatientID = getDePatID(mr_number);
                        if(sysPatientID.equals("")){
                            csvPatientID.add(mr_number);
                        }
                              
                        if(sysPatientID.length()>0)
                            dayDifference = sysUID.getDayDiffrence(sysPatientID);
                        
                        if(!sysPatientID.equals("")){    
                            //CSVtoJSONObj.put("mr_number", mr_number);
                            System.out.println("---------------------PatientInfo Start ("+mr_number+")------------------");
                            CSVtoJSONObj.put("sysPatientID", sysPatientID);
                            CSVtoJSONObj.put("project",selectedProject);
                            
                        for(int m=0;m<patientinfoMap.size();m++){
                            try{
                                String patientinfoRecord = csvRecords.get(patientinfoMap.get(chaviAttributeList.get(m)));
                                System.out.println(chaviAttributeList.get(m)+" : "+patientinfoRecord);
                                patientinfoRecord = isDMYFormat(patientinfoRecord)?sysUID.deidentifiedDate(dmyTOymd(patientinfoRecord).replaceAll("\\-+","").replaceAll("\\/+", ""),dayDifference):isYMDFormat(patientinfoRecord)?sysUID.deidentifiedDate(patientinfoRecord.replaceAll("\\-+","").replaceAll("\\/+", ""),dayDifference):patientinfoRecord;
                                CSVtoJSONObj.put(chaviAttributeList.get(m), patientinfoRecord);
                                System.out.println(chaviAttributeList.get(m)+" (Deidentified) : "+patientinfoRecord);                            
                            }catch(Exception ex){System.out.println("Could not find attribute from stageinformation Err892: "+ex.toString());}
                        } 

                            System.out.println("sysPatientID: "+ sysPatientID);
                            
                            System.out.println("----------------------PatientInfo End-------------------------");
                        }
                        
                    }
                    
                    //System.out.println(CSVtoJSONObj.toString());
                    // DIAGNOSIS (diagnosis) START 
                    if(!sysPatientID.equals("")){
                       try{
                    if(redcap_repeat_instrument.equalsIgnoreCase("diagnosis")){
                        diagnosisObj = new JSONObject();
                        //System.out.println(mu.getAttributeMap("diagnosis", selectedProject));
                        Map <String, String> diagnosisMap= mu.getAttributeMap("diagnosis", selectedProject);
                        ArrayList<String> chaviAttributeList = new ArrayList(diagnosisMap.keySet());
                        System.out.println("----------------------Diagnosis start("+mr_number+")------------------------");
                        diagnosisObj.put("id", redcap_repeat_instance);
                        for(int m=0;m<diagnosisMap.size();m++){
                            //System.out.print(chaviAttributeList.get(m) + " : ");
                            //System.out.println(diagnosisMap.get(chaviAttributeList.get(m)));
                            try{
                                String diagnosisRecord = csvRecords.get(diagnosisMap.get(chaviAttributeList.get(m)));
                                System.out.println(chaviAttributeList.get(m)+" : "+ diagnosisRecord);
                                diagnosisRecord = isDMYFormat(diagnosisRecord)?sysUID.deidentifiedDate(dmyTOymd(diagnosisRecord).replaceAll("\\-+","").replaceAll("\\/+", ""),dayDifference):isYMDFormat(diagnosisRecord)?sysUID.deidentifiedDate(diagnosisRecord.replaceAll("\\-+","").replaceAll("\\/+", ""),dayDifference):diagnosisRecord;
                                diagnosisObj.put(chaviAttributeList.get(m), diagnosisRecord);
                                System.out.println(chaviAttributeList.get(m)+" (Deidentified) : "+diagnosisRecord);
                            }catch(Exception ex){System.out.println("column not found- Err003: "+ex.toString());}
                        }
                        System.out.println("----------------------Diagnosis End------------------------");
                        diagnosisArray.add(diagnosisObj);
                    }
                        }catch(Exception ex){
                           System.out.println("Err234728: "+ex.toString());
                       }
                    // DIAGNOSIS END 
                    
                    // STAGE INFORMATION START 
                    try{
                    if(redcap_repeat_instrument.equalsIgnoreCase("stage_information")){
                        stageObj = new JSONObject();
                        Map <String, String> stageMap = mu.getAttributeMap("stageinformation", selectedProject);
                        System.out.println("StegeMap: "+stageMap);
                        ArrayList<String> chaviAttributeList = new ArrayList(stageMap.keySet());
                        System.out.println("-----------------------StageInformation Start("+mr_number+")----------------------");
                        stageObj.put("id",redcap_repeat_instance);
                        for(int m=0;m<stageMap.size();m++){
                            try{
                                String stageRecord = csvRecords.get(stageMap.get(chaviAttributeList.get(m)));
                                System.out.println(chaviAttributeList.get(m)+" : "+stageRecord);
                                stageRecord = isDMYFormat(stageRecord)?sysUID.deidentifiedDate(dmyTOymd(stageRecord).replaceAll("\\-+","").replaceAll("\\/+", ""),dayDifference):isYMDFormat(stageRecord)?sysUID.deidentifiedDate(stageRecord.replaceAll("\\-+","").replaceAll("\\/+", ""),dayDifference):stageRecord;
                                stageObj.put(chaviAttributeList.get(m), stageRecord);
                                System.out.println(chaviAttributeList.get(m)+" (Deidentified) : "+stageRecord);
                            }catch(Exception ex){System.out.println("Could not find attribute from stageinformation Err892: "+ex.toString());}
                        }                        
                        System.out.println("-------------------------------StageInformation End-------------------------");
                        stageArray.add(stageObj);
                        if(diagnosisArray.size()>0){
                            JSONObject tempdiagnosis = (JSONObject) diagnosisArray.get(0); //set at first position
                            tempdiagnosis.put("stage",stageArray);
                        }
                        //diagnosisObj.put("stage",stageArray);
                        //diagnosisArray.add(diagnosisObj);
                    }
                    }catch(Exception ex){
                        System.out.println(ex.toString());}
                    // STAGE INFORMATION END                        
                    // SURGERY (surgery) START 
                    try{
                    if(redcap_repeat_instrument.equalsIgnoreCase("surgery")){
                        //System.out.println("stageObj"+stageObj);
                        //System.out.println("treatment_intent"+intentObj);
                        JSONObject surgeryObj = new JSONObject();
                        Map <String, String> surgeryMap = mu.getAttributeMap("surgery", selectedProject);
                        ArrayList<String> chaviAttributeList = new ArrayList(surgeryMap.keySet());
                        System.out.println("------------------------Surgery Start ("+mr_number+")----------------------------");
                        surgeryObj.put("id", redcap_repeat_instance);
                        for(int m=0;m<surgeryMap.size();m++){
                            try{
                                String surgeryRecord = csvRecords.get(surgeryMap.get(chaviAttributeList.get(m)));
                                System.out.println(chaviAttributeList.get(m)+" : "+surgeryRecord);
                                surgeryRecord = isDMYFormat(surgeryRecord)?sysUID.deidentifiedDate(dmyTOymd(surgeryRecord).replaceAll("\\-+","").replaceAll("\\/+", ""),dayDifference):isYMDFormat(surgeryRecord)?sysUID.deidentifiedDate(surgeryRecord.replaceAll("\\-+","").replaceAll("\\/+", ""),dayDifference):surgeryRecord;
                                surgeryObj.put(chaviAttributeList.get(m), surgeryRecord);
                                System.out.println(chaviAttributeList.get(m)+" (Deidentified) : "+surgeryRecord);                           
                            }catch(Exception ex){System.out.println("Could not found attribute from surgery Err2398: "+ex.toString());}
                        }                        
                        System.out.println("-------------------------Syrgery End-------------------------");
                        surgeryArray.add(surgeryObj);
                        //System.out.println(intentObj+" : "+intentArray+" : "+intentArray.size());
 
                        if(stageArray.size()>0){
                            JSONObject tempstageObj = (JSONObject) stageArray.get(0);
                            tempstageObj.put("surgery", surgeryArray);
                        }
                        else{
                            stageObj = new JSONObject();
                            stageObj.put("surgery", surgeryArray);                        
                        }                         
                        
                    }
                    }catch(Exception ex){                     
                        System.out.println("Err2308: "+ex.toString());}
                    // SURGERY END 
                    // CHEMO THERAPY (chemo_therapy) START 
                    try{
                    if(redcap_repeat_instrument.equalsIgnoreCase("chemo_therapy")){
                        JSONObject chemoObj = new JSONObject();
                        Map <String, String> chemoMap = mu.getAttributeMap("chemotherapy", selectedProject);
                        ArrayList<String> chaviAttributeList = new ArrayList(chemoMap.keySet());
                        System.out.println("-----------------------------Chemotherapy Start ("+mr_number+")--------------------------");
                        chemoObj.put("id",redcap_repeat_instance);
                        for(int m=0;m<chemoMap.size();m++){
                            try{
                                String chemoRecord = csvRecords.get(chemoMap.get(chaviAttributeList.get(m)));
                                System.out.println(chaviAttributeList.get(m)+" : "+chemoRecord);
                                chemoRecord = isDMYFormat(chemoRecord)?sysUID.deidentifiedDate(dmyTOymd(chemoRecord).replaceAll("\\-+","").replaceAll("\\/+", ""),dayDifference):isYMDFormat(chemoRecord)?sysUID.deidentifiedDate(chemoRecord.replaceAll("\\-+","").replaceAll("\\/+", ""),dayDifference):chemoRecord;
                                chemoObj.put(chaviAttributeList.get(m), chemoRecord);
                                System.out.println(chaviAttributeList.get(m)+" (Deidentified) : "+chemoRecord);
                            }catch(Exception ex){System.out.println("Could not found the attribute in chemotherapy Err5463: "+ex.toString());}
                        }
                        System.out.println("----------------------------Chemothrapy End----------------------------");
                        chemoArray.add(chemoObj);
                        //System.out.println(intentObj+" : "+intentArray+" : "+intentArray.size());
                        if(stageArray.size()>0){
                            JSONObject tempstageObj = (JSONObject) stageArray.get(0);
                            tempstageObj.put("chemotherapy", chemoArray);
                        }
                        else{
                            stageObj = new JSONObject();
                            stageObj.put("chemotherapy", chemoArray);                        
                        }                                                 
                        //System.out.println(mr_number+" : "+intentObj);
                    }
                    }catch(Exception ex){                   
                        System.out.println("err23482: "+ex.toString());}
                    // CHEMO THERAPY END 
                    // RADIOTHERAPY (radiotherapy) START 
                    try{
                    if(redcap_repeat_instrument.equalsIgnoreCase("radiotherapy")){
                        JSONObject radiotherapyObj = new JSONObject();
                        Map <String, String> radiotherapyMap = mu.getAttributeMap("radiotherapy", selectedProject);
                        ArrayList<String> chaviAttributeList = new ArrayList(radiotherapyMap.keySet());
                        System.out.println("-----------------------------Radiotherapy Start ("+mr_number+")-----------------------------");
                        radiotherapyObj.put("id", redcap_repeat_instance);
                        for(int m=0;m<radiotherapyMap.size();m++){
                            try{
                                String radiotherapyRecord = csvRecords.get(radiotherapyMap.get(chaviAttributeList.get(m)));
                                System.out.println(chaviAttributeList.get(m)+" : "+radiotherapyRecord);
                                radiotherapyRecord = isDMYFormat(radiotherapyRecord)?sysUID.deidentifiedDate(dmyTOymd(radiotherapyRecord).replaceAll("\\-+","").replaceAll("\\/+", ""),dayDifference):isYMDFormat(radiotherapyRecord)?sysUID.deidentifiedDate(radiotherapyRecord.replaceAll("\\-+","").replaceAll("\\/+", ""),dayDifference):radiotherapyRecord;
                                radiotherapyObj.put(chaviAttributeList.get(m), radiotherapyRecord);
                                System.out.println(chaviAttributeList.get(m)+" (Deidentified) : "+radiotherapyRecord);
                            }catch(Exception ex){System.out.println("Could not find the attribute in radiotherapy Er923: "+ex.toString());}
                        }                        
                        System.out.println("--------------------------------Radiotherapy End-----------------------------");
                        radiotherapyArray.add(radiotherapyObj);

                        if(stageArray.size()>0){
                            JSONObject tempstageObj = (JSONObject) stageArray.get(0);
                            tempstageObj.put("radiotherapy", radiotherapyArray);
                        }
                        else{
                            stageObj = new JSONObject();
                            stageObj.put("radiotherapy", radiotherapyArray);                        
                        }                          
                    }
                    }catch(Exception ex){                     
                        System.out.println("Err2348234: "+ex.toString());}
                    // RADIOTHERAPY END 
                    // TARGETED THERAPY (targeted_therapy) START
                    try{
                    if(redcap_repeat_instrument.equalsIgnoreCase("targeted_therapy")){
                        JSONObject targetedtherapyObj = new JSONObject();
                        Map <String, String> targetedtherapyMap = mu.getAttributeMap("targetedtherapy", selectedProject);
                        ArrayList<String> chaviAttributeList = new ArrayList(targetedtherapyMap.keySet());
                        System.out.println("------------------------------------Targetedtherapy Start ("+mr_number+")-------------------------------");
                        targetedtherapyObj.put("id", redcap_repeat_instance);
                        for(int m=0;m<targetedtherapyMap.size();m++){
                            try{
                                String targetedtherapyRecord = csvRecords.get(targetedtherapyMap.get(chaviAttributeList.get(m)));
                                System.out.println(chaviAttributeList.get(m)+" : "+targetedtherapyRecord);
                                targetedtherapyRecord = isDMYFormat(targetedtherapyRecord)?sysUID.deidentifiedDate(dmyTOymd(targetedtherapyRecord).replaceAll("\\-+","").replaceAll("\\/+", ""),dayDifference):isYMDFormat(targetedtherapyRecord)?sysUID.deidentifiedDate(targetedtherapyRecord.replaceAll("\\-+","").replaceAll("\\/+", ""),dayDifference):targetedtherapyRecord;
                                targetedtherapyObj.put(chaviAttributeList.get(m), targetedtherapyRecord);
                                System.out.println(chaviAttributeList.get(m)+" (Deidentified) : "+targetedtherapyRecord);
                            }catch(Exception ex){System.out.println("Could not find the attribute in targetedtherapy Err3457: "+ex.toString());}
                        }                        
                        System.out.println("---------------------------Targetedtherapy End-------------------------------");
                        targetedtherapyArray.add(targetedtherapyObj);
 
                        if(stageArray.size()>0){
                            JSONObject tempstageObj = (JSONObject) stageArray.get(0);
                            tempstageObj.put("targeted_therapy", targetedtherapyArray);
                        }
                        else{
                            stageObj = new JSONObject();
                            stageObj.put("targeted_therapy", targetedtherapyArray);                        
                        }                         
                    }
                    }catch(Exception ex){                    
                        System.out.println("Err024339: "+ex.toString());}
                    // TARGETED THERAPY END 
                    // HORMONE THERAPY (hormone_therapy) START 
                    try{
                    if(redcap_repeat_instrument.equalsIgnoreCase("hormone_therapy")){
                        JSONObject hormonetherapyObj = new JSONObject();
                        Map <String, String> hormonetherapyMap = mu.getAttributeMap("hormonetherapy", selectedProject);
                        ArrayList<String> chaviAttributeList = new ArrayList(hormonetherapyMap.keySet());
                        System.out.println("--------------------------Hormonetherapy Start ("+mr_number+")---------------------------------");
                        hormonetherapyObj.put("id", redcap_repeat_instance);
                        for(int m=0;m<hormonetherapyMap.size();m++){
                            try{
                                String hormonetherapyRecord = csvRecords.get(hormonetherapyMap.get(chaviAttributeList.get(m)));
                                hormonetherapyRecord = isDMYFormat(hormonetherapyRecord)?sysUID.deidentifiedDate(dmyTOymd(hormonetherapyRecord).replaceAll("\\-+","").replaceAll("\\/+", ""),dayDifference):isYMDFormat(hormonetherapyRecord)?sysUID.deidentifiedDate(hormonetherapyRecord.replaceAll("\\-+","").replaceAll("\\/+", ""),dayDifference):hormonetherapyRecord;
                                hormonetherapyObj.put(chaviAttributeList.get(m), hormonetherapyRecord);
                                System.out.println(chaviAttributeList.get(m)+" (Deidentified) : "+hormonetherapyRecord);
                            }catch(Exception ex){System.out.println("Could not find the attribute in hormonetherapy Er746: "+ex.toString());}
                        }
                        System.out.println("----------------------------Hormonetherapy End------------------------------");
                        hormonetherapyArray.add(hormonetherapyObj);
                        if(stageArray.size()>0){
                            JSONObject tempstageObj = (JSONObject) stageArray.get(0);
                            tempstageObj.put("hormonetherapy",hormonetherapyArray);
                        }
                        else{
                            stageObj = new JSONObject();
                            stageObj.put("hormonetherapy",hormonetherapyArray);                        
                        }                        
                    }
                    }catch(Exception ex){                     
                        System.out.println("Err3453408 : "+ex);}
                    // HORMONE THERAPY END  */                       
                        try{
                             CSVtoJSONObj.put("diagnosis",diagnosisArray);
                             System.out.println("DiagObj: "+diagnosisArray);
                         }catch(Exception ex){                    
                             System.out.println("Er2426: "+ex.toString());}                      
                   }
                   try{
                       //System.out.println("PatID : "+prev_mr_number+" : "+mr_number);
                    if(!sysPatientID.equals("") && redcap_repeat_instrument.equals("")){   
                           
                        //System.out.println(prev_mr_number+" : "+mr_number);                    
                        CSVtoJSONArray.add(CSVtoJSONObj);
                        //System.out.println(CSVtoJSONArray);
                    }
                    else{
                        csvLog.put("mr_number", csvPatientID);
                        csvLog.put("title", "Patient data could not be de-identified because system patient id is not found");                    
                    }
                   }catch(Exception ex){
                       System.out.println("Err345340 : "+ex);}
                }}catch(Exception ext){System.out.println("ERr3453453400: "+ext.toString());}
                String deIdentifiedFileName = String.valueOf(System.currentTimeMillis());
                writeJSONFile(System.getProperty("user.dir")+GetConfig.deidentifiedDCMSource,"PatientEHR_"+deIdentifiedFileName+".json",CSVtoJSONArray.toJSONString());
                writeJSONFile(System.getProperty("user.dir")+GetConfig.deidentifiedDCMSource,"Not_deintified_EHR_"+deIdentifiedFileName+".json",csvLog.toJSONString());
                System.out.println(CSVtoJSONArray);
                System.out.println(csvLog);
                //System.out.println();
                csvFileName.setText(selectedCSVFile.getName() + " loaded!");
            }catch(Exception ex){
                System.out.println(ex.toString());
                csvFileName.setText(selectedCSVFile.getName() + " error!");
                Alert msg = new Alert(AlertType.ERROR);
                msg.setTitle("90909 Error Log");
                msg.setContentText(ex.toString());
                msg.show(); 
            }   
        }
        else{
            csvFileName.setText("Choose a valid CSV file");
        }
    }
    
    @FXML
    private void saveDataAction(ActionEvent event) throws IOException, InterruptedException{     //Save button is clicked
        splitSpane.setDividerPosition(0, 0.13);
        if(jsObj == null)
            savePatientData();
        try {
            loadUserHome("/UserHome/UserIndex.fxml");
        } catch (IOException ex) {
            Logger.getLogger(UserIndexController.class.getName()).log(Level.SEVERE, null, ex);
        }   
        
        /*Thread unzipprogressStatusTh = new Thread(unzipProgress);
        unzipprogressStatusTh.setDaemon(true);
        unzipprogressStatusTh.start();*/
        //Thread.sleep(2000);

        /*Thread progressStatusTh = new Thread(deidentificationProgress);
        progressStatusTh.setDaemon(true);
        progressStatusTh.start(); */
        //Thread.sleep(500);
        /*Thread overallStatusTh1 = new Thread(overallStatus1);
        overallStatusTh1.setDaemon(true);
        overallStatusTh1.start();
        Thread overallStatusTh2 = new Thread(overallStatus2);
        overallStatusTh2.setDaemon(true);
        overallStatusTh2.start(); */ 
    Thread unzipProgress = new Thread(new Runnable(){
        @Override
        public void run(){
            Runnable progress = new Runnable(){
                @Override
                public void run(){
                    unzipProgress();
                }
            };
        while(unzipStatus<=1.0 && unzipStatus>=0){
            try{
                Thread.sleep(500);
            }catch(Exception ex){System.out.println("ERr0534531 : "+ex.toString());}
            Platform.runLater(progress);
        }
        }
    });
    unzipProgress.setDaemon(true);
    unzipProgress.start();        
    Thread deIdentProgress = new Thread(new Runnable(){
        @Override
        public void run(){
            Runnable progress = new Runnable(){
                @Override
                public void run(){
                    deProgress();
                }
            };
        while(progressStatus<=1.0 && progressStatus>=0){
            try{
                Thread.sleep(500);
            }catch(Exception ex){System.out.println("ERr0534532 : "+ex.toString());}
            Platform.runLater(progress);
        }
        }
    });
    deIdentProgress.setDaemon(true);
    deIdentProgress.start();
    Thread overallStatus1 = new Thread(new Runnable(){
        @Override
        public void run(){
            Runnable progress = new Runnable(){
                @Override
                public void run(){
                    overallStatus1();
                }
            };
        while(progressStatus<=1.0 && progressStatus>=0){
            try{
                Thread.sleep(500);
            }catch(Exception ex){System.out.println("ERr0534533 : "+ex.toString());}
            Platform.runLater(progress);
        }
        }
    });
    overallStatus1.setDaemon(true);
    overallStatus1.start();
    Thread overallStatus2 = new Thread(new Runnable(){
        @Override
        public void run(){
            Runnable progress = new Runnable(){
                @Override
                public void run(){
                    overallStatus2();
                }
            };
        while(progressStatus<=1.0 && progressStatus>=0){
            try{
                Thread.sleep(500);
            }catch(Exception ex){System.out.println("ERr0534534 : "+ex.toString());}
            Platform.runLater(progress);
        }
        }
    });
    overallStatus2.setDaemon(true);
    overallStatus2.start();    
    } 
    private void deProgress(){
        deIdentificationStatus.setProgress(progressStatus); 
    }
    private void unzipProgress(){
        preProcessStatus.setProgress(unzipStatus);
    }
    private void overallStatus1(){
        if(totalDeidentified>totalFile)
           totalDeidentified=totalFile;
        totalDeDICOM.setText(totalDeidentified+"/"+totalFile);    
    }
    private void overallStatus2(){
        if(totalAlreadyDeidentified>totalFile)
            totalAlreadyDeidentified = totalFile;
        totalAlDeDICOM.setText(totalAlreadyDeidentified+"/"+totalFile);
    }
/*    Task deidentificationProgress = new Task<Void>() {      //Thread creation type 1
        @Override protected Void call() throws Exception {
                
                while(progressStatus<=1.0 && progressStatus>=0){
                    Thread.sleep(500);
                    deIdentificationStatus.setProgress(progressStatus);     
                }
                deIdentificationStatus.setProgress(1.0);
            return null;
        }
    };  
    Task unzipProgress = new Task<Void>() {      //Thread creation type 2
        @Override protected Void call() throws Exception {
            while(progressStatus<=1.0 && progressStatus>=0){
                Thread.sleep(500);
                preProcessStatus.setProgress(unzipStatus);
            }
            preProcessStatus.setProgress(1.0);
            return null;
        }
    };  */
    /*Task overallStatus1 = new Task<Void>(){
        @Override protected Void call() throws Exception{
            while(progressStatus<=1.0 && progressStatus>=0){
                Thread.sleep(500);
                totalDeDICOM.setText(totalDeidentified+"/"+totalFile);
            }    
            return null;
        }
    };    
    Task overallStatus2 = new Task<Void>(){
        @Override protected Void call() throws Exception{
            while(progressStatus<=1.0 && progressStatus>=0){
                Thread.sleep(500);
                totalAlDeDICOM.setText(totalAlreadyDeidentified+"/"+totalFile);
            }    
            return null;
        }
    };*/
    
    public void loadUserHome(String fxmlPath) throws IOException{
        Parent root = FXMLLoader.load(getClass().getResource(fxmlPath));
        Scene scene = new Scene(root);
        Stage stage = new Stage(StageStyle.DECORATED);
        stage.setTitle("CHAVIRO -DICOM De-identification System");
        stage.setScene(scene);
        stage.show();        
    }     
    public static void clearTempDir(){
        try{
            FileUtils.cleanDirectory(new File(GetConfig.tempDirectory));
            //System.out.println(System.getProperty("user.dir")+"/"+GetConfig.tempDirectory);
        }catch(Exception ex){System.out.println("err248755: "+ex);}
    }    
    
    public void setDeIdentifiedDIR(){
        String deidentifiedDCMSource = GetConfig.deidentifiedDCMSource;
        //System.out.println(deidentifiedDCMSource);
        File dir =new File(System.getProperty("user.dir")+"/"+deidentifiedDCMSource);
        if(!dir.exists())
            dir.mkdirs();
        dicomList.setRoot(getNodesForDirectory(dir));
        dicomList.setShowRoot(false);    
    }
    public TreeItem<String> getNodesForDirectory(File directory) { 
        TreeItem<String> root = new TreeItem<String>(directory.getName());
        for(File f : directory.listFiles()) {
            //System.out.println("Loading " + f.getName());
            if(f.isDirectory()) { 
                root.getChildren().add(getNodesForDirectory(f));
            } else {
                root.getChildren().add(new TreeItem<String>(f.getName()));
            }
        }
        return root;
    }
    
    public void savePatientData() throws IOException{
        String patientID ="";
            patientID = patId.getText();
        if(patientID.equals(""))
            patientID = "patientData0000";
            
        jsObj = new JSONObject();
        jsObj.put("age", age.getText());
        jsObj.put("gender",gender.getValue());
        jsObj.put("dia_anato_loc",dia_anato_loc.getText());
        jsObj.put("dia_dis_site",dia_dis_site.getText());
        jsObj.put("dia_patho",dia_patho.getText());
        jsObj.put("treat_intent",treat_intent.getText());
        jsObj.put("surg_done",surg_done.getValue());
        jsObj.put("surg_date",surg_date.getValue());
        jsObj.put("chemo_given",chemo_given.getValue());
        jsObj.put("chemo_type",chemo_type.getText());
        jsObj.put("chemo_regimn_used",chemo_regimn_used.getText());
        jsObj.put("chemo_cycle",chemo_cycle.getText());
        jsObj.put("chemo_start_date",chemo_start_date.getValue());
        jsObj.put("chemo_end_date",chemo_end_date.getValue());
        jsObj.put("radio_given",radio_given.getValue());
        jsObj.put("radio_ebrt_given", radio_ebrt_given.getValue());
        jsObj.put("radio_ebrt_start", radio_ebrt_start.getValue());
        jsObj.put("radio_ebrt_end",radio_ebrt_end.getValue());
        jsObj.put("hormn_given",hormn_given.getValue());
        jsObj.put("hormn_type",hormn_type.getText());
        jsObj.put("hormn_reg_used",hormn_reg_used.getText());
        jsObj.put("hormn_start_date",hormn_start_date.getValue());
        jsObj.put("hormn_end_date", hormn_end_date.getValue());
        jsObj.put("targt_given", targt_given.getValue());
        jsObj.put("targt_given", targt_given.getValue());
        jsObj.put("targt_reg_used",targt_reg_used.getText());
        jsObj.put("targt_start_date", targt_start_date.getValue());
        jsObj.put("targt_end_date", targt_end_date.getValue());
        jsObj.put("immuno_given", immuno_given.getValue());
        jsObj.put("immuno_type", immuno_type.getText());
        jsObj.put("immuno_reg_used", immuno_reg_used.getText());
        jsObj.put("immuno_start_date", immuno_start_date.getValue());
        jsObj.put("immuno_end_date", immuno_end_date.getValue());

        //System.out.println(patientAge+" : "+patientGender+" : "+diago_anato_loc+" : "+diago_dis_site+" : "+diago_patho+" : "+treatment_intent);
        //System.out.println(surgery_date+" : "+surgery_done+" : "+chemotherapy_given+" : "+chemotherapy_type);
    }
    public ArrayList<String> getDriveList(){
        ArrayList<String> list = new ArrayList<String>();
        File[] drives = File.listRoots();
        if (drives != null && drives.length > 0) {
            for (File aDrive : drives) {
                list.add(String.valueOf(aDrive));
            }
        }
        return list;
    }
    public String getDePatID(String mrn_number){ // Fetch CHAVIRO patient id from the mrn_number
        String chaviID="";
        String sql = "select SysPatientID from patientid_map where DCMPatientID=?";
        String parms[] = new String[1];
        parms[0] = mrn_number;
        //System.out.println("MRDDB : "+mrn_number);
        try{
            chaviID = crypto.decryptString(new DatabaseQuery().getSingleRecord(sql,parms,1));
            //System.out.println("CHAVIID : "+chaviID);
        }catch(Exception ex){
            sysLog.errorLog(u_id, ex.toString());
            Alert msg = new Alert(AlertType.ERROR);
            msg.setTitle("Error Log");
            msg.setContentText(ex.toString());
            msg.show();             
        }
        return chaviID;
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
        Alert msg = new Alert(AlertType.INFORMATION);
        msg.setTitle("CSV De-identification status");
        msg.setContentText("New de-identified JSON file is created >>"+destFolder+"/Patient_EHR_"+String.valueOf(System.currentTimeMillis())+".json");
        msg.show();                     
        }catch(Exception ex){System.out.println("Er3454:"+ex.toString());}    
    }
    public String dmyTOymd(String dmfFormateDate) throws java.text.ParseException{
        Date initDate = new SimpleDateFormat("dd-MM-yyyy").parse(dmfFormateDate);
        SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd");
        return formatter.format(initDate);    
    }
    public boolean isDMYFormat(String date){
        SimpleDateFormat sdf = new SimpleDateFormat("dd-MM-yyyy");
        sdf.setLenient(false);
        if(date==null)
            return false;
        try{
            Date dt = sdf.parse(date);
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
    
    @FXML
    private void mapProjectAction(ActionEvent event) {
        try {
            loadUserHome("/UserHome/MapPatientProject.fxml");
        } catch (IOException ex) {
            Logger.getLogger(UserhomeController.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
}
