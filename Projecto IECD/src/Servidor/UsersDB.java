package Servidor;

import java.io.File;
import java.io.IOException;

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

public class UsersDB {
	
	String filePath = "C:/Users/ASUS/workspace/Projecto IECD pos-ferias/src/Server_Files/usersDB.xml";
	private DocumentBuilder parser;
	private Document parserDoc;
	
	
	/**
	 * Cria um parser XML com o fileName dado em parametro
	 * 
	 * @param fileName String com o nome di ficheiro XML
	 * @throws ParserConfigurationException
	 * @throws SAXException
	 * @throws IOException
	 */
	private void createParser(String fileName) {
		try {
			parser = DocumentBuilderFactory.newInstance().newDocumentBuilder();
			parserDoc = parser.parse(new java.io.File(filePath));
		} catch (ParserConfigurationException | SAXException | IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	/**
	 * Escreve num ficheiro xml o documento passado em parametro
	 * 
	 * @param doc      Documento que vai ser guardado
	 * @param fileName String com o nome do user a preencher no xml
	 * @throws TransformerException
	 */
	private void writeXML(Document doc, String fileName) {
		TransformerFactory transformerFactory = TransformerFactory
				.newInstance();
		try {
			Transformer transformer = transformerFactory.newTransformer();
			DOMSource source = new DOMSource(doc);
			StreamResult result = new StreamResult(new File(fileName));
			transformer.transform(source, result);
		} catch (TransformerException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	/**
	 * Indica se já existe um userName no ficheiro xml com o nome atribuido em
	 * parametro.
	 * 
	 * @param userName
	 *            userName que vai procurado na base de dados xml
	 * @return retorna true se encontrar um userName igual, caso contrário,
	 *         devolve false
	 * @throws XPathExpressionException
	 */
	public boolean existUser(String userName){
		createParser(filePath);
		// função xpath para procurar algum user com o nome em parametro
		String expression = "/userDatabase/user/@userName=\"" + userName + "\"";
		XPath xpath = XPathFactory.newInstance().newXPath();
		try {
			if (xpath.evaluate(expression, parserDoc).equals("true"))
				return true;
		} catch (XPathExpressionException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return false;
	}
	
	/**
	 * Verifica se a password inserida coincide com o nome de utilizador
	 * registado no xml
	 * 
	 * @param userName
	 *            String com o nome do utilizador
	 * @param password
	 *            String com a password que vai ser verificada
	 * @return retorna true se a password coincidir com a registada no xml
	 * @throws XPathExpressionException
	 */
	public boolean isPasswordCorrect(String userName, String password){
		createParser(filePath);
		if (!this.existUser(userName))
			return false;
		// Função xpath para verificar se a password do user está correcta
		String expression = "/userDatabase/user[@userName=\"" + userName
				+ "\"]/password/text()";
		XPath xpath = XPathFactory.newInstance().newXPath();
		NodeList nodes;
		try {
			nodes = (NodeList) xpath.evaluate(expression, parserDoc,
					XPathConstants.NODESET);
			// Como o "user" tem apenas um elemento filho (password), escolhemos o
			// item de indice "0" da NodeList
			if (nodes.item(0).getNodeValue().equals(password))
				return true;
		} catch (XPathExpressionException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return false;
	}
	
	/**
	 * Adiciona uma nova conta ao ficheiro xml que inclui o nome e password do
	 * utilizador.
	 * 
	 * @param fileName String com o nome do user a preencher no xml
	 * @param password String com a password definida pelo user
	 * @return retorna true caso tenha sido adicionado com exito e false se já
	 *         existir um user com aquele nome
	 * @throws XPathExpressionException
	 */
	public boolean addAccount(String userName, String password){
		this.createParser(this.filePath);
		if (this.existUser(userName)){
			return false;
		}
		// Faz o get do elemento raiz (userDatabase)
		Node userDatabase = parserDoc.getFirstChild();

		// Cria um user (userPass) com o atributo "userName"
		Element user = parserDoc.createElement("user");
		user.setAttribute("userName", userName);

		// Cria o elemento password
		Element pass = parserDoc.createElement("password");
		pass.setTextContent(password);

		// Acrescenta o elemento ao user
		user.appendChild(pass);

		// Acrescentamos os elementos ao Node raiz
		userDatabase.appendChild(user);

		// Escreve num ficheiro xml
		writeXML(parserDoc, this.filePath);
		return true;
	}
	
	
	/**
	 * Muda a pass do user dado em parametro
	 * @param user
	 * @param novaPass
	 */
	public void mudaPass(String user, String novaPass){
		createParser(filePath);
		String expression = "/userDatabase/user[@userName=\"" + user + "\"]/password";
		System.out.println("expressão xpath = " + expression);
		XPath xpath = XPathFactory.newInstance().newXPath();
		NodeList nodes;
		try {
			nodes = (NodeList) xpath.evaluate(expression, parserDoc,
					XPathConstants.NODESET);
			System.out.println("tamanho do node = " + nodes.getLength());
			nodes.item(0).setTextContent(novaPass);

		} catch (XPathExpressionException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		// Escreve num ficheiro xml
		writeXML(parserDoc, filePath);
	}

	public static void main(String[] args) {
		UsersDB a= new UsersDB();
		System.out.println("Existe jorge "+a.existUser("jorge"));
		System.out.println("Existe cremilde "+a.existUser("cremilde"));
		System.out.println("Pw jorge é 145 "+a.isPasswordCorrect("jorge", "145"));
		System.out.println("Pw fsdbhfdssfd é 1234 "+a.isPasswordCorrect("fsdbhfdssfd", "1234"));
		System.out.println("Existe magnólia "+a.existUser("magnólia"));
		System.out.println("Add magnólia "+a.addAccount("magnólia", "1785"));
	}

}