package com.lagou.config;

import com.lagou.enums.MethodType;
import com.lagou.pojo.Configuration;
import com.lagou.pojo.MappedStatement;
import com.mysql.jdbc.StringUtils;
import org.dom4j.Document;
import org.dom4j.DocumentException;
import org.dom4j.Element;
import org.dom4j.io.SAXReader;

import java.io.InputStream;
import java.util.List;

public class XMLMapperBuilder {

    private Configuration configuration;

    public XMLMapperBuilder(Configuration configuration) {
        this.configuration = configuration;
    }

    public void parse(InputStream inputStream) throws DocumentException {

        Document document = new SAXReader().read(inputStream);
        Element rootElement = document.getRootElement();

        String namespace = rootElement.attributeValue("namespace");

        List<Element> allElements = rootElement.selectNodes("//select");

        //======补充其他标签属性的==============
        List<Element> insertListElements = rootElement.selectNodes("//insert");
        List<Element> updateListElements = rootElement.selectNodes("//update");
        List<Element> deleteListElements = rootElement.selectNodes("//delete");
        //计算集合
        allElements.addAll(insertListElements);
        allElements.addAll(updateListElements);
        allElements.addAll(deleteListElements);

        for (Element element : allElements) {
            String id = element.attributeValue("id");
            String resultType = element.attributeValue("resultType");
            String parameterType = element.attributeValue("parameterType");
            String sqlText = element.getTextTrim();
            MappedStatement mappedStatement = new MappedStatement();
            mappedStatement.setId(id);
            mappedStatement.setResultType(resultType);
            mappedStatement.setParameterType(parameterType);
            mappedStatement.setSql(sqlText);

            //补充executeType:
            MethodType methodType = MethodType.getTypeByXmlTag(element.getQName().getName());
            mappedStatement.setExecuteType(methodType);
            String attr = element.attributeValue("useGeneratedKeys");
            mappedStatement.setUseGeneratedKeys(MethodType.INSERT.equals(methodType) && "true".equalsIgnoreCase(attr));
            String idProp = element.attributeValue("pkJavaProp");
            if (!StringUtils.isNullOrEmpty(idProp)) {
                mappedStatement.setPkJavaProp(idProp);
            }

            String key = namespace + "." + id;
            configuration.getMappedStatementMap().put(key, mappedStatement);
        }


    }


}
