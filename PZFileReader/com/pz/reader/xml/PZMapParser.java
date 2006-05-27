/*
 Copyright 2006 Paul Zepernick

 Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except in compliance with the License. You may obtain a copy of the License at 

 http://www.apache.org/licenses/LICENSE-2.0 

 Unless required by applicable law or agreed to in writing, software distributed under the License is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the License for the specific language governing permissions and limitations under the License.  
 */
/*
 * Created on Dec 31, 2004
 */
package com.pz.reader.xml;

import java.io.File;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.jdom.Attribute;
import org.jdom.Document;
import org.jdom.Element;
import org.jdom.input.SAXBuilder;

import com.pz.reader.structure.ColumnMetaData;
import com.pz.reader.util.ParserUtils;

/**
 * @author zepernick
 * 
 * Parses a PZmap definition XML file
 */
public class PZMapParser {

	/**
	 * Constructor
	 * 
	 * @param XMLDocument -
	 *            xml file to be parsed
	 */
	private PZMapParser() {
	}

	/**
	 * Reads the XMLDocument for a PZMap file.
	 * Parses the XML file, and returns a List of ColumnMetaData.
	 * @param xmlFile XML file
	 * @return List of ColumnMetaData
	 * @throws Exception
     * @deprecated
	 */
	public static Map parse(File xmlFile) throws Exception {
		Map mdIndex = null;
		InputStream xmlStream = ParserUtils.createInputStream(xmlFile);
		mdIndex = parse(xmlStream);
		if (mdIndex == null) {
		    mdIndex = new HashMap();
		}
		return mdIndex;
	}

	/**
     * TODO New method based on InputStream.
	 * Reads the XMLDocument for a PZMap file from an InputStream, WebStart combatible.
	 * Parses the XML file, and returns a Map containing Lists of ColumnMetaData.
	 * @param xmlStream
	 * @return
	 * @throws Exception
	 */
	public static Map parse(InputStream xmlStream) throws Exception {
		Document document = null;
		SAXBuilder builder = null;
		Element root = null;
		List columns = null;
		Element xmlElement = null;
		Map mdIndex = new HashMap();

		builder = new SAXBuilder();
		builder.setValidation(true);
		document = builder.build(xmlStream);

		root = document.getRootElement();
		
		//lets first get all of the columns that are declared directly under the PZMAP
		columns = getColumnChildren(root);
		mdIndex.put("detail",columns);
		
		//get all of the "record" elements and the columns under them
		Iterator recordDescriptors = root.getChildren("RECORD").iterator();
		while (recordDescriptors.hasNext()){
		    xmlElement = (Element)recordDescriptors.next();
		    
		    //make sure the id attribute does not have a value of "detail"  this is the harcoded
		    //value we are using to mark columns specified outside of a <RECORD> element
		    if (xmlElement.getAttributeValue("id").equals("detail")){
		        throw new Exception("The ID 'detail' on the <RECORD> element is reserved, please select another id");
		    }
		    
			columns = getColumnChildren(xmlElement);
			XMLRecordElement xmlre = new XMLRecordElement();
		    xmlre.setColumns(columns);
		    xmlre.setIndicator(xmlElement.getAttributeValue("indicator"));
		    xmlre.setElementNumber(convertAttributeToInt(xmlElement.getAttribute("elementNumber")));
		    xmlre.setStartPosition(convertAttributeToInt(xmlElement.getAttribute("startPosition")));
		    xmlre.setEndPositition(convertAttributeToInt(xmlElement.getAttribute("endPosition")));
			mdIndex.put(xmlElement.getAttributeValue("id"),xmlre);
		}
		

		return mdIndex;
	}
	
	//helper to convert to integer
	private static int convertAttributeToInt(Attribute attribute){
	    
	    if (attribute == null){
	        return 0;
	    }
	    
	    try{
	        return attribute.getIntValue();
	    }catch(Exception ex){}
	    
	    return 0;
	    
	}
	
	
	//helper to retrieve the "COLUMN" elements from the given parent
	private static List getColumnChildren(Element parent) throws Exception{
	    List columnResults = new ArrayList();
	    Iterator xmlChildren = parent.getChildren("COLUMN").iterator();
	    Element xmlColumn = null;
	    ColumnMetaData cmd = null;
	    
		while (xmlChildren.hasNext()){
			cmd = new ColumnMetaData();
			xmlColumn = (Element)xmlChildren.next();

			// make sure the name attribute is present on the column
			if (xmlColumn.getAttributeValue("name") == null) {
				throw new Exception(
						"Name attribute is required on the column tag!");
			}

			cmd.setColName(xmlColumn.getAttributeValue("name"));

			// check to see if the column length can be set
			if (xmlColumn.getAttributeValue("length") != null) {
				try {
					cmd.setColLength(Integer.parseInt(xmlColumn
							.getAttributeValue("length")));
				} catch (Exception ex) {
					throw new Exception(
							"LENGTH ATTRIBUTE ON COLUMN ELEMENT MUST BE AN INTEGER.  GOT: "
									+ xmlColumn.getAttributeValue("length"));
				}
			}

			// System.out.println("Column Name: " +
			// column.getAttributeValue("name") + " LENGTH: " +
			// column.getAttributeValue("length"));

			columnResults.add(cmd);

		}
		
		return columnResults;
	    
	}
}
