package br.com.andrelfreis.typesconverter.oracle;

import static org.junit.Assert.assertEquals;

import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.Types;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

import oracle.jdbc.OracleCallableStatement;
import oracle.jdbc.OracleConnection;
import oracle.jdbc.OracleTypes;

import org.junit.After;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import br.com.andrelfreis.typesconverter.oracle.printer.STRUCTPrinter;

public class OracleTypesConverterPrimitivosTest {
	
	private static final String FC_TESTAR_ENVIO_OBJ = "{? = call PK_TESTE_ORACLETYPESASSEMBLY.FC_TESTAR_ENVIO_OBJ(?,?)}";
	private static final String FC_TESTAR_RETORNO_OBJ = "{? = call PK_TESTE_ORACLETYPESASSEMBLY.FC_TESTAR_RETORNO_OBJ}";
	private OracleConnection oconn;
	private OracleCallableStatement cs;
	private ConnectionConfig config;
		
	
	@BeforeClass
	public static void prepareTest() throws Exception {
		DriverManager.registerDriver (new oracle.jdbc.OracleDriver());
	}
	
	@Before
	public void setUp() throws Exception {
		config = new ConnectionConfig();
		
		String url = config.getUrl();
		String user = config.getUser();
		String password = config.getPass();
		
		Connection connection = DriverManager.getConnection(url, user, password);        
		//oconn = OracleConnectionUnwrapper.unwrapOracleConnection(connection);		
		oconn = (OracleConnection) connection;
	}

	@After
	public void tearDown() throws Exception {
		cs.close();
        oconn.close();
	}
	
	/**
	 * Para enviar texto a partir do oracle 11g, cujo charset é AL16UTF16, é
	 * necessário importar a lib no pom:
	 * 
	 * <pre>
	 * &lt;dependency&gt; 
	 *     &lt;groupId&gt;com.oracle&lt;/groupId&gt; 
	 *     &lt;artifactId&gt;orai18n&lt;/artifactId&gt;
	 *     &lt;version&gt;11.2.0.3.0&lt;/version&gt;
	 *     &lt;scope>test&lt;/scope&gt;			 
	 * &lt;/dependency&gt;
	 * </pre>
	 * 
	 */
	@Test	
	public void enviarParametrosPreenchidos() throws Exception {		
		
		System.out.println("Variaveis de ambiente");
		
		System.out.println("url: " + config.getUrl());
		System.out.println("user: " + config.getUser());
		System.out.println("pass: " + config.getPass());

		System.out.println("url s: " + System.getProperty("url"));
		System.out.println("user s: " + System.getProperty("user"));
		System.out.println("pass s: " + System.getProperty("pass"));
		
		
		OracleTypesObjPrimitivos param1 = new OracleTypesObjPrimitivos();
		
		param1.setByte_j((byte) 127); // -128 a 127
		param1.setShort_j((short) -32768); // -32.768 a 32.767
		param1.setInt_j(2147483647); //  -2,147,483,648 a 2,147,483,647
		param1.setLong_j(-9223372036854775808l); // -9,223,372,036,854,775,808 a 9,223,372,036,854,775,807
		param1.setFloat_j(12.34f); // NUMBER(4,2) no Oracle
		param1.setDouble_j(-1234.5678); // NUMBER(8,4) no Oracle
		param1.setBigDecimal_j(new BigDecimal("1234567890123.45")); // NUMBER(17,2) no Oracle
		param1.setString_j("Aqui tem TRINTA CARACTERES..."); // VARCHAR2(30) no Oracle
		param1.setDate_j(new Date());
		param1.setBoolean_j(true);
		param1.setClob_j("Aqui tem um clob?"); // CLOB no Oracle - Não é possivel testar um CLOB sem gravá-lo numa tabela
		param1.setSempreEnviaNullParaOracle("BLA BLA BLA VAI CHEGAR NULL");
		
		DecimalFormat df = new DecimalFormat();  
        df.applyPattern("###0.00");
        String float_s = df.format(param1.getFloat_j());
        df.applyPattern("###0.00##");
        String double_s = df.format(param1.getDouble_j());
        df.applyPattern("##############0.00");
        String big_s = df.format(param1.getBigDecimal_j());
        SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss");
        String date_s = sdf.format(param1.getDate_j());
        
        String expected = "BYTE_J = " + param1.getByte_j() + 
        		          ", SHORT_J = " + param1.getShort_j() +
        		          ", INT_J = " + param1.getInt_j() +
        		          ", LONG_J = " + param1.getLong_j() +
        		          ", FLOAT_J = " + float_s +
        		          ", DOUBLE_J = " + double_s +
        		          ", BIGDECIMAL_J = " + big_s +
        		          ", TEXTO = " + param1.getString_j() +
        		          ", DATE_J = " + date_s +
        		          ", BOOLEAN_J = " + (param1.isBoolean_j() ? 1 : 0) +
        		          ", CLOB_J = " + "" + // Empty Clob não inicializado, pois não estamos efetuando insert em tabela
        		          ", RECEBE_NULL = " + ""
        		          ;
        
        cs = (OracleCallableStatement) oconn.prepareCall(FC_TESTAR_ENVIO_OBJ);

        cs.registerOutParameter(1, Types.VARCHAR);
        cs.setSTRUCT(2, OracleTypesConverter.toSTRUCT(oconn, param1));
        cs.registerOutParameter(3, OracleTypes.CLOB);
        
        STRUCTPrinter.print(OracleTypesConverter.toSTRUCT(oconn, param1));
        
        cs.execute();
        
        String actual = cs.getString(1);
     /* Não recuperamos o clob pois o EmptyClob só é inicializado quando inserido em tabela
        CLOB clob = cs.getCLOB(3);
		Writer sw = clob.setCharacterStream(1L); 
		sw.write(ob.getDescricao().trim().toCharArray());
		sw.flush();
		sw.close();
      */ 
        assertEquals(expected, actual);        
	}
	
	
	
