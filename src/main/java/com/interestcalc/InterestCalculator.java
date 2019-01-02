package com.interestcalc;

import java.io.FileOutputStream;
import java.io.IOException;
//import java.text.DecimalFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

import org.apache.poi.xssf.usermodel.XSSFCell;
import org.apache.poi.xssf.usermodel.XSSFRow;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

public class InterestCalculator {
	
	public static SimpleDateFormat sdf=new SimpleDateFormat("dd/MM/yyyy");
	public static long amountSanctioned;	
	public static long initialTenure;
	public static long emi;
	public static int emiDay;
	public static long totalDisbursedAmount;	

	public static long calculateEmi(long principal, double roi, long tenureMonthsRemaining) {		
		double monthlyRoiFract=(roi/12)/100;		
		double emi=(principal*monthlyRoiFract*Math.pow(1+monthlyRoiFract,tenureMonthsRemaining))/(Math.pow(1+monthlyRoiFract,tenureMonthsRemaining)-1);
		return Math.round(emi);
	}	
	
	public static double calculateDailyInterest(long outstandingBalance, double roi) {		
		double dailyRoiFract=(roi/365)/100;
		double interest=outstandingBalance*dailyRoiFract;
		return interest;
	}
	
	public static String getNextDate(String inputDate) {		
		
		Date entryDate=null;
		try {
			entryDate=sdf.parse(inputDate);
			entryDate=new Date(entryDate.getTime() + (24*60*60*1000));
			return sdf.format(entryDate);			
		} catch (ParseException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return "";
	}
	
	public static String getPreviousDate(String inputDate) {		
		
		Date entryDate=null;
		try {
			entryDate=sdf.parse(inputDate);
			entryDate=new Date(entryDate.getTime() - (24*60*60*1000));
			return sdf.format(entryDate);			
		} catch (ParseException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return "";
	}
	
	public static String getNextInstallmentDate(String inputDate) {
		String[] dateParts=inputDate.split("/");
		int nextDay=emiDay;
		int nextMonth=new Integer(dateParts[1]) + 1;
		int nextYear=new Integer(dateParts[2]);
		if(nextMonth==13)
		{
			nextMonth=1;
			nextYear++;
		}		
		try {
			return sdf.format(sdf.parse(nextDay + "/" + nextMonth + "/" + nextYear));
		} catch (ParseException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return null;
	}
	
	public static String getPreviousInstallmentDate(String inputDate) {
		String[] dateParts=inputDate.split("/");
		int previousDay=emiDay;
		int previousMonth=new Integer(dateParts[1]) - 1;
		int previousYear=new Integer(dateParts[2]);
		if(previousMonth==0)
		{
			previousMonth=12;
			previousYear--;
		}		
		try {
			return sdf.format(sdf.parse(previousDay + "/" + previousMonth + "/" + previousYear));
		} catch (ParseException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return null;
	}
	
	public static long calculatePeriodInterest(String startDate, String endDate) {
		long outstandingBalance=getNextTransaction(startDate).openingPrincipal;
		double totalInterest=0;	
		while(startDate.equals(endDate)==false) {
			if(Transactions.getTransaction(startDate)!=null) {
				outstandingBalance=getNextTransaction(startDate).openingPrincipal;
				if(getNextTransaction(startDate).transactionType.equals("PartPayment"))
				{
					totalInterest=0;
				}
			}
			totalInterest=totalInterest + calculateDailyInterest(outstandingBalance, ROITrend.getRate(startDate));			
			startDate=getNextDate(startDate);
		}
		return Math.round(totalInterest);
	}
	
	public static Transaction getPreviousTransaction(String transactionDate) {
		
		String previousInstallmentStartDate=getPreviousInstallmentDate(transactionDate);
		//int previousTransactionIndex=Transactions.transactions.indexOf(Transactions.getTransaction(transactionDate))-1;
		transactionDate=getPreviousDate(transactionDate);
		while(transactionDate.equals(previousInstallmentStartDate)==false) {
			if(Transactions.getTransaction(transactionDate)!=null) {
				return Transactions.getTransaction(transactionDate);
			}
			transactionDate=getPreviousDate(transactionDate);
		}
		return Transactions.getTransaction(transactionDate);
	}
	
	public static Transaction getNextTransaction(String transactionDate) {
		int nextTransactionIndex=Transactions.transactions.indexOf(Transactions.getTransaction(transactionDate))+1;
		return Transactions.transactions.get(nextTransactionIndex);		
	}
	
	public static Transaction getPreviousInstallmentTransaction(String transactionDate) {
		return Transactions.getTransaction(getPreviousInstallmentDate(transactionDate));
	}
	
	public static void correctCurrentTransaction(String transactionDate) {
		Transaction previousTransaction=getPreviousTransaction(transactionDate);
		if(previousTransaction!=null)
		{
			Transaction currentTransaction=Transactions.getTransaction(transactionDate);		
			currentTransaction.openingPrincipal=previousTransaction.closingPrincipal;
			if(currentTransaction.transactionType.equals("PartDisbursement"))
			{
				currentTransaction.principalComponent=0;
				currentTransaction.interestComponent=0;
				currentTransaction.closingPrincipal=currentTransaction.openingPrincipal+currentTransaction.amount;
				totalDisbursedAmount=totalDisbursedAmount+currentTransaction.amount;
			}	
			else
			{
				Transaction previousInstallmentTransaction=previousTransaction;
				if(getPreviousInstallmentTransaction(transactionDate)!=null)
				{
					previousInstallmentTransaction=getPreviousInstallmentTransaction(transactionDate);
				}
				if(currentTransaction.transactionType.equals("PartPayment"))
				{				
					currentTransaction.interestComponent=calculatePeriodInterest(previousInstallmentTransaction.transactionDateStr,transactionDate);
					currentTransaction.principalComponent=currentTransaction.amount-currentTransaction.interestComponent;
					currentTransaction.closingPrincipal=currentTransaction.openingPrincipal-currentTransaction.principalComponent;
					if(currentTransaction.closingPrincipal<0)
					{
						currentTransaction.closingPrincipal=0;
					}
				}			
				else if(currentTransaction.transactionType.equals("PreEMI"))
				{					
					currentTransaction.interestComponent=calculatePeriodInterest(previousInstallmentTransaction.transactionDateStr,transactionDate);
					currentTransaction.principalComponent=0;
					currentTransaction.amount=currentTransaction.interestComponent;
					currentTransaction.closingPrincipal=currentTransaction.openingPrincipal-currentTransaction.principalComponent;			
				}		
				else if(currentTransaction.transactionType.equals("PreEMIInterestCorrection"))
				{
					currentTransaction.principalComponent=0;
					currentTransaction.amount=currentTransaction.interestComponent;
					currentTransaction.closingPrincipal=currentTransaction.openingPrincipal;								
				}
				else if(currentTransaction.transactionType.equals("EMI"))
				{
					currentTransaction.amount=emi;
					currentTransaction.interestComponent=calculatePeriodInterest(previousInstallmentTransaction.transactionDateStr,transactionDate);
					currentTransaction.principalComponent=emi-currentTransaction.interestComponent;	
					currentTransaction.closingPrincipal=currentTransaction.openingPrincipal-currentTransaction.principalComponent;
					if(currentTransaction.closingPrincipal<0)
					{
						currentTransaction.closingPrincipal=0;
						currentTransaction.principalComponent=currentTransaction.openingPrincipal;
						currentTransaction.amount=currentTransaction.principalComponent+currentTransaction.interestComponent;
					}
				}
				else if(currentTransaction.transactionType.equals("EMIInterestCorrection"))
				{
					currentTransaction.amount=emi;
					currentTransaction.principalComponent=emi-currentTransaction.interestComponent;	
					currentTransaction.closingPrincipal=currentTransaction.openingPrincipal-currentTransaction.principalComponent;
					if(currentTransaction.closingPrincipal<0)
					{
						currentTransaction.closingPrincipal=0;
						currentTransaction.principalComponent=currentTransaction.openingPrincipal;
						currentTransaction.amount=currentTransaction.principalComponent+currentTransaction.interestComponent;
					}
				}
			}
			
			System.out.println(currentTransaction.getDate() + "\t" +
					currentTransaction.transactionType + "\t" +
					currentTransaction.openingPrincipal + "\t" +
					currentTransaction.amount + "\t" +
					currentTransaction.principalComponent + "\t" +
					currentTransaction.interestComponent + "\t" +
					currentTransaction.roi + "\t" +
					currentTransaction.closingPrincipal);
		}
		//below will always be first disbursement transaction
		else {	
			Transaction currentTransaction=Transactions.getTransaction(transactionDate);	
			currentTransaction.closingPrincipal=currentTransaction.amount;
		}
	}
	
	public static void generateNextInstallmentTransaction(String transactionDate) {					
				
		String nextInstallmentDate=getNextInstallmentDate(transactionDate);
		Transaction transaction;
		transaction=new Transaction(getNextInstallmentDate(transactionDate), 0, "");	
		if(Transactions.getTransaction(nextInstallmentDate)==null)		
		{	
			Transaction previousTransaction=getPreviousTransaction(transaction.transactionDateStr);
			if(previousTransaction!=null)
			{
				if(previousTransaction.transactionType.equals("PartDisbursement")||previousTransaction.transactionType.equals("PartPayment")) 
				{											
					if(previousTransaction.transactionType.equals("PartDisbursement"))
					{
						if((totalDisbursedAmount + previousTransaction.amount)==amountSanctioned)
						{
							Transactions.addEmi(transaction);
							return;
						}
					}	
					Transactions.addPreEmi(transaction);
					return;						
				}
			}					
			Transactions.addEmi(transaction);
		}						
	}
	
	public static void generateSchedule() {
		
		int currentTransactionIndex=0;
		Transaction currentTransaction=null;		
		do{
			currentTransaction=Transactions.transactions.get(currentTransactionIndex);			
			correctCurrentTransaction(currentTransaction.transactionDateStr);
			if(currentTransaction.closingPrincipal>0)
			{
				String currentTransactionDate=currentTransaction.getDate();
				generateNextInstallmentTransaction(currentTransactionDate);				
			}
			currentTransactionIndex++;
		} while(currentTransaction.closingPrincipal!=0);
	}
	
	public static void main(String[] args) {		
		
		amountSanctioned=23456;
		initialTenure=110;
		emiDay=5;
		ROITrend.addEntry("14/02/2011", 6);
		Transactions.addPartDisbursement("12/10/2012", 23432);
		
		emi=calculateEmi(amountSanctioned,ROITrend.roi.get(0).rate,initialTenure);
		
		ROITrend.addEntry("15/04/2013", 2);		
		
		Transactions.addPreEMIInterestCorrection("10/12/2012", 484);
		Transactions.addEMIInterestCorrection("10/04/2013", 568);
				
		generateSchedule();
		
		long totalPrincipal=0;
		long totalInterest=0;
		long totalAmount=0;
		for(Transaction transaction:Transactions.transactions)
		{
			totalPrincipal=totalPrincipal + transaction.principalComponent;
			totalInterest=totalInterest + transaction.interestComponent;
		}
		totalAmount=totalPrincipal + totalInterest;
		
		XSSFWorkbook workbook=new XSSFWorkbook();
		XSSFSheet sheet=workbook.createSheet("schedule");
		XSSFRow row=sheet.createRow(0);
		XSSFCell cell=row.createCell(0);
		cell.setCellValue("Date");
		cell=row.createCell(1);
		cell.setCellValue("TransactionType");
		cell=row.createCell(2);
		cell.setCellValue("OpeningPrincipal");
		cell=row.createCell(3);
		cell.setCellValue("Amount");
		cell=row.createCell(4);
		cell.setCellValue("Principal");
		cell=row.createCell(5);
		cell.setCellValue("Interest");
		cell=row.createCell(6);
		cell.setCellValue("ROI");
		cell=row.createCell(7);
		cell.setCellValue("ClosingPrincipal");
		
		int rowIndex=1;		
		
		for(Transaction trans:Transactions.transactions) {
			
			int colIndex=0;
			row=sheet.createRow(rowIndex);
			cell=row.createCell(colIndex);
			colIndex++;
			cell.setCellValue(trans.getDate());
			cell=row.createCell(colIndex);
			colIndex++;
			cell.setCellValue(trans.transactionType);
			cell=row.createCell(colIndex);
			colIndex++;
			cell.setCellValue(trans.openingPrincipal);
			cell=row.createCell(colIndex);
			colIndex++;
			cell.setCellValue(trans.amount);
			cell=row.createCell(colIndex);
			colIndex++;
			cell.setCellValue(trans.principalComponent);
			cell=row.createCell(colIndex);
			colIndex++;
			cell.setCellValue(trans.interestComponent);
			cell=row.createCell(colIndex);
			colIndex++;
			cell.setCellValue(trans.roi);
			cell=row.createCell(colIndex);
			colIndex++;
			cell.setCellValue(trans.closingPrincipal);
			
			/*			
			System.out.println(trans.getDate() + "\t" +
								trans.transactionType + "\t" +
								trans.openingPrincipal + "\t" +
								trans.amount + "\t" +
								trans.principalComponent + "\t" +
								trans.interestComponent + "\t" +
								trans.roi + "\t" +
								trans.closingPrincipal);
			*/
			rowIndex++;
		}
		
		row=sheet.createRow(rowIndex);
		cell=row.createCell(1);
		cell.setCellValue("Total");
		cell=row.createCell(3);
		cell.setCellValue(totalAmount);
		cell=row.createCell(4);
		cell.setCellValue(totalPrincipal);
		cell=row.createCell(5);
		cell.setCellValue(totalInterest);
		
		 FileOutputStream outPutStream = null;
	        try {
	            outPutStream = new FileOutputStream("target/Schedule.xlsx");
	            workbook.write(outPutStream);
	        } catch (IOException e) {
	            e.printStackTrace();
	        } finally {
	            if (outPutStream != null) {
	                try {
	                    outPutStream.flush();
	                    outPutStream.close();
	                } catch (IOException e) {
	                    e.printStackTrace();
	                }
	            }
	        }
		
	}
}
