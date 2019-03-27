package com.wondersgroup.tzscws1.controller;

import java.awt.print.Book;
import java.io.File;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import com.wondersgroup.tzscws1.entity.HeaderDataEntty;
import org.dom4j.Attribute;
import org.dom4j.Document;
import org.dom4j.DocumentException;
import org.dom4j.Element;
import org.dom4j.io.SAXReader;



public class ReadXMLByDom4j {

    private List<HeaderDataEntty> headerList = null;
    private HeaderDataEntty headerData = null;

    public List<HeaderDataEntty> getBooks(File file){

        SAXReader reader = new SAXReader();
        try {
            Document document = reader.read(file);
            Element bookstore = document.getRootElement();
            Iterator storeit = bookstore.elementIterator();

            headerList = new ArrayList<HeaderDataEntty>();
            while(storeit.hasNext()){

                headerData = new HeaderDataEntty();
                Element bookElement = (Element) storeit.next();
                //遍历bookElement的属性
                List<Attribute> attributes = bookElement.attributes();
                for(Attribute attribute : attributes){
                    System.out.println(attribute.getName());
//                    System.out.println(attribute.getValue());



                    if(attribute.getName().equals("id")){
//                        String id = attribute.getValue();//System.out.println(id);
//                        book.setId(Integer.parseInt(id));
                    }
                }

                Iterator bookit = bookElement.elementIterator();
                while(bookit.hasNext()){
                    Element child = (Element) bookit.next();


                    String nodeName = child.getName();
                    System.out.println(nodeName);

                    if(nodeName.equals("eventId")){
                        headerData.setEventId(child.getStringValue());
                    }
                    if(nodeName.equals("hosId")){
                        headerData.setHosId(child.getStringValue());
                    }
                    if(nodeName.equals("requestTime")){
                        headerData.setRequestTime(child.getStringValue());
                    }
                    if(nodeName.equals("headSign")){
                        headerData.setHeadSign(child.getStringValue());
                    }
                    if(nodeName.equals("bodySign")){
                        headerData.setBodySign(child.getStringValue());
                    }

                }
                headerList.add(headerData);

                headerData = null;

            }
        } catch (DocumentException e) {

            e.printStackTrace();
        }


        return headerList;

    }


    /**
     * @param args
     */
    public static void main(String[] args) {
        // TODO Auto-generated method stub
        File file = new File("D:\\Colleague file\\zhoumin\\test.xml");
        List<HeaderDataEntty> bookList = new ReadXMLByDom4j().getBooks(file);
        for(HeaderDataEntty book : bookList){
            System.out.println(book.getEventId());
            System.out.println("*******************");
            System.out.println(book.getHosId());
        }
    }
}
