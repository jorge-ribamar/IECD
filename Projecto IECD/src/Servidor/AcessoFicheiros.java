package Servidor;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.concurrent.Semaphore;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import classes_auxiliares.Poi;
import classes_auxiliares.PoiTuple;

public class AcessoFicheiros {

	String baseFilePath;
	private DocumentBuilder parser;
	private Document parserDoc;
	private ArrayList<String> regionFileNames;
	private Semaphore accessoFicheiro;
	String ImageFilePath = "C:/Users/ASUS/workspace/Projecto IECD pos-ferias/WebContent/imagens/POIs/";

	public AcessoFicheiros(Semaphore accessoFicheiro) {
		baseFilePath = "C:/Users/ASUS/workspace/Projecto IECD pos-ferias/src/Server_Files/";
		regionFileNames = new ArrayList<String>();
		regionFileNames.add("aveiro.xml");
		regionFileNames.add("beja.xml");
		regionFileNames.add("braga.xml");
		regionFileNames.add("bragança.xml");
		regionFileNames.add("castelo branco.xml");
		regionFileNames.add("coimbra.xml");
		regionFileNames.add("évora.xml");
		regionFileNames.add("faro.xml");
		regionFileNames.add("guarda.xml");
		regionFileNames.add("leiria.xml");
		regionFileNames.add("lisboa.xml");
		regionFileNames.add("portalegre.xml");
		regionFileNames.add("porto.xml");
		regionFileNames.add("santarém.xml");
		regionFileNames.add("setúbal.xml");
		regionFileNames.add("viana do castelo.xml");
		regionFileNames.add("vila real.xml");
		regionFileNames.add("viseu.xml");
		this.accessoFicheiro = accessoFicheiro;
	}

