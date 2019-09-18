package com.test.report;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.apache.commons.io.FileUtils;
import org.h2.tools.Csv;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.haulmont.yarg.formatters.factory.DefaultFormatterFactory;
import com.haulmont.yarg.loaders.factory.DefaultLoaderFactory;
import com.haulmont.yarg.loaders.impl.GroovyDataLoader;
import com.haulmont.yarg.loaders.impl.JsonDataLoader;
import com.haulmont.yarg.reporting.ReportOutputDocument;
import com.haulmont.yarg.reporting.Reporting;
import com.haulmont.yarg.reporting.RunParams;
import com.haulmont.yarg.structure.Report;
import com.haulmont.yarg.structure.xml.impl.DefaultXmlReader;
import com.haulmont.yarg.util.groovy.DefaultScriptingImpl;
import com.test.report.model.Employees;
import com.test.report.model.Usage;

/**
 * Hello world!
 *
 */
public class App 
{
    
	public static void main( String[] args ) throws Exception
    {
        List<Employees> custList = App.getCustomers(); 
        System.out.println("custList length "+custList.size());
        List<Usage> custUsageList = App.getCustomersUsage(); 
        System.out.println("custUsageList length "+custUsageList.size());
        JsonObject header = App.prepareHeaderData(custList,custUsageList);
        Gson gson = new Gson();
        String jsonString = gson.toJson(header);
		App.generateEmpUsageReport(jsonString);
    }
	
	 
    public static JsonObject prepareHeaderData(List<Employees> custList, List<Usage> custUsageList) {
		JsonObject headerJson = new JsonObject();
		headerJson.addProperty("Report_Run_Date", convertDateToString(new Date(),"MM/dd/yyyy"));
		headerJson.addProperty("Number_of_Phones",custList.size());
		Integer totalMins = getTotalMins(custUsageList);
		Double totalDataUsed = getTotalData(custUsageList);
		headerJson.addProperty("Total_Minutes", totalMins );
		headerJson.addProperty("Total_Data", totalDataUsed );
		headerJson.addProperty("Average_Minutes",totalMins/custList.size());
		headerJson.addProperty("Average_Data",totalDataUsed/custList.size()); 
		JsonObject headerSecJson = new JsonObject();
		headerSecJson.add("main", headerJson);
		createItemsJson(headerSecJson,custList,custUsageList);
		return headerSecJson;
	}


	private static void createItemsJson(JsonObject headerSecJson, List<Employees> custList, List<Usage> custUsageList) {
		JsonArray itemsJson = new JsonArray();
		for(Usage usage : custUsageList) {
			JsonObject itemJson = new JsonObject();
			itemJson.addProperty("id", usage.getEmployeeId());
			Employees emp = getCustomer(usage.getEmployeeId(),custList);
			itemJson.addProperty("name", (emp!= null?emp.getEmployeeName():""));
			
			itemJson.addProperty("model", (emp!=null ?emp.getModel():""));
			itemJson.addProperty("purchaseDate",(emp!=null? convertDateToString(emp.getPurchaseDate(),"MM/dd/yyyy"):""));
			itemJson.addProperty("minutesUsage", usage.getMinutesUsed()+"");
			itemJson.addProperty("dataUsage", usage.getDataUsed()+"");
			itemsJson.add(itemJson );
		}
		
		headerSecJson.add("items", itemsJson );
	}


	private static Employees getCustomer(Integer employeeId, List<Employees> custList) {
		for(Employees emp : custList) {
			if(employeeId.equals(emp.getEmployeeId())) {
				return emp;
			}
		}
		return null;
	}


	private static JsonArray getMinutesUsage(List<Usage> custUsageList, Integer employeeId) {
		JsonArray minuteJsonArray = new JsonArray();
		for(Usage usage : custUsageList) {
			if(employeeId.equals(usage.getEmployeeId())) {				
				JsonObject minuteJsonObj = new JsonObject();
				minuteJsonObj.addProperty("date", convertDateToString(usage.getDate(),"MM/dd/yyyy"));
				minuteJsonObj.addProperty("callUsage", usage.getMinutesUsed());
				minuteJsonArray.add(minuteJsonObj);
			}
		}
		return minuteJsonArray;
	}


