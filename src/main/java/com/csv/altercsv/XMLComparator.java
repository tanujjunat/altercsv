package com.csv.altercsv;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.io.StringWriter;
import java.util.List;

import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.custommonkey.xmlunit.DetailedDiff;
import org.custommonkey.xmlunit.Diff;
import org.custommonkey.xmlunit.Difference;
import org.custommonkey.xmlunit.DifferenceConstants;
import org.custommonkey.xmlunit.DifferenceListener;
import org.custommonkey.xmlunit.ElementNameAndTextQualifier;
import org.custommonkey.xmlunit.MatchTracker;
import org.custommonkey.xmlunit.NodeDetail;
import org.custommonkey.xmlunit.XMLUnit;
import org.w3c.dom.Node;
import org.xml.sax.SAXException;
import org.xmlunit.diff.ElementSelector;
import org.xmlunit.diff.ElementSelectors;

public class XMLComparator {

	public static void main(String[] args) throws SAXException, IOException {
		// TODO Auto-generated method stub

		// reading two xml file to compare in Java program
		FileInputStream fis1 = new FileInputStream("C:/Catalog/testing/files/xmlCompare/source.xml");
		FileInputStream fis2 = new FileInputStream("C:/Catalog/testing/files/xmlCompare/target.xml");

		// using BufferedReader for improved performance
		BufferedReader source = new BufferedReader(new InputStreamReader(fis1));
		BufferedReader target = new BufferedReader(new InputStreamReader(fis2));
		
		
		// configuring XMLUnit to ignore white spaces
		XMLUnit.setIgnoreWhitespace(true);
		
		XMLUnit.setIgnoreAttributeOrder(true);
		XMLUnit.setCompareUnmatched(Boolean.FALSE);
		//XMLUnit.setIgnoreDiffBetweenTextAndCDATA(true);

		// comparing two XML using XMLUnit in Java
		List<Difference> differences = compareXML(source, target);

		// showing differences found in two xml files
		printDifferences(differences);
	}

	public static List<Difference> compareXML(Reader source, Reader target) throws SAXException, IOException {
		ElementSelector st = ElementSelectors.conditionalBuilder().whenElementIsNamed("HotelCategory")
				.thenUse(ElementSelectors.byNameAndAttributes("code")).elseUse(ElementSelectors.byName).build();
		
		//creating Diff instance to compare two XML files
        //Diff xmlDiff = DiffBuilder.compare(target).withTest(source).checkForSimilar().withNodeMatcher(new DefaultNodeMatcher(st)).build();
		Diff xmlDiff = new Diff(source, target);
     
        //for getting detailed differences between two xml files
        DetailedDiff detailXmlDiff = new DetailedDiff(xmlDiff);
        //detailXmlDiff.overrideMatchTracker(new MatchTrackerImpl());
        detailXmlDiff.overrideElementQualifier(new ElementNameAndTextQualifier());
        detailXmlDiff.overrideDifferenceListener(new DifferenceListener() {
			
			@Override
			public void skippedComparison(Node control, Node test) {
				// TODO Auto-generated method stub
				
			}
			
			@Override
		    public int differenceFound(Difference diff) {
		      if (diff.getId() == DifferenceConstants.NAMESPACE_URI_ID) {
		        return RETURN_IGNORE_DIFFERENCE_NODES_IDENTICAL;
		      }
		      if (diff.getId() == DifferenceConstants.CHILD_NODELIST_SEQUENCE_ID) {
			        return RETURN_IGNORE_DIFFERENCE_NODES_IDENTICAL;
			      }
		      return RETURN_ACCEPT_DIFFERENCE;
		    }
		});

     
        return detailXmlDiff.getAllDifferences();
	}

	public static void printDifferences(List<Difference> differences) {
		int totalDifferences = differences.size();
		System.out.println("===============================");
		System.out.println("Total differences : " + totalDifferences);
		System.out.println("================================");

		for (Difference difference : differences) {
			System.out.println(difference);
			System.out.println();
		}
	}

}


class MatchTrackerImpl implements MatchTracker {
	 
    public void matchFound(Difference difference) {
        if (difference != null) {
            NodeDetail controlNode = difference.getControlNodeDetail();
            NodeDetail testNode = difference.getTestNodeDetail();
 
            String controlNodeValue = printNode(controlNode.getNode());
            String testNodeValue = printNode(testNode.getNode());
 
            if (controlNodeValue != null) {
                //System.out.println("####################");
                //System.out.println("Control Node: " + controlNodeValue);
            }
            if (testNodeValue != null) {
                //System.out.println("Test Node: " + testNodeValue);
                //System.out.println("####################");
            }
        }
    }
 
    private static String printNode(Node node) {
        if (node != null && node.getNodeType() == Node.ELEMENT_NODE) {
            StringWriter sw = new StringWriter();
            try {
                Transformer t = TransformerFactory.newInstance().newTransformer();
                t.setOutputProperty(OutputKeys.OMIT_XML_DECLARATION, "yes");
                t.transform(new DOMSource(node), new StreamResult(sw));
            } catch (TransformerException te) {
                System.out.println("nodeToString Transformer Exception");
            }
            return sw.toString();
 
        }
        return null;
    }
}
