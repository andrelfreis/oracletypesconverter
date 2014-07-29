package br.com.andrelfreis.typesconverter.oracle;

import static org.junit.Assert.assertEquals;

import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.Types;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import oracle.jdbc.OracleCallableStatement;
import oracle.jdbc.OracleConnection;
import oracle.jdbc.OracleTypes;

import org.junit.After;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import br.com.andrelfreis.typesconverter.oracle.printer.STRUCTPrinter;

public class OracleTypesConverterWrappersTest {
	
	private static final String FC_TESTAR_ENVIO_OBJ = "{? = call PK_TESTE_ORACLETYPESASSEMBLY.FC_TESTAR_ENVIO_OBJ(?,?)}";
	private static final String FC_TESTAR_RETORNO_OBJ = "{? = call PK_TESTE_ORACLETYPESASSEMBLY.FC_TESTAR_RETORNO_OBJ}";
	private static final String FC_TESTAR_ENVIO_LISTA = "{? = call PK_TESTE_ORACLETYPESASSEMBLY.FC_TESTAR_ENVIO_LISTA(?)}";
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
		OracleTypesObjWrappers param1 = new OracleTypesObjWrappers();
		
		param1.setByte_j((byte) 127); // -128 a 127
		param1.setShort_j((short) -32768); // -32.768 a 32.767
		param1.setInt_j(2147483647); //  -2,147,483,648 a 2,147,483,647
		param1.setLong_j(-9223372036854775808l); // -9,223,372,036,854,775,808 a 9,223,372,036,854,775,807
		param1.setFloat_j(12.34f); // NUMBER(4,2) no Oracle
		param1.setDouble_j(1234.5678); // NUMBER(8,4) no Oracle
		param1.setBigDecimal_j(new BigDecimal("1234567890123.45")); // NUMBER(17,2) no Oracle
		param1.setString_j("Aqui tem TRINTA CARACTERES..."); // VARCHAR2(30) no Oracle
		param1.setDate_j(new Date());
		param1.setBoolean_j(true);
		param1.setClob_j("Aqui tem um clob?"); // CLOB no Oracle - Não é possivel testar um CLOB sem gravá-lo numa tabela
		param1.setSempreEnviaNullParaOracle("Não adianta preencher aqui porque vai null");
		
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
        		          ", BOOLEAN_J = " + (param1.getBoolean_j() ? 1 : 0) +
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
        /* 
        Não recuperamos o clob pois não o EmptyClob só é inicializado quando inserido em tabela
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
		OracleTypesObjWrappers param1 = new OracleTypesObjWrappers();
        
        String expected = "BYTE_J = " + "" + 
        		          ", SHORT_J = " + "" +
        		          ", INT_J = " + "" +
        		          ", LONG_J = " + "" +
        		          ", FLOAT_J = " + "" +
        		          ", DOUBLE_J = " + "" +
        		          ", BIGDECIMAL_J = " + "" +
        		          ", TEXTO = " + "" +
        		          ", DATE_J = " + "" +
        		          ", BOOLEAN_J = " + (param1.getBoolean_j() == null ? "" : param1.getBoolean_j() ? 1 : "") +
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
	public void receberParametrosWrappers() throws Exception {
		OracleTypesObjWrappers expected = new OracleTypesObjWrappers();
		
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
        
		OracleTypesObjWrappers actual = OracleTypesConverter.toBean(OracleTypesObjWrappers.class, cs.getSTRUCT(1));
	        
	    assertEquals(expected.toString(), actual.toString());		
	}
	
	
	
	
	@Test
	public void enviarERetornarListaDeObjetos() throws Exception {		
		OracleTypesObjWrappers param1 = new OracleTypesObjWrappers();		
		param1.setByte_j((byte) 2); // -128 a 127
		param1.setShort_j((short) 2); // -32.768 a 32.767
		param1.setInt_j(2); //  -2,147,483,648 a 2,147,483,647
		param1.setLong_j(2l); // -9,223,372,036,854,775,808 a 9,223,372,036,854,775,807
		param1.setFloat_j(2f); // NUMBER(4,2) no Oracle
		param1.setDouble_j(2d); // NUMBER(8,4) no Oracle
		param1.setBigDecimal_j(new BigDecimal("2")); // NUMBER(17,2) no Oracle
		param1.setString_j("I = -"); // VARCHAR2(30) no Oracle
		param1.setDate_j(new Date(1979-1900,1,19,5,15,39));
		param1.setBoolean_j(true);
		param1.setClob_j("Clob"); // CLOB no Oracle - Não é possivel testar um CLOB sem gravá-lo numa tabela		
		
		OracleTypesObjWrappers param2 = new OracleTypesObjWrappers();		
		param2.setByte_j((byte) 2); // -128 a 127
		param2.setShort_j((short) 2); // -32.768 a 32.767
		param2.setInt_j(2); //  -2,147,483,648 a 2,147,483,647
		param2.setLong_j(2l); // -9,223,372,036,854,775,808 a 9,223,372,036,854,775,807
		param2.setFloat_j(2f); // NUMBER(4,2) no Oracle
		param2.setDouble_j(2d); // NUMBER(8,4) no Oracle
		param2.setBigDecimal_j(new BigDecimal("2")); // NUMBER(17,2) no Oracle
		param2.setString_j("I = -"); // VARCHAR2(30) no Oracle
		param2.setDate_j(new Date(2013-1900,4,16,16,35,00));
		param2.setBoolean_j(true);
		param2.setClob_j("Clob2"); // CLOB no Oracle - Não é possivel testar um CLOB sem gravá-lo numa tabela

		List<OracleTypesObjWrappers> lista = new ArrayList<OracleTypesObjWrappers>();
		lista.add(param1);
		lista.add(param2);
		
		// Na proc de teste do Oracle é subtraido o índice do loop: -1 no primeiro e -2 no segundo.
        String expected1 = "OracleTypesObjWrappers [byte_j=1, short_j=1, int_j=1, long_j=1, float_j=1.0, double_j=1.0, bigDecimal_j=1, string_j=I = -1, date_j=18/02/1979 05:15:39, boolean_j=false, clob_j=null, sempreEnviaNullParaOracle=POPULADO PELO ORACLE]";
        String expected2 = "OracleTypesObjWrappers [byte_j=0, short_j=0, int_j=0, long_j=0, float_j=0.0, double_j=0.0, bigDecimal_j=0, string_j=I = -2, date_j=14/05/2013 16:35:00, boolean_j=true, clob_j=null, sempreEnviaNullParaOracle=POPULADO PELO ORACLE]";
        
        cs = (OracleCallableStatement) oconn.prepareCall(FC_TESTAR_ENVIO_LISTA);

        cs.registerOutParameter(1, OracleTypes.ARRAY, "TESTE_ORACLETYPES_OBJS");
        cs.setARRAY(2, OracleTypesConverter.toARRAY(oconn, lista, "TESTE_ORACLETYPES_OBJS"));        
        
        STRUCTPrinter.print(OracleTypesConverter.toARRAY(oconn, lista, "TESTE_ORACLETYPES_OBJS"));
        
        cs.execute();

        List<OracleTypesObjWrappers> listaRetorno = OracleTypesConverter.toList(OracleTypesObjWrappers.class, cs.getARRAY(1));
        
        assertEquals("Expected1 = ", expected1, listaRetorno.get(0).toString());
        assertEquals("Expected2 = ", expected2, listaRetorno.get(1).toString()); 
	}
	
}
