package br.com.fiap.resolveai.model;

/** Municipio atendido pelo ResolveAI. */
public class Municipio {

    private Long id;
    private String nome;
    private String uf;
    private Integer habitantes;

    public Municipio() {
    }

    public Municipio(Long id, String nome, String uf, Integer habitantes) {
        this.id = id;
        this.nome = nome;
        this.uf = uf;
        this.habitantes = habitantes;
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getNome() { return nome; }
    public void setNome(String nome) { this.nome = nome; }

    public String getUf() { return uf; }
    public void setUf(String uf) { this.uf = uf; }

    public Integer getHabitantes() { return habitantes; }
    public void setHabitantes(Integer habitantes) { this.habitantes = habitantes; }
}
