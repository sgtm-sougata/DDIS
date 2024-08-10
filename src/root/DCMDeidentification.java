/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package root;

import UserHome.UserIndexController;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.dcm4che2.data.DicomElement;
import org.dcm4che2.data.DicomObject;
import org.dcm4che2.data.SequenceDicomElement;
import org.dcm4che2.data.Tag;
import org.dcm4che2.data.VR;
import org.dcm4che2.io.DicomInputStream;
import org.dcm4che2.io.DicomOutputStream;
import org.dcm4che2.util.TagUtils;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;

/**
 *
 * @author SURAJIT
 */
public class DCMDeidentification {
    File dcmFile;
    String deidentifiedDCMSource="",deidentifiedDCMDest="",globalPath="";
    GenerateSystemUID sysUID;
    String SysFrameOfReferenceUID="";
    boolean isPatientJSONCreated = false;
    public DCMDeidentification(File dcmFile){
        this.dcmFile = dcmFile;
        sysUID = new GenerateSystemUID();
        deidentifiedDCMSource = GetConfig.deidentifiedDCMSource;
        deidentifiedDCMDest = GetConfig.deidentifiedDCMDest;
        globalPath = GetConfig.globalPath;   
        System.out.println("start Time : "+new Date());
    }
    public String deIdentifyDCM(String SysPatId, String SysStudyUID, String SysSeriesUID, String SysSOPInsUID){
        String dcmURI = "";
        try{
            /////System.out.println("System SOPUID : "+SysSOPInsUID);
                DicomInputStream dis = new DicomInputStream(dcmFile);
                DicomObject dcmObj = dis.readDicomObject();
                dcmObj.putString(Tag.PatientID, VR.LO, SysPatId);
                dcmObj.putString(Tag.StudyInstanceUID , VR.UI, SysStudyUID);
                dcmObj.putString(Tag.SeriesInstanceUID, VR.UI, SysSeriesUID);
                dcmObj.putString(Tag.SOPInstanceUID, VR.UI, SysSOPInsUID);
                String dcmFrameOfReferenceUID = dcmObj.getString(Tag.FrameOfReferenceUID);
                SysFrameOfReferenceUID = sysUID.getFrameOfReferenceUID(dcmFrameOfReferenceUID);
                if(dcmFrameOfReferenceUID !=null && SysFrameOfReferenceUID.isEmpty()){
                    SysFrameOfReferenceUID = sysUID.createSysFrameOfReferencedUID(SysStudyUID);
                    //showLog(msg5+SysFrameOfReferencedUID);
                    //sysLog.otherLog(uid,msg5+SysFrameOfReferencedUID); 
                    if(sysUID.insertFrameOfReferenceUID(dcmFrameOfReferenceUID,SysFrameOfReferenceUID,SysStudyUID)){
                       // sysLog.otherLog(uid,imsg5+SysFrameOfReferencedUID);
                    }
                }
                /*else{
                    SysFrameOfReferencedUID = sysUID.getFrameOfReferenceUID(SysStudyUID);
                } */                
                if(dcmFrameOfReferenceUID != null)
                    dcmObj.putString(Tag.FrameOfReferenceUID, VR.UI, SysFrameOfReferenceUID);
                String patDateofBirth = "";
                patDateofBirth = dcmObj.getString(Tag.PatientBirthDate);
                //System.out.println("DOB : "+patDateofBirth.replace(patDateofBirth, patDateofBirth.substring(0,4)+"0101"));
                if(patDateofBirth != null){
                    if(patDateofBirth.length()==8)
                        dcmObj.putString(Tag.PatientBirthDate,VR.DT, patDateofBirth.replace(patDateofBirth, patDateofBirth.substring(0,4)+"0101"));
                }
                String modality = dcmObj.getString(Tag.Modality);
                ArrayList<Integer> tagListInt = new ArrayList<Integer>(); 
                JSONParser parser = new JSONParser();
                Object obj = parser.parse(new FileReader("deidentificationRule.json"));
                JSONObject jsobj = (JSONObject) obj;
                JSONArray tagList = (JSONArray) jsobj.get("tagList"); 
                JSONObject deidentifiedValue = (JSONObject) jsobj.get("deidentifyValue");
                JSONObject nestedObj=null;
                if (tagList != null) { 
                   for (int i=0;i<tagList.size();i++){ 
                        nestedObj = (JSONObject) tagList.get(i);
                        int detag = Tag.toTag((String)nestedObj.get("tagName"));
                        //System.out.println("\""+detag+"\" : [\""+(String)nestedObj.get("tagName")+"\",],");
                    tagListInt.add(detag);
                   } 
                }                
		Iterator<DicomElement> iter = dcmObj.iterator();
                    while (iter.hasNext()) {
                            DicomElement element = iter.next();
                            int tag = element.tag();
                            VR v = element.vr();
                            if(dcmObj.vrOf(tag).equals(VR.SQ)){
                                parseSequenceObject(dcmObj,SysPatId,tag,modality,SysSeriesUID);
                            }                                
                            else{
                                //System.out.println("Is Private tag : "+TagUtils.isPrivateDataElement(tag));
                                if(TagUtils.isPrivateDataElement(tag)){
                                    System.out.println("Private tag: "+tag+" : "+String.valueOf(tag));
                                    deIdentifyImage(dcmObj, tag, "", SysPatId);
                                }
                                if(tagListInt.contains(tag)){
                                    String tagToString = String.valueOf(tag);
                                    JSONArray deArray = (JSONArray) deidentifiedValue.get(tagToString);
                                    //System.out.println(deArray+" : "+tag);
                                    String deValue = (String) deArray.get(1);
                                    deIdentifyImage(dcmObj, tag, deValue, SysPatId);
                                //   System.out.println("oo : "+((DicomObject) dcmObj).vrOf(tag).toString()+" : "+((DicomObject) dcmObj).nameOf(tag).toString()+" : "+dcmObj.getString(tag));
                                }
                            }
                    }
                
                //String DCMPath = System.getProperty("user.dir")+deidentifiedDCMSource+SysPatId+"/"+SysStudyUID+"/"+SysSeriesUID+"/";
                String DCMPath = System.getProperty("user.dir")+deidentifiedDCMSource+SysPatId+"/"+SysFrameOfReferenceUID+"/"+SysStudyUID+"/";
                File modifiedDCMDir = new File(DCMPath);
                if(! modifiedDCMDir.exists())
                {
                    modifiedDCMDir.mkdirs();
                }
                if(isPatientJSONCreated == false){
                    writePatientJSON(SysPatId, SysStudyUID,DCMPath);
                    isPatientJSONCreated = true;
                }
                			
                String cDCMFile = SysSOPInsUID+".dcm";
                dcmURI = modifiedDCMDir+"/"+cDCMFile;	
                File f = new File(dcmURI);			
                writeFile(dcmObj, f);
                dis.close();

        }catch(Exception ex){System.out.println("Err : "+ex.toString());}
        System.out.println("End Time : "+new Date());
        return dcmURI;
	
    }
    private void writeFile(DicomObject obj, File f) {
        //File f = new File(copyServer + fileName);
        FileOutputStream fos=null;
        try {
            fos = new FileOutputStream(f);
        } catch (FileNotFoundException e) {
            System.out.println(e.toString());
        }
        BufferedOutputStream bos = new BufferedOutputStream(fos);
        DicomOutputStream dos = new DicomOutputStream(bos);
        try {
            dos.writeDicomFile(obj);
        } catch (IOException e) {
            System.out.println(e.toString());
            return;
        } finally {
            try {
                dos.close();
            } catch (IOException ignore) {
            }
        }
    }
    private void writePatientJSON(String sysPatientID, String sysStudyUID,String path) throws IOException{
        File fileDir = new File(path);
        if(!fileDir.exists())
            fileDir.mkdirs();
        File file = new File(path+"/"+sysPatientID+"_"+sysStudyUID+".json");
        if(!file.exists())
            file.createNewFile();
        FileWriter fw = new FileWriter(file);
        fw.write(UserIndexController.patientJSONData.toJSONString());
        fw.close();    
    }
    public void parseSequenceObject(DicomObject Obj, String SysPatId, int tag, String modality, String SysSeriesUID) throws Exception{
        ArrayList<String> notImgModality = new ArrayList<String>();
        notImgModality.add("RTSTRUCT");
        notImgModality.add("RTDOSE");
        notImgModality.add("RTPLAN");        
        SequenceDicomElement sqEle = (SequenceDicomElement) Obj.get(tag);
        int sqElement = sqEle.countItems();
        //System.out.println("RR : \t\t"+sqElement+"\t");
        ArrayList<Integer> tagListInt = new ArrayList<Integer>(); 
        JSONParser parser = new JSONParser();
        Object obj =null;
        try{
            obj= parser.parse(new FileReader("deidentificationRule.json"));
        }catch(Exception ex){System.out.println("deidentificationRule.json parsing ERROR : "+ex.toString());}
        JSONObject jsobj = (JSONObject) obj;
        JSONArray tagList = (JSONArray) jsobj.get("tagList"); 
        JSONObject deidentifiedValue = (JSONObject) jsobj.get("deidentifyValue");
        JSONObject nestedObj=null;
        if (tagList != null) { 
           for (int i=0;i<tagList.size();i++){ 
                nestedObj = (JSONObject) tagList.get(i);
                int detag = Tag.toTag((String)nestedObj.get("tagName"));
                //System.out.println("\""+detag+"\" : [\""+(String)nestedObj.get("tagName")+"\",],");
            tagListInt.add(detag);
           } 
        }       
        
        for (int i = 0; i < sqEle.countItems(); i++) {  
            long timestamp = System.currentTimeMillis();
            DicomObject SQdcmObj = sqEle.getDicomObject(i);
            Iterator<DicomElement> SQItr = SQdcmObj.iterator();
            while(SQItr.hasNext()){
                DicomElement SQnesElement = SQItr.next();
                int sqTag = SQnesElement.tag();
                if(SQdcmObj.vrOf(sqTag).equals(VR.SQ)){
                    //System.out.println(SQnesElement.countItems()+"\tSQ : "+((DicomObject) SQdcmObj).nameOf(sqTag)+" : "+sqTag );
                    parseSequenceObject(SQdcmObj,SysPatId,sqTag,modality,SysSeriesUID);   
                }
                else{
                    //System.out.println(sqTag+"\tNes V Parse : "+SQdcmObj.nameOf(sqTag)+" : "+SQdcmObj.getString(sqTag));
                    //System.out.println("Is Private tag1 : "+TagUtils.isPrivateDataElement(sqTag));
                    if(TagUtils.isPrivateDataElement(sqTag)){
                        deIdentifyImage(SQdcmObj, sqTag, "", SysPatId);
                   // System.out.println("Private tag: "+sqTag+" : "+String.valueOf(sqTag));
                    }
                    if(tagListInt.contains(sqTag)){
                        String tagToString = String.valueOf(sqTag);
                        JSONArray deArray = (JSONArray) deidentifiedValue.get(tagToString);
                        String deValue = (String) deArray.get(1);
                        deIdentifyImage(SQdcmObj, sqTag, deValue, SysPatId);                        
                        
                    //  System.out.println("oo : "+((DicomObject) dcmObj).vrOf(tag).toString()+" : "+((DicomObject) dcmObj).nameOf(tag).toString()+" : "+dcmObj.getString(tag));
                    }
                    /**
                     * If any DICOM image sequence contains UID then the original UID is replaced by system generated UID
                     * and keep the original UID reference in the database table. 
                     */
                    if(SQdcmObj.vrOf(sqTag).equals(VR.UI) && SQdcmObj.getString(sqTag) != null && !notImgModality.contains(modality) && !SQdcmObj.nameOf(sqTag).contains("Class UID")){
                        String orgUID = SQdcmObj.getString(sqTag);
                        String deUID = SysSeriesUID+".00."+timestamp;
                        String updeUID = sysUID.getOtherUID(orgUID,deUID);
                        SQdcmObj.putString(sqTag, SQdcmObj.vrOf(sqTag), updeUID);   
                    }                    
                    if(modality.equalsIgnoreCase("RTSTRUCT")){
                        /**
                         If modality is a structure set DICOM (RTSTRUCT) file then read all the original id/UID and replace
                         * with the de-identified id/UID. If tag match then apply de-identification on that tag.
                         */
                        String deValueRS = SQdcmObj.getString(sqTag);
                        if((sqTag==2097234 || sqTag==805699620) && deValueRS != null){  //Tag.FrameOfReferenceUID = 2097234 and Tag.ReferencedFrameOfReferenceUID = 80569920
                            deIdentifyImage(SQdcmObj, sqTag, sysUID.getFrameOfReferenceUID(deValueRS), SysPatId);
                            SysFrameOfReferenceUID = sysUID.getFrameOfReferenceUID(deValueRS);
                        }
                        if(sqTag==528725 && tag==805699602)   //Tag.ReferencedSOPInstanceUID = 528725 and Tag.RTReferencedStudySequence = 805699602    
                            deIdentifyImage(SQdcmObj, sqTag, sysUID.getSysStudyUID(deValueRS), SysPatId);
                        if(sqTag==2097166)    //Tag.SeriesInstanceUID = 2097166
                            deIdentifyImage(SQdcmObj, sqTag, sysUID.getSysSeriesUID(deValueRS), SysPatId);
                        if(sqTag==528725 && tag==805699606)    //Tag.ReferencedSOPInstanceUID = 528725 and Tag.ContourImageSequence = 805699602
                            deIdentifyImage(SQdcmObj, sqTag, sysUID.getSysSOPUID(deValueRS), SysPatId);                  
                        //System.out.println(sqTag+"\tREF : "+SQdcmObj.nameOf(sqTag)+" : "+SQdcmObj.getString(sqTag));
                    } 
                    
                    if(modality.equalsIgnoreCase("RTDOSE")){
                        String deValueRS = SQdcmObj.getString(sqTag);
                       // System.out.println(SQdcmObj.nameOf(sqTag)+" Dose Ref : "+sysUID.getSysSOPUID(deValueRS));
                        if(sqTag==528725 && tag==806092802) //Tag.ReferencedSOPInstanceUID = 528725 and Tag.ReferencedRTPlanSequence = 806092802
                            deIdentifyImage(SQdcmObj, sqTag, sysUID.getSysSOPUID(deValueRS), SysPatId);    
                        if(sqTag==528725 && tag==806092896) //Tag.ReferencedSOPInstanceUID = 528725 and Tag.ReferencedStructureSetSequence = 806092896
                            deIdentifyImage(SQdcmObj, sqTag, sysUID.getSysSOPUID(deValueRS), SysPatId);
                    }
                    if(modality.equalsIgnoreCase("RTPLAN")){
                        String deValueRS = SQdcmObj.getString(sqTag);
                       // System.out.println(SQdcmObj.nameOf(sqTag)+" Dose Ref : "+sysUID.getSysSOPUID(deValueRS));
                        if(sqTag==528725 && tag==806092896) //Tag.ReferencedSOPInstanceUID = 528725 and Tag.ReferencedStructureSetSequence = 806092896
                            deIdentifyImage(SQdcmObj, sqTag, sysUID.getSysSOPUID(deValueRS), SysPatId);
                        else{
                            if(SQdcmObj.vrOf(sqTag).equals(VR.UI) && SQdcmObj.getString(sqTag) != null && !SQdcmObj.nameOf(sqTag).contains("Class UID")){
                                String orgUID = SQdcmObj.getString(sqTag);
                                String deUID = SysSeriesUID+".000."+timestamp;
                                /////System.out.println(orgUID+" : OtherUID : "+deUID);
                                String updeUID = sysUID.getOtherUID(orgUID,deUID);
                                SQdcmObj.putString(sqTag, SQdcmObj.vrOf(sqTag), updeUID);   
                            }
                        }
                    }                    
                   // System.out.println("\t\t"+((DicomObject) SQdcmObj).vrOf(sqTag)+" : "+((DicomObject) SQdcmObj).nameOf(sqTag)+" : "+((DicomObject) SQdcmObj).getString(sqTag));
                }
            }
            //new MySqlCon().disconnect();
        }
    }
    public void deIdentifyImage(DicomObject dcmObj, int tag, String deValue, String SysPatId) throws SQLException{
       // System.out.println("Tag : "+dcmObj.nameOf(tag)+" : "+dcmObj.getString(tag));
        if(deValue.length()>0){
            dcmObj.putString(tag, dcmObj.vrOf(tag), deValue);
        }
        else{
            if((dcmObj.vrOf(tag).equals(VR.DA) && dcmObj.getString(tag) != null)&&tag!=Tag.PatientBirthDate) //Use Apply Date replacement rule
                dcmObj.putString(tag, dcmObj.vrOf(tag), sysUID.getDeidentifiedDate(SysPatId,dcmObj.getString(tag)));                                                         
            if(dcmObj.vrOf(tag).equals(VR.DT) && dcmObj.getString(tag) != null){ //DateTme Field
                String tagValue = dcmObj.getString(tag);
                if(tagValue.length()>=8)
                    dcmObj.putString(tag, dcmObj.vrOf(tag), (sysUID.getDeidentifiedDate(SysPatId,tagValue.substring(0, 8))+tagValue.substring(8, tagValue.length())));
            }
            if(dcmObj.vrOf(tag).equals(VR.UI) && dcmObj.getString(tag) != null)
                dcmObj.putString(tag, dcmObj.vrOf(tag), "0.0");  
            
        }
        
    }
    public void deidentifyRS(){
    
    }
    
}
