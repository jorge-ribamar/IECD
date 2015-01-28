package Servidor;

import java.awt.image.BufferedImage;
import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.concurrent.Semaphore;

import javax.imageio.ImageIO;

import sun.misc.BASE64Decoder;
import classes_auxiliares.Poi;
import classes_auxiliares.PoiTuple;

public class Servidor {

	int port;
	ServerSocket serverSocket;
	Socket newSock;
	BufferedReader is;
	PrintWriter os;
	private Semaphore accessFile;
	

	public Servidor(int port) {
		this.port = port;
		serverSocket = null;
		newSock = null;
		is = null;
		os = null;
		accessFile = new Semaphore(1);
	}

	public void correr() {
		try {
			serverSocket = new ServerSocket(port);

			Socket newSock = null;

			for (;;) {
				System.out
						.println("Servidor TCP concorrente aguarda ligacao no porto "
								+ port + "...");

				// Espera connect do cliente
				newSock = serverSocket.accept();

				Thread th = new ThreadServidor(newSock, accessFile);
				th.start();
			}
		} catch (IOException e) {
			System.err.println("Excepçãoo no servidor: " + e);
		}
	}

	public static void main(String[] args) {
		Servidor servidor = new Servidor(5000);
		servidor.correr();
	}

}

class ThreadServidor extends Thread {

	private Socket connection;
	AcessoFicheiros ficheiros;
	LeituraStringsXML leituraString;
	UsersDB userDB = new UsersDB();
	private Semaphore accessoFicheiro;

	public ThreadServidor(Socket connection, Semaphore accessFile) {
		this.connection = connection;
		this.accessoFicheiro = accessFile;
		ficheiros = new AcessoFicheiros(accessoFicheiro);
		leituraString = new LeituraStringsXML(accessoFicheiro);
	}
	
