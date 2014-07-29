package br.com.andrelfreis.typesconverter.oracle.unwrapper;
//package typesconverter;
//
//import java.lang.reflect.Method;
//import java.sql.Connection;
//
//import oracle.jdbc.OracleConnection;
//
//import org.hibernate.jdbc.BorrowedConnectionProxy;
//
//import typesconverter.OracleTypesAssembly;
//
///**
// * Classe utilit&aacute;ria para extrair a inst&acirc;ncia da
// * {@link oracle.jdbc.OracleConnection} encapsulada pelo Jboss e Hibernate versão 3.5.6.
// *
// * @author Andre Reis <andre-luis.reis@serpro.gov.br>
// * 
// * @see OracleTypesAssembly 
// */
//public class OracleConnectionUnwrapper {
//
//	/**
//	 * Extrai a OracleConnection encapsulada pelo Jboss/Hibernate<br/> 
//	 * Esta classe foi construída devido ao fato de a API OracleTypesAssembly
//	 * precisar de uma OracleConnection. Exemplo de utilização:
//	 * 
//	 * <PRE>
//	 * HibernateUtil.getInstance().getSession().doWork(new Work() {
//	 * 
//	 *    <code>@Override</code>
//	 *    public void execute(Connection connection) {
//	 *     
//	 *    OracleCallableStatement cs = null;
//	 *    try {
//	 *        OracleConnection <strong>oconn</strong> = <i>OracleConnectionUnwrapper.unwrapOracleConnection(connection)</i>;
//	 *        cs = (OracleCallableStatement) oconn.prepareCall(&quot;{call PK_PACKAGE.PR_PROC_TESTE(?)}&quot;);
//	 *        cs.setSTRUCT(1, OracleTypesAssembly.toSTRUCT(<strong>oconn</strong>, pojoJava));
//	 *        cs.execute();
//	 * 
//	 * // restante do c&oacute;digo suprimido
//	 * </PRE>
//	 * 
//	 * @param defaultConnection
//	 *            A conexão sendo utilizada pela aplicação
//	 * @return A OracleConnection desencapsulada.
//	 * 
//	 */
//	public static OracleConnection unwrapOracleConnection(Connection defaultConnection) {
//		
//		OracleConnection oconn = null;
//		
//		Connection proxyCon = BorrowedConnectionProxy.getWrappedConnection(defaultConnection);
//		
//		// Wrapped subclass of Jboss versions on IF condition:
//		// "org.jboss.resource.adapter.jdbc.jdk5.WrappedConnectionJDK5" 
//		// "org.jboss.resource.adapter.jdbc.jdk6.WrappedConnectionJDK6" 
//		// "org.jboss.jca.adapters.jdbc.jdk6.WrappedConnectionJDK6" => Jboss 6 EAP!
//		String wrappedClassName = proxyCon.getClass().getSuperclass().getName();
//
//		if ("org.jboss.resource.adapter.jdbc.WrappedConnection".equals(wrappedClassName) ||
//			"org.jboss.jca.adapters.jdbc.WrappedConnection".equals(wrappedClassName)) {
//			
//			try { 
//			    Method method = (proxyCon.getClass()).getMethod("getUnderlyingConnection", null);
//			    oconn = ((OracleConnection) method.invoke(proxyCon, null));
//			} 
//			catch (Exception e) {
//			    throw new RuntimeException("Erro ao tentar extrair a OracleConnection da super classe: " + wrappedClassName, e);
//			}			
//			
//		}
//		else {
//			oconn = (OracleConnection) proxyCon;
//		}
//		
//		return oconn;
//	}
//
//	
//}
