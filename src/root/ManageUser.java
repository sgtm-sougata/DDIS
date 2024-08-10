/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package root;

/**
 *
 * @author Surajit Kundu
 */
import AdministratorHome.AdministratorhomeController;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import AdministratorHome.AdministratorhomeController.UserRegistration;
import AdministratorHome.ProjectTemplateController;
import java.io.FileReader;
import java.sql.ResultSetMetaData;
import java.sql.Statement;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Vector;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;
//import sk.dids.com.AdminHome.UserRegistration;

/**
 *
 * @author SURAJIT
 */
public class ManageUser{
    Connection con = null;
    PreparedStatement pst = null;
    ResultSet rs = null;
    SystemLog sysLog;
    Crypto cr = null;
    String uid="",utype="";
    
    public ManageUser(String uid,String utype){
        this.uid = uid;
        this.utype = utype;
        sysLog = new SystemLog();
        cr = new Crypto();
        //JOptionPane.showMessageDialog(null,uid);
    }
 
    public boolean createNewUser(UserRegistration userReg){
        String name="",emailid="",contact="",password="",usertype="",loginuid="";
        final int status = 1;
        boolean insertationStatus=false;
        loginuid = userReg.loginid;
        name = userReg.name;
        emailid = userReg.emailid;
        contact = userReg.contact;
        password = userReg.pass;
        usertype = userReg.utype;
        con = MySqlConnect.connectMysql();
        String sql = "insert into login (uid,pass,name,emailid,contact,utype,status) values(?,?,?,?,?,?,?)";
        try{
            pst = con.prepareStatement(sql);
            pst.setString(1, loginuid);
            pst.setString(2, cr.encryptString(password));
            pst.setString(3, name);
            pst.setString(4, emailid);
            pst.setString(5, contact);
            pst.setString(6, usertype);
            pst.setInt(7, status);
            int i = pst.executeUpdate();
            if(i>0){
                insertationStatus = true;
                sysLog.sqlLog(uid, sql);
            }
        }catch(Exception ex){
            System.out.println(ex.toString());
            
            sysLog.errorLog(uid, ex.toString());
            insertationStatus = false;
        }
        finally{
            try{
            con.close();
            pst.close();
            }catch(Exception ex){}
        }
        return insertationStatus;
    }
    public boolean createNewProject(AdministratorhomeController.ProjectRegistration preg){
        String name="", projectid="", approvalno="", approvaldate="", centerlist="",piname="",scientifictitle="",countrieslist="",projectabstract="";
        boolean isAnimalImage; 
        boolean insertationStatus=false;
        name = preg.name;
        projectid = preg.projectid;
        approvalno = preg.approvalno;
        approvaldate = preg.approvaldate;
        centerlist = preg.centerlist;
        piname = preg.piname;
        scientifictitle = preg.scientifictitle;
        countrieslist = preg.countrieslist;
        projectabstract = preg.projectabstract;
        isAnimalImage = preg.isAnimalImage;
        
        con = MySqlConnect.connectMysql();
        String sql = "insert into project (projectname,projectid,approvalno,approvaldate,centerlist,piname,countrieslist,scientifictitle, projectabstract, iscontainanimalimage) values(?,?,?,?,?,?,?,?,?,?)";
        try{
            pst = con.prepareStatement(sql);
            pst.setString(1, name);
            pst.setString(2, projectid);
            pst.setString(3, approvalno);
            pst.setString(4, approvaldate);
            pst.setString(5, centerlist);
            pst.setString(6, piname);
            pst.setString(7, countrieslist);
            pst.setString(8, scientifictitle);
            pst.setString(9, projectabstract);
            pst.setBoolean(10, isAnimalImage);
            
            int i = pst.executeUpdate();
            if(i>0){
                insertationStatus = true;
                sysLog.sqlLog(uid, sql);
            }
        }catch(Exception ex){
            System.out.println(ex.toString());
            
            sysLog.errorLog(uid, ex.toString());
            insertationStatus = false;
        }
        finally{
            try{
            con.close();
            pst.close();
            }catch(Exception ex){}
        }
        return insertationStatus;        
    } 
    public boolean createProjectTemplate(ProjectTemplateController.CreateTemplate newTemplate){
        //.out.println(newTemplate.project);
        //System.out.println(newTemplate.table.get(0));
        //System.out.println(newTemplate.attribute.get(0));
        String project = newTemplate.project;
        Vector tableObject = newTemplate.table;
        Vector attributeObject = newTemplate.attribute;
        int objSize = tableObject.size(); //The size of tableObject and attributeObject is always equal. They are one to one.
        
        String values="";
        for(int i=0;i<objSize-1;i++){
            values += "('"+project+"','"+attributeObject.get(i)+"','"+tableObject.get(i)+"'),";
        }
        values += "('"+project+"','"+attributeObject.get(objSize-1)+"','"+tableObject.get(objSize-1)+"')";
        String sql = "insert into projecttemplate(project_id,chavi_attribute,chavi_table) values"+values+"";
        System.out.println(sql);
        con = MySqlConnect.connectMysql();
        try{
            Statement st = con.createStatement();
            int i = st.executeUpdate(sql);
            if(i>0)
                return true;
            st.close();
        }catch(Exception ex){
            System.out.println("Err345479: "+ex.toString());
            sysLog.errorLog(uid, ex.toString());            
            try {
                con.close();
            } catch (SQLException ex1) {
                Logger.getLogger(ManageUser.class.getName()).log(Level.SEVERE, null, ex1);
            }
            return false;
        }
        finally{
            try {
                con.close();
            } catch (SQLException ex) {
                Logger.getLogger(ManageUser.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        return false;
    }
    public ArrayList<String> getOtherAttributeName(){
        ArrayList<String> attributeList = new ArrayList<String>();
        con = MySqlConnect.connectMysql();
        String sql = "select attributename from projectTemplateOtherAttribute";
        try{
            pst = con.prepareStatement(sql);
            rs = pst.executeQuery();
            sysLog.sqlLog(uid, sql);
            while(rs.next())
                attributeList.add(rs.getString("attributename"));
        }catch(Exception ex){
            sysLog.errorLog(uid, ex.toString());
        }
        finally{
            try{
                con.close();
                pst.close();
                rs.close();
            }catch(SQLException ex){}
        }        
        return attributeList;           
    }
    public boolean isProjectExist(String projectid, String approvalno){
        con = MySqlConnect.connectMysql();
        String sql = "select * from project where projectid=? or approvalno=?";
        try{
            pst = con.prepareStatement(sql);
            pst.setString(1, projectid);
            pst.setString(2,approvalno);
            rs = pst.executeQuery();
            sysLog.sqlLog(uid, sql);
            if(rs.next())
                return true;
        }catch(Exception ex){
            sysLog.errorLog(uid, ex.toString());
            return false;
        }
        finally{
            try{
                con.close();
                pst.close();
                rs.close();
            }catch(SQLException ex){}
        }        
        return false;        
    } 
    public Map<String,String> getProjectList(){
        Map<String, String> projectList = new HashMap();
        con = MySqlConnect.connectMysql();
        String sql = "select * from project"; 
        
        try{
            pst = con.prepareStatement(sql);
            rs = pst.executeQuery();
            sysLog.sqlLog(uid, sql);
            while(rs.next()){
                projectList.put(rs.getString("projectid"),rs.getString("projectname")+": ("+rs.getString("projectid")+")");
            }
        }catch(Exception ex){
            sysLog.errorLog(uid, ex.toString());
        }
        finally{
            try{
                con.close();
                pst.close();
                rs.close();
            }catch(SQLException ex){}
        }        
        return projectList;
    }
    public Map<String,String> getDiseaseSiteList(){
        Map<String, String> diseaseSiteList = new HashMap();
        con = MySqlConnect.connectMysql();
        String sql = "select * from diseasesite order by name"; 
        
        try{
            pst = con.prepareStatement(sql);
            rs = pst.executeQuery();
            sysLog.sqlLog(uid, sql);
            while(rs.next()){
                diseaseSiteList.put(rs.getString("shortname"),rs.getString("name"));
            }
        }catch(Exception ex){
            sysLog.errorLog(uid, ex.toString());
        }
        finally{
            try{
                con.close();
                pst.close();
                rs.close();
            }catch(SQLException ex){}
        }        
        return diseaseSiteList;
    }
    public String getCSVMRNumField(String mr_number,String projectID){
        String csvAttributeName = "";
        String sql = "select csvattribute from csvjsonattributemap where projectid=? and chaviattribute=?";
        try{
            pst = con.prepareStatement(sql);
            pst.setString(1, projectID);
            pst.setString(2, mr_number);
            rs = pst.executeQuery();
            sysLog.sqlLog(uid, sql);
            if(rs.next()){
                csvAttributeName = rs.getString("csvattribute");
            }
        }catch(Exception ex){
            sysLog.errorLog(uid, ex.toString());
            System.out.println("Err025322: "+ex.toString());
        }
        finally{
            try{
                con.close();
                pst.close();
                rs.close();
            }catch(SQLException ex){}
        }           
        return csvAttributeName;
    }
    public JSONArray getProjectListJSON(){
        JSONArray projectList = null;
        con = MySqlConnect.connectMysql();
        String sql = "select * from project limit 0,400"; 
    
        try{
            pst = con.prepareStatement(sql);
            rs = pst.executeQuery();
            sysLog.sqlLog(uid, sql);
            ResultSetMetaData metaData = rs.getMetaData();
            int count = metaData.getColumnCount(); //number of column
            String columnName[] = new String[count];  
            JSONArray jsArray = new JSONArray();
            while(rs.next()){
                JSONObject jsObj = new JSONObject();
                for (int i = 0; i < count; i++)
                {
                   columnName[i] = metaData.getColumnLabel(i+1);
                   //System.out.println(columnName[i]);
                   jsObj.put(columnName[i], rs.getString(columnName[i]));
                }
                jsArray.add(jsObj);
            }
            projectList = jsArray;
        }catch(Exception ex){
            sysLog.errorLog(uid, ex.toString());
        }
        finally{
            try{
                con.close();
                pst.close();
                rs.close();
            }catch(SQLException ex){}
        }  
        return projectList;
    }
    public ArrayList<String> getPatientList(){
        ArrayList<String> patientList = new ArrayList<String>();
        con = MySqlConnect.connectMysql();
        String sql = "select * from patientid_map limit 0,2000"; 
        
        try{
            pst = con.prepareStatement(sql);
            rs = pst.executeQuery();
            sysLog.sqlLog(uid, sql);
            while(rs.next()){
                patientList.add(rs.getString("DCMPatientID"));
            }
        }catch(Exception ex){
            sysLog.errorLog(uid, ex.toString());
            System.out.println("Err02639: "+ex.toString());
        }
        finally{
            try{
                con.close();
                pst.close();
                rs.close();
            }catch(SQLException ex){}
        }   
        System.out.println("patientList: "+patientList);
        return patientList;        
    }
    public String getDeidentifiedPatientID(String originalPatientID){
        String deIdentifiedID = "";
        con = MySqlConnect.connectMysql();
        String sql = "select SysPatientID from patientid_map where DCMPatientID=?";
        try{
            pst = con.prepareStatement(sql);
            pst.setString(1, originalPatientID);
            rs = pst.executeQuery();
            sysLog.sqlLog(uid, sql);
            if(rs.next()){
                deIdentifiedID = rs.getString("SysPatientID");
            }
        }catch(Exception ex){
            sysLog.errorLog(uid, ex.toString());
            System.out.println("Err023279: "+ex.toString());
        }
        finally{
            try{
                con.close();
                pst.close();
                rs.close();
            }catch(SQLException ex){}
        }           
        return deIdentifiedID; 
    }
    public boolean mapProjectPatient(String projectid, String syspatientid){
        con = MySqlConnect.connectMysql();
        String sql = "insert into projectmap(projectid, patientid) values(?,?)";
        try{
            pst = con.prepareStatement(sql);
            pst.setString(1, projectid);
            pst.setString(2, syspatientid);
            int i = pst.executeUpdate();
            sysLog.sqlLog(uid, sql);
            if(i>0)
                return true;
        }catch(Exception ex){
            System.out.println("Er823402: "+ex.toString());
            sysLog.errorLog(uid, "Er823402: "+ex.toString());
            return false;
        }
        finally{
            try{
                con.close();
                pst.close();
            }catch(SQLException ex){}
        }  
        return false;
    }
    public boolean setUserActive(String uid){
        con = MySqlConnect.connectMysql();
        String sql = "update login set status=? where uid=?";
        try{
            pst = con.prepareStatement(sql);
            pst.setInt(1, 1);
            pst.setString(2,uid);
            int i = pst.executeUpdate();
            if(i>0)
                return true;
        }catch(Exception ex){
            System.out.println("Err012300 : "+ex.toString());
            sysLog.errorLog(uid, "Err012300 : "+ex.toString());
            return false;
        }
        finally{
            try{
                con.close();
                pst.close();
            }catch(SQLException ex){}
        }        
        return false;
    }
    public boolean setUserDeactive(String uid){
        con = MySqlConnect.connectMysql();
        String sql = "update login set status=? where uid=?";
        try{
            pst = con.prepareStatement(sql);
            pst.setInt(1, 0);
            pst.setString(2, uid);
            int i = pst.executeUpdate();
            if(i>0)
                return true;
        }catch(Exception ex){
            System.out.println("Err012301 : "+ex.toString());
            sysLog.errorLog(uid, "Err012301 : "+ex.toString());
            return false;
        }
        finally{
            try{
                con.close();
                pst.close();
            }catch(SQLException ex){}
        }        
        return false;
    }    
    public boolean isUserExist(String email, String contact){
        con = MySqlConnect.connectMysql();
        String sql = "select * from login where emailid=? or contact=?";
        try{
            pst = con.prepareStatement(sql);
            pst.setString(1, email);
            pst.setString(2,contact);
            rs = pst.executeQuery();
            sysLog.sqlLog(uid, sql);
            if(rs.next())
                return true;
        }catch(Exception ex){
            sysLog.errorLog(uid, ex.toString());
            return false;
        }
        finally{
            try{
                con.close();
                pst.close();
                rs.close();
            }catch(SQLException ex){}
        }        
        return false;
    }
    
    
    
    public ResultSet getUserList(){
        ResultSet userList = null;
        con = MySqlConnect.connectMysql();
        String sql = "select uid,name,emailid,contact,utype from login limit 0,200";
        try{
            pst = con.prepareStatement(sql);
           userList = pst.executeQuery();
            if(userList.next())
               return userList;
            else
                return null;
           
        }catch(Exception ex){System.out.println("getUserlist() Err0882 : "+ex);}
       /* finally{
            try{  
                con.close();
                pst.close();
                userList.close();
               // rs.close();
            }catch(SQLException ex){}
        }*/        
        return userList;
    }
    public ResultSet getMoleculeSet(){
        ResultSet moleculeList = null;
        con = MySqlConnect.connectMysql();
        String sql = "select symbol,moleculetype,moleculename from moleculescatalog limit 0,3000";
        try{
            pst = con.prepareStatement(sql);
           moleculeList = pst.executeQuery();
            if(moleculeList.next())
               return moleculeList;
            else
                return null;
        }catch(Exception ex){System.out.println("getMoleculeList() Err02390 : "+ex);}
        return moleculeList;    
    }
    public ResultSet getFilteredUserList(int i) throws SQLException{
        ResultSet userList = null;
        con = MySqlConnect.connectMysql();
        String sql = "";
        //System.out.println("Case : "+i);
        switch(i){
            case 0: 
                sql = "select uid,name,emailid,contact,utype from login";
                pst = con.prepareStatement(sql);
                break;
            case 1:
                sql = "select uid,name,emailid,contact,utype from login where utype=?";
                pst = con.prepareStatement(sql); 
                pst.setString(1,"U");
                break;
            case 2:
                sql = "select uid,name,emailid,contact,utype from login where utype=?";
                pst = con.prepareStatement(sql);
                pst.setString(1,"A");
                break;
            case 3:
                sql = "select uid,name,emailid,contact,utype from login where status=?";
                pst = con.prepareStatement(sql);
                pst.setInt(1, 1);
                break;
            case 4:
                sql = "select uid,name,emailid,contact,utype from login where status=?";
                pst = con.prepareStatement(sql);
                pst.setInt(1, 0);
                break;
            default:
                sql = "select uid,name,emailid,contact,utype from login";
                pst = con.prepareStatement(sql);
                break;
        }
        userList = pst.executeQuery();        
        return userList;
    }    
    public boolean changePassword(String oldPass, String newPass){
        con = MySqlConnect.connectMysql();
        String sql = "update login set pass=? where uid=? and pass=?";
        try{
            pst = con.prepareStatement(sql);
            pst.setString(1, cr.encryptString(newPass));
            pst.setString(2, uid);
            pst.setString(3,cr.encryptString(oldPass));
            int i = pst.executeUpdate();
            sysLog.sqlLog(uid,sql);
            if(i>0){
                return true;
            }
            else{
                return false;
            }
        }catch(Exception ex){
            sysLog.errorLog(uid,ex.toString());
            return false;
        }
        finally{
            try{
                con.close();
                pst.close();
            }catch(SQLException ex){}
        }    
    }
    public boolean changePassword(String oldPass, String newPass,String uid){
        con = MySqlConnect.connectMysql();
        String sql = "update login set pass=? where uid=? and pass=?";
        try{
            pst = con.prepareStatement(sql);
            pst.setString(1, cr.encryptString(newPass));
            pst.setString(2, uid);
            pst.setString(3,cr.encryptString(oldPass));
            int i = pst.executeUpdate();
            sysLog.sqlLog(uid,sql);
            if(i>0){
                return true;
            }
            else{
                return false;
            }
        }catch(Exception ex){
            sysLog.errorLog(uid,ex.toString());
            return false;
        }
        finally{
            try{
                con.close();
                pst.close();
            }catch(SQLException ex){}
        }
    } 
    
    public Map<String,String> getAttributeMap(String tableName, String projectID){
        Map<String, String> attributeMapList = new HashMap();
        String sql = "select * from csvjsonattributemap where tablename=? and projectid=?"; 
        con = MySqlConnect.connectMysql();
        try{
            pst = con.prepareStatement(sql);
            pst.setString(1, tableName);
            pst.setString(2, projectID);
            rs = pst.executeQuery();
            sysLog.sqlLog(uid, sql);
            while(rs.next()){
                attributeMapList.put(rs.getString("chaviattribute"),rs.getString("csvattribute"));
            }
        }catch(Exception ex){
            sysLog.errorLog(uid, ex.toString());
        }
        finally{
            try{
                con.close();
                pst.close();
                rs.close();
            }catch(SQLException ex){}
        }        
        return attributeMapList;
    }
    public List<Map<String,List<String>>> getChaviAttribute(String projectCode){       
        Map <String,List<String>> attributeMap = new HashMap();
        List<Map<String,List<String>>> tableList = new ArrayList<Map<String,List<String>>>();
        con = MySqlConnect.connectMysql();
        String sql = "SELECT tablename,chaviattribute,csvattribute from csvjsonattributemap where projectid=? and chaviattribute != ?";
        try{
            pst = con.prepareStatement(sql);
            pst.setString(1, projectCode);
            pst.setString(2, "mr_number");
            rs = pst.executeQuery();
            sysLog.sqlLog(uid, sql);
            while(rs.next()){
                List<String> attributeList = new ArrayList<String>(); 
                attributeList.add(rs.getString("tablename"));
                attributeList.add(rs.getString("chaviattribute"));
                attributeList.add(rs.getString("csvattribute"));
                attributeMap.put(rs.getString("csvattribute"), attributeList);                
            }
            tableList.add(attributeMap);
        }catch(Exception ex){
            sysLog.errorLog(uid, ex.toString());
        }
        finally{
            try{
                con.close();
                pst.close();
                rs.close();
            }catch(SQLException ex){}
        }          
        return tableList;
    } 
    public List<String> getMoleculeList(){
        List<String> moleculeList = new ArrayList<String>();
        con = MySqlConnect.connectMysql();
        String sql = "select symbol,moleculetype,moleculename from moleculescatalog limit 0,3000";
        try{
            pst = con.prepareStatement(sql);
            rs = pst.executeQuery();
            sysLog.sqlLog(uid, sql);
            while(rs.next()){
                moleculeList.add(rs.getString("symbol").toUpperCase());   
            }
        }catch(Exception ex){sysLog.errorLog(uid, ex.toString());}
        return moleculeList;    
    }
    public Map<String,String> getDeidenfitiedPatientIDMap(){
        Map<String, String> patientIDMap = new HashMap<String, String>();
        con = MySqlConnect.connectMysql();
        String sql = "select DCMPatientID, SysPatientID from patientid_map";
        try{
            pst = con.prepareStatement(sql);
            rs = pst.executeQuery();
            sysLog.sqlLog(uid, sql);
            while(rs.next())
                patientIDMap.put(rs.getString("DCMPatientID"), cr.decryptString(rs.getString("SysPatientID")));
        }catch(Exception ex){sysLog.errorLog(uid, ex.toString());}    
        return patientIDMap;
    }    
    
}
