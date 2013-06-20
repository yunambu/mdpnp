package org.mdpnp.dts.reader;

import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Hashtable;
import java.util.List;
import java.util.StringTokenizer;

import org.mdpnp.dts.data.DTSdata;
import org.mdpnp.dts.statistics.OffsetStatisticsImpl;

/**
 * 
 * @author dalonso@mdpnp.org
 * This class is basically the reader of the data file.
 * We populate our data structures as we read each row, and these structures will be 
 * inquired for statistics (or other data) later 
 *
 */
public class Reader {
	
	public static final String SEP_PIPE = "|"; //column/token separator PIPE
	public static final int DEFAULT_SKIPPED_LINES =5; //default header lines
 	
	private String filePath;
	private String fileName;
	private String fieldSeparator;//column/token separator
	private int skippedLines;//skips the first number of indicated lines when reading a file (Header lines)
	private int linesReaded = 0;
	
	//TODO Consider: create here vs dependency injection for these attributes
	private List<DTSdata> dataList = new ArrayList<DTSdata>();	
	private Hashtable byDeviceType = new Hashtable<>();//data by device type
	private Hashtable byConnection = new Hashtable<>();//data by Networked / Standalone
	private Hashtable byThresholdRange = new Hashtable<>();//data by threshold
	
	//cons 
	public Reader(){
		this.skippedLines = DEFAULT_SKIPPED_LINES;
		this.fieldSeparator = SEP_PIPE;
	}
	
	public Reader(String filePath, String fileName, String fieldSeparator, int skippedLines){
		this.filePath = filePath;
		this.fileName = fileName;
		this.fieldSeparator = fieldSeparator;
		this.skippedLines = skippedLines;
	}

	//getters & setters
	public String getFilePath() {
		return filePath;
	}

	public void setFilePath(String filePath) {
		this.filePath = filePath;
	}

	public String getFileName() {
		return fileName;
	}

	public void setFileName(String fileName) {
		this.fileName = fileName;
	}

	public String getFieldSeparator() {
		return fieldSeparator;
	}

	public void setFieldSeparator(String fieldSeparator) {
		this.fieldSeparator = fieldSeparator;
	}

	public int getSkippedLines() {
		return skippedLines;
	}

	public void setSkippedLines(int skippedLines) {
		this.skippedLines = skippedLines;
	}

	public List<DTSdata> getDataList() {
		return dataList;
	}

	public void setDataList(List<DTSdata> dataList) {
		this.dataList = dataList;
	}

	public Hashtable getStatsByDeviceType(){
		return byDeviceType;
	}
	
	public Hashtable getStatsByConnection(){
		return byConnection;
	}
	
	public Hashtable getByThresholdRange() {
		return byThresholdRange;
	}

//	public int geLinesReaded(){
//		if(linesReaded> DEFAULT_SKIPPED_LINES) return linesReaded - DEFAULT_SKIPPED_LINES;
//		else return 0;
//	}
	