	private void obterAcessoFicheiro() {
		try {
			accessoFicheiro.acquire();
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
	}

	private void libertarAcessoFicheiros() {
		accessoFicheiro.release();
	}

	/**
	 * Cria um parser XML com o fileName dado em parametro
	 * 
	 * @param fileName
	 *            String com o nome di ficheiro XML
	 * @throws ParserConfigurationException
	 * @throws SAXException
	 * @throws IOException
	 */
	public void createParser(String fileName) {
		obterAcessoFicheiro();
		try {
			parser = DocumentBuilderFactory.newInstance().newDocumentBuilder();
		} catch (ParserConfigurationException e) {
			e.printStackTrace();
		}
		try {
			parserDoc = parser.parse(new java.io.File(baseFilePath + fileName));
		} catch (SAXException | IOException e) {
			e.printStackTrace();
		}

	}

	/**
	 * Faz a listagem dos users que estão no ficheiro usersDB e imprime o
	 * resultado
	 * 
	 * @throws ParserConfigurationException
	 * @throws SAXException
	 * @throws IOException
	 * @throws XPathExpressionException
	 */
	public ArrayList<String> getUsers() {
		ArrayList<String> Users = new ArrayList<String>();
		String expression = "//user/@userName";
		createParser("usersDB.xml");
		XPath xpath = XPathFactory.newInstance().newXPath();
		NodeList nodes = null;
		try {
			nodes = (NodeList) xpath.evaluate(expression, parserDoc,
					XPathConstants.NODESET);
		} catch (XPathExpressionException e) {
			e.printStackTrace();
		}
		for (int i = 0; i < nodes.getLength(); i++) {
			Users.add(nodes.item(i).getTextContent());
		}
		libertarAcessoFicheiros();
		return Users;
	}

	public ArrayList<PoiTuple> getPOIsUser(String User) {
		ArrayList<PoiTuple> POIs = new ArrayList<PoiTuple>();
		for (String fileName : regionFileNames) {
			createParser(fileName);
			XPath xpath = XPathFactory.newInstance().newXPath();
			String expressao = "//poi[@criador='" + User + "']";
			NodeList nodes;
			try {
				nodes = (NodeList) xpath.evaluate(expressao, parserDoc,
						XPathConstants.NODESET);
				for (int n = 0; n < nodes.getLength(); n++) {
					String POIname = nodes.item(n).getAttributes()
							.getNamedItem("designacao").getNodeValue();
					POIs.add(new PoiTuple(POIname, fileName));
				}
			} catch (XPathExpressionException e) {
				e.printStackTrace();
			}
			libertarAcessoFicheiros();
		}
		
		return POIs;
	}

	/**
	 * Mostra os Pois da uma região dada em parametro
	 * 
	 * @param NomeRegiao
	 *            String com o nome da região
	 * @throws ParserConfigurationException
	 * @throws SAXException
	 * @throws IOException
	 * @throws XPathExpressionException
	 */
	public ArrayList<PoiTuple> showPOIRegion(String NomeRegiao) {

		ArrayList<PoiTuple> POIs = new ArrayList<PoiTuple>();
		// O zero nca e usado pk e p voltar ao menu... Assim, em zero guarda-se
		// o valor da regiao p abrir posteriormente
		createParser(NomeRegiao.toLowerCase() + ".xml");
		XPath xpath = XPathFactory.newInstance().newXPath();
		String expression = "//poi";
		NodeList nodes;
		try {
			nodes = (NodeList) xpath.evaluate(expression, parserDoc,
					XPathConstants.NODESET);
			for (int n = 0; n < nodes.getLength(); n++) {
				String POIname = nodes.item(n).getAttributes()
						.getNamedItem("designacao").getNodeValue();
				POIs.add(new PoiTuple(POIname, NomeRegiao.toLowerCase()
						+ ".xml"));
			}
		} catch (XPathExpressionException e) {
			e.printStackTrace();
		}
		libertarAcessoFicheiros();
		return POIs;

	}

	/**
	 * Mostra todos os POIs de uma categoria dada em parametro
	 * 
	 * @param Categoria
	 *            String com o nome da categoria
	 * @throws ParserConfigurationException
	 * @throws SAXException
	 * @throws IOException
	 * @throws XPathExpressionException
	 */
	public ArrayList<PoiTuple> showPOICategorie(String Categoria) {
		ArrayList<PoiTuple> POIs = new ArrayList<PoiTuple>();
		// O zero nca e usado pk e p voltar ao menu... Assim, em zero guarda-se
		// o valor da regiao p abrir posteriormente
		for (String fileName : regionFileNames) {
			createParser(fileName);
			XPath xpath = XPathFactory.newInstance().newXPath();
			String expression = "//poi[categorias/tipo='" + Categoria + "']";
			NodeList nodes;
			try {
				nodes = (NodeList) xpath.evaluate(expression, parserDoc,
						XPathConstants.NODESET);
				for (int n = 0; n < nodes.getLength(); n++) {
					String POIname = nodes.item(n).getAttributes()
							.getNamedItem("designacao").getNodeValue();
					POIs.add(new PoiTuple(POIname, fileName));
				}
			} catch (XPathExpressionException e) {
				e.printStackTrace();
			}
			libertarAcessoFicheiros();
		}
		return POIs;
	}

	public ArrayList<PoiTuple> searchByName(String POIname) {
		ArrayList<PoiTuple> POIs = new ArrayList<PoiTuple>();
		for (String file : regionFileNames) {
			if (existPOI(file, POIname)) {
				POIs.add(new PoiTuple(POIname, file));
			}
		}
		if (POIs.size() == 0) {
			return null;
		}
		return POIs;
	}

	public Poi getPoi(String designacao, String filename) {
		Poi POI = new Poi(designacao);
		createParser(filename);

		XPath xpath = XPathFactory.newInstance().newXPath();
		String expressao = "//regiao/@designacao";

		NodeList nodes = null;
		try {
			nodes = (NodeList) xpath.evaluate(expressao, parserDoc,
					XPathConstants.NODESET);
		} catch (XPathExpressionException e) {
			e.printStackTrace();
		}
		Node node = nodes.item(0);
		String regiao = node.getNodeValue();

		POI.setRegiao(regiao);

		expressao = "//poi[@designacao='" + designacao + "']";

		nodes = null;
		try {
			nodes = (NodeList) xpath.evaluate(expressao, parserDoc,
					XPathConstants.NODESET);
		} catch (XPathExpressionException e) {
			e.printStackTrace();
		}

		node = nodes.item(0);

		String criador = node.getAttributes().getNamedItem("criador")
				.getNodeValue();
		POI.setCriador(criador);

		String loc = node.getAttributes().getNamedItem("localizacao")
				.getNodeValue();
		POI.setLocalizacao(loc);

		String descricao = ((Element) node).getElementsByTagName("descricao")
				.item(0).getTextContent();

		POI.setDescricao(descricao);
		
		 Node imageAtribute = node.getAttributes().getNamedItem("imagem");
		
		String imageName = "";
		 if (imageAtribute!=null){
			 imageName = imageAtribute.getNodeValue();
		 }

		POI.setImageName(imageName);

		NodeList categorias = ((Element) node)
				.getElementsByTagName("categorias").item(0).getChildNodes();
		for (int i = 0; i < categorias.getLength(); i++) {
			if (categorias.item(i).getNodeType() == Node.ELEMENT_NODE) {
				POI.addCategoria(categorias.item(i).getTextContent());
			}
		}

		NodeList multimedia = ((Element) node)
				.getElementsByTagName("multimedia").item(0).getChildNodes();

		if (multimedia.getLength() != 0) {
			for (int m = 0; m < multimedia.getLength(); m++) {
				if (multimedia.item(m).getNodeType() == Node.ELEMENT_NODE) {
					POI.addMultimedia(multimedia.item(m).getTextContent());
				}
			}
		}

		int nAdorei = ((Element) node).getElementsByTagName("adorei")
				.getLength();
		POI.setAdorei(nAdorei);

		int Gostei = ((Element) node).getElementsByTagName("gostei")
				.getLength();
		POI.setGostei(Gostei);

		int nNaoVoltar = ((Element) node)
				.getElementsByTagName("nao_vou_voltar").getLength();
		POI.setNao_vou_voltar(nNaoVoltar);
		libertarAcessoFicheiros();
		return POI;
	}

	/**
	 * Edita a opinião do POI com o nome dado em parametro
	 * 
	 * @param namePOI
	 *            String com o nome do POI
	 * @param regiaoFilename
	 *            String com o nome do ficheiro onde se encontra o POI
	 * @throws XPathExpressionException
	 * @throws ParserConfigurationException
	 * @throws SAXException
	 * @throws IOException
	 * @throws TransformerException
	 */
	public void darOpiniao(String nomePOI, String regiaoFilename,
			String Opiniao, String User) {
		if (Opiniao.equals("adorei")) {
			System.out.println("adorei");
			removeOpinion(nomePOI, regiaoFilename, User);
			adorar(nomePOI, regiaoFilename, User);
		}
		if (Opiniao.equals("gostei")) {
			System.out.println("gostei");
			removeOpinion(nomePOI, regiaoFilename, User);
			gostar(nomePOI, regiaoFilename, User);
		}
		if (Opiniao.equals("nao_vou_voltar")) {
			System.out.println("nao_vou_voltar");
			removeOpinion(nomePOI, regiaoFilename, User);
			naoVoltar(nomePOI, regiaoFilename, User);
		}

	}

	/**
	 * Permite obter o nó onde está um POI com o nome inserido em parametro
	 * 
	 * @param nomePOI
	 *            Nome do POI a obter
	 * @param regiaoFilename
	 *            String com o nome do ficheiro onde ele se encontra
	 * @return retorna o Nó pretendido
	 * @throws ParserConfigurationException
	 * @throws SAXException
	 * @throws IOException
	 * @throws XPathExpressionException
	 */
	private Node getPOINode(String nomePOI, String regiaoFilename) {
		String filename = regiaoFilename;
		createParser(filename);
		XPath xpath = XPathFactory.newInstance().newXPath();
		String expression = "//poi[@designacao='" + nomePOI + "']";
		NodeList nodes;
		try {
			nodes = (NodeList) xpath.evaluate(expression, parserDoc,
					XPathConstants.NODESET);
			libertarAcessoFicheiros();
			return nodes.item(0);
		} catch (XPathExpressionException e) {
			e.printStackTrace();
		}
		libertarAcessoFicheiros();
		return null;
	}

	/**
	 * Adiciona um "adoro" a um POI
	 * 
	 * @param namePOI
	 *            String com o nome do POI a editar
	 * @param regiaoFilename
	 *            String com o nome do ficheiro onde ele se encontra
	 * @throws XPathExpressionException
	 * @throws ParserConfigurationException
	 * @throws SAXException
	 * @throws IOException
	 * @throws TransformerException
	 */
	private void adorar(String namePOI, String regiaoFilename, String userName) {
		Node nodePOI = getPOINode(namePOI, regiaoFilename);
		obterAcessoFicheiro();
		Node nodeOpinioes = ((Element) nodePOI)
				.getElementsByTagName("opinioes").item(0);
		Element adoreiNode = parserDoc.createElement("adorei");
		adoreiNode.setTextContent(userName);
		nodeOpinioes.appendChild(adoreiNode);
		writeXML(parserDoc, baseFilePath + regiaoFilename);
		libertarAcessoFicheiros();
	}

	/**
	 * Adiciona um "gosto" a um POI
	 * 
	 * @param namePOI
	 *            String com o nome do POI a editar
	 * @param regiaoFilename
	 *            String com o nome do ficheiro onde ele se encontra
	 * @throws XPathExpressionException
	 * @throws ParserConfigurationException
	 * @throws SAXException
	 * @throws IOException
	 * @throws TransformerException
	 */
	private void gostar(String namePOI, String regiaoFilename, String userName) {
		Node nodePOI = getPOINode(namePOI, regiaoFilename);
		obterAcessoFicheiro();
		Node nodeOpinioes = ((Element) nodePOI)
				.getElementsByTagName("opinioes").item(0);
		Element gosteiNode = parserDoc.createElement("gostei");
		gosteiNode.setTextContent(userName);
		nodeOpinioes.appendChild(gosteiNode);
		writeXML(parserDoc, baseFilePath + regiaoFilename);
		libertarAcessoFicheiros();
	}

	/**
	 * Adiciona um "não voltar" a um POI
	 * 
	 * @param namePOI
	 *            String com o nome do POI a editar
	 * @param regiaoFilename
	 *            String com o nome do ficheiro onde ele se encontra
	 * @throws XPathExpressionException
	 * @throws ParserConfigurationException
	 * @throws SAXException
	 * @throws IOException
	 * @throws TransformerException
	 */
	private void naoVoltar(String namePOI, String regiaoFilename,
			String userName) {
		Node nodePOI = getPOINode(namePOI, regiaoFilename);
		obterAcessoFicheiro();
		Node nodeOpinioes = ((Element) nodePOI)
				.getElementsByTagName("opinioes").item(0);
		Element naoVoltarNode = parserDoc.createElement("nao_vou_voltar");
		naoVoltarNode.setTextContent(userName);
		nodeOpinioes.appendChild(naoVoltarNode);
		writeXML(parserDoc, baseFilePath + regiaoFilename);
		libertarAcessoFicheiros();
	}

	/**
	 * Permite remover uma opinião de um POI
	 * 
	 * @param namePOI
	 *            String com o nome do POI que vai ser editado
	 * @param regiaoFilename
	 *            String com o filename do POI que está a ser editado
	 * @throws ParserConfigurationException
	 * @throws SAXException
	 * @throws IOException
	 * @throws XPathExpressionException
	 * @throws TransformerException
	 */
	private void removeOpinion(String nomePOI, String regiaoFilename,
			String userName) {
		String filename = regiaoFilename;
		createParser(filename);
		XPath xpath = XPathFactory.newInstance().newXPath();
		String expressao = "//poi[@designacao='" + nomePOI
				+ "']/opinioes/child::node()[text() ='" + userName + "']";

		try {
			NodeList nodes;
			nodes = (NodeList) xpath.evaluate(expressao, parserDoc,
					XPathConstants.NODESET);
			Node node = nodes.item(0);
			if (node != null) {
				node.getParentNode().removeChild(node);
				writeXML(parserDoc, baseFilePath + regiaoFilename);
			}
		} catch (XPathExpressionException e) {
			e.printStackTrace();
		}
		libertarAcessoFicheiros();
	}

	/**
	 * Escreve num ficheiro XML um documento recebido
	 * 
	 * @param doc
	 *            documento a ser gravado
	 * @param fileName
	 *            String com o nome do ficheiro xml
	 * @throws TransformerException
	 */
	private void writeXML(Document doc, String fileName) {
		try {
			TransformerFactory transformerFactory = TransformerFactory
					.newInstance();
			Transformer transformer = transformerFactory.newTransformer();
			DOMSource source = new DOMSource(doc);
			StreamResult result = new StreamResult(new File(fileName));
			transformer.transform(source, result);
		} catch (TransformerException e) {
			e.printStackTrace();
		}
	}

	/**
	 * Verifica dentro de um xml se existe um poi com o nome dado em argumento
	 * 
	 * @param fileName
	 *            String com o nome do ficheiro XML
	 * @param nomePoi
	 *            String com o nome do POI a ser pesquisado
	 * @return Retorna true se encontrar o POI, caso contrário retorna false
	 * @throws ParserConfigurationException
	 * @throws SAXException
	 * @throws IOException
	 * @throws XPathExpressionException
	 */
	public boolean existPOI(String fileName, String nomePoi) {
		createParser(fileName.toLowerCase());
		XPath xpath = XPathFactory.newInstance().newXPath();
		String expression = "//poi[@designacao=\"" + nomePoi + "\"]";
		try {
			System.out.println(xpath.evaluate(expression, parserDoc));
			System.out.println(xpath.evaluate(expression, parserDoc).equals(""));
			if (!(xpath.evaluate(expression, parserDoc).equals(""))){
				libertarAcessoFicheiros();
				System.out.println("existe");
				return true;
			}
		} catch (XPathExpressionException e) {
			e.printStackTrace();
		}
		libertarAcessoFicheiros();
		System.out.println("Não existe");
		return false;
	}

	/**
	 * Método que cria de raiz um POI. Se necessário, cria um ficheiro XML para
	 * o receber
	 */
	public void adicionarPOI(Poi POI, String userName) {
		obterAcessoFicheiro();
		Document doc = null;
		DocumentBuilderFactory docFactory = DocumentBuilderFactory
				.newInstance();
		DocumentBuilder docBuilder;
		try {
			docBuilder = docFactory.newDocumentBuilder();
			String regiao = POI.getRegiao().toLowerCase();
			System.out.println("Adicionar POI a: " +"file:///"+baseFilePath +regiao  + ".xml");
			doc = docBuilder.parse("file:///"+baseFilePath +regiao  + ".xml");
		} catch (ParserConfigurationException | SAXException | IOException e) {
			e.printStackTrace();
		}
		// faz o "parser" xml
		// faz o get do elemento raiz (regiao)
		Node elementRegiao = doc.getFirstChild();
		// Elemento e atributo do POI
		Element elementPoi = doc.createElement("poi");
		elementPoi.setAttribute("criador", userName);
		elementPoi.setAttribute("designacao", POI.getDesignacao());
		elementPoi.setAttribute("localizacao", POI.getLocalizacao());
		elementPoi.setAttribute("imagem", POI.getImageName());
		// Elemento e atributo da descricao
		Element elementDescricao = doc.createElement("descricao");
		elementDescricao.setTextContent(POI.getDescricao());
		// Elemento categorias
		Element elementCategorias = doc.createElement("categorias");
		for (String tipo : POI.getCategorias()) {
			Element elementTipo = doc.createElement("tipo");
			elementTipo.setTextContent(tipo);
			elementCategorias.appendChild(elementTipo);
		}
		// Elemento opinioes
		Element elementOpinioes = doc.createElement("opinioes");
		// Elemento multimedia
		Element elementMultimedia = doc.createElement("multimedia");
		
		for (String mult: POI.getMultimedia()){
			Element elementMultFilho = doc.createElement("documento");
			elementMultFilho.setTextContent(mult);
			elementMultimedia.appendChild(elementMultFilho);
		}
		
		// Faz o append dos vários elementos ao poi
		elementPoi.appendChild(elementDescricao);
		elementPoi.appendChild(elementCategorias);
		elementPoi.appendChild(elementOpinioes);
		elementPoi.appendChild(elementMultimedia);
		// Faz o append do poi ao rootElement regiao
		elementRegiao.appendChild(elementPoi);
		writeXML(doc, baseFilePath + POI.getRegiao() + ".xml");
		libertarAcessoFicheiros();
	}

	/**
	 * Permite editar a localização de um POI
	 * 
	 * @param POI
	 *            POI que vai ser editado
	 * @param Localizacao
	 *            String com a nova localização
	 * @param regiaoFilename
	 *            String com o filename do POI que está a ser editado
	 * @throws ParserConfigurationException
	 * @throws SAXException
	 * @throws IOException
	 * @throws XPathExpressionException
	 * @throws TransformerException
	 */
	public void alterarCoordenadas(String nomePOI, String regiaoFilename,
			String Localizacao) {
		createParser(regiaoFilename);
		XPath xpath = XPathFactory.newInstance().newXPath();
		String expression = "//poi[@designacao='" + nomePOI + "']";
		NodeList nodes;
		try {
			nodes = (NodeList) xpath.evaluate(expression, parserDoc,
					XPathConstants.NODESET);
			Node POI = nodes.item(0);
			((Element) POI).getAttributes().getNamedItem("localizacao")
					.setTextContent(Localizacao);
		} catch (XPathExpressionException e) {
			e.printStackTrace();
		}
		writeXML(parserDoc, baseFilePath + regiaoFilename);
		libertarAcessoFicheiros();
	}

	
	public void eliminarPoi(String nomePOI, String regiaoFilename){
		Poi poi = getPoi( nomePOI, regiaoFilename);
		
		System.out.println("---------------POI---------------");
		System.out.println(poi.getDesignacao());
		System.out.println(poi.getImageName());
		
		
		if (poi.getImageName()!=null && !poi.getImageName().equals("")){
			boolean sucesso = new File(this.ImageFilePath+poi.getImageName()).delete();
			System.out.println("Imagem "+this.ImageFilePath+poi.getImageName()+" eliminada = "+sucesso);
		}
		createParser(regiaoFilename);
		XPath xpath = XPathFactory.newInstance().newXPath();
		String expression = "//poi[@designacao='" + nomePOI + "']";
		NodeList nodes;
		Node POInode = null;
		try {
			nodes = (NodeList) xpath.evaluate(expression, parserDoc,
					XPathConstants.NODESET);
			POInode = nodes.item(0);
			if (POInode != null) {
				POInode.getParentNode().removeChild(POInode);
			}	
		} catch (XPathExpressionException e) {
			e.printStackTrace();
		}
		writeXML(parserDoc, baseFilePath + regiaoFilename);
		libertarAcessoFicheiros();

	}
	
	
	
	
	
	/**
	 * Permite editar a descrição de um POI
	 * 
	 * @param POI
	 *            POI que vai ser editado
	 * @param descricao
	 *            String com a nova descrição
	 * @param regiaoFilename
	 *            String com o filename do POI que está a ser editado
	 * @throws ParserConfigurationException
	 * @throws SAXException
	 * @throws IOException
	 * @throws XPathExpressionException
	 * @throws TransformerException
	 */
	public void alterarDescricao(String nomePOI, String regiaoFilename,
			String Descricao) {
		createParser(regiaoFilename);
		XPath xpath = XPathFactory.newInstance().newXPath();
		String expression = "//poi[@designacao='" + nomePOI + "']";
		NodeList nodes;
		try {
			nodes = (NodeList) xpath.evaluate(expression, parserDoc,
					XPathConstants.NODESET);
			Node POI = nodes.item(0);
			((Element) POI).getElementsByTagName("descricao").item(0)
					.setTextContent(Descricao);
		} catch (XPathExpressionException e) {
			e.printStackTrace();
		}

		writeXML(parserDoc, baseFilePath + regiaoFilename);
		libertarAcessoFicheiros();
	}

	/**
	 * Faz a listagem de todas as categorias inseridas nos pois e imprime o
	 * resultado
	 * 
	 * @throws ParserConfigurationException
	 * @throws SAXException
	 * @throws IOException
	 * @throws XPathExpressionException
	 */
	public ArrayList<String> listCategories() {
		ArrayList<String> categorias = new ArrayList<String>();
		String expression = "//poi/categorias/tipo/text()";
		for (String fileName : regionFileNames) {
			createParser(fileName);
			XPath xpath = XPathFactory.newInstance().newXPath();
			NodeList nodes;
			try {
				nodes = (NodeList) xpath.evaluate(expression, parserDoc,
						XPathConstants.NODESET);
				for (int i = 0; i < nodes.getLength(); i++) {
					if (!categorias.contains(nodes.item(i).getTextContent())) {
						categorias.add(nodes.item(i).getTextContent());
					}
				}
			} catch (XPathExpressionException e) {
				e.printStackTrace();
			}
			libertarAcessoFicheiros();
		}
		return categorias;
	}

	/**
	 * Faz a a listagem de todas as regiões disponiveis imprimindo o resultado
	 * 
	 * @throws ParserConfigurationException
	 * @throws SAXException
	 * @throws IOException
	 * @throws XPathExpressionException
	 */
	public ArrayList<String> listRegions() {
		ArrayList<String> regioes = new ArrayList<String>();
		for (String fileName : regionFileNames) {
			String expressao = "//regiao/@designacao";
			createParser(fileName);
			XPath xpath = XPathFactory.newInstance().newXPath();
			String Regiao;
			try {
				Regiao = xpath.evaluate(expressao, parserDoc);
				if (!Regiao.equals("")) {
					regioes.add(Regiao);
				}
			} catch (XPathExpressionException e) {
				e.printStackTrace();
			}
			libertarAcessoFicheiros();
		}
		return regioes;

	}

	public ArrayList<PoiTuple> SearchByRegion(String regiao) {
		if (regionFileNames.contains(regiao.toLowerCase() + ".xml")) {
			return this.showPOIRegion(regiao.toLowerCase());
		}
		return null;
	}
	
}