package com.example.springtilimuzxml.service;

import com.example.springtilimuzxml.payload.ApiResponse;
import org.apache.commons.io.IOUtils;
import org.docx4j.XmlUtils;
import org.docx4j.openpackaging.exceptions.Docx4JException;
import org.docx4j.openpackaging.exceptions.InvalidFormatException;
import org.docx4j.openpackaging.packages.WordprocessingMLPackage;
import org.docx4j.openpackaging.parts.WordprocessingML.MainDocumentPart;
import org.docx4j.openpackaging.parts.relationships.Namespaces;
import org.docx4j.openpackaging.parts.relationships.RelationshipsPart;
import org.docx4j.relationships.Relationship;
import org.python.core.PyObject;
import org.python.google.common.io.Resources;
import org.python.util.PythonInterpreter;
import org.springframework.boot.autoconfigure.web.WebProperties;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import javax.xml.bind.JAXBException;
import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

@Service
public class LatinToKirilService {

    public String replacer(PythonInterpreter pythonInterpreter, String text) throws UnsupportedEncodingException, UnsupportedEncodingException {
        PyObject eval = pythonInterpreter.eval("to_cyrillic('" + text + "')");
        String response = eval.toString();
        byte bytes[] = response.getBytes("ISO-8859-1");
        String result = new String(bytes, StandardCharsets.UTF_8);
        return result;
    }

    public void saveFileToLocal(MultipartFile file) throws IOException {

        File docx = new File(file.getOriginalFilename());
        FileOutputStream os = new FileOutputStream(docx);
        os.write(file.getBytes());

    }

    public String getXML(MultipartFile file) throws IOException {

        File docx = new File(file.getOriginalFilename());

        ZipFile zipFile = new ZipFile(docx);
        ZipEntry zipEntry = zipFile.getEntry("word/document.xml");

        InputStream inputStream = zipFile.getInputStream(zipEntry);
        String result = "";
        result = IOUtils.toString(inputStream, StandardCharsets.UTF_8);
        inputStream.close();

        return result;

    }

    public String translatedXML(String xml) {
        PythonInterpreter pythonInterpreter = new PythonInterpreter();
        pythonInterpreter.execfile(Resources.getResource("scripts/translate.py").getPath());

        Matcher matcher = Pattern.compile("(?:<w:t>)(.*?)(<\\/w:t>)").matcher(xml);

        String t = "nimaydi";

        while (matcher.find()) {
            t = matcher.group(0).substring(5, matcher.group(0).length()-6);
            try {
                xml = xml.replace(t, replacer(pythonInterpreter, t));
            } catch (Exception e) {
                System.out.println(t);
            }
        }

        xml = xml.replaceAll("w:ваl", "w:val");

        return xml;
    }

    public void replaceXMLFileAndSave(MultipartFile file, String xml) throws Docx4JException, JAXBException {
        WordprocessingMLPackage wordMLPackage = WordprocessingMLPackage.load(new File(file.getOriginalFilename()));

        RelationshipsPart relationshipsPart = wordMLPackage.getMainDocumentPart().getRelationshipsPart();

        Relationship sourceRelationship = wordMLPackage.getMainDocumentPart().getSourceRelationship();

        MainDocumentPart mainDocumentPart = new MainDocumentPart();

        wordMLPackage.getParts().put(mainDocumentPart);
        mainDocumentPart.setPackage(wordMLPackage);

        wordMLPackage.setPartShortcut(mainDocumentPart, Namespaces.DOCUMENT);

        mainDocumentPart.setSourceRelationship(sourceRelationship);
        mainDocumentPart.setRelationships(relationshipsPart);

        mainDocumentPart.setJaxbElement((org.docx4j.wml.Document) XmlUtils.unmarshalString(xml));

//        wordMLPackage.save(new File(getClass().getClassLoader().getResource("uploads").getPath()+"/Result.docx"));
        wordMLPackage.save(new File("Result.docx"));
    }

    public ApiResponse translator(MultipartFile file) throws IOException, JAXBException, Docx4JException {
        try {
            saveFileToLocal(file);
            String xml = getXML(file);
            xml = translatedXML(xml);
            replaceXMLFileAndSave(file, xml);
            new File(file.getOriginalFilename()).delete();
            return new ApiResponse("success", true);
        } catch (Exception e) {
            System.out.println(e.getMessage());
            return new ApiResponse("failed", false);
        }
    }
}
