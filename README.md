#OracleTypesConverter

Uma simples API para facilitar a passagem de parâmetros entre aplicações Java e procedures no Oracle, transformando pojos com a convenção JavaBean (getter's and setter's) para objetos oracle.sql.STRUCT ou coleções oracle.sql.ARRAY.


O objetivo desta classe &eacute; facilitar a passagem de parametros entre java e procedures no Oracle, por meio da transforma&ccedil;&atilde;o de pojos com a conven&ccedil;&atilde;o JavaBean (getter's and setter's) para objetos <code>oracle.sql.STRUCT</code> ou para cole&ccedil;&otilde;es <code>oracle.sql.ARRAY</code>, com o aux&iacute;lio das anota&ccedil;&otilde;es: <code>DBType</code>, <code>NotInType</code> e <code>SendNull</code>.

<P>
Exemplo de um simples pojo java e seu respectivo objeto no Oracle:
<P>
<strong>A classe java abaixo:</strong>

<PRE>
<code>@DBType(&quot;TP_POJO_QUALQUER&quot;)</code>
public class UmPojoQualquer {

    private Long nrSq;
    private String nome;
    private Date dataRegistro;
    
    // metodos getters and setters suprimidos
}
</PRE>

<strong>Corresponde ao respectivo objeto no Oracle:</strong>
 
<PRE>
CREATE OR REPLACE TYPE TP_POJO_QUALQUER AS OBJECT (
    id        NUMBER,
    nome      VARCHAR2(150),
    dataReg   DATE
);
</PRE>

<P>
Para efeitos de convers&atilde;o &eacute; considerado apenas <u>a ordem</u> dos atributos, que devem ser de tipos compat&iacute;veis. O nome dos atributos n&atilde;o t&ecirc;m nenhum efeito na convers&atilde;o. Exemplo de atributos compat&iacute;veis:
<table cellpadding="2px;" border="1">
  <tr>
    <td>Tipos JAVA</td>
    <td>converte para</td>
    <td>Tipos PL/SQL</td>
  </tr>
  <tr>
    <td>String</td>
    <td align="center">&lt;-&gt;</td>
    <td>VARCHAR / VACHAR2 <strong>*</strong></td>
  </tr>
  <tr>
    <td>Long, Integer, Short, Byte, Float, Double e BigDecimal</td>
    <td align="center">&lt;-&gt;</td>
    <td>NUMBER <strong>**</strong></td>
  </tr>
  <tr>
    <td>Date, Timestamp</td>
    <td align="center">&lt;-&gt;</td>
    <td>DATE</td>
  </tr>
  <tr>
    <td>Boolean</td>
    <td align="center">&lt;-&gt;</td>
    <td>NUMBER(1) <strong>***</strong></td>
  </tr>
  <tr>
    <td>String anotada com @DBType</td>
    <td align="center">&lt;-&gt;</td>
    <td>CLOB</td>
  </tr>
</table>

<ul>
  <li><strong>*</strong> Como no Oracle os tipos <code>VARCHAR</code> e <code>VARCHAR2</code> possuem limite de caracteres e a <code>String</code> no java &eacute; ilimitada, recomenda-se fazer a verifica&ccedil;&atilde;o antes da convers&atilde;o, sob risco de receber uma exce&ccedil;&atilde;o <code>ORA-06502: PL/SQL: numeric or value error string</code>.
  </li>
  <li><strong>**</strong> O tipo <code>NUMBER</code> tamb&eacute;m possui limite de caracteres, ent&atilde;o fazer a verifica&ccedil;&atilde;o antes da convers&atilde;o.
  </li>
  <li><strong>***</strong> N&atilde;o existe suporte para o tipo <code>Boolean</code> do PL/SQL, portanto o Boolean no java &eacute;
convertido para: <code>true = 1, false = 0</code> e vice-versa. Quando vindo do banco, o valor 0 &eacute; convertido para <code>false</code>, e qualquer outro valor diferente de zero &eacute; convertivo para <code>true</code>.
  </li>
</ul>

<P>
Por padr&atilde;o, todos atributos do pojo java ser&atilde;o considerados para popular o objeto correspondente no oracle, exceto os anotados com <code>@NotInType</code>. J&aacute; os atributos anotados com <code>@SendNull</code>, ser&atilde;o considerados, por&eacute;m o seu valor ser&aacute; sempre enviado como <code>null</code>. Consulte as anota&ccedil;&otilde;es para mais informa&ccedil;&otilde;es.
 
