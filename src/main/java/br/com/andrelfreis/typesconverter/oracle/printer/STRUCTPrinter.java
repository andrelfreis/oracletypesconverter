package br.com.andrelfreis.typesconverter.oracle.printer;

import java.io.IOException;
import java.io.Reader;
import java.sql.SQLException;

import br.com.andrelfreis.typesconverter.oracle.OracleTypesConverter;
import oracle.sql.ARRAY;
import oracle.sql.CLOB;
import oracle.sql.Datum;
import oracle.sql.STRUCT;

/**
 * Classe para aux&iacute;lio no processo de debug. Serve para visualizar no console um
 * STRUCT ou ARRAY formatados. Basta chamar o m&eacute;todo est&aacute;tico
 * <code>{@link STRUCTPrinter#print(Datum)}</code> passando como
 * par&acirc;metro um STRUCT ou ARRAY criados pelo
 * {@link OracleTypesConverter#toSTRUCT(oracle.jdbc.OracleConnection, Object)} ou
 * {@link OracleTypesConverter#toARRAY(oracle.jdbc.OracleConnection, java.util.Collection, String)}
 * 
 * @author Andre Reis <andre-luis.reis@serpro.gov.br>
 * 
 */
public class STRUCTPrinter {

	/**
	 * Imprime um {@link oracle.sql.Datum} formatado.<BR>
	 * A classe {@link Datum} &eacute; pai da {@link oracle.sql.STRUCT} e da
	 * {@link oracle.sql.ARRAY}
	 * 
	 * @param datum
	 *            Um objeto do tipo {@link STRUCT} ou {@link ARRAY} para
	 *            impress&atilde;o no console.
	 * @throws SQLException
	 *             Se houver um erro na leitura do objeto passado como
	 *             parametro.
	 * @throws IOException
	 *             Se houver um erro na leitura de um atributo do tipo CLOB
	 */
	public static void print(Datum datum) throws SQLException, IOException {
		printD(datum, null);
	}

	private static void printD(Datum datum, String tab) throws SQLException,
			IOException {

		if (null == tab || "".equals(tab)) {
			tab = "\t";
		}

		try {
			printS((STRUCT) datum, tab);
		} catch (ClassCastException e) {
			try {
				printA((ARRAY) datum, tab);
			} catch (ClassCastException cce) {
				System.out.println("\n\t\t *** ERRO NO CAST DO OBJETO DATUM: "
						+ datum);
				cce.printStackTrace();
			}
		}
	}

	private static void printA(ARRAY array, String tab) throws SQLException,
			IOException {
		if (null != array) {
			Datum lista[] = array.getOracleArray();
			System.out.println(/* "ARRAY: "+ */array.getDescriptor().getName()
					+ "(");
			int ultimo = lista.length - 1;
			for (int j = 0; j < lista.length; j++) {
				System.out.print(tab/* +j+" = " */);
				if (lista[j] instanceof STRUCT) {
					printS((STRUCT) lista[j], tab + "\t");
					System.out.println((j == ultimo ? tab + ")" : tab + "),"));
				} else if (lista[j] instanceof ARRAY) {
					printS((STRUCT) lista[j], tab + "\t");
					System.out.println((j == ultimo ? tab + ")" : tab + "),"));
				} else {
					System.out.println(lista[j].stringValue());
				}
			}
			System.out.println(");");
		}
	}

	private static void printS(STRUCT stru, String tab) throws SQLException,
			IOException {

		if (null != stru) {
			System.out.println(/* "STRUCT: "+ */stru.getDescriptor().getName()
					+ "(");

			Object attributes[] = stru.getAttributes();
			int ultimo = attributes.length - 1;

			for (int i = 0; i < attributes.length; i++) {
				if (attributes[i] instanceof ARRAY) {
					System.out.print(tab/* +i+" = " */);
					printA((ARRAY) attributes[i], tab + "\t");
					System.out.println((i == ultimo ? tab + ")" : tab + "),"));
				} else if (attributes[i] instanceof STRUCT) {
					System.out.print(tab/* +i+" = " */);
					printS((STRUCT) attributes[i], tab + "\t");
					System.out.println((i == ultimo ? tab + ")" : tab + "),"));
				} else if (null == attributes[i]) {
					System.out.println(tab/* +i+" = null" */+ "null"
							+ (i == ultimo ? "" : ","));
				} else {
					String valor;
					if (attributes[i] instanceof String) {
						valor = "\'" + attributes[i].toString() + "\'";
					} else if (attributes[i] instanceof CLOB) {
						CLOB cl = ((CLOB) attributes[i]);
						if (!cl.isEmptyLob()) {
							Reader r = cl.characterStreamValue();
							StringBuffer sb = new StringBuffer();
							int nchars = 0;
							char[] buffer = new char[10];
							while ((nchars = r.read(buffer)) != -1) {
								sb.append(buffer, 0, nchars);
							}
							r.close();
							valor = sb.toString();
						} else {
							valor = "\'CLOB is Empty\'";
						}
					} else {
						valor = attributes[i].toString();
					}
					System.out.println(tab/* +i+" = " */+ valor
							+ (i == ultimo ? "" : ","));
				}
			}
			System.out.println(");");
		}
	}

}
