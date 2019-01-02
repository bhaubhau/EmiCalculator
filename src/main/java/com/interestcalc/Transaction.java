package com.interestcalc;

import java.text.ParseException;
import java.util.Date;

public class Transaction {
	
	public Date transactionDate;
	public String transactionDateStr;
	public long amount;
	public String transactionType;
	public long openingPrincipal;
	public long principalComponent;
	public long interestComponent;
	public long closingPrincipal;
	public double roi;
	public double dailyInterest;
	
	public Transaction(String transactionDate, long amount, String transactionType) {
		
		this.amount = amount;
		this.transactionType=transactionType;
		this.transactionDateStr=transactionDate;
		try {
			this.transactionDate = InterestCalculator.sdf.parse(transactionDate);
		} catch (ParseException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}		
	}
	
	public String getDate() {
		return transactionDateStr;
	}

}