	@Test
	public void enviarParametrosDefault() throws Exception {		
		OracleTypesObjPrimitivos param1 = new OracleTypesObjPrimitivos();
        
        String expected = "BYTE_J = " + param1.getByte_j() + 
        		          ", SHORT_J = " + param1.getShort_j() +
        		          ", INT_J = " + param1.getInt_j() +
        		          ", LONG_J = " + param1.getLong_j() +
        		          ", FLOAT_J = " + 0 +
        		          ", DOUBLE_J = " + 0 +
        		          ", BIGDECIMAL_J = " + "" +
        		          ", TEXTO = " + "" +
        		          ", DATE_J = " + "" +
        		          ", BOOLEAN_J = " + (param1.isBoolean_j() ? 1 : 0) +
        		          ", CLOB_J = " + "" +
        		          ", RECEBE_NULL = " + ""
        		          ;
		
        cs = (OracleCallableStatement) oconn.prepareCall(FC_TESTAR_ENVIO_OBJ);

        cs.registerOutParameter(1, Types.VARCHAR);
        cs.setSTRUCT(2, OracleTypesConverter.toSTRUCT(oconn, param1));
        cs.registerOutParameter(3, OracleTypes.CLOB);
        
        STRUCTPrinter.print(OracleTypesConverter.toSTRUCT(oconn, param1));
        
        cs.execute();
        
        String actual = cs.getString(1);
        
        assertEquals(expected, actual);
       
	}
	
	
	
	@Test
	public void receberParametrosPrimitivos() throws Exception {
		OracleTypesObjPrimitivos expected = new OracleTypesObjPrimitivos();
		
		expected.setByte_j((byte) -128); // -128 a 127
		expected.setShort_j((short) 32767); // -32.768 a 32.767
		expected.setInt_j(-2147483648); //  -2,147,483,648 a 2,147,483,647
		expected.setLong_j(9223372036854775807l); // -9,223,372,036,854,775,808 a 9,223,372,036,854,775,807
		expected.setFloat_j(-55f); // NUMBER(4,2) no Oracle
		expected.setDouble_j(1.9999); // NUMBER(8,4) no Oracle
		expected.setBigDecimal_j(new BigDecimal("-1798.72")); // NUMBER(17,2) no Oracle
		expected.setString_j("TEXTO COM TRINTA CARACTERES!!!"); // VARCHAR2(30) no Oracle        
		expected.setDate_j(new Date(2013-1900,4,13,14,9,18));		
		expected.setBoolean_j(true);
		expected.setClob_j("TESTE"); // CLOB no Oracle - Não é possivel testar um CLOB sem gravá-lo numa tabela
		expected.setSempreEnviaNullParaOracle("CAMPO POPULADO PELO ORACLE");
		
		cs = (OracleCallableStatement) oconn.prepareCall(FC_TESTAR_RETORNO_OBJ);

		cs.registerOutParameter(1, OracleTypes.STRUCT, OracleTypesConverter.getDBTypeAnnotation(OracleTypesObjPrimitivos.class));
        
		cs.execute();
        
        OracleTypesObjPrimitivos actual = OracleTypesConverter.toBean(OracleTypesObjPrimitivos.class, cs.getSTRUCT(1));
	        
	    assertEquals(expected.toString(), actual.toString());		
	}
	
	
}
