package com.interestcalc;

import java.text.ParseException;
import java.util.ArrayList;
import java.util.Date;

public class Transactions {
	
	public static ArrayList<Transaction> transactions=new ArrayList<Transaction>();	
	
	private static Transaction addEntry(Transaction transaction) {
		
		Date entryDate=transaction.transactionDate;		
		
		for(int i=0;i<transactions.size();i++) {
			if(entryDate.equals(transactions.get(i).transactionDate))
			{
				transactions.remove(i);
				transactions.add(i, transaction);
				return transaction;
			}
			if(entryDate.before(transactions.get(i).transactionDate))
			{
				transactions.add(i, transaction);
				return transaction;
			}
		}
		transactions.add(transaction);
		return transaction;
	}	
	
	public static Transaction addPartDisbursement(String transactionDate, long amount) {
		Transaction transaction=new Transaction(transactionDate, amount, "PartDisbursement");		
		transaction.roi=ROITrend.getRate(transactionDate);
		return addEntry(transaction);		
	}
	
	public static Transaction addPartPayment(String transactionDate, long amount) {
		Transaction transaction=new Transaction(transactionDate, amount, "PartPayment");
		transaction.roi=ROITrend.getRate(transactionDate);
		return addEntry(transaction);			
	}
	
	public static Transaction addPreEmi(Transaction transaction) {
		transaction.transactionType="PreEMI";
		transaction.roi=ROITrend.getRate(transaction.transactionDateStr);
		return addEntry(transaction);			
	}
	
	public static Transaction addEmi(Transaction transaction) {
		transaction.transactionType="EMI";
		transaction.roi=ROITrend.getRate(transaction.transactionDateStr);
		return addEntry(transaction);					
	}
	
	public static Transaction addPreEMIInterestCorrection(String transactionDate, long interest) {
		Transaction transaction=new Transaction(transactionDate, 0, "PreEMIInterestCorrection");
		transaction.interestComponent=interest;
		transaction.roi=ROITrend.getRate(transactionDate);
		return addEntry(transaction);				
	}	
	
	public static Transaction addEMIInterestCorrection(String transactionDate, long interest) {
		Transaction transaction=new Transaction(transactionDate, 0, "EMIInterestCorrection");
		transaction.interestComponent=interest;
		transaction.roi=ROITrend.getRate(transactionDate);
		return addEntry(transaction);				
	}
	
	public static Transaction addGeneralCorrection(Transaction transaction) {
		transaction.transactionType="GeneralCorrection";
		return addEntry(transaction);				
	}
	
	public static Transaction getTransaction(String transactionDate) {
		
		Date entryDate=null;
		try {
			entryDate = InterestCalculator.sdf.parse(transactionDate);
		} catch (ParseException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		for(int i=0;i<transactions.size();i++) {
			if(entryDate.equals(transactions.get(i).transactionDate))
			{
				return transactions.get(i);
			}
		}
		return null;
	}

}
