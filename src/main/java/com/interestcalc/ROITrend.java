package com.interestcalc;

import java.text.ParseException;
import java.util.ArrayList;
import java.util.Date;

public class ROITrend {
	
	public static ArrayList<ROI> roi=new ArrayList<ROI>();
	
	public static void addEntry(String effectiveDate, double rate) {
				
		Date entryDate=null;
		try {
			entryDate=InterestCalculator.sdf.parse(effectiveDate);
		} catch (ParseException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		ROI rateEntry=new ROI(effectiveDate,rate);
		for(int i=0;i<roi.size();i++) {
			if(entryDate.equals(roi.get(i).effectiveDate))
			{
				roi.remove(i);
				roi.add(i, rateEntry);
				return;
			}
			if(entryDate.before(roi.get(i).effectiveDate))
			{
				roi.add(i, rateEntry);
				return;
			}
		}
		roi.add(rateEntry);
	}	
	
	public static double getRate(String roiDate) {
				
		Date entryDate=null;
		try {
			entryDate=InterestCalculator.sdf.parse(roiDate);
		} catch (ParseException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		if(entryDate.before(roi.get(0).effectiveDate)) {
			return roi.get(0).rate;
		}
		else if(entryDate.after(roi.get(roi.size()-1).effectiveDate)) {
			return roi.get(roi.size()-1).rate;
		}
		else {
			for(int i=0;i<roi.size();i++) {
				long lBound=roi.get(i).effectiveDate.getTime();
				long entryTime=entryDate.getTime();
				long uBound=0;
				try {
					uBound=roi.get(i+1).effectiveDate.getTime();
				} catch (IndexOutOfBoundsException e) {
					uBound=lBound+1;
				}
				if((entryTime>=lBound)&&(entryTime<uBound))
				{					
					return roi.get(i).rate;
				}
			}			
		}
		return 0;
	}
	

}
