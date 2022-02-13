package br.ufc.smd.meucloset.model;

import java.io.Serializable;
import java.util.List;
import java.util.Objects;

public class Usuario implements Serializable {

    private String usuario;

    private String senha;

    private String nome;

    private String email;

    private String telefone;

    private List<Produto> produtos;

    public String getUsuario() {
        return usuario;
    }

    public void setUsuario(String usuario) {
        this.usuario = usuario;
    }

    public String getSenha() {
        return senha;
    }

    public void setSenha(String senha) {
        this.senha = senha;
    }

    public String getNome() {
        return nome;
    }

    public void setNome(String nome) {
        this.nome = nome;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getTelefone() {
        return telefone;
    }

    public void setTelefone(String telefone) {
        this.telefone = telefone;
    }

    public List<Produto> getProdutos() {
        return produtos;
    }

    public void setProdutos(List<Produto> produtos) {
        this.produtos = produtos;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Usuario usuario1 = (Usuario) o;
        return usuario.equals(usuario1.usuario);
    }

    @Override
    public int hashCode() {
        return Objects.hash(usuario);
    }

    @Override
    public String toString() {
        return "Usuario { " +
                "usuario = '" + usuario + '\'' +
                ", senha = '" + senha + '\'' +
                '}';
    }
}
