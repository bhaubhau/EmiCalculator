package com.interestcalc;

import java.text.ParseException;
import java.util.Date;

public class ROI {
	
	public Date effectiveDate;
	public double rate;
	
	public ROI(String effectiveDate, double rate) {
		
		this.rate = rate;			
		try {
			this.effectiveDate = InterestCalculator.sdf.parse(effectiveDate);
		} catch (ParseException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}		
	}
	
}
