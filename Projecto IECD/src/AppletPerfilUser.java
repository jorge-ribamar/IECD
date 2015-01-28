import java.awt.Color;
import java.awt.event.ActionEvent;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.io.StringReader;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.ArrayList;

import javax.swing.JApplet;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPasswordField;
import javax.swing.JTextField;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;

import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;


public class AppletPerfilUser extends JApplet{

	private static final long serialVersionUID = 1L;
	private static JLabel userLabel;
	private static JTextField  userText;
	private static JLabel antigaPasswordLabel;
	private static JPasswordField antigaPasswordText;
	private static JLabel novaPasswordLabel;
	private static JPasswordField novaPasswordText;
	private static JLabel novaPasswordLabel2;
	private static JPasswordField novaPasswordText2;
	private static JButton loginButton;
	private static JLabel erroLabel;
	

	private String resposta = "";
	private String host = "localhost";
	private int port = 5000;

	
	public void init() {
		this.setSize(315, 200);
		JPanel panel = new JPanel();
		placeComponents(panel);
		this.setContentPane(panel);
	}

	
	/**
	 * Método responsavel principalmente pela parte grafica da applet
	 * 
	 * @param panel
	 */
	private void placeComponents(JPanel panel) {

		panel.setLayout(null);

		userLabel = new JLabel("User");
		userLabel.setBounds(10, 10, 120, 25);
		panel.add(userLabel);

		userText = new JTextField(20);
		userText.setBounds(140, 10, 160, 25);
		panel.add(userText);
		
		antigaPasswordLabel = new JLabel("Antiga Password");
		antigaPasswordLabel.setBounds(10, 40, 120, 25);
		panel.add(antigaPasswordLabel);

		antigaPasswordText = new JPasswordField(20);
		antigaPasswordText.setBounds(140, 40, 160, 25);
		panel.add(antigaPasswordText);

		novaPasswordLabel = new JLabel("Nova Password");
		novaPasswordLabel.setBounds(10, 70, 120, 25);
		panel.add(novaPasswordLabel);

		novaPasswordText = new JPasswordField(20);
		novaPasswordText.setBounds(140, 70, 160, 25);
		panel.add(novaPasswordText);
		
		novaPasswordLabel2 = new JLabel("Repita a Password");
		novaPasswordLabel2.setBounds(10, 100, 120, 25);
		panel.add(novaPasswordLabel2);

		novaPasswordText2 = new JPasswordField(20);
		novaPasswordText2.setBounds(140, 100, 160, 25);
		panel.add(novaPasswordText2);
		
		erroLabel = new JLabel();
		erroLabel.setBounds(10, 170, 250, 25);
		erroLabel.setForeground(Color.red);
		panel.add(erroLabel);

		loginButton = new JButton("Alterar");
		loginButton.setBounds(10, 140, 80, 25);
		loginButton.addActionListener(new java.awt.event.ActionListener(){
			public void actionPerformed(ActionEvent e) {
				if(userText.getText().equals(""))
					erroLabel.setText("Insira um UserName válido");
				else if (novaPasswordText.getText().equals(""))
					erroLabel.setText("A password não pode ser vazia");
				else if(!novaPasswordText.getText().equals(novaPasswordText2.getText()) )
					erroLabel.setText("Repita a mesma password");
					
				else{
					erroLabel.setText("");
					String[][] infoMsg = { { "tipo", "alterar_pass" }, { "user", userText.getText() }, { "antigaPass", antigaPasswordText.getText() }, { "novaPass", novaPasswordText.getText() } };
					new AppletPerfilUser().comunicarServidor(infoMsg);
					if(resposta.equals(""))
						System.out.println("resposta vazia");
					else
						System.out.println("resposta preenchida");
			
					}
			}
			
		});
		
		panel.add(loginButton);	
	}
	
	
	/**
	 * Estabelece a conexão com o servidor
	 * 
	 * @param host endereço IP do socket
	 * @param port porto do socket
	 * @param dados String a enviar
	 * @return a resposta do servidor
	 */
	private String clienteTCP(String host, int port, String dados) {

		PrintWriter out = null;
		BufferedReader in = null;
		Socket s = null;

		try {
			s = new Socket(host, port);

			out = new PrintWriter(s.getOutputStream(), true);

			out.println(dados); // Envia a string em formato xml para o server

			in = new BufferedReader(new InputStreamReader(s.getInputStream()));
			
			String msgRecebida;
			do{
				msgRecebida = (String) in.readLine();
				if (msgRecebida!=null){
					resposta += msgRecebida;
				}
			}while(msgRecebida!=null);
			
				// Faz reset aos campos preenchidos e mostra uma pop window de confirmação
				if(getMessageValues(resposta)[0][1].equals("pass_alterada")){
					JOptionPane.showMessageDialog(null,  "Password alterada com sucesso");
					userText.setText("");
					novaPasswordText.setText("");
					novaPasswordText2.setText("");
					antigaPasswordText.setText("");
					erroLabel.setText("");
				}
				
				// Indica que a pass está inválida
				if(getMessageValues(resposta)[0][1].equals("pass_invalida"))
					erroLabel.setText(getMessageValues(resposta)[1][1]);
				
				// Indica que o user está inválido
				if(getMessageValues(resposta)[0][1].equals("user_invalido")){
					erroLabel.setText(getMessageValues(resposta)[1][1]);
				}
				
			System.err.println("Recebi: " + resposta);
			
			System.out.println("getMessageValue = " + getMessageValues(resposta)[0][1]);
		} catch (UnknownHostException uhEx) {
			uhEx.printStackTrace();
		} catch (IOException ioEx) {
			ioEx.printStackTrace();
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			try {
				if (in != null) {
					in.close();
				}
				if (out != null) {
					out.flush();
					out.close();
				}
				if (s != null) {
					s.close();
				}
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		return resposta;
	}
	
	
	/**
	 * Recebe a string bidireccional com varios parametros e reencaminha-os para o servidor
	 * 
	 * @param parametros String bidimensional com os valores a enviar para o servidor
	 * @return retorna a resposta do servidor ao pedido efectuado
	 */
	public String comunicarServidor(String[][] parametros) {

		String xmlString = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>";
		xmlString += "<" + parametros[0][0] + " id=\"" + parametros[0][1]
				+ "\">";

		for (int i = 1; i < parametros.length; i++) {
			xmlString += "<" + parametros[i][0] + ">" + parametros[i][1] + "</"
					+ parametros[i][0] + ">";
		}
		xmlString += "</" + parametros[0][0] + ">";

		return clienteTCP(this.host, this.port, xmlString); // Retorna a mensagem de resposta do servidor 

	}
	
	
	/**
	 * Mostra a mensagem enviada pelo servidor fazendo parsing para se saber o que fazer a partir dai
	 * 
	 * @param mensagemRecebida String enviada pelo server
	 * @return retorna a mensagem
	 */
	public String[][] getMessageValues(String mensagemRecebida) {
		// Parsing da stringXML
		DocumentBuilderFactory docFactory = DocumentBuilderFactory
				.newInstance();
		DocumentBuilder docBuilder;
		Document parserDoc = null;
		try {
			docBuilder = docFactory.newDocumentBuilder();
			parserDoc = docBuilder.parse(new InputSource(new StringReader(
					mensagemRecebida)));
		} catch (Exception e) {
			e.printStackTrace();
		}

		String expression = "/tipo"; // expressão para o user
		XPath xpath = XPathFactory.newInstance().newXPath();
		NodeList nodes = null;
		Node raiz = null;
		try {
			nodes = (NodeList) xpath.evaluate(expression, parserDoc,
					XPathConstants.NODESET);
			raiz = nodes.item(0);
		} catch (XPathExpressionException e) {
			e.printStackTrace();
		}

		NodeList filhos = raiz.getChildNodes();
		ArrayList<String> elementName = new ArrayList<String>();
		ArrayList<String> elementValue = new ArrayList<String>();
		elementName.add("tipo");
		elementValue.add(this.getTipo(mensagemRecebida));

		for (int i = 0; i < filhos.getLength(); i++) {
			Node childNode = filhos.item(i);
			if (childNode.getNodeType() == Node.ELEMENT_NODE) {
				elementName.add(childNode.getNodeName());
				elementValue.add(childNode.getTextContent());
			}
		}
		String[][] valores = new String[elementName.size()][2];
		for (int i = 0; i < elementName.size(); i++) {
			valores[i][0] = elementName.get(i);
			valores[i][1] = elementValue.get(i);
			System.out.println(valores[i][0] + " " + valores[i][1]);

		}
		return valores;
	}

	
	/**
	 * Através de parsing da string, indica o tipo da mensagem recebida
	 * 
	 * @param mensagemRecebida
	 * @return o tipo da mensagem
	 */
	private String getTipo(String mensagemRecebida) {

		// Parsing da stringXML
		DocumentBuilderFactory docFactory = DocumentBuilderFactory
				.newInstance();
		DocumentBuilder docBuilder;
		Document parserDoc = null;
		try {
			docBuilder = docFactory.newDocumentBuilder();
			parserDoc = docBuilder.parse(new InputSource(new StringReader(
					mensagemRecebida)));
		} catch (Exception e) {
			e.printStackTrace();
		}

		String expression = "/tipo/@id"; // Expressão para o user
		XPath xpath = XPathFactory.newInstance().newXPath();
		NodeList nodes;
		String tipo = null;
		try {
			nodes = (NodeList) xpath.evaluate(expression, parserDoc,
					XPathConstants.NODESET);
			tipo = nodes.item(0).getNodeValue();
		} catch (XPathExpressionException e) {
			e.printStackTrace();
		}
		return tipo;
	}
	
}