package br.com.andrelfreis.typesconverter.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import br.com.andrelfreis.typesconverter.oracle.OracleTypesConverter;
import br.com.andrelfreis.typesconverter.oracle.printer.STRUCTPrinter;

/**
 * Objetivo: informar ao {@link OracleTypesConverter} que o atributo com esta
 * anota&ccedil;&atilde;o nunca ser&aacute; <i>enviado*</i> ao seu respectivo objeto no Oracle, ou
 * seja, sempre ser&aacute; atribu&iacute;do o valor null.<BR>
 * <i>*Somente no envio da informa&ccedil;&atilde;o. No retorno &eacute; preenchido normalmente
 * sempre que for setado pela procedure.</i>
 * <P>
 * Exemplo de atributos que n&atilde;o precisam ter seus valores enviados:
 * <ul>
 * <li>O total de registros selecionados numa pesquisa com pagina&ccedil;&atilde;o.</li>
 * <li>Atributos que sempre ser&atilde;o resultado de processamento apenas dentro na
 * procedure.</li>
 * </ul>
 * 
 * @author Andr&eacute; Luis <andre-luis.reis@serpro.gov.br>
 * 
 * @see OracleTypesConverter
 * @see DBType
 * @see NotInType
 * @see STRUCTPrinter
 * 
 */
@Retention(RetentionPolicy.RUNTIME)
@Target({ ElementType.FIELD })
public @interface SendNull {

}
