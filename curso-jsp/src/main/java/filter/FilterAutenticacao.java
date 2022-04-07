package filter;

import java.io.IOException;
import java.sql.Connection;
import java.sql.SQLException;

import connection.SingleConnectionBanco;
import jakarta.servlet.FilterChain;
import jakarta.servlet.FilterConfig;
import jakarta.servlet.RequestDispatcher;
import jakarta.servlet.ServletException;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.ServletResponse;
import jakarta.servlet.annotation.WebFilter;
import jakarta.servlet.http.HttpFilter;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;

@WebFilter(urlPatterns = {"/principal/*"})/*Intercepta todas as requisi��es que vierem do projeto ou mapeamento*/
public class FilterAutenticacao extends HttpFilter {
	
	
	private static Connection connection;
	

    public FilterAutenticacao() {

    }

    /*Encerra os processos quando o servidor � parado*/
    /*Mataria os precessos de conecx�o com o banco*/
	public void destroy() {
		try {
			connection.close();
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}

	/*Intercepta as requisi��e e as respostas no sistema*/
	/*Tudo que fizer no sistema vai fazer por aqui*/
	/*Valida��o de autentica��o*/
	/*Dar commite e rolback de transa��es do banco*/
	/*Validar e fazer redirecionamento de paginas*/
	public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain) 
			throws IOException, ServletException {
		try {
		HttpServletRequest req = (HttpServletRequest) request;
		HttpSession session = req.getSession();
		
		String usuarioLogado = (String) session.getAttribute("usuario");
		
		String urlParaAutenticar = req.getServletPath();/*url que esta sendo acessada*/
		
		/*Validar se est� logado sen�o redirecionar para tela de login*/
		if(usuarioLogado == null &&
				!urlParaAutenticar.equalsIgnoreCase("/principal/ServletLogin")) {/*N�o est� logado*/
			
			RequestDispatcher redireciona = request.getRequestDispatcher("/index.jsp?url=" + urlParaAutenticar);
			request.setAttribute("msg", "Por favor realize o login!");
			redireciona.forward(request, response);
			return; /*Para a execu��o e redireciona para o login*/
			
		}else {
		
		chain.doFilter(request, response);
		}
		
		connection.commit();/*Deu tudo certo, ent�o comita as altera��es no banco de dados*/
		
		}catch (Exception e) {
			e.printStackTrace();
			RequestDispatcher redirecionar = request.getRequestDispatcher("erro.jsp");
			request.setAttribute("msg", e.getMessage());
			redirecionar.forward(request, response);
			
			try {
				connection.rollback();
			} catch (SQLException e1) {
				e1.printStackTrace();
			}
		}
	}

	/*Inicia os processos ou recursos quando o servidor sobe o prejeto*/
	/*iniciar a conex�o com o banco*/
	public void init(FilterConfig fConfig) throws ServletException {
		connection = SingleConnectionBanco.getConnection();

	}

}
