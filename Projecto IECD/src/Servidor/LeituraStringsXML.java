package Servidor;

import java.io.StringReader;
import java.util.ArrayList;
import java.util.concurrent.Semaphore;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;

import classes_auxiliares.Poi;
import classes_auxiliares.PoiTuple;

public class LeituraStringsXML {
	
	AcessoFicheiros ficheiros;
	
	public LeituraStringsXML(Semaphore accessFile) {
		ficheiros = new AcessoFicheiros(accessFile);
	}

	public String getTipo(String mensagemRecebida) {

		System.out.println("mensagemRecebida: " + mensagemRecebida);

		// Parsing da stringXML
		DocumentBuilderFactory docFactory = DocumentBuilderFactory
				.newInstance();
		DocumentBuilder docBuilder;
		Document parserDoc = null;
		try {
			docBuilder = docFactory.newDocumentBuilder();
			parserDoc = docBuilder.parse(new InputSource(new StringReader(
					mensagemRecebida)));
			// Document parserDoc = docBuilder.parse (fileName); // Se fosse
			// através de ficheiro
		} catch (Exception e) {
			e.printStackTrace();
		}

		String expression = "/tipo/@id"; // expressão para o user
		XPath xpath = XPathFactory.newInstance().newXPath();
		NodeList nodes;
		String tipo = null;
		try {
			nodes = (NodeList) xpath.evaluate(expression, parserDoc,
					XPathConstants.NODESET);
			tipo = nodes.item(0).getNodeValue();
		} catch (XPathExpressionException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		System.out.println("recebi tipo = " + tipo);
		return tipo;
	}

	public String getChildNode(String mensagemRecebida, String nodeName) {

		// Parsing da stringXML
		DocumentBuilderFactory docFactory =  DocumentBuilderFactory
				.newInstance();
		DocumentBuilder docBuilder;
		Document parserDoc = null;
		try {
			docBuilder = docFactory.newDocumentBuilder();
			parserDoc = docBuilder.parse(new InputSource(new StringReader(
					mensagemRecebida)));
			// Document parserDoc = docBuilder.parse (fileName); // Se fosse
			// através de ficheiro
		} catch (Exception e) {
			e.printStackTrace();
		}

		String expression = "/tipo"; // expressão para o user
		XPath xpath = XPathFactory.newInstance().newXPath();
		NodeList nodes = null;
		String valor = null;
		try {
			nodes = (NodeList) xpath.evaluate(expression, parserDoc,
					XPathConstants.NODESET);
			valor = nodes.item(0).getNodeValue();
		} catch (XPathExpressionException e) {
			e.printStackTrace();
		}
		
		Node node = nodes.item(0);
		valor = ((Element) node).getElementsByTagName(nodeName)
				.item(0).getTextContent();
		
		System.out.println("nó "+nodeName+" = " + valor);
		return valor;
	}

	public String gerarResposta(String[][] parametros) {
		String xmlString = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>";
		xmlString += "<" + parametros[0][0] + " id=\"" + parametros[0][1]
				+ "\">";

		for (int i = 1; i < parametros.length; i++) {
			xmlString += "<" + parametros[i][0] + ">" + parametros[i][1] + "</"
					+ parametros[i][0] + ">";
		}
		xmlString += "</" + parametros[0][0] + ">";
		return xmlString;
	}

	
	public String gerarXMLPoiList(ArrayList<PoiTuple> PoiList, String TipoMsg) {
		String xmlString = "";
		xmlString+="<?xml version=\"1.0\" encoding=\"UTF-8\"?>";
		xmlString += "<tipo id=\"" + TipoMsg +"\">";
		for (PoiTuple PoiTuple : PoiList){
			Poi poi = ficheiros.getPoi(PoiTuple.getdesignacao(), PoiTuple.getFilename());
			xmlString += gerarXMLPoi( poi,PoiTuple.getFilename(), false, TipoMsg);
		}
		xmlString += "</tipo>";
		return xmlString;
	}
	
	
	public String gerarXMLPoi(Poi Poi,String Filename, boolean DocCompleto, String TipoMsg) {
		String xmlString = "";
		 if (DocCompleto) {xmlString+="<?xml version=\"1.0\" encoding=\"UTF-8\"?>";
		 xmlString += "<tipo id=\"" + TipoMsg +"\">";
		 }
		 xmlString += "<POI>";
		 xmlString += "<file>"+Filename +"</file>";
		 xmlString += "<imagem>"+Poi.getImageName()+"</imagem>";
		 xmlString += "<designacao>"+Poi.getDesignacao() +"</designacao>";
		 xmlString += "<criador>"+Poi.getCriador() +"</criador>";
		 xmlString += "<descricao>"+Poi.getDescricao() +"</descricao>";
		 xmlString += "<localizacao>"+Poi.getLocalizacao() +"</localizacao>";
		 xmlString += "<regiao>"+Poi.getRegiao() +"</regiao>";
		 xmlString += "<adorei>"+Poi.getAdorei() +"</adorei>";
		 xmlString += "<localizacao>"+Poi.getLocalizacao() +"</localizacao>";
		 xmlString += "<gostei>"+Poi.getGostei() +"</gostei>";
		 xmlString += "<n_voltar>"+Poi.getNao_vou_voltar() +"</n_voltar>";
		 xmlString += "<multimedia>";
		 for (String documento : Poi.getMultimedia()){
			 xmlString += "<documento>"+documento+"</documento>";
		 }
		 xmlString +="</multimedia>";
		 xmlString += "<categoria>";
		 for (String tipocategoria : Poi.getCategorias()){
			 xmlString += "<tipo>"+tipocategoria+"</tipo>";
		 }
		 xmlString +="</categoria>";
		 xmlString += "</POI>";
		 if (DocCompleto) {xmlString += "</tipo>";}
		return xmlString;
	}
		
}