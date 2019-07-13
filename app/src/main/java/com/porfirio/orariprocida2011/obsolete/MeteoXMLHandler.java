package com.porfirio.orariprocida2011.obsolete;
//OBSOLETA
import android.util.Log;

import com.porfirio.orariprocida2011.activities.OrariProcida2011Activity;

import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

class MeteoXMLHandler extends DefaultHandler {

    private final OrariProcida2011Activity callingActivity;

	public MeteoXMLHandler(OrariProcida2011Activity orariProcida2011Activity) {
			callingActivity=orariProcida2011Activity;
		}

		@Override
	    public void startDocument() throws SAXException {
//	        Log.d("ORARI","Start document");
	    } 
	    
	    @Override
	    public void endDocument() throws SAXException {
//	    	Log.d("ORARI","End document");
	    } 
	    
	    @Override
	    public void startElement(String uri, String localName, String qName,
	    Attributes attributes) throws SAXException {
//	    	Log.d("ORARI","Start element");
	    	if (localName.equals("wind_condition")) {
                /* Get attribute value */
	    		String attr = attributes.getValue("data");
	    		setMeteo(attr);
	    		Log.d("ORARI", attr);
	    	}

	    }

	    private void setMeteo(String attr) {	    	
	    	if (attr.contains(" N ")) {
                callingActivity.meteo.getOsservazione().get(0).setWindDirection(0);
                callingActivity.meteo.getOsservazione().get(0).setWindDirectionString("Nord");
            }
	    	else if (attr.contains(" NE ")) {
                callingActivity.meteo.getOsservazione().get(0).setWindDirection(45);
                callingActivity.meteo.getOsservazione().get(0).setWindDirectionString("Nord-Est");
            }
	    	else if (attr.contains(" E ")) {
                callingActivity.meteo.getOsservazione().get(0).setWindDirection(90);
                callingActivity.meteo.getOsservazione().get(0).setWindDirectionString("Est");
            }
	    	else if (attr.contains(" SE ")) {
                callingActivity.meteo.getOsservazione().get(0).setWindDirection(135);
                callingActivity.meteo.getOsservazione().get(0).setWindDirectionString("Sud-Est");
            }
	    	else if (attr.contains(" S ")) {
                callingActivity.meteo.getOsservazione().get(0).setWindDirection(180);
                callingActivity.meteo.getOsservazione().get(0).setWindDirectionString("Sud");
            }
	    	else if (attr.contains(" SW ")) {
                callingActivity.meteo.getOsservazione().get(0).setWindDirection(225);
                callingActivity.meteo.getOsservazione().get(0).setWindDirectionString("Sud-Ovest");
            }
	    	else if (attr.contains(" W ")) {
                callingActivity.meteo.getOsservazione().get(0).setWindDirection(270);
                callingActivity.meteo.getOsservazione().get(0).setWindDirectionString("Ovest");
            }
	    	else if (attr.contains(" NW ")) {
                callingActivity.meteo.getOsservazione().get(0).setWindDirection(315);
                callingActivity.meteo.getOsservazione().get(0).setWindDirectionString("Nord-Ovest");
            }

            Log.d("ORARI", "Vento da " + callingActivity.meteo.getOsservazione().get(0).getWindDirection());
	    	
	    	//TODO gestire anche i kmh
	    	int pos= attr.indexOf(" at ");
	    	int pos2=attr.indexOf("mph");
	    	String wind=attr.substring(pos+4, pos2-1);
	    	double wkmh=Integer.parseInt(wind)*1.609;
            callingActivity.meteo.getOsservazione().get(0).setWindKmh(wkmh);
	    	if (wkmh<=1)
                callingActivity.meteo.getOsservazione().get(0).setWindBeaufort(0.0);
	    	else if (wkmh>1 && wkmh<6)
                callingActivity.meteo.getOsservazione().get(0).setWindBeaufort(1 + (wkmh - 3) / (5 - 1));
	    	else if (wkmh>=6 && wkmh<12)
                callingActivity.meteo.getOsservazione().get(0).setWindBeaufort(2 + (wkmh - 8.5) / (11 - 6));
	    	else if (wkmh>=12 && wkmh<20)
                callingActivity.meteo.getOsservazione().get(0).setWindBeaufort(3 + (wkmh - 15.5) / (19 - 12));
	    	else if (wkmh>=20 && wkmh<29)
                callingActivity.meteo.getOsservazione().get(0).setWindBeaufort(4 + (wkmh - 24) / (28 - 20));
	    	else if (wkmh>=29 && wkmh<39)
                callingActivity.meteo.getOsservazione().get(0).setWindBeaufort(5 + (wkmh - 33.5) / (38 - 29));
	    	else if (wkmh>=39 && wkmh<50)
                callingActivity.meteo.getOsservazione().get(0).setWindBeaufort(6 + (wkmh - 44) / (49 - 39));
	    	else if (wkmh>=50 && wkmh<62)
                callingActivity.meteo.getOsservazione().get(0).setWindBeaufort(7 + (wkmh - 55.5) / (61 - 50));
	    	else if (wkmh>=62 && wkmh<75)
                callingActivity.meteo.getOsservazione().get(0).setWindBeaufort(8 + (wkmh - 68) / (74 - 62));
	    	else if (wkmh>=75 && wkmh<89)
                callingActivity.meteo.getOsservazione().get(0).setWindBeaufort(9 + (wkmh - 81.5) / (88 - 75));
	    	else if (wkmh>=89 && wkmh<103)
                callingActivity.meteo.getOsservazione().get(0).setWindBeaufort(10 + (wkmh - 95.5) / (102 - 89));
	    	else if (wkmh>=103 && wkmh<118)
                callingActivity.meteo.getOsservazione().get(0).setWindBeaufort(11 + (wkmh - 110) / (117 - 103));
	    	else if (wkmh>=118)
                callingActivity.meteo.getOsservazione().get(0).setWindBeaufort(12.0);
            Log.d("ORARI", "Vento forza " + callingActivity.meteo.getOsservazione().get(0).getWindBeaufort());
		}

		@Override
	    public void endElement(String uri, String localName, String qName)
	    throws SAXException {
//	    	Log.d("ORARI","End element");
	    }

//	    /** Called to get tag characters ( ex:- <name>AndroidPeople</name>
//	    * -- to get AndroidPeople Character ) */
//	    @Override
//	    public void characters(char[] ch, int start, int length)
//	    throws SAXException {
//
//	    if (currentElement) {
//	    currentValue = new String(ch, start, length);
//	    currentElement = false;
//	    }
//
//	    }

}
