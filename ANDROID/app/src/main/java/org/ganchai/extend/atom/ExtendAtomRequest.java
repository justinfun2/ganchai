package org.ganchai.extend.atom;

import com.google.api.client.http.GenericUrl;
import com.google.api.client.http.HttpContent;
import com.google.api.client.http.HttpRequest;
import com.google.api.client.http.UrlEncodedContent;
import com.octo.android.robospice.request.googlehttpclient.GoogleHttpClientSpiceRequest;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

public class ExtendAtomRequest extends GoogleHttpClientSpiceRequest<ExtendAtomModelList> {

    String url = null;
    HashMap<String, String > postParameters;

    public ExtendAtomRequest() {
        super(ExtendAtomModelList.class);
    }

    @Override
    public ExtendAtomModelList loadDataFromNetwork() throws Exception {

        HttpRequest request = null;
        GenericUrl genericUrl = new GenericUrl(url);

        if (postParameters == null) {
            request = buildGetRequest(genericUrl);
        } else {
            HttpContent content = new UrlEncodedContent(postParameters);
            request = buildPostRequest(genericUrl, content);
        }

        return readRssXml(request.execute().getContent());
    }

    private HttpRequest buildGetRequest(GenericUrl url) throws IOException {
        System.setProperty("http.keepAlive", "false");
        HttpRequest request = getHttpRequestFactory().buildGetRequest(url);
        customHttpHeader(request);
        return request;
    }

    private HttpRequest buildPostRequest(GenericUrl url, HttpContent content) throws IOException {
        System.setProperty("http.keepAlive", "false");
        HttpRequest request = getHttpRequestFactory().buildPostRequest(url, content);
        customHttpHeader(request);
        return request;
    }

    private void customHttpHeader(HttpRequest request) {
        request.getHeaders().setAcceptEncoding("gzip");
        request.getHeaders().set("Connection", "close");
        request.getHeaders().setAccept("text/html,application/xhtml+xml,application/xml,application/json");
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public void setPostParameters(HashMap<String, String> postParameters) {
        this.postParameters = postParameters;
    }

    public ExtendAtomModelList readRssXml(InputStream rssXml) {

        ExtendAtomModelList rssModelList = new ExtendAtomModelList();

        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();

        try {
            DocumentBuilder builder = factory.newDocumentBuilder();
            Document dom = builder.parse(rssXml);

            Element root = dom.getDocumentElement();

            NodeList items = root.getElementsByTagName("entry");


            for (int i = 0; i < items.getLength(); i++) {
                ExtendAtomModel rssModel = new ExtendAtomModel();

                //item节点
                Element itemNode = (Element) items.item(i);
                NodeList childsNodes = itemNode.getChildNodes();

                for (int j = 0; j < childsNodes.getLength(); j++) {
                    Node node = childsNodes.item(j);

                    if(node.getNodeType() == Node.ELEMENT_NODE){
                        Element childNode = (Element) node;

                        if ("title".equals(childNode.getNodeName())) {
                            rssModel.setTitle(childNode.getFirstChild().getNodeValue());
                        } else if ("id".equals(childNode.getNodeName())) {
                            if (childNode.getChildNodes().getLength() > 1) {
                                rssModel.setId(childNode.getFirstChild().getNextSibling().getNodeValue());
                            } else {
                                rssModel.setId(childNode.getFirstChild().getNodeValue());
                            }
                        } else if ("published".equals(childNode.getNodeName())) {
                            rssModel.setPublished(childNode.getFirstChild().getNodeValue());
                        } else if ("summary".equals(childNode.getNodeName())) {
                            rssModel.setSummary(childNode.getFirstChild().getNodeValue());
                        }
                    }
                }

                rssModelList.add(rssModel);
            }

        } catch (Exception e) {
            e.printStackTrace();
        }

        return rssModelList;
    }
}