<P>
<strong>Exemplo de passagem de parametro POJO -> STRUCT para uma procedure/function no oracle:</strong>

<PRE>
HibernateUtil.getInstance().getSession().doWork(new Work() {

   <code>@Override</code>
   public void execute(Connection connection) {
    
   OracleCallableStatement cs = null;
   try {
       OracleConnection <strong>oconn</strong> = OracleConnectionUnwrapper.unwrapOracleConnection(connection);
       cs = (OracleCallableStatement) oconn.prepareCall(&quot;{call PK_PACKAGE.PR_PROC_TESTE(?)}&quot;);
       cs.setSTRUCT(1, OracleTypesConverter.toSTRUCT(<strong>oconn</strong>, <strong>pojoJava</strong>));
       cs.execute();

// restante do c&oacute;digo suprimido
</PRE>

Onde:
<ul>
  <li><strong><code>OracleConnectionUnwrapper</code></strong> &eacute; a classe respons&aacute;vel por desencapsular a OracleConnection de dentro da classe Wrapper do Hibernate e Jboss, por meio do seu m&eacute;todo est&aacute;tico: <code>unwrapOracleConnection(Connection)</code>.
  </li>
</ul>

<P>
O m&eacute;todo <code>toSTRUCT(OracleConnection, Object)</code>, &eacute; respons&aacute;vel pela convers&atilde;o entre o pojo java e o type no Oracle:<BR>
<ul>
  <li>O par&acirc;metro <strong><code>pojoJava</code></strong> &eacute; uma inst&acirc;ncia do pojo java, anotado com <code>@DBType</code>.</li>
</ul>

<P>
<strong>Exemplo de passagem de uma cole&ccedil;&atilde;o de pojos -> ARRAY para uma procedure/function no oracle:</strong>

<P>
Para parametros do tipo Collection, utilizar o m&eacute;todo <code>toARRAY(OracleConnection, Collection, String)</code>, passando a inst&acirc;ncia da <code>OracleConnection</code>, uma collection com pojos java e a String correspondente &agrave; cole&ccedil;&atilde;o de objetos no oracle, como no exemplo:

<PRE>
cs.setARRAY(1, OracleTypesConverter.toARRAY(<strong>oconn</strong>, <b>listaPojoJava</b>, &quot;TAB_LISTA_IDS&quot;));
</PRE>

<BR>
------------------------------------------------------------------------<BR>
<strong>A id&eacute;ia para o desenvolvimento desta classe foi baseada na documenta&ccedil;&atilde;o oficial Oracle.<BR>
Compat&iacute;vel com a versão 10g release 2 do driver jdbc ou superior: (ojdbc14.jar)</strong><BR>
<P>
<strong>Oracle Database JDBC Developer's Guide and Reference:</strong><BR>
<a href="http://download.oracle.com/docs/cd/B19306_01/java.102/b14355/oraoot.htm" target="_blank">1 - Working with Oracle Object Types</a> <BR>
<a href="http://download.oracle.com/docs/cd/B19306_01/appdev.102/b14261/collections.htm" target="_blank">5 - Using PL/SQL Collections and Records</a>

<P>
<strong>Oracle Database JPublisher User's Guide</strong><BR>
<a href="http://download.oracle.com/docs/cd/B19306_01/java.102/b14188/intro.htm" target="_blank">1 - Introduction to JPublisher</a> <BR>
<a href="http://download.oracle.com/docs/cd/B19306_01/java.102/b14188/intro.htm#sthref50" target="_blank" >1 - Introduction to JPublisher # JDBC Mapping</a>

<P>
<strong>Download Oracle Drivers</strong><BR>
<a href="http://www.oracle.com/technetwork/database/features/jdbc/index-091264.html" target="_blank">JDBC, SQLJ, Oracle JPublisher and Universal Connection Pool</a> <BR>
<a href="http://www.oracle.com/technetwork/database/enterprise-edition/jdbc-112010-090769.html" target="_blank" >Oracle Database 11g Release 2 JDBC Drivers</a> <BR>
<a href="http://www.oracle.com/technetwork/database/enterprise-edition/jdbc-10201-088211.html" target="_blank" >Oracle Database 10g Release 2 JDBC Drivers</a> <BR>
 
@author Andr&eacute; Reis : arghos at gmail.com
 
@see DBType<BR>
@see NotInType<BR>
@see SendNull<BR>
@see STRUCTPrinter<BR>