	/**
     * Descodifica uma String numa imagem
     * @param imageString A string a descodificar
     * @return decoded image
     */
    public static BufferedImage decodeToImage(String imageString) {

        BufferedImage image = null;
        byte[] imageByte;
        try {
            BASE64Decoder decoder = new BASE64Decoder();
            imageByte = decoder.decodeBuffer(imageString);
            ByteArrayInputStream bis = new ByteArrayInputStream(imageByte);
            image = ImageIO.read(bis);
            bis.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return image;
    }

	public void run() {

		BufferedReader is = null;
		PrintWriter os = null;

		try {
			// Circuito virtual estabelecido: socket cliente na variavel newSock
			System.out.println("Thread " + this.getId() + ": "
					+ connection.getRemoteSocketAddress());

			is = new BufferedReader(new InputStreamReader(
					connection.getInputStream()));

			os = new PrintWriter(connection.getOutputStream(), true);

			String xml = "";
            
            do{
            	xml += is.readLine()+"\n";
            }while(!xml.substring(xml.length()-7-1).equals("</tipo>\n"));
            
            System.out.println("Recebi -> " + xml);

            String resposta = this.responderPedido(xml);

			os.println(resposta);
		} catch (IOException e) {
			System.err.println("erro na ligação " + connection + ": "
					+ e.getMessage());
		} finally {
			// garantir que o socket é fechado
			try {
				if (is != null)
					is.close();
				if (os != null)
					os.close();

				if (connection != null)
					connection.close();
			} catch (IOException e) {
			}
		}
	} // end run

	private String responderPedido(String mensagemRecebida) {
		String tipo = this.leituraString.getTipo(mensagemRecebida);
		String mensagemResposta = null;
		String user;
		String pass1;
		String pass2;
		String novaPass;
		String antigaPass;
		boolean userValido;
		boolean antigaPassValida;
		boolean passwordCorrecta;
		String Filename;
		String designacao;
		switch (tipo) {
		case "enviar_login":
			user = leituraString.getChildNode(mensagemRecebida, "user");
			pass1 = leituraString.getChildNode(mensagemRecebida, "pass");
			userValido = userDB.existUser(user);
			passwordCorrecta = userDB.isPasswordCorrect(user, pass1);
			System.out.println("user existe: " + userValido);
			System.out.println("pass correcta: " + passwordCorrecta);
			if (userValido && passwordCorrecta) {
				String[][] parametros = { { "tipo", "menu_principal" },
						{ "erro", "" } };
				mensagemResposta = this.leituraString.gerarResposta(parametros);
			} else if (!userValido) {
				String[][] parametros = { { "tipo", "login" },
						{ "erro", "O user não está registado." } };
				mensagemResposta = this.leituraString.gerarResposta(parametros);
			} else if (!passwordCorrecta) {
				String[][] parametros = { { "tipo", "login" },
						{ "erro", "Password incorrecta." } };
				mensagemResposta = this.leituraString.gerarResposta(parametros);
			}
			break;

		case "alterar_pass": // Finalizar o case
			user = leituraString.getChildNode(mensagemRecebida, "user");
			antigaPass = leituraString.getChildNode(mensagemRecebida, "antigaPass");
			novaPass = leituraString.getChildNode(mensagemRecebida, "novaPass");
			userValido = userDB.existUser(user);
			antigaPassValida = userDB.isPasswordCorrect(user, antigaPass);
			if (userValido) {
				if(antigaPassValida){
					userDB.mudaPass(user, novaPass);
					String[][] parametros = { { "tipo", "pass_alterada" },
							{ "erro", "" } };
					mensagemResposta = this.leituraString.gerarResposta(parametros);
				}
				else{ // Se a antiga password estiver errada
					String[][] parametros = { { "tipo", "pass_invalida" },
							{ "erro", "A password antiga está incorrecta" } };
					mensagemResposta = this.leituraString.gerarResposta(parametros);
				}
			
			} else {
				String[][] parametros = { { "tipo", "user_invalido" },
						{ "erro", "O user indicado não está registado" } };
				mensagemResposta = this.leituraString.gerarResposta(parametros);
			}

			break;
		case "enviar_registo":
			user = leituraString.getChildNode(mensagemRecebida, "userName");
			pass1 = leituraString.getChildNode(mensagemRecebida, "pass1");
			pass2 = leituraString.getChildNode(mensagemRecebida, "pass2");
			userValido = !userDB.existUser(user);
			passwordCorrecta = pass1.equals(pass2);
			System.out.println("user válido: " + userValido);
			System.out.println("pass correcta: " + passwordCorrecta);
			System.out.println("password = " + pass1);
			if(user.equals("")){
				String[][] parametros = { { "tipo", "registo" }, { "erro", "O campo Utilizador não pode estar vazio." } };
				mensagemResposta = this.leituraString.gerarResposta(parametros);		
			
			} else if (pass1.equals("")){
				String[][] parametros = { { "tipo", "registo" }, { "erro", "O campo Password não pode estar vazio." } };
				mensagemResposta = this.leituraString.gerarResposta(parametros);
			
			} else if (userValido && passwordCorrecta) {
				userDB.addAccount(user, pass1);
				String[][] parametros = { { "tipo", "menu_principal" },
						{ "erro", "" } };
				mensagemResposta = this.leituraString.gerarResposta(parametros);
			
			} else if (!userValido) {
				String[][] parametros = {
						{ "tipo", "registo" },
						{ "erro",
								"Nome de utilizador existente, por favor escolha outro." } };
				mensagemResposta = this.leituraString.gerarResposta(parametros);
			
			} else {
				String[][] parametros = { { "tipo", "registo" },
						{ "erro", "Insira as duas passwords iguais." } };
				mensagemResposta = this.leituraString.gerarResposta(parametros);
			}
			break;
		case "apresentar_users":
			ArrayList<String> users = ficheiros.getUsers();
			for (String userShow : users) {
				System.out.println(userShow);
			}

			String[][] parametros = new String[users.size() + 1][2];
			parametros[0][0] = "tipo";
			parametros[0][1] = "apresentar_users";
			for (int i = 0; i < users.size(); i++) {
				parametros[i + 1][0] = "user";
				parametros[i + 1][1] = users.get(i);
			}
			mensagemResposta = this.leituraString.gerarResposta(parametros);
			break;
		case "enviar_adicionar_poi":
			user = leituraString.getChildNode(mensagemRecebida, "user");
			String nomePoi = leituraString.getChildNode(mensagemRecebida,
					"nomePoi");
			String regiao = leituraString.getChildNode(mensagemRecebida,
					"regiao");
			String categoria = leituraString.getChildNode(mensagemRecebida,
					"categoria");
			String descricao = leituraString.getChildNode(mensagemRecebida,
					"descricao");
			String coordenadas = leituraString.getChildNode(mensagemRecebida,
					"coordenadas");
			String String_Imagem = leituraString.getChildNode(mensagemRecebida,
					"imagem");
			
			String multimedia1 = leituraString.getChildNode(mensagemRecebida,
					"multimedia1");
			String multimedia2 = leituraString.getChildNode(mensagemRecebida,
					"multimedia2");
			String multimedia3 = leituraString.getChildNode(mensagemRecebida,
					"multimedia3");
			String multimedia4 = leituraString.getChildNode(mensagemRecebida,
					"multimedia4");

			System.out.println("Info POI");
			System.out.println(nomePoi);
			System.out.println(regiao);
			System.out.println(categoria);
			System.out.println(descricao);
			System.out.println(coordenadas);

			if (nomePoi.equals("null") || nomePoi.equals("")
					|| categoria == null) {
				String erro = "Por favor, preencha os campos obrigatórios";
				String[][] Parametros = { { "tipo", "adicionar_poi" },
						{ "erro", erro } };
				mensagemResposta = this.leituraString.gerarResposta(Parametros);
			} else if (ficheiros.existPOI(regiao.toLowerCase() + ".xml",
					nomePoi)) {
				String erro = "Impossivel adicionar um POI existente";
				String[][] Parametros = { { "tipo", "adicionar_poi" },
						{ "erro", erro } };
				mensagemResposta = this.leituraString.gerarResposta(Parametros);
			} else {
				// Fazer a adição do POI
				Poi POI = new Poi(nomePoi);
				POI.setCriador(user);
				POI.setRegiao(regiao);
				POI.addCategoria(categoria);
				POI.setDescricao(descricao);
				POI.setLocalizacao(coordenadas);
				if (multimedia1 != null && !multimedia1.equals("") && !multimedia1.equals("null")){
					POI.addMultimedia(multimedia1);
				}
				if (multimedia2 != null && !multimedia2.equals("") && !multimedia2.equals("null")){
					POI.addMultimedia(multimedia2);
				}
				if (multimedia3 != null && !multimedia3.equals("") && !multimedia3.equals("null")){
					POI.addMultimedia(multimedia3);
				}
				if (multimedia4 != null && !multimedia4.equals("") && !multimedia4.equals("null")){
					POI.addMultimedia(multimedia4);
				}
				if (!String_Imagem.equals("")){
					BufferedImage newImg;
					newImg = decodeToImage(String_Imagem);
			        try {
			        	String imageName =nomePoi+"_"+regiao+".png";
						ImageIO.write(newImg, "png", new File(ficheiros.ImageFilePath +imageName));
						POI.setImageName(imageName);
					} catch (IOException e) {
						e.printStackTrace();
					}
				}
				
				ficheiros.adicionarPOI(POI, user);
				
				String[][] Parametros = { { "tipo", "menu_principal" },
						{ "erro", "" } };
				mensagemResposta = this.leituraString.gerarResposta(Parametros);

			}
			break;
		case "apresentar_user":
			System.out.println("recebi apresentar user: " + mensagemRecebida);
			user = leituraString.getChildNode(mensagemRecebida, "showUser");
			ArrayList<PoiTuple> POIs = ficheiros.getPOIsUser(user);
			mensagemResposta = this.leituraString.gerarXMLPoiList(POIs,
					"apresentar_user");
			break;
		case "apresentar_poi":
		case "alterar_poi":
			tipo = leituraString.getTipo(mensagemRecebida);
			System.out.println("Recebi + " + mensagemRecebida);
			designacao = leituraString.getChildNode(mensagemRecebida,
					"designacao");
			Filename = leituraString.getChildNode(mensagemRecebida, "Filename");
			Poi POI = ficheiros.getPoi(designacao, Filename);
			mensagemResposta = this.leituraString.gerarXMLPoi(POI, Filename,
					true, tipo);
			break;
		case "eliminar_poi":
			tipo = leituraString.getTipo(mensagemRecebida);
			System.out.println("Recebi + " + mensagemRecebida);
			designacao = leituraString.getChildNode(mensagemRecebida,
					"designacao");
			Filename = leituraString.getChildNode(mensagemRecebida, "Filename");

			ficheiros.eliminarPoi(designacao, Filename);

			String[][] Par = { { "tipo", "menu_principal" } };
			mensagemResposta = this.leituraString.gerarResposta(Par);
			break;
		case "votar_poi":
			user = leituraString.getChildNode(mensagemRecebida, "user");
			designacao = leituraString.getChildNode(mensagemRecebida,
					"designacao");
			String opiniao = leituraString.getChildNode(mensagemRecebida,
					"opiniao");
			Filename = leituraString.getChildNode(mensagemRecebida, "fileName");
			if (opiniao != null && !opiniao.equals("null")
					&& !opiniao.equals("")) {
				ficheiros.darOpiniao(designacao, Filename, opiniao, user);
			}
			String[][] Parametros = { { "tipo", "apresentar_poi" } };
			mensagemResposta = this.leituraString.gerarResposta(Parametros);
			break;
		case "alterar_campo_poi":
			String campo = leituraString
					.getChildNode(mensagemRecebida, "campo");
			Filename = leituraString.getChildNode(mensagemRecebida, "fileName");
			designacao = leituraString.getChildNode(mensagemRecebida,
					"designacao");
			if (campo.equals("descricao")) {
				String novadescricao = leituraString.getChildNode(
						mensagemRecebida, "descricao");
				ficheiros.alterarDescricao(designacao, Filename, novadescricao);

			} else if (campo.equals("localizacao")) {
				String novaCoordenadas = leituraString.getChildNode(
						mensagemRecebida, "localizacao");
				ficheiros.alterarCoordenadas(designacao, Filename,
						novaCoordenadas);

			}

			String[][] Parametro = { { "tipo", "alterar_poi" } };
			mensagemResposta = this.leituraString.gerarResposta(Parametro);
			break;
		case "listar_categorias":

			ArrayList<String> categorias = ficheiros.listCategories();

			for (String categoria1 : categorias) {
				System.out.println(categoria1);
			}

			String[][] parametros1 = new String[categorias.size() + 1][2];
			parametros1[0][0] = "tipo";
			parametros1[0][1] = "listar_categorias";
			for (int i = 0; i < categorias.size(); i++) {
				parametros1[i + 1][0] = "categoria";
				parametros1[i + 1][1] = categorias.get(i);
			}
			mensagemResposta = this.leituraString.gerarResposta(parametros1);

			break;
		case "listar_regioes":
			ArrayList<String> regioes = ficheiros.listRegions();

			for (String regiao1 : regioes) {
				System.out.println(regiao1);
			}

			String[][] parametros2 = new String[regioes.size() + 1][2];
			parametros2[0][0] = "tipo";
			parametros2[0][1] = "listar_categorias";
			for (int i = 0; i < regioes.size(); i++) {
				parametros2[i + 1][0] = "regiao";
				parametros2[i + 1][1] = regioes.get(i);
			}
			mensagemResposta = this.leituraString.gerarResposta(parametros2);

			break;
		case "apresentar_regiao":
			System.out.println("recebi apresentar regiao: " + mensagemRecebida);
			regiao = leituraString.getChildNode(mensagemRecebida, "regiao");
			POIs = ficheiros.showPOIRegion(regiao);
			mensagemResposta = this.leituraString.gerarXMLPoiList(POIs,
					"apresentar_regiao");
			break;
		case "apresentar_categoria":
			System.out.println("recebi apresentar categoria: "
					+ mensagemRecebida);
			categoria = leituraString.getChildNode(mensagemRecebida,
					"categoria");
			POIs = ficheiros.showPOICategorie(categoria);
			mensagemResposta = this.leituraString.gerarXMLPoiList(POIs,
					"apresentar_categoria");
			break;
		case "apresentar_pesquisa":
			System.out.println("recebi apresentar_pesquisa: "
					+ mensagemRecebida);
			String Pesquisa = leituraString.getChildNode(mensagemRecebida,
					"Pesquisa");
			String tipo_pesquisa = leituraString.getChildNode(mensagemRecebida,
					"tipo_pesquisa");
			// tipo_pesquisa = nome , regiao , categoria
			if (tipo_pesquisa.equals("regiao")) {
				POIs = ficheiros.SearchByRegion(Pesquisa);
				if (POIs == null) {
					String[][] Parametro3 = { { "tipo", "pesquisa_pois" } };
					mensagemResposta = this.leituraString
							.gerarResposta(Parametro3);
				} else {
					mensagemResposta = this.leituraString.gerarXMLPoiList(POIs,
							"apresentar_pesquisa");
				}
				break;
			} else if (tipo_pesquisa.equals("nome")) {
				POIs = ficheiros.searchByName(Pesquisa);
				if (POIs == null) {
					String[][] Parametro3 = { { "tipo", "pesquisa_pois" } };
					mensagemResposta = this.leituraString
							.gerarResposta(Parametro3);
				} else {
					mensagemResposta = this.leituraString.gerarXMLPoiList(POIs,
							"apresentar_pesquisa");
				}
				break;
			} else if (tipo_pesquisa.equals("categoria")) {
				if (ficheiros.listCategories().contains(Pesquisa)) {
					POIs = ficheiros.showPOICategorie(Pesquisa);
					mensagemResposta = this.leituraString.gerarXMLPoiList(POIs,
							"apresentar_pesquisa");
				} else {
					String[][] Parametro3 = { { "tipo", "pesquisa_pois" } };
					mensagemResposta = this.leituraString
							.gerarResposta(Parametro3);
				}
				break;
			}
			break;
		case "ranking_pois":
			ArrayList<String> regioes1 = ficheiros.listRegions();
			POIs = new ArrayList<PoiTuple>();
			for (String regiao1 : regioes1) {
				POIs.addAll(ficheiros.showPOIRegion(regiao1));
			}
			// Sorting
			Collections.sort(POIs, new Comparator<PoiTuple>() {
				@Override
				public int compare(PoiTuple tuple1, PoiTuple tuple2) {
					Poi POI1 = ficheiros.getPoi(tuple1.getdesignacao(),
							tuple1.getFilename());
					Poi POI2 = ficheiros.getPoi(tuple2.getdesignacao(),
							tuple2.getFilename());
					int pont1 = POI1.getPontuacao();
					int pont2 = POI2.getPontuacao();
					return pont2 - pont1;
				}
			});
			mensagemResposta = this.leituraString.gerarXMLPoiList(POIs,
					"ranking_pois");
			break;
		}
		return mensagemResposta;

	}

} // end HandleConnectionThread