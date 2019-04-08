package com.stars.util;

import org.w3c.dom.Document;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/**
 * xml信息读取工具类
 */
public class XmlReadUtil {
    /**
     * 读取xml文件信息
     *
     * @param filePath
     * @return
     */
    public static List<com.stars.util._XmlNode> read(String filePath) {
        List<com.stars.util._XmlNode> list = new ArrayList<com.stars.util._XmlNode>();
        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        DocumentBuilder builder;
        try {
            builder = factory.newDocumentBuilder();
            Document doc = builder.parse(filePath);
            NodeList parentNodeList = doc.getChildNodes();
            Node node;
            for (int i = 0; i < parentNodeList.getLength(); i++) {
                node = parentNodeList.item(i);
                if (node.getNodeName().equals("#comment")) continue;
                list.add(readNode(node));
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return list;
    }

    /**
     * 读取节点信息
     *
     * @param node
     * @return
     */
    private static com.stars.util._XmlNode readNode(Node node) {
        com.stars.util._XmlNode xmlNode;
        com.stars.util._XmlNode xmlNode2;
        HashMap attrMap = new HashMap();
        //如果该节点有属性
        if (node.getAttributes() != null && node.getAttributes().getLength() != 0) {
            NamedNodeMap nodeMap = node.getAttributes();
            for (int i = 0; i < nodeMap.getLength(); i++) {
                attrMap.put(nodeMap.item(i).getNodeName(),
                        nodeMap.item(i).getNodeValue());
            }
        }
        List<com.stars.util._XmlNode> xmlNodeList = new ArrayList<com.stars.util._XmlNode>();
        //如果该节点还有子节点,递归 ，没有就获取值
        if (node.getChildNodes().getLength() != 1) {
            for (int j = 0; j < node.getChildNodes().getLength(); j++) {
                String nodeName = node.getChildNodes().item(j).getNodeName();
                if (nodeName.equals("#text")) continue;
                Node tempNode = node.getChildNodes().item(j);
                xmlNodeList.add(readNode(tempNode));
            }
        } else {
            xmlNode = new com.stars.util._XmlNode(node.getNodeName(), attrMap, node.getTextContent());
            return xmlNode;
        }
        if (xmlNodeList != null) {
            xmlNode2 = new _XmlNode(node.getNodeName(), attrMap, xmlNodeList);
            return xmlNode2;
        }
        return null;
    }
}