	private static Double getTotalData(List<Usage> custUsageList) {
		 
		return custUsageList.stream().mapToDouble(Usage::getDataUsed).sum();
	}


	public static Integer getTotalMins(List<Usage> custUsageList) {
		 
         
		return custUsageList.stream().mapToInt(Usage::getMinutesUsed).sum();
	}


	public static String convertDateToString(Date date, String format) {
		SimpleDateFormat sdf = new SimpleDateFormat(format);
    	return sdf.format(date); 
	}


	public static void generateEmpUsageReport(String jsonString) throws Exception {
		Report report = new DefaultXmlReader().parseXml(FileUtils.readFileToString(new File("./src/main/resources/usage-json.xml")));

        Reporting reporting = new Reporting();
        DefaultFormatterFactory formatterFactory = new DefaultFormatterFactory();
        reporting.setFormatterFactory(formatterFactory);
        reporting.setLoaderFactory(new DefaultLoaderFactory()
                .setGroovyDataLoader(new GroovyDataLoader(new DefaultScriptingImpl()))
                .setJsonDataLoader(new JsonDataLoader()));

        String json = jsonString ;//FileUtils.readFileToString(new File("./src/test/resources/invoice-data.json"));
        ReportOutputDocument reportOutputDocument = reporting.runReport(new RunParams(report).param("param1", json),
                new FileOutputStream("./src/main/resources/usage.pdf"));
    }
    
    public static List<Employees> getCustomers() throws Exception {
    	ResultSet rs = new Csv().read("./src/main/resources/CellPhone.csv", null, null);
        ResultSetMetaData meta = rs.getMetaData();
        List<Employees> customers = new ArrayList<Employees>();
        while (rs.next()) {
        	Employees employee = new Employees();
        	employee.setEmployeeId(rs.getInt("EMPLOYEEID"));
        	employee.setEmployeeName(rs.getString("EMPLOYEENAME"));
        	employee.setPurchaseDate(convertStringToDate(rs.getString("PURCHASEDATE"),"yyyyMMdd"));
        	employee.setModel(rs.getString("MODEL"));
        	customers.add(employee);
			/*
			 * for (int i = 0; i < meta.getColumnCount(); i++) { System.out.println(
			 * meta.getColumnLabel(i + 1) + ": " + rs.getString(i + 1));
			 * 
			 * }
			 */
             
        }
        rs.close();
        
        return customers;
    }
    public static Date convertStringToDate(String strDate,String format) throws Exception {
    	SimpleDateFormat sdf = new SimpleDateFormat(format);
    	return sdf.parse(strDate);
    }
    public static List<Usage> getCustomersUsage() throws Exception {
    	ResultSet rs = new Csv().read("./src/main/resources/CellPhoneUsageByMonth.csv", null, null);
        ResultSetMetaData meta = rs.getMetaData();
        List<Usage> customersUsage = new ArrayList<Usage>();
        while (rs.next()) {
        	Usage employeeUsage = new Usage();
        	employeeUsage.setEmployeeId(rs.getInt("emplyeeId"));
        	employeeUsage.setDate(convertStringToDate(rs.getString("date"),"MM/dd/yyyy"));
        	employeeUsage.setMinutesUsed(rs.getInt("totalMinutes"));
        	employeeUsage.setDataUsed(rs.getDouble("totalData"));
        	customersUsage.add(employeeUsage);
			/*
			 * for (int i = 0; i < meta.getColumnCount(); i++) { System.out.println(
			 * meta.getColumnLabel(i + 1) + ": " + rs.getString(i + 1));
			 * 
			 * }
			 */
             
        }
        rs.close();
        
        return customersUsage;
    }
 
}
