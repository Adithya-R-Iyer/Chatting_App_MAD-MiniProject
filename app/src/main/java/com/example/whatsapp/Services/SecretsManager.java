package com.example.whatsapp.Services;

import android.content.Context;
import android.content.res.AssetManager;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import java.io.IOException;
import java.io.InputStream;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

public class SecretsManager {

    public static String firebaseServerKey;
    public static String agoraApiKey;
    public static String agoraAppCertificate;

    public static String readSecrets(Context context, String key) {
        AssetManager assetManager = context.getAssets();
        try{
            InputStream inputStream = assetManager.open("secrets.xml");
            DocumentBuilderFactory documentBuilderFactory = DocumentBuilderFactory.newInstance();
            DocumentBuilder documentBuilder = documentBuilderFactory.newDocumentBuilder();
            Document document = documentBuilder.parse(inputStream);
            document.getDocumentElement().normalize();

            NodeList nodeList = document.getElementsByTagName("secrets");
            Node node = nodeList.item(0);
            if(node.getNodeType() == Node.ELEMENT_NODE) {
                Element element = (Element) node;
                firebaseServerKey = element.getElementsByTagName("firebaseServerKey").item(0).getTextContent();
                agoraApiKey = element.getElementsByTagName("agoraAppId").item(0).getTextContent();
                agoraAppCertificate = element.getElementsByTagName("agoraAppCertificate").item(0).getTextContent();
            }
        } catch (IOException | ParserConfigurationException | SAXException e) {
            e.printStackTrace();
        }
        switch (key) {
            case "firebaseServerKey":
                return firebaseServerKey;
            case "agoraAppId":
                return agoraApiKey;
            case "agoraAppCertificate":
                return agoraAppCertificate;
            default:
                return null;
        }
    }
}