	/**
	 * Open the file for reading for each line:
	 * 	1. Get tokens. Each token is a different data
	 *  2. Do calculations with the data
	 *  TODO Right now, we completely trust the data read. Should we calculate our own Offset/ Threshold, etc...????
	 *  3. Feed the data to the aux. structures in charge of the statistical calculations 
	 */
	public void readFile(){
		try{
		
		 // Open the file
		  FileInputStream fstream = new FileInputStream(filePath+"//"+fileName);
		  // Get the object of DataInputStream
		  DataInputStream in = new DataInputStream(fstream);
		  BufferedReader br = new BufferedReader(new InputStreamReader(in));

		  String strLine;
		  		  
		  //Read File Line By Line
		  while ((strLine = br.readLine()) != null)   {
			  //skip lines if proceed
			  if(strLine.indexOf(fieldSeparator)>0 && (++linesReaded>skippedLines)){
				  //tokenize
				  StringTokenizer st = new StringTokenizer(strLine, fieldSeparator);
				  while(st.hasMoreTokens()){
					  //Reads a line whose fields should follow the order described by DSTdata
					  try{
						  DTSdata data = new DTSdata();
						  //each next column is st.nextToken().trim()
						  data.setNumber(Integer.parseInt(st.nextToken().trim())); //number
						  data.setInstitution(st.nextToken().trim().toUpperCase()); //institution
						  data.setDeviceLocation(st.nextToken().trim().toLowerCase());//device location
						  data.setDeviceRoom(st.nextToken().trim().toLowerCase());//device location (room)
						  data.setDeviceType(st.nextToken().trim().toLowerCase());//device type
						  data.setSpecification(st.nextToken().trim().toLowerCase());//manufacturer/model
						  data.setConnection(st.nextToken().trim().toLowerCase());//networked / standalone
						  data.setSyncTime(st.nextToken().trim());
						  data.setBioMedChanged(st.nextToken().trim());
						  data.setPictureTaken(st.nextToken().trim());
						  
						  data.setsNTP_referenceTime(st.nextToken().trim());
						  data.setReferenceEXIF(st.nextToken().trim());
						  data.setCameraOffset(st.nextToken().trim());
						  data.setCameraErrorMargin(st.nextToken().trim());
						  data.setPictureTaken(st.nextToken().trim());
						  data.setDeviceTimeDisplayed(st.nextToken().trim());
						  data.setDisplaysSeconds(Boolean.parseBoolean(st.nextToken().trim()));
						  data.setDeviceErrorMargin(st.nextToken().trim());
						  data.setCurrentlyOnDST(Boolean.parseBoolean(st.nextToken().trim()));
						  data.setDeviceTimeCorrectedForDST(st.nextToken().trim());
						  data.setDeviceTime(st.nextToken().trim());
						  data.setEXIFTime(st.nextToken().trim());
						  data.setCorrectedEXIFTime(st.nextToken().trim());
						  data.setDeviceOffset(st.nextToken().trim());
						  data.setAbsDeviceOffset(st.nextToken().trim());//device offset w/out negative sign
						  data.setOffsetSign(st.nextToken().trim()); //device offset sign in words
 
						  String threshold = st.nextToken().trim();//void value threshold > 2 sec
						  threshold = st.nextToken().trim().toLowerCase();
						  data.setThreshold(threshold);
						  data.setThresholdGT_2sec(!threshold.equals("no offset") && !threshold.equals("offset less than 2 sec"));
						  st.nextToken().trim();//void value threshold > 1 min
						  threshold = st.nextToken().trim().toLowerCase();
						  data.setThresholdGT_1min(!threshold.equals("no offset") && !threshold.equals("offset less than 1 min"));
						  threshold = st.nextToken().trim();//void value threshold > 5 min
						  threshold = st.nextToken().trim().toLowerCase();//void value
						  data.setThresholdGT_5min(!threshold.equals("no offset") && !threshold.equals("offset less than 5 min"));
						  threshold = st.nextToken().trim();//void value threshold > 10 min
						  threshold = st.nextToken().trim().toLowerCase();//void value
						  data.setThresholdGT_10min(!threshold.equals("no offset") && !threshold.equals("offset less than 10 min"));
						  threshold = st.nextToken().trim();//void value threshold > 15 min
						  threshold = st.nextToken().trim().toLowerCase();//void value
						  data.setThresholdGT_15min(!threshold.equals("no offset") && !threshold.equals("offset less than 15 min"));
						  threshold = st.nextToken().trim();//void value threshold > 30 min
						  threshold = st.nextToken().trim().toLowerCase();//void value
						  data.setThresholdGT_30min(!threshold.equals("no offset") && !threshold.equals("offset less than 30 min"));
						  threshold = st.nextToken().trim();//void value threshold > 1 hour
						  threshold = st.nextToken().trim().toLowerCase();//void value
						  data.setThresholdGT_1Hour(!threshold.equals("no offset") && !threshold.equals("offset less than 1 hour"));
						  threshold = st.nextToken().trim();//void value threshold > 2 hour
						  threshold = st.nextToken().trim().toLowerCase();//void value
						  data.setThresholdGT_2Hour(!threshold.equals("no offset") && !threshold.equals("offset less than 2 hours"));
						  						  
						 							  
						  //Add it to our list/structures for statistics
						  dataList.add(data);						  
						  loadByDeviceType(byDeviceType, data);
						  loadByConnection(data);
						  genericLoad(byThresholdRange, data.getThresholdCase(), data.getAbsDeviceOffasetAsLong());//by  Threshold
							  
						  
					  }catch(Exception e){
						  System.out.println(fileName+" row "+linesReaded);
						  e.printStackTrace();
					  }
				  }
			  }
		  }
		  //Close the input stream
		  in.close();
		  fstream.close();
		  }catch (Exception e){//Catch exception if any
			  System.err.println("Error: " + e.getMessage());
		  }
	}	

/**
 * Loads information in the aux structure for statistics by device type
 * @param ht Hastable: key is the device type as String. Value is stats info as OffsetStatisticsImpl Object
 * @param data DTSdata Object (raw data)
 */
	private void loadByDeviceType(Hashtable ht, DTSdata data){
		  if(byDeviceType.containsKey(data.getDeviceType())){
			  OffsetStatisticsImpl x = (OffsetStatisticsImpl)byDeviceType.get(data.getDeviceType());
			  x.addOffset(data.getAbsDeviceOffasetAsLong());
			  byDeviceType.put(data.getDeviceType(), x);
		  }else{
			  OffsetStatisticsImpl x = new OffsetStatisticsImpl();
			  x.addOffset(data.getAbsDeviceOffasetAsLong());
			  byDeviceType.put(data.getDeviceType(), x);
		  }
	}
	
	/**
	 * Loads information in the aux structure for statistics by Networked / Standalone Med Device
	 * @param data DTSdata Object (raw data)
	 */
	private void loadByConnection(DTSdata data){
		if(byConnection.containsKey(data.getConnection())){
			OffsetStatisticsImpl x = (OffsetStatisticsImpl)byConnection.get(data.getConnection());
			x.addOffset(data.getAbsDeviceOffasetAsLong());
			byConnection.put(data.getConnection(), x);		
		}else{
			  OffsetStatisticsImpl x = new OffsetStatisticsImpl();
			  x.addOffset(data.getAbsDeviceOffasetAsLong());
			  byConnection.put(data.getConnection(), x);
		}
		
	}
	
	/**
	 * Loads information in a generic Hastable
	 * @param ht Hastable: key is the criteria. Value is stats info as OffsetStatisticsImpl Object
	 * @param criteria filter 
	 * @param offset long in miliseconts to add to the statistics
	 */
	private void genericLoad(Hashtable ht, Object criteria, long offset){
		if(ht.containsKey(criteria)){
			OffsetStatisticsImpl x = (OffsetStatisticsImpl)ht.get(criteria);
			x.addOffset(offset);
			ht.put(criteria, x);
		}else{
			  OffsetStatisticsImpl x = new OffsetStatisticsImpl();
			  x.addOffset(offset);
			  ht.put(criteria, x);
		}
	}

}