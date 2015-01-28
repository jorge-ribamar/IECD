package classes_auxiliares;

import java.util.ArrayList;

public class Poi {

	private String criador;
	private String designacao;
	private String localizacao;
	private String descricao;
	private String regiao;
	private int gostei;
	private int adorei;
	private int nao_vou_voltar;
	private ArrayList<String> Categorias;
	private ArrayList<String> multimedia;
	private String imageName ;

	public Poi(String designacao) {
		this.designacao = designacao;
		this.Categorias= new ArrayList<String>();
		this.multimedia= new ArrayList<String>();
	}

	
	public int getGostei() {
		return gostei;
	}
	
	public int getPontuacao() {
		return 3 * adorei + 1 * gostei - 2 * nao_vou_voltar;
	}


	public void setGostei(int gostei) {
		this.gostei = gostei;
	}


	public int getAdorei() {
		return adorei;
	}


	public void setAdorei(int adorei) {
		this.adorei = adorei;
	}


	public int getNao_vou_voltar() {
		return nao_vou_voltar;
	}


	public void setNao_vou_voltar(int nao_vou_voltar) {
		this.nao_vou_voltar = nao_vou_voltar;
	}

	

	public ArrayList<String> getCategorias() {
		return Categorias;
	}


	public ArrayList<String> getMultimedia() {
		return multimedia;
	}


	public void addCategoria(String Categoria){
		Categorias.add(Categoria);
	}
	public void addMultimedia(String documento){
		multimedia.add(documento);
	}
	
	
	
	public String getCriador() {
		return criador;
	}

	public void setCriador(String criador) {
		this.criador = criador;
	}

	public String getDesignacao() {
		return designacao;
	}

	public void setDesignacao(String designacao) {
		this.designacao = designacao;
	}

	public String getLocalizacao() {
		return localizacao;
	}

	public void setLocalizacao(String localizacao) {
		this.localizacao = localizacao;
	}

	public String getDescricao() {
		return descricao;
	}

	public void setDescricao(String descricao) {
		this.descricao = descricao;
	}


	public String getRegiao() {
		return regiao;
	}


	public void setRegiao(String regiao) {
		this.regiao = regiao;
	}


	public String getImageName() {
		return imageName;
	}


	public void setImageName(String imageName) {
		this.imageName = imageName;
	}

}