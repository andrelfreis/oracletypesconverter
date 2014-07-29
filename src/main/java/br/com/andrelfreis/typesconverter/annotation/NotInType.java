package br.com.andrelfreis.typesconverter.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import br.com.andrelfreis.typesconverter.oracle.OracleTypesConverter;
import br.com.andrelfreis.typesconverter.oracle.printer.STRUCTPrinter;

/**
 * Objetivo: informar ao {@link OracleTypesConverter} que o atributo com esta
 * anota&ccedil;&atilde;o n&atilde;o existe no seu respectivo objeto no Oracle, ou seja, ser&aacute;
 * ignorado na convers&atilde;o.<BR>
 * <P>
 * Serve para ignorar atributos que s&atilde;o apenas utilizados na camada Java, como
 * por exemplo, atributos de controle de exibi&ccedil;&atilde;o de tela, de fluxo, etc.
 * 
 * @author Andr&eacute; Luis <andre-luis.reis@serpro.gov.br>
 * 
 * @see OracleTypesConverter
 * @see DBType
 * @see SendNull
 * @see STRUCTPrinter
 * 
 */
@Retention(RetentionPolicy.RUNTIME)
@Target({ ElementType.FIELD })
public @interface NotInType {

}
