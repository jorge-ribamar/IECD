<%@page import="javax.xml.xpath.XPathExpressionException"%>
<%@page import="javax.xml.xpath.XPathConstants"%>
<%@page import="org.w3c.dom.NodeList"%>
<%@page import="javax.xml.xpath.XPathFactory"%>
<%@page import="javax.xml.xpath.XPath"%>
<%@page import="java.io.StringReader"%>
<%@page import="org.xml.sax.InputSource"%>
<%@page import="org.w3c.dom.Document"%>
<%@page import="javax.xml.parsers.DocumentBuilder"%>
<%@page import="javax.xml.parsers.DocumentBuilderFactory"%>
<%@page import="java.io.IOException"%>
<%@page import="java.net.UnknownHostException"%>
<%@page import="java.io.InputStreamReader"%>
<%@page import="java.net.Socket"%>
<%@page import="java.io.BufferedReader"%>
<%@page import="java.io.PrintWriter"%>
<%@page import="org.w3c.dom.Element"%>
<%@page import="java.io.StringReader"%>
<%@page import="java.util.ArrayList"%>
<%@page import="javax.xml.parsers.DocumentBuilder"%>
<%@page import="javax.xml.xpath.XPath"%>
<%@page import="javax.xml.xpath.XPathConstants"%>
<%@page import="javax.xml.xpath.XPathExpressionException"%>
<%@page import="javax.xml.xpath.XPathFactory"%>
<%@page import="org.w3c.dom.Document"%>
<%@page import="org.w3c.dom.Node"%>
<%@page import="org.w3c.dom.NodeList"%>
<%@page import="org.xml.sax.InputSource"%>
<%@page import="java.io.ByteArrayOutputStream"%>
<%@page import="java.awt.image.BufferedImage"%>
<%@page import="sun.misc.BASE64Encoder"%>
<%@page import="javax.imageio.ImageIO"%>
<%@page import="java.io.File"%>



<%@ page language="java" contentType="text/html; charset=UTF-8"
	pageEncoding="UTF-8"%>
	
<%request.setCharacterEncoding("UTF-8");
					response.setCharacterEncoding("UTF-8");%>


