package br.com.andrelfreis.typesconverter.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.util.Collection;

import br.com.andrelfreis.typesconverter.oracle.OracleTypesConverter;
import br.com.andrelfreis.typesconverter.oracle.printer.STRUCTPrinter;

/**
 * O objetivo dessa anota&ccedil;&atilde;o &eacute; informar para o
 * {@link OracleTypesConverter} qual tipo de objeto ou de atributo a classe
 * anotada representa no Oracle. Exemplos de utiliza&ccedil;&atilde;o:
 * 
 * <P>
 * Em classes (Define qual objeto no oracle esta classe representa):
 * 
 * <PRE>
 * <code><strong>@DBType(&quot;&lt;&lt;SCHEMA(opcional)&gt;&gt;.&lt;&lt;NOME DO OBJETO ORACLE&gt;&gt;&quot;)</strong></code>
 * <code>public class UmPojoQualquer {...}</code>
 * 
 * </PRE>
 * 
 * <P>
 * Em atributos do tipo <i>{@link Collection}</i>. (Define qual colecao o
 * atributo de uma classe anotada representa no Oracle):
 * 
 * <PRE>
 * <code>@DBType(&quot;SCHEMAX.TP_POJO_QUALQUER&quot;)</code>
 * public class UmPojoQualquer {
 * 
 *     private Long id;
 *     private String nome;
 *     <code><strong>@DBType(&quot;&lt;&lt;SCHEMA(opcional)&gt;&gt;.TAB_MOTIVO&quot;)</strong></code>
 *     private List<MotivoBean> motivos;
 *     ...
 * </PRE>
 * 
 * <P>
 * Em atributos do tipo CLOB no Oracle, devemos anotar um atributo do
 * tipo {@link String} com o valor &quot;CLOB&quot;: (N&atilde;o utilizar para
 * atributos do tipo VARCHAR*)
 * 
 * <PRE>
 * <code>@DBType(&quot;TP_OUTRO_OBJ_ORACLE&quot;)</code>
 * public class OutroPojoQualquer {
 * 
 *     private Long id;
 *     private String nome;     
 *     <code><strong>@DBType(&quot;CLOB&quot;)</strong></code>
 *     private String descricao;
 *     ...
 * </PRE>
 * 
 * <P>
 * Tamb&eacute;m existe uma op&ccedil;&atilde;o para atributos do tipo
 * {@link String} que representam 3 estados diferentes, por exemplo: <BR>
 * <ul>
 * <li>&quot;1&quot; = Somente os ATIVOS;</li>
 * <li>&quot;0&quot; = Somente os INATIVOS;</li>
 * <li>(null ou diferente de &quot;0&quot; ou &quot;1&quot;) = TODOS (ativos e
 * inativos).</li>
 * </ul>
 * Para utilizar, basta anotar um atributo do tipo {@link String} com
 * <code>@DBType(&quot;TRISTATES&quot;)</code><BR>
 * O OracleTypesAssembly s&oacute; considera os valores &quot;0&quot;,
 * &quot;1&quot; e (null ou diferente de &quot;0&quot; ou &quot;1&quot;).
 * Utilizar este atributo no java com um atributo do tipo <code>NUMBER(1)</code>
 * no Oracle, pois aceita os mesmos 3 estados: 0, 1 ou NULL.
 * 
 * <PRE>
 * <code>@DBType(&quot;TP_OBJ_PESQUISA&quot;)</code>
 * public class PojoDePesquisa {
 * 
 *     private Long id;
 *     private String titulo;     
 *     <code><strong>@DBType(&quot;TRISTATES&quot;)</strong></code>
 *     private String inAtivo;
 *     private String descricao;
 *     ...
 * </PRE>
 * 
 * 
 * @author Andre Reis <andre-luis.reis@serpro.gov.br>
 * 
 * @see OracleTypesConverter
 * @see NotInType
 * @see SendNull
 * @see STRUCTPrinter
 * 
 */
@Retention(RetentionPolicy.RUNTIME)
@Target({ ElementType.TYPE, ElementType.FIELD })
public @interface DBType {
	String value();
}
