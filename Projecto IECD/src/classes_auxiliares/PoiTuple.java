package classes_auxiliares;

public class PoiTuple {

	private String designacao;
	private String filename;
	
	public PoiTuple(String designacao, String filename) {
		this.setdesignacao(designacao);
		this.setFilename(filename);
	}

	public String getdesignacao() {
		return designacao;
	}

	public void setdesignacao(String designacao) {
		this.designacao = designacao;
	}

	public String getFilename() {
		return filename;
	}

	public void setFilename(String filename) {
		this.filename = filename;
	}
	
	
}