<%!private String host = "localhost";
	private int port = 5000;


	private String comunicarServidor(String[][] parametros) {

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
     * Codifica imagem numa String
     * @param image A imagem para codificar
     * @param type jpeg, bmp, ...
     * @return encoded string
     */
    public static String encodeImageToString(BufferedImage image, String type) {
        String imageString = null;
        ByteArrayOutputStream bos = new ByteArrayOutputStream();

        try {
            ImageIO.write(image, type, bos);
            byte[] imageBytes = bos.toByteArray();

            BASE64Encoder encoder = new BASE64Encoder();
            imageString = encoder.encode(imageBytes);

            bos.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return imageString;
    }

	public class TuploReturn{
	
		private String tipo;
		private String dados;
		private String tipoPesquisa;
		
		public TuploReturn(String tipoPag, String dadosApresentacao) {
			tipo = tipoPag;
			dados = dadosApresentacao;
			
		}
	
		public String getTipo() {
			return tipo;
		}
	
		public String getDados() {
			return dados;
		}
	
		public String getTipoPesquisa() {
			return tipoPesquisa;
		}
	
		public void setTipoPesquisa(String tipoPesquisa) {
			this.tipoPesquisa = tipoPesquisa;
		}
	}


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
			//Document parserDoc = docBuilder.parse (fileName); // Se fosse através de ficheiro
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
			e.printStackTrace();
		}
		return tipo;
	}

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
			// Document parserDoc = docBuilder.parse (fileName); // Se fosse
			// através de ficheiro
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



	private String clienteTCP(String host, int port, String dados) {

		PrintWriter out = null;
		BufferedReader in = null;

		Socket s = null;
		String resposta = "";

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
			
		
			System.err.println("Recebi: " + resposta);
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




	private void showPoiList(String mensagemPoiList, JspWriter stream,
			String user, TuploReturn tuplo) {
		DocumentBuilderFactory docFactory = DocumentBuilderFactory
				.newInstance();
		DocumentBuilder docBuilder;
		Document parserDoc = null;
		try {
			docBuilder = docFactory.newDocumentBuilder();
			parserDoc = docBuilder.parse(new InputSource(new StringReader(
					mensagemPoiList)));
			// Document parserDoc = docBuilder.parse (fileName); // Se fosse
			// através de ficheiro
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
		int nPois=0;
		NodeList Pois = raiz.getChildNodes();
		for (int i = 0; i < Pois.getLength(); i++) {
			Node childNode = Pois.item(i);
			if (childNode.getNodeType() == Node.ELEMENT_NODE) {
				//tratar do Poi
				showPoiSumary(childNode, stream, user, tuplo);
				nPois++;
			}
		}
		if(nPois==0){
		try{
			stream.write("<p>Não existem POIs para mostrar");
		}catch(IOException e){
			e.printStackTrace();
		}
		}
	}

	private void showPoiSumary(Node PoiNode, JspWriter stream, String user, TuploReturn tuplo) {
		String designacao = ((Element) PoiNode)
				.getElementsByTagName("designacao").item(0).getTextContent();
		String criador = ((Element) PoiNode).getElementsByTagName("criador")
				.item(0).getTextContent();
		String file = ((Element) PoiNode).getElementsByTagName("file").item(0)
				.getTextContent();
		String descricao = ((Element) PoiNode)
				.getElementsByTagName("descricao").item(0).getTextContent();
		String localizacao = ((Element) PoiNode)
				.getElementsByTagName("localizacao").item(0).getTextContent();
		String regiao = ((Element) PoiNode).getElementsByTagName("regiao")
				.item(0).getTextContent();
		String adorei = ((Element) PoiNode).getElementsByTagName("adorei")
				.item(0).getTextContent();
		String gostei = ((Element) PoiNode).getElementsByTagName("gostei")
				.item(0).getTextContent();
		String n_voltar = ((Element) PoiNode).getElementsByTagName("n_voltar")
				.item(0).getTextContent();
		String imagem = ((Element) PoiNode).getElementsByTagName("imagem")
				.item(0).getTextContent();

		try {
			
			
			if(!imagem.equals("")){
				stream.write("<p><img src = \"imagens/POIs/"+ imagem+"\" width=\" 230px\" height=\" 230px\"/ style= \"border:3px solid white\">"); 
			}else{
				stream.write("<p><img src = \"imagens/placeholder2.png\" width=\" 230px\" height=\" 230px\"/ style= \"border:3px solid white\">");
			}
			stream.write("<p/>");
			stream.write("<form action=\"RaizCliente.jsp\" method =\"post\">");
			stream.write("<input type=\"hidden\" id=\"tipo\" name=\"tipo\" value=\"apresentar_poi\"/>");
			stream.write("<input type=\"hidden\" id=\"user\" name=\"user\" value=\""
					+ user + "\"/>");
			stream.write("<input type=\"hidden\" id=\"fileName\" name=\"fileName\" value=\""
					+ file + "\"/>");
			stream.write("<input type=\"submit\" id=\"designacao\" name=\"designacao\" class=\"simpleButton\" value=\""
					+ designacao + "\"/>");
					
			if (tuplo!=null){
			if (tuplo.getTipo().equals("apresentar_user")){
					stream.write("<input type=\"hidden\" id=\"tipoReturn\" name=\"tipoReturn\" value=\""+tuplo.getTipo()+"\"/>");
					stream.write("<input type=\"hidden\" id=\"dadosResturn\" name=\"dadosResturn\" value=\""+tuplo.getDados()+"\"/>");
				}else if (tuplo.getTipo().equals("apresentar_pesquisa")){
					stream.write("<input type=\"hidden\" id=\"tipoReturn\" name=\"tipoReturn\" value=\""+tuplo.getTipo()+"\"/>");
					stream.write("<input type=\"hidden\" id=\"dadosResturn\" name=\"dadosResturn\" value=\""+tuplo.getDados()+"\"/>");
					stream.write("<input type=\"hidden\" id=\"tipoPesquisaReturn\" name=\"tipoPesquisaReturn\" value=\""+tuplo.getTipoPesquisa()+"\"/>");
				}else if (tuplo.getTipo().equals("apresentar_regiao")){
					stream.write("<input type=\"hidden\" id=\"tipoReturn\" name=\"tipoReturn\" value=\""+tuplo.getTipo()+"\"/>");
					stream.write("<input type=\"hidden\" id=\"dadosResturn\" name=\"dadosResturn\" value=\""+tuplo.getDados()+"\"/>");
				}else if (tuplo.getTipo().equals("ranking_pois")){
					stream.write("<input type=\"hidden\" id=\"tipoReturn\" name=\"tipoReturn\" value=\""+tuplo.getTipo()+"\"/>");
					stream.write("<input type=\"hidden\" id=\"dadosResturn\" name=\"dadosResturn\" value=\""+tuplo.getDados()+"\"/>");
				}else if (tuplo.getTipo().equals("apresentar_categoria")){
					stream.write("<input type=\"hidden\" id=\"tipoReturn\" name=\"tipoReturn\" value=\""+tuplo.getTipo()+"\"/>");
					stream.write("<input type=\"hidden\" id=\"dadosResturn\" name=\"dadosResturn\" value=\""+tuplo.getDados()+"\"/>");
				}
			}			
			stream.write("</form>");	
			stream.write("<p><b>Criador:</b> " + criador);
			stream.write("<p><b>Região:</b> " + regiao);
			stream.write("<p><b>Descrição:</b> " + descricao);
			stream.write("<p><b>Localização:</b> " + localizacao);
			stream.write("<p><b>Pontuação:</b> "
					+ String.valueOf(3 * Integer.valueOf(adorei) + 1
							* Integer.valueOf(gostei) - 2
							* Integer.valueOf(n_voltar))) ;
			
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	private void showPoiFull(String mensagemPoiList, JspWriter stream,
			String user, String campo, javax.servlet.http.HttpServletRequest req) {
		if (campo != null && ( campo.equals("") || campo.equals("null")  )) {
			campo = null;
		}
		DocumentBuilderFactory docFactory = DocumentBuilderFactory
				.newInstance();
		DocumentBuilder docBuilder;
		Document parserDoc = null;
		try {
			docBuilder = docFactory.newDocumentBuilder();
			parserDoc = docBuilder.parse(new InputSource(new StringReader(
					mensagemPoiList)));
			// Document parserDoc = docBuilder.parse (fileName); // Se fosse
			// através de ficheiro
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
		Node PoiNode = raiz.getChildNodes().item(0);

		String tipo = raiz.getAttributes().getNamedItem("id").getNodeValue();

		String file = ((Element) PoiNode).getElementsByTagName("file").item(0)
				.getTextContent();

		String designacao = ((Element) PoiNode)
				.getElementsByTagName("designacao").item(0).getTextContent();
		String criador = ((Element) PoiNode).getElementsByTagName("criador")
				.item(0).getTextContent();
		String descricao = ((Element) PoiNode)
				.getElementsByTagName("descricao").item(0).getTextContent();
		String localizacao = ((Element) PoiNode)
				.getElementsByTagName("localizacao").item(0).getTextContent();
		String regiao = ((Element) PoiNode).getElementsByTagName("regiao")
				.item(0).getTextContent();
		String adorei = ((Element) PoiNode).getElementsByTagName("adorei")
				.item(0).getTextContent();
		String gostei = ((Element) PoiNode).getElementsByTagName("gostei")
				.item(0).getTextContent();
		String n_voltar = ((Element) PoiNode).getElementsByTagName("n_voltar")
				.item(0).getTextContent();

		try {
			stream.println("<h2>" + designacao + "</h2>");
			stream.println("<p><b>Criador: </b>" + criador);
			stream.println("<p><b>Região: </b>" + regiao);

			if (tipo.equals("alterar_poi") && campo == null
					&& (campo == null || !campo.equals("descricao"))) {
				stream.println("<form action=\"RaizCliente.jsp\" method =\"post\">");
				stream.println("<p><b>Descrição:</b> " + descricao);
				stream.println("<input type=\"hidden\" id=\"tipo\" name=\"tipo\" value=\"alterar_poi\"/>");
				stream.println("<input type=\"hidden\" id=\"alteracao\" name=\"campo\" value=\"descricao\"/>");
				stream.println("<input type=\"hidden\" id=\"user\" name=\"user\" value=\""
						+ user + "\"/>");
				stream.println("<input type=\"hidden\" id=\"fileName\" name=\"fileName\" value=\""
						+ file + "\"/>");
				stream.println("<input type=\"hidden\" id=\"designacao\" name=\"designacao\" value=\""
						+ designacao + "\"/>");
				stream.println("<input type=\"submit\" id=\"alteracao\" name=\"alteracao\" value=\"Alterar\"/></form>");

			} else if (campo != null && campo.equals("descricao")) {

				stream.println("<form action=\"RaizCliente.jsp\" method =\"post\">");

				stream.println("<input type=\"hidden\" id=\"tipo\" name=\"tipo\" value=\"alterar_campo_poi\"/>");
				stream.println("<input type=\"hidden\" id=\"alteracao\" name=\"campo\" value=\"descricao\"/>");
				stream.println("<input type=\"hidden\" id=\"user\" name=\"user\" value=\""
						+ user + "\"/>");
				stream.println("<input type=\"hidden\" id=\"fileName\" name=\"fileName\" value=\""
						+ file + "\"/>");
				stream.println("<input type=\"hidden\" id=\"designacao\" name=\"designacao\" value=\""
						+ designacao + "\"/>");

				stream.println("<textarea rows=\"4\" cols=\"50\" name=\"descricao\" placeholder=\"Nova descrição\"></textarea>");
				stream.println("<input type=\"submit\" id=\"Alterar\" name=\"Alterar\" value=\"Alterar\"/></form>");
				stream.println("</form>");

			} else {
				stream.println("<p><b>Descrição:</b> " + descricao);
			}

			if (tipo.equals("alterar_poi") && campo == null
					&& (campo == null || !campo.equals("localizacao"))) {
				stream.println("<form action=\"RaizCliente.jsp\" method =\"post\">");
				stream.println("<p><b>Localização: </b>" + localizacao);
				stream.println("<input type=\"hidden\" id=\"tipo\" name=\"tipo\" value=\"alterar_poi\"/>");
				stream.println("<input type=\"hidden\" id=\"alteracao\" name=\"campo\" value=\"localizacao\"/>");
				stream.println("<input type=\"hidden\" id=\"user\" name=\"user\" value=\""
						+ user + "\"/>");
				stream.println("<input type=\"hidden\" id=\"fileName\" name=\"fileName\" value=\""
						+ file + "\"/>");
				stream.println("<input type=\"hidden\" id=\"designacao\" name=\"designacao\" value=\""
						+ designacao + "\"/>");
				stream.println("<input type=\"submit\" id=\"alteracao\" name=\"alteracao\" value=\"Alterar\"/></form>");
			} else if (campo != null && campo.equals("localizacao")) {

				stream.println("<form action=\"RaizCliente.jsp\" method =\"post\">");

				stream.println("<input type=\"hidden\" id=\"tipo\" name=\"tipo\" value=\"alterar_campo_poi\"/>");
				stream.println("<input type=\"hidden\" id=\"alteracao\" name=\"campo\" value=\"localizacao\"/>");
				stream.println("<input type=\"hidden\" id=\"user\" name=\"user\" value=\""
						+ user + "\"/>");
				stream.println("<input type=\"hidden\" id=\"fileName\" name=\"fileName\" value=\""
						+ file + "\"/>");
				stream.println("<input type=\"hidden\" id=\"designacao\" name=\"designacao\" value=\""
						+ designacao + "\"/>");

				stream.println("<input type=\"text\" id=\"localizacao\" name=\"localizacao\" placeholder=\"Coordenadas Geográficas\" />");
				stream.println("<input type=\"submit\" id=\"Alterar\" name=\"Alterar\" value=\"Alterar\"/></form>");
				stream.println("</form>");

			} else {
				stream.println("<p><b>Localização:</b> " + localizacao);
			}

			NodeList categoria = ((Element) PoiNode)
					.getElementsByTagName("categoria").item(0).getChildNodes();
			stream.println("<p><b>Categorias:</b> ");
			for (int i = 0; i < categoria.getLength(); i++) {
				Node childNode = categoria.item(i);
				if (childNode.getNodeType() == Node.ELEMENT_NODE) {
					stream.println("	" + childNode.getTextContent());
				}
			}

			stream.println("<p><b>Elementos multimédia:</b> ");
			NodeList multimedia = ((Element) PoiNode)
					.getElementsByTagName("multimedia").item(0).getChildNodes();
			for (int i = 0; i < multimedia.getLength(); i++) {
				Node childNode = multimedia.item(i);
				if (childNode.getNodeType() == Node.ELEMENT_NODE) {
					stream.println("<p>" + childNode.getTextContent());
				}
			}

			stream.println("<p><b>Nº de Adorei:</b> " + adorei);
			stream.println("<p><b>Nº de Gostos:</b> " + gostei);
			stream.println("<p><b>Nº de Não Voltar:</b> " + n_voltar);
			stream.println("<p><b>Pontuação:</b> "
					+ String.valueOf(3 * Integer.valueOf(adorei) + 1
							* Integer.valueOf(gostei) - 2
							* Integer.valueOf(n_voltar)));

			if (criador.equals(user) && tipo.equals("apresentar_poi")) {
				stream.println("<form action=\"RaizCliente.jsp\" method =\"post\">");
				stream.println("<input type=\"hidden\" id=\"tipo\" name=\"tipo\" value=\"alterar_poi\"/>");
				stream.println("<input type=\"hidden\" id=\"designacao\" name=\"designacao\" value=\""
						+ designacao + "\"/>");
				stream.println("<input type=\"hidden\" id=\"user\" name=\"user\" value=\""
						+ user + "\"/>");
				stream.println("<input type=\"hidden\" id=\"fileName\" name=\"fileName\" value=\""
						+ file + "\"/>");
				stream.println("<input type=\"submit\" id=\"Alterar Poi\" name=\"Alterar Poi\" value=\"Alterar Poi\"/>");
				stream.println("</form>");
				
				stream.println("<form action=\"RaizCliente.jsp\" method =\"post\">");
				stream.println("<input type=\"hidden\" id=\"tipo\" name=\"tipo\" value=\"eliminar_poi\"/>");
				stream.println("<input type=\"hidden\" id=\"designacao\" name=\"designacao\" value=\""
						+ designacao + "\"/>");
				stream.println("<input type=\"hidden\" id=\"user\" name=\"user\" value=\""
						+ user + "\"/>");
				stream.println("<input type=\"hidden\" id=\"fileName\" name=\"fileName\" value=\""
						+ file + "\"/>");
						
				String tipoReturn = req.getParameter("tipoReturn");
				String dadosReturn = req.getParameter("dadosResturn");
				String tipoPesquisaReturn = req.getParameter("tipoPesquisaReturn"); 		
						
				if (tipoReturn!=null){
					stream.println("<input type=\"hidden\" id=\"tipoReturn\" name=\"tipoReturn\" value=\""+tipoReturn+"\"/>");
					stream.println("<input type=\"hidden\" id=\"dadosResturn\" name=\"dadosResturn\" value=\""+dadosReturn+"\"/>");
					stream.println("<input type=\"hidden\" id=\"tipoPesquisaReturn\" name=\"tipoPesquisaReturn\" value=\""+tipoPesquisaReturn+"\"/>");
				}
				stream.println("<input type=\"submit\" id=\"Eliminar Poi\" name=\"Eliminar Poi\" value=\"Eliminar Poi\"/>");
				stream.println("</form>");
				
				stream.println("<p>");
				
			}
			if (tipo.equals("apresentar_poi")) {
				stream.println("<form action=\"RaizCliente.jsp\" method =\"post\">");
				stream.println("<input type=\"hidden\" id=\"user\" name=\"user\" value=\""
						+ user + "\"/>");
				stream.println("<input type=\"hidden\" id=\"tipo\" name=\"tipo\" value=\"votar_poi\"/>");
				stream.println("<input type=\"hidden\" id=\"fileName\" name=\"fileName\" value=\""
						+ file + "\"/>");
				stream.println("<input type=\"hidden\" id=\"designacao\" name=\"designacao\" value=\""
						+ designacao + "\"/>");
				stream.println("<input type=\"radio\" id=\"Adorei\" name=\"opiniao\" value=\"adorei\">Adorei");
				stream.println("<input type=\"radio\" id=\"Gostei\" name=\"opiniao\" value=\"gostei\">Gostei");
				stream.println("<input type=\"radio\" id=\"Não Voltar\" name=\"opiniao\" value=\"nao_vou_voltar\">Não Voltar");
				stream.println("<input type=\"submit\" value=\"Votar\"/>");
				stream.println("</form>");
				stream.println("<p>");
			}
		} catch (IOException e) {
			e.printStackTrace();
		}

		NodeList multimedia = ((Element) PoiNode)
				.getElementsByTagName("multimedia").item(0).getChildNodes();
		for (int i = 0; i < multimedia.getLength(); i++) {
			Node childNode = multimedia.item(i);
			if (childNode.getNodeType() == Node.ELEMENT_NODE) {
				System.out.println(childNode.getTextContent());
			}
		}

		NodeList categoria = ((Element) PoiNode)
				.getElementsByTagName("categoria").item(0).getChildNodes();

		for (int i = 0; i < categoria.getLength(); i++) {
			Node childNode = categoria.item(i);
			if (childNode.getNodeType() == Node.ELEMENT_NODE) {
				System.out.println(childNode.getTextContent());
			}
		}
	}%>




<!DOCTYPE html>
<html>
<head>
	<style type="text/css">
		<%@ include file="css/myStyle.css" %>
	</style>	
	<meta http-equiv="Content-Type" content="text/html; charset=UTF-8" />
	<title>POI Search</title>
</head>
<body>



	<%@ page autoFlush="true" buffer="1094kb"%>  
	<div id ="divPrincipal">
		
			<%	String tipo = request.getParameter("tipo");
				if (tipo == null) {
					tipo = "";
				}
			%>

			<!-- PÁGINA DE LOGIN -->
			<%	if (tipo.equals("") || tipo.equals("login")) {
			%>
			
			<header id="headerPrincipal"></header>
			<section id = "sectionCorpo">
				<img alt="Logo" id="imgLogo" src="imagens/Logo.png" />
				<form id="formLogin" action="RaizCliente.jsp" method="post" accept-charset="utf-8">
					<input type="hidden" name="tipo" value="enviar_login"> <br>
					<input type="text" class="inputLogin" id="userName" name="user"	placeholder="Username" /> <br> 
					<input type="password" class="inputLogin" id="pass" name="pass" placeholder="Password" />
					<br> <input type="submit" id="botaoLogin" value="Login"	title="Faz o login do user" />
					<% 
					String erro = request.getParameter("erro");
					if (erro != null && !erro.equals("")) {
			%>
						<p id="erro">
						<%=erro%></p>
			<%		}
			%>
				</form>
				<form id="formRegisto1" action="RaizCliente.jsp" method="post">
					<input type="hidden" name="tipo" value="registo"> 
					Se ainda não está registado<input type="submit" id="botaoRegistar" value="clique aqui." title="Registar" onclick="buttonSound()"/>
				</form>
					
			</section>

			<%
				} else if (tipo.equals("enviar_login")) {
			
					tipo = request.getParameter("tipo");
					String user = request.getParameter("user");
					System.out.println("user:" + user);

					String pass = request.getParameter("pass");
					System.out.println("pass:" + pass);

					String[][] InfoMsg = { { "tipo", tipo }, { "user", user }, { "pass", pass } };
					String resposta = comunicarServidor(InfoMsg);
					String novoTipo = getTipo(resposta);
					String erro = getMessageValues(resposta)[1][1];
			%>

					<jsp:forward page="RaizCliente.jsp">
						<jsp:param name="tipo" value="<%=novoTipo%>" />
						<jsp:param name="erro" value="<%=erro%>" />
						<jsp:param name="user" value="<%=user%>" />
						<jsp:param name="mensagemXML" value="<%=resposta%>" />
					</jsp:forward>


			<!-- PÁGINA DE REGISTO -->
			<%	} else if (tipo.equals("registo")) {
				

			%>
					<header id ="headerPrincipal">
						<img alt="LogoSmall" id="imgLogoSmall" src="imagens/LogoSmall.png" />
					</header>
					<section id = "sectionCorpo">
						<section id="sectionRegisto">
							<header id = "headerRegisto">
								<h1 id = "h1Registo">Registe-se</h1>
								<p id="pRegisto">É <b>grátis</b> e demora menos que 15 segundos.</p>
							</header>
							
							<form id="formRegisto2" action="RaizCliente.jsp" method="post">
								<img alt="userIcon" id="userIcon" src="imagens/userIcon.png" />
								<input type="text" class="inputLogin" id="userNameRegisto" name="userName" placeholder="Insira um nome de utilizador" /> 
								<br/><img alt="passIcon" id="passIcon" src="imagens/passwordIcon.png" />
								<input type="password" class="inputLogin" id="pass1" name="pass1" placeholder="Insira uma Password" />
								<br/><img alt="repeatIcon" id="repeatIcon" src="imagens/repeatIcon.png" />
								<input type="password" class="inputLogin" id="pass2" name="pass2" placeholder="Repita a mesma Password" /> 
								<br/><input type="hidden" name="tipo" value="enviar_registo"> 
								<input type="submit" id="botaoSubmeterRegisto" value="Submeter" title="Submeter Dados" />
							</form>
							
						</section>
					</section>
					
					<form action="RaizCliente.jsp" method="post">
						<input type="hidden" id="tipo" name="tipo" value="login" />
						<input type="hidden" id="user" name="user" value="" /> 
						<input type = "submit" id="voltar" name="voltar" value="" title="Voltar">
					</form>
					
			<%
					String erro = request.getParameter("erro");
					if (erro != null && !erro.equals("")) {
			%>
						<p id="erro" style="top:530px; left:620px; position:absolute;">
						<%=erro%></p>
			<%		}
			%>

			<%	} else if (tipo.equals("enviar_registo")) {
			

					tipo = request.getParameter("tipo");
					String userName = request.getParameter("userName");

					String pass1 = request.getParameter("pass1");
					String pass2 = request.getParameter("pass2");

					String[][] InfoMsg = { { "tipo", tipo }, { "userName", userName }, { "pass1", pass1 }, { "pass2", pass2 } };
					String resposta = comunicarServidor(InfoMsg);
					String novoTipo = getTipo(resposta);
					String erro = getMessageValues(resposta)[1][1];
			%>

					<jsp:forward page="RaizCliente.jsp">
						<jsp:param name="tipo" value="<%=novoTipo%>" />
						<jsp:param name="erro" value="<%=erro%>" />
						<jsp:param name="mensagemXML" value="<%=resposta%>" />
						<jsp:param name="user" value="<%=userName%>" />
					</jsp:forward>


			<!-- PÁGINA MENU INICIAL -->
			<%	} else if (tipo.equals("menu_principal")) {
			
					String user = request.getParameter("user");
			%>
			
					<header id ="headerPrincipal">
					
						<img alt="LogoSmall" id="imgLogoSmall" src="imagens/LogoSmall.png" />
						
						<form id="formLogoff" action="RaizCliente.jsp" method="post">
							<input type="hidden" id="tipo" name="tipo" value="login" />
							<input type="hidden" id="user" name="user" value="" /> 
							<input type="submit" id="logoff" value="" title="Logoff" />
						</form>
						<p id ="user">Bem-vindo, &nbsp;<%=user%></p>
					</header>
					
					<img alt="" src="imagens/menu.png" style="position: absolute; left:354px; top:150px;">
					
					<section id = "sectionCorpo">
						<section id = "sectionMenuPrincipal">
							<form action="RaizCliente.jsp" method="post">
								<input type="hidden" id="tipo" name="tipo" value="pesquisa_pois" />
								<input type="hidden" id="user" name="user" value="<%=user%>" /> 
								<input type="submit" class = "iconsMenu" id="PesquisarPois" value="" title="Faz pesquisa de Pois" />
							</form>
				
							<form action="RaizCliente.jsp" method="post">
								<input type="hidden" id="tipo" name="tipo" value="adicionar_poi" />
								<input type="hidden" id="user" name="user" value="<%=user%>" /> 
								<input type="submit" class = "iconsMenu" id="AdicionarPOI" value="" title="Adiciona um POI" />
							</form>
				
							<form action="RaizCliente.jsp" method="post">
								<input type="hidden" id="tipo" name="tipo" value="apresentar_users" />
								<input type="hidden" id="user" name="user" value="<%=user%>" /> 
								<input type="submit" class = "iconsMenu" id="ApresentarUsers" value="" title="Apresenta os Users" />
							</form>
							
							<img alt="" src="imagens/pesquisarPoi.png" style="position: relative; bottom:230px; right: 80px;">
							<img alt="" src="imagens/adicionarPoi.png" style="position: relative; bottom:230px; left: 13px;">
							<img alt="" src="imagens/apresentarUsers.png" style="position: relative; bottom:230px; left: 99px;">
						</section>
					</section>
					
				
			<!-- PÁGINA ADICIONAR POI -->
			<%	} else if (tipo.equals("adicionar_poi")) {
			
					String user = request.getParameter("user");
			%>
			
					<header id ="headerPrincipal">
						<img alt="LogoSmall" id="imgLogoSmall" src="imagens/LogoSmall.png" />
						<form id="formLogoff" action="RaizCliente.jsp" method="post">
							<input type="hidden" id="tipo" name="tipo" value="login" />
							<input type="hidden" id="user" name="user" value="" /> 
							<input type="submit" id="logoff" value="" title="Logoff" />
						</form>
						<p id ="user">Bem-vindo, &nbsp;<%=user%></p>
					</header>
					
					
					
					<section id="sectionCorpo">
						<img id="miniLogo" src="imagens/add-mini.png"/>
						<p id="indicador">Adicionar POI</p>
						<section id="sectionMiddle">
							<form action="RaizCliente.jsp" method="post">
								<input type="hidden" id="tipo" name="tipo" value="enviar_adicionar_poi" /> 
								<input type="hidden" id="user" name="user" value="<%=user%>" /> 
								<section class="inputAddPoi">
									<p class="textoAddPoi">Nome do Poi*:</p>
									<input type="text" id="nomePoi" class = "addPoi" name="nomePoi" required="required" placeholder="Nome do POI" style="left: 41px; position: relative;"/> 
								</section>
								<section class="inputAddPoi">
									<p class="textoAddPoi">Região*:</p>
									<select	name="regiao" class = "addPoi" style="left: 80px; position: relative;">
										<option value="Aveiro">Aveiro</option>
										<option value="Beja">Beja</option>
										<option value="Braga">Braga</option>
										<option value="Bragança">Bragança</option>
										<option value="Castelo Branco">Castelo Branco</option>
										<option value="Coimbra">Coimbra</option>
										<option value="Évora">Évora</option>
										<option value="Faro">Faro</option>
										<option value="Guarda">Guarda</option>
										<option value="Leiria">Leiria</option>
										<option value="Lisboa">Lisboa</option>
										<option value="Portalegre">Portalegre</option>
										<option value="Porto">Porto</option>
										<option value="Santarém">Santarém</option>
										<option value="Setúbal">Setúbal</option>
										<option value="Viana do Castelo">Viana do Castelo</option>
										<option value="Vila Real">Vila Real</option>
										<option value="Viseu">Viseu</option>
									</select>
								</section>
								<section class="inputAddPoi">
									<p class="textoAddPoi" style="position: relative; bottom:15px;">Categoria/as*:</p>
									<select name="categoria" class = "addPoi" multiple="multiple" required="required" style="left: 47px; position: relative;">
										<option value="Bar">Bar</option>
										<option value="Biblioteca">Biblioteca</option>
										<option value="Centro de Exposições">Centro de Exposições</option>
										<option value="Cinema">Cinema</option>
										<option value="Desporto">Desporto</option>
										<option value="Discoteca">Discoteca</option>
										<option value="Espaço de Espectáculos">Espaço de Espectáculos</option>
										<option value="Estabelecimento de Ensino">Estabelecimento de Ensino</option>
										<option value="Jardim">Jardim</option>
										<option value="Lojas">Lojas</option>
										<option value="Miradouro">Miradouro</option>
										<option value="Monumento">Monumento</option>
										<option value="Museu">Museu</option>
										<option value="Praia">Praia</option>
										<option value="Rua">Rua</option>
										<option value="Spa/Termas">Spa/Termas</option>
										<option value="Teatro">Teatro</option>
										<option value="Templo Religioso">Templo Religioso</option>
									</select>
								</section>
								<section class="inputAddPoi">
									<p class="textoAddPoi" style="position: relative; bottom:26px;">Descrição:</p>
									<textarea class = "addPoi" rows="4" cols="50" name="descricao" placeholder="Descrição" style="left: 71px; position: relative;"></textarea>
								</section>
								<section class="inputAddPoi">
									<p class="textoAddPoi">Coordenadas:</p>
									<input type="text" id="cordenadas" class ="addPoi" name="coordenadas" placeholder="Coordenadas Geográficas" style="left: 51px; position: relative;"/> 
								</section>
								<section class="inputAddPoi">
									<p class="textoAddPoi">Endereço da imagem:</p>
									<input type="text" id="imagem" class = "addPoi" name="imagem" placeholder="Endereço da imagem" />
								</section>
								
								<section class="inputAddPoi" >
									<p class="textoAddPoi">Multimédia:</p>
									<input type="text" id="multimedia" class = "addPoi" name="multimedia1" placeholder="Endereço URL" style="left: 60px; position: relative;"/>
									<p><input type="text" id="multimedia" class = "addPoi" name="multimedia2" placeholder="Endereço URL" style="left: 143px; position: relative;"/>
									<p><input type="text" id="multimedia" class = "addPoi" name="multimedia3" placeholder="Endereço URL" style="left: 143px; position: relative;"/>
									<p><input type="text" id="multimedia" class = "addPoi" name="multimedia4" placeholder="Endereço URL" style="left: 143px; position: relative;"/>
								</section>	

								<section class="inputAddPoi" style="margin-bottom: 12px;">
									<input type="hidden" id="user" name="user" value="<%=user%>" /> 
									<input type="submit" id="submeterPOI" class = "addPoi"name="submeterPOI" value="Submeter POI" /> 
									<input type="reset" id="reset" value="Reset" />
								</section>
							</form>
						</section>
					</section>
					
			<%
					String erro = request.getParameter("erro");
					if (erro != null && !erro.equals("")) {
			%>
						<p id="erro" style="top:550px; left:600px; position:absolute;">
						<%=erro%></p>
			<%		}
			%>
					
					<form action="RaizCliente.jsp" method="post">
						<input type="hidden" id="user" name="user" value="<%=user%>"> 
						<input type="hidden" id="tipo" name="tipo" value="menu_principal"> 
						<input type = "submit" id="voltar" name="voltar" value="" title="Voltar" onclick="">
					</form>
					
					

			<%	} else if (tipo.equals("enviar_adicionar_poi")) {
			
					tipo = request.getParameter("tipo");
					String user = request.getParameter("user");

					String nomePoi = request.getParameter("nomePoi");
					String regiao = request.getParameter("regiao");
					String categoria = request.getParameter("categoria");
					String descricao = request.getParameter("descricao");
					String cordenadas = request.getParameter("coordenadas");
					
					String multimedia1 = request.getParameter("multimedia1");
					String multimedia2 = request.getParameter("multimedia2");
					String multimedia3 = request.getParameter("multimedia3");
					String multimedia4 = request.getParameter("multimedia4");

					String Path_Img = request.getParameter("imagem");
					
					String erro = "";
					
					String imgstr = "";
					if (!Path_Img.equals("")){
						try{
						BufferedImage img = ImageIO.read(new File(Path_Img));
				        BufferedImage newImg;
				        imgstr = encodeImageToString(img, "png");
				        // Caso o endereço da imagem esteja errado
						}catch(IOException e){ 
							System.out.println("Endereço inválido");
							erro = "O endereço da imagem introduzido é inválido";
			%>
							<jsp:forward page="RaizCliente.jsp">
								<jsp:param name="erro" value="<%=erro%>" />
								<jsp:param name="tipo" value="adicionar_poi" />
								<jsp:param name="user" value="<%=user%>" />
							</jsp:forward>		
			<%
						}
					}
					
						
					String[][] InfoMsg = { { "tipo", tipo }, { "user", user },
							{ "nomePoi", nomePoi }, { "regiao", regiao },
							{ "categoria", categoria }, { "descricao", descricao },
							{ "coordenadas", cordenadas },
							{ "imagem", imgstr },
							{ "multimedia1", multimedia1 },
							{ "multimedia2", multimedia2 },
							{ "multimedia3", multimedia3 },
							{ "multimedia4", multimedia4 },
							 };
					String resposta = comunicarServidor(InfoMsg);
					String novoTipo = getTipo(resposta);
					erro = getMessageValues(resposta)[1][1];
					
			%>

					<jsp:forward page="RaizCliente.jsp">
						<jsp:param name="erro" value="<%=erro%>" />
						<jsp:param name="tipo" value="<%=novoTipo%>" />
						<jsp:param name="user" value="<%=user%>" />
						<jsp:param name="mensagemXML" value="<%=resposta%>" />
					</jsp:forward>


			<!-- PÁGINA APRESENTAR USERS -->
			<%	} else if (tipo.equals("apresentar_users")) {
			
					tipo = request.getParameter("tipo");
					String user = request.getParameter("user");

					String[][] InfoMsg = { { "tipo", tipo } };
					String resposta = comunicarServidor(InfoMsg);
					System.out.println("apresentar_users: " + resposta);
			
					String[][] values = this.getMessageValues(resposta);
			%>
					<header id ="headerPrincipal">
						<img alt="LogoSmall" id="imgLogoSmall" src="imagens/LogoSmall.png" />
						<form id="formLogoff" action="RaizCliente.jsp" method="post">
							<input type="hidden" id="tipo" name="tipo" value="login" />
							<input type="hidden" id="user" name="user" value="" /> 
							<input type="submit" id="logoff" value="" title="Logoff" />
						</form>
						<p id ="user">Bem-vindo, &nbsp;<%=user%></p>
					</header>
					
					
					<section id="sectionCorpo">
						<img id="miniLogo" src="imagens/user-mini.png"/>
						<p id="indicador">Apresentar Users</p>
							<section id="sectionMiddle">
							
					
			<%		for (String[] userShow : values) {
						if (userShow[0].equals("user")) {
			%>
							<form action="RaizCliente.jsp" method="post">
								<input type="hidden" id="user" name="user" value="<%=user%>" /> 
								<input type="hidden" id="tipo" name="tipo" value="apresentar_user" /> 
								<input type="submit" id="showUser" class ="simpleButton" name="showUser" value="<%=userShow[1]%>" />
							</form>
			<%			}
					}
			%>
							</section>
					</section>
			
					<form action="RaizCliente.jsp" method="post">
						<input type="hidden" id="tipo" name="tipo" value="menu_principal" />
						<input type="hidden" id="user" name="user" value="<%=user%>" /> 
						<input type = "submit" id="voltar" name="voltar" value="" title="Voltar">
					</form>


			<!-- PÁGINA APESENTAR USER -->
			<%	} else if (tipo.equals("apresentar_user")) {
			

					tipo = request.getParameter("tipo");
					String user = request.getParameter("user");
					String showUser = request.getParameter("showUser");
			
					String[][] InfoMsg = { { "tipo", tipo }, { "showUser", showUser } };
					String resposta = comunicarServidor(InfoMsg);
			%>
					<header id ="headerPrincipal">
						<img alt="LogoSmall" id="imgLogoSmall" src="imagens/LogoSmall.png" />
						<form id="formLogoff" action="RaizCliente.jsp" method="post">
							<input type="hidden" id="tipo" name="tipo" value="login" />
							<input type="hidden" id="user" name="user" value="" /> 
							<input type="submit" id="logoff" value="" title="Logoff" />
						</form>
						<p id ="user">Bem-vindo, &nbsp;<%=user%></p>
					</header>
					
					
							<section id="sectionCorpo">
						<p id="indicador">POI's criados pelo user</p>
					<section id="sectionMiddle">
			<%		showPoiList(resposta, out, user, new TuploReturn("apresentar_user", showUser));
			%>
					</section>
					</section>
					
					<form action="RaizCliente.jsp" method="post">
						<input type="hidden" id="tipo" name="tipo" value="apresentar_users" />
						<input type="hidden" id="user" name="user" value="<%=user%>" /> 
						<input type = "submit" id="voltar" name="voltar" value="" title="Voltar">
					</form>
			
			
			
			<!-- PÁGINA APESENTAR POI -->
			<%	} else if (tipo.equals("apresentar_poi")) {
			
					tipo = request.getParameter("tipo");
					String user = request.getParameter("user");
					String designacao = request.getParameter("designacao");
					String Filename = request.getParameter("fileName");
					String tipoReturn = request.getParameter("tipoReturn");
					String dadosReturn = request.getParameter("dadosResturn");
					String tipoPesquisaReturn = request.getParameter("tipoPesquisaReturn");
						
					String[][] InfoMsg = { { "tipo", tipo }, { "designacao", designacao }, { "Filename", Filename } };
					String resposta = comunicarServidor(InfoMsg);	
			%>
			
					<header id ="headerPrincipal">
						<img alt="LogoSmall" id="imgLogoSmall" src="imagens/LogoSmall.png" />
						<form id="formLogoff" action="RaizCliente.jsp" method="post">
							<input type="hidden" id="tipo" name="tipo" value="login" />
							<input type="hidden" id="user" name="user" value="" /> 
							<input type="submit" id="logoff" value="" title="Logoff" />
						</form>
						<p id ="user">Bem-vindo, &nbsp;<%=user%></p>
					</header>
					
					<section id="sectionCorpo">
					<p id="indicador">Apresentar POI</p>
						<section id="sectionMiddle">
			<%		showPoiFull(resposta, out, user, null, request);
			
			%>
					</section>
					</section>
			<%		
			
					if (tipoReturn!=null){
					
						
					
						if (tipoReturn.equals("apresentar_pesquisa")){
						%>
						
						<form action="RaizCliente.jsp" method="post">
							<input type="hidden" id="user" name="user" value="<%=user%>" /> 
							<input type="hidden" id="tipo" name="tipo" value="apresentar_pesquisa" /> 
							<input type="hidden" id="Pesquisa" name="Pesquisa" value="<%=dadosReturn%>" /> 
							<input type="hidden" id="tipo_pesquisa" name="tipo_pesquisa" value="<%=tipoPesquisaReturn%>" /> 
							<input type = "submit" id="voltar" name="voltar" value="" title="Voltar">
						</form>
						
						
				
						<% 
						
						}else if (tipoReturn.equals("apresentar_user")){
						%>
						
						<form action="RaizCliente.jsp" method="post">
							<input type="hidden" id="user" name="user" value="<%=user%>" /> 
							<input type="hidden" id="tipo" name="tipo" value="apresentar_user" /> 
							<input type="hidden" id="showUser" name="showUser" value="<%=dadosReturn%>" /> 
	
							<input type = "submit" id="voltar" name="voltar" value="" title="Voltar">
						</form>
						
						<% 
						}else if (tipoReturn.equals("apresentar_regiao")){
						%>
						

						
						<form action="RaizCliente.jsp" method="post">
							<input type="hidden" id="user" name="user" value="<%=user%>" /> 
							<input type="hidden" id="tipo" name="tipo" value="apresentar_regiao" /> 
							<input type="hidden" id="regiao" name="regiao" value="<%=dadosReturn%>" /> 
							<input type = "submit" id="voltar" name="voltar" value="" title="Voltar">
						</form>
						<% 
						}else if (tipoReturn.equals("ranking_pois")){
						%>
						<form action="RaizCliente.jsp" method="post">
							<input type="hidden" id="user" name="user" value="<%=user%>" /> 
							<input type="hidden" id="tipo" name="tipo" value="ranking_pois" /> 
							<input type = "submit" id="voltar" name="voltar" value="" title="Voltar">
						</form>
						<% 
						}else if (tipoReturn.equals("apresentar_categoria")){
						%>
						
						<form action="RaizCliente.jsp" method="post">
							<input type="hidden" id="user" name="user" value="<%=user%>" /> 
							<input type="hidden" id="tipo" name="tipo" value="apresentar_categoria" /> 
							<input type="hidden" id="categoria" name="categoria" value="<%=dadosReturn%>" /> 
							<input type = "submit" id="voltar" name="voltar" value="" title="Voltar">
						</form>
						<% 
						}
					}else{%>
						<form action="RaizCliente.jsp" method="post">
							<input type="hidden" id="user" name="user" value="<%=user%>" /> 
							<input type="hidden" id="tipo" name="tipo" value="menu_principal" />
							<input type = "submit" id="voltar" name="voltar" value="" title="Voltar">
						</form>
					
					<% }
					
				} else if (tipo.equals("eliminar_poi")) {	
					
					String user = request.getParameter("user");
					String designacao = request.getParameter("designacao");
					String Filename = request.getParameter("fileName");
					
					
					String tipoReturn = request.getParameter("tipoReturn");
					String dadosReturn = request.getParameter("dadosResturn");
					
					String tipoPesquisaReturn = request.getParameter("tipoPesquisaReturn"); 
					
					String[][] InfoMsg = { { "tipo", tipo }, { "designacao", designacao }, { "Filename", Filename } };
					String resposta = comunicarServidor(InfoMsg);
					
					if (tipoReturn!=null){
						if (tipoReturn.equals("apresentar_pesquisa")){
						%>
						
						<jsp:forward page="RaizCliente.jsp">
							<jsp:param name="user" value="<%=user%>" />
							<jsp:param name="tipo" value="apresentar_pesquisa" />
							<jsp:param name="Pesquisa" value="<%=dadosReturn%>" />
							<jsp:param name="tipo_pesquisa" value="<%=tipoPesquisaReturn%>" />
						</jsp:forward> 

						<% 
						}else if (tipoReturn.equals("apresentar_user")){
						%>
						
						<jsp:forward page="RaizCliente.jsp">
							<jsp:param name="user" value="<%=user%>" />
							<jsp:param name="tipo" value="apresentar_user" />
							<jsp:param name="showUser" value="<%=dadosReturn%>" />
						</jsp:forward> 

						
						<% 
						}else if (tipoReturn.equals("apresentar_regiao")){
						%>
						
						<jsp:forward page="RaizCliente.jsp">
							<jsp:param name="user" value="<%=user%>" />
							<jsp:param name="tipo" value="apresentar_regiao" />
							<jsp:param name="regiao" value="<%=dadosReturn%>" />
						</jsp:forward> 
						
						<% 
						}else if (tipoReturn.equals("ranking_pois")){
						%>
						
						<jsp:forward page="RaizCliente.jsp">
							<jsp:param name="user" value="<%=user%>" />
							<jsp:param name="tipo" value="ranking_pois" />
						</jsp:forward> 

						<% 
						}else if (tipoReturn.equals("apresentar_categoria")){
						%>
						<jsp:forward page="RaizCliente.jsp">
							<jsp:param name="user" value="<%=user%>" />
							<jsp:param name="tipo" value="apresentar_categoria" />
							<jsp:param name="categoria" value="<%=dadosReturn%>" />
						</jsp:forward> 
	
						<% 
						}
					}else{%>
					
						<jsp:forward page="RaizCliente.jsp">
							<jsp:param name="user" value="<%=user%>" />
							<jsp:param name="tipo" value="menu_principal" />
						</jsp:forward> 
					
					<% }
					
			
			%>
					
					<jsp:forward page="RaizCliente.jsp">
						<jsp:param name="tipo" value="<%=tipo%>" />
						<jsp:param name="user" value="<%=user%>" />
					</jsp:forward> 
			<%
				} else if (tipo.equals("alterar_poi")) {
			
					tipo = "alterar_poi";
					String user = request.getParameter("user");
					String designacao = request.getParameter("designacao");
					String Filename = request.getParameter("fileName");
					String campo = request.getParameter("campo");
					
					String[][] InfoMsg = { { "tipo", tipo }, { "designacao", designacao }, { "Filename", Filename } };
					String resposta = comunicarServidor(InfoMsg);
			
					
					
				%>
				
					<header id ="headerPrincipal">
						<img alt="LogoSmall" id="imgLogoSmall" src="imagens/LogoSmall.png" />
						<form id="formLogoff" action="RaizCliente.jsp" method="post">
							<input type="hidden" id="tipo" name="tipo" value="login" />
							<input type="hidden" id="user" name="user" value="" /> 
							<input type="submit" id="logoff" value="" title="Logoff" />
						</form>
						<p id ="user">Bem-vindo, &nbsp;<%=user%></p>
					</header>
					
					<section id="sectionCorpo">
						<p id="indicador">Alterar POI</p>
					<section id="sectionMiddle">
				
				<% 
					showPoiFull(resposta, out, user, campo, request);
					
				%>
			
					
			</section>
			</section>
				
				<form action="RaizCliente.jsp" method="post">
					<input type="hidden" id="tipo" name="tipo" value=apresentar_poi />
					<input type="hidden" id="user" name="user" value="<%=user%>" /> 
					<input type="hidden" id="designacao" name="designacao" value="<%=designacao%>" />
					<input type="hidden" id="fileName" name="fileName" value="<%=Filename%>" />
					<input type = "submit" id="voltar" name="voltar" value="" title="Voltar">
				</form>


			<%	} else if (tipo.equals("alterar_campo_poi")) {
		
					String campo = request.getParameter("campo");
					String user = request.getParameter("user");
					String fileName = request.getParameter("fileName");
					String designacao = request.getParameter("designacao");
					String novaDescricao = request.getParameter("descricao");
					String novasCoordenadas = request.getParameter("localizacao");

					if (novaDescricao != null) {
						String[][] InfoMsg = { { "tipo", tipo },
								{ "campo", campo }, { "fileName", fileName },
								{ "designacao", designacao },
								{ "descricao", novaDescricao } };
						String resposta = comunicarServidor(InfoMsg);
					}
					if (novasCoordenadas != null) {
						String[][] InfoMsg = { { "tipo", tipo },
								{ "campo", campo }, { "fileName", fileName },
								{ "designacao", designacao },
								{ "localizacao", novasCoordenadas } };
						String resposta = comunicarServidor(InfoMsg);
					}
			%>
			
 				<jsp:forward page="RaizCliente.jsp">
						<jsp:param name="tipo" value="alterar_poi" />
						<jsp:param name="campo" value="null" />
						<jsp:param name="designacao" value="<%=designacao%>" />
						<jsp:param name="fileName" value="<%=fileName%>" />
						<jsp:param name="user" value="<%=user%>" />
					</jsp:forward> 
					


			<%	} else if (tipo.equals("votar_poi")) {

					String user = request.getParameter("user");
					String fileName = request.getParameter("fileName");
					String opiniao = request.getParameter("opiniao");
					String designacao = request.getParameter("designacao");
					tipo = "votar_poi";

					String[][] InfoMsg = { { "tipo", tipo }, { "user", user },
							{ "fileName", fileName }, { "designacao", designacao },
							{ "opiniao", opiniao } };
					String resposta = comunicarServidor(InfoMsg);
					String novoTipo = getTipo(resposta);
					
			%>
				<jsp:forward page="RaizCliente.jsp">
						<jsp:param name="tipo" value="<%=novoTipo%>"/> 
						<jsp:param name="designacao" value="<%=designacao%>"/>
						<jsp:param name="fileName" value="<%=fileName%>"/>
						<jsp:param name="user" value="<%=user%>"/>
					</jsp:forward>  
	
			<!-- PESQUISA DE POI'S -->
			<%	} else if (tipo.equals("pesquisa_pois")) {

				  	String user = request.getParameter("user");
				  	String erro = request.getParameter("erro");
			%>
						
					<header id ="headerPrincipal">
						<img alt="LogoSmall" id="imgLogoSmall" src="imagens/LogoSmall.png" />
						<form id="formLogoff" action="RaizCliente.jsp" method="post">
							<input type="hidden" id="tipo" name="tipo" value="login" />
							<input type="hidden" id="user" name="user" value="" /> 
							<input type="submit" id="logoff" value="" title="Logoff" />
						</form>
						<p id ="user">Bem-vindo, &nbsp;<%=user%></p>
					</header>
					
					
					
					<section id="sectionCorpo">
						<img id="miniLogo" src="imagens/pesquisar-mini.png"/>
						<p id="indicador">Pesquisar POI's</p>
						<section id="sectionPesquisa">
							<form action="RaizCliente.jsp" method="post">
								<input type="hidden" id="tipo" name="tipo" value="apresentar_pesquisa" />
								<input type="text" id="Pesquisa" name="Pesquisa" placeholder="Pesquisa" style="margin-top: 12px;"/>
								<input type="hidden" id="user" name="user" value="<%= user %>" /> 
								<input type="submit" id="Pesquisar" value="Pesquisar" title="Pesquisar" />
								<p>
								<input type="radio" id ="radio" name=tipo_pesquisa value="nome" checked="checked"/> Nome
								<input type="radio" id ="radio" name=tipo_pesquisa value="regiao"/> Região
								<input type="radio" id ="radio" name=tipo_pesquisa value="categoria"/> Categoria
							</form>
							
							<form action="RaizCliente.jsp" method="post">
								<input type="hidden" id="tipo" name="tipo" value="listar_regioes" />
								<input type="hidden" id="user" name="user" value="<%= user %>" /> 
								<input type="submit" id="Listar Regiões" value="Listar Regiões" title="Listar Regiões" style="width: 164px; margin-top: 10px;"/>
							</form>
							
							<form action="RaizCliente.jsp" method="post">
								<input type="hidden" id="tipo" name="tipo" value="listar_categorias" />
								<input type="hidden" id="user" name="user" value="<%= user %>" /> 
								<input type="submit" id="Listar Categorias" value="Listar Categorias" title="Listar Categorias" style="width: 164px; margin-top: 10px;"/>
							</form>
							
							<form action="RaizCliente.jsp" method="post">
								<input type="hidden" id="tipo" name="tipo" value="ranking_pois" />
								<input type="hidden" id="user" name="user" value="<%= user %>" /> 
								<input type="submit" id="Apresentar Ranking de POIs" value="Apresentar Ranking de POIs" title="Apresentar Ranking de POIs" style="width: 164px; margin-top: 10px; margin-bottom: 12px;"/>
							</form>
						</section>
					</section>
					
					<form action="RaizCliente.jsp" method="post">
						<input type="hidden" id="user" name="user" value="<%=user%>" /> 
						<input type="hidden" id="tipo" name="tipo" value="menu_principal" /> 
						<input type = "submit" id="voltar" name="voltar" value="" title="Voltar">
					</form>
						
						
			<%
					if (erro != null && !erro.equals("")) {
			%>
						<p id="erro" style="position: absolute; top: 400px; left:595px;">
						<%=erro%></p>
			<%		}
					
			}else if (tipo.equals("apresentar_pesquisa")) {
					
				String user = request.getParameter("user");
			%>
			
					<header id ="headerPrincipal">
						<img alt="LogoSmall" id="imgLogoSmall" src="imagens/LogoSmall.png" />
						<form id="formLogoff" action="RaizCliente.jsp" method="post">
							<input type="hidden" id="tipo" name="tipo" value="login" />
							<input type="hidden" id="user" name="user" value="" /> 
							<input type="submit" id="logoff" value="" title="Logoff" />
						</form>
						<p id ="user">Bem-vindo, &nbsp;<%=user%></p>
					</header>
					
					<section id="sectionCorpo">
					<p id="indicador">Resultado da pesquisa</p>
						<section id="sectionMiddle">
			<%	
				
					String Pesquisa = request.getParameter("Pesquisa");
					String tipo_pesquisa = request.getParameter("tipo_pesquisa"); //nome , regiao , categoria 
					String[][] InfoMsg = { { "tipo", tipo }, { "Pesquisa", Pesquisa }, { "tipo_pesquisa", tipo_pesquisa } };
					String resposta = comunicarServidor(InfoMsg);

					if (getTipo(resposta).equals("apresentar_pesquisa")){
						System.out.println("apresentar_pesquisa: " + resposta);
						TuploReturn tuplo = new TuploReturn("apresentar_pesquisa", Pesquisa);
						tuplo.setTipoPesquisa(tipo_pesquisa);
						showPoiList(resposta, out, user, tuplo);
			%>
					</section>
					</section>
						
						<form action="RaizCliente.jsp" method="post">
							<input type="hidden" id="user" name="user" value="<%=user%>" /> 
							<input type="hidden" id="tipo" name="tipo" value=pesquisa_pois /> 
							<input type = "submit" id="voltar" name="voltar" value="" title="Voltar">
						</form>

						
					<%}else{
					
					String erro = "Não foi encontrado nenhum resultado";
					System.out.println("Não foi encontrado nenhum resultado");
					%>
					
					<jsp:forward page="RaizCliente.jsp">
						<jsp:param name="tipo" value="<%=getTipo(resposta)%>"/> 
						<jsp:param name="user" value="<%=user%>"/>
						<jsp:param name="erro" value="<%=erro%>" />
					</jsp:forward>  
					
					
					<%} 
						
			
					
				} else if (tipo.equals("listar_regioes")) {
			
					String user = request.getParameter("user");
					tipo = request.getParameter("tipo");	
			
					String[][] InfoMsg = { { "tipo", tipo } };
					String resposta = comunicarServidor(InfoMsg);
					System.out.println("apresentar_regioes: " + resposta);
			
					String[][] regioes = this.getMessageValues(resposta);
			%>
			
					<header id ="headerPrincipal">
						<img alt="LogoSmall" id="imgLogoSmall" src="imagens/LogoSmall.png" />
						<form id="formLogoff" action="RaizCliente.jsp" method="post">
							<input type="hidden" id="tipo" name="tipo" value="login" />
							<input type="hidden" id="user" name="user" value="" /> 
							<input type="submit" id="logoff" value="" title="Logoff" />
						</form>
						<p id ="user">Bem-vindo, &nbsp;<%=user%></p>
					</header>

					
					<section id="sectionCorpo">
					<p id="indicador">Regiões Registadas</p>
						<section id="sectionMiddle">

			<%		for (String[] regiao : regioes) {
						if (regiao[0].equals("regiao")) {
			%>
							<form action="RaizCliente.jsp" method="post">
								<input type="hidden" id="user" name="user" value="<%=user%>" /> 
								<input type="hidden" id="tipo" name="tipo" value="apresentar_regiao" /> 
								<input type="submit" id="regiao" class ="simpleButton" name="regiao" value="<%=regiao[1] %>" />
							</form>
			<%			}
					}
			%>
					</section>
					</section>
					<form action="RaizCliente.jsp" method="post">
						<input type="hidden" id="user" name="user" value="<%=user%>" /> 
						<input type="hidden" id="tipo" name="tipo" value=pesquisa_pois /> 
						<input type = "submit" id="voltar" name="voltar" value="" title="Voltar">
					</form>
					
						
			<%	} else if (tipo.equals("listar_categorias")) {
					String user = request.getParameter("user");
					tipo = request.getParameter("tipo");	
					
					

					String[][] InfoMsg = { { "tipo", tipo } };
					String resposta = comunicarServidor(InfoMsg);
					System.out.println("apresentar_categorias: " + resposta);

					String[][] categorias = this.getMessageValues(resposta);
			%>
			
					<header id ="headerPrincipal">
						<img alt="LogoSmall" id="imgLogoSmall" src="imagens/LogoSmall.png" />
						<form id="formLogoff" action="RaizCliente.jsp" method="post">
							<input type="hidden" id="tipo" name="tipo" value="login" />
							<input type="hidden" id="user" name="user" value="" /> 
							<input type="submit" id="logoff" value="" title="Logoff" />
						</form>
						<p id ="user">Bem-vindo, &nbsp;<%=user%></p>
					</header>
			
				
				
					<section id="sectionCorpo">
					<p id="indicador">Categorias Registadas</p>
						<section id="sectionMiddle">
					

			<%		for (String[] categoria : categorias) {
						if (categoria[0].equals("categoria")) {
			%>
							<form action="RaizCliente.jsp" method="post">
								<input type="hidden" id="user" class ="simpleButton" name="user" value="<%=user%>" /> 
								<input type="hidden" id="tipo" class ="simpleButton" name="tipo" value="apresentar_categoria" /> 
								<input type="submit" id="categoria" class ="simpleButton" name="categoria" value="<%=categoria[1] %>" />
							</form>
			<%			}
					}
			%>
			
						</section>
					</section>
				
					<form action="RaizCliente.jsp" method="post">
						<input type="hidden" id="user" name="user" value="<%=user%>" /> 
						<input type="hidden" id="tipo" name="tipo" value="pesquisa_pois" /> 
						<input type = "submit" id="voltar" name="voltar" value="" title="Voltar">
					</form>
			
			<%	} else if (tipo.equals("apresentar_regiao")) {

				String user = request.getParameter("user");
				tipo = request.getParameter("tipo");	
				String regiao = request.getParameter("regiao");	
			%>
			
					<header id ="headerPrincipal">
						<img alt="LogoSmall" id="imgLogoSmall" src="imagens/LogoSmall.png" />
						<form id="formLogoff" action="RaizCliente.jsp" method="post">
							<input type="hidden" id="tipo" name="tipo" value="login" />
							<input type="hidden" id="user" name="user" value="" /> 
							<input type="submit" id="logoff" value="" title="Logoff" />
						</form>
						<p id ="user">Bem-vindo, &nbsp;<%=user%></p>
					</header>
					
					<section id="sectionCorpo">
					<p id="indicador">Região <%=regiao%></p>
						<section id="sectionMiddle">
			<%	
				
				
				
		
				String[][] InfoMsg = { { "tipo", tipo }, { "regiao", regiao } };
				String resposta = comunicarServidor(InfoMsg);
				System.out.println("apresentar_regiao: "+regiao+" : " + resposta);
		
				showPoiList(resposta, out, user, new TuploReturn("apresentar_regiao", regiao));
			%>
			</section>
			</section>
			
			<form action="RaizCliente.jsp" method="post">
				<input type="hidden" id="user" name="user" value="<%=user%>" /> 
				<input type="hidden" id="tipo" name="tipo" value="listar_regioes" /> 
				<input type = "submit" id="voltar" name="voltar" value="" title="Voltar">
			</form>
		
						
			<%	} else if (tipo.equals("ranking_pois")) {
					String user = request.getParameter("user");
			%>
					<header id ="headerPrincipal">
						<img alt="LogoSmall" id="imgLogoSmall" src="imagens/LogoSmall.png" />
						<form id="formLogoff" action="RaizCliente.jsp" method="post">
							<input type="hidden" id="tipo" name="tipo" value="login" />
							<input type="hidden" id="user" name="user" value="" /> 
							<input type="submit" id="logoff" value="" title="Logoff" />
						</form>
						<p id ="user">Bem-vindo, &nbsp;<%=user%></p>
					</header>
					
					<section id="sectionCorpo">
					<p id="indicador">Ranking POI's</p>
						<section id="sectionMiddle">
			<%
					
					String[][] InfoMsg = { { "tipo", tipo }};
					String resposta = comunicarServidor(InfoMsg);
					System.out.println("apresentar_ranking: " + resposta);
					showPoiList(resposta, out, user, new TuploReturn("ranking_pois", null));

			%>					
					</section>
					</section>
					
					<form action="RaizCliente.jsp" method="post">
						<input type="hidden" id="user" name="user" value="<%=user%>" /> 
						<input type="hidden" id="tipo" name="tipo" value=pesquisa_pois /> 
						<input type = "submit" id="voltar" name="voltar" value="" title="Voltar">
					</form>
						
			<%	} else if (tipo.equals("apresentar_categoria")) {
					String user = request.getParameter("user");
			%>
			
					<header id ="headerPrincipal">
						<img alt="LogoSmall" id="imgLogoSmall" src="imagens/LogoSmall.png" />
						<form id="formLogoff" action="RaizCliente.jsp" method="post">
							<input type="hidden" id="tipo" name="tipo" value="login" />
							<input type="hidden" id="user" name="user" value="" /> 
							<input type="submit" id="logoff" value="" title="Logoff" />
						</form>
						<p id ="user">Bem-vindo, &nbsp;<%=user%></p>
					</header>
					
					<section id="sectionCorpo">
					<p id="indicador">Ranking POI's</p>
						<section id="sectionMiddle">
			<%
				
				tipo = request.getParameter("tipo");	
				String categoria = request.getParameter("categoria");	
		
				String[][] InfoMsg = { { "tipo", tipo }, { "categoria", categoria } };
				String resposta = comunicarServidor(InfoMsg);
				System.out.println("apresentar_categoria: "+categoria+" : " + resposta);
		
				showPoiList(resposta, out, user,new TuploReturn("apresentar_categoria", categoria));
			%>
					</section>
				</section>
			
				<form action="RaizCliente.jsp" method="post">
					<input type="hidden" id="tipo" name="tipo" value="listar_categorias" />
					<input type="hidden" id="user" name="user" value="<%=user%>" /> 
					<input type = "submit" id="voltar" name="voltar" value="" title="Voltar">
				</form>

			<%	}
			%>
			
		<footer>Criado por João Sousa e Jorge Fernandes</footer>
	</div>
</body>
</html>