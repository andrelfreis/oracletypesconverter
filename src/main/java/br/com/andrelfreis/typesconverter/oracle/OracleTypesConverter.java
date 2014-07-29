package br.com.andrelfreis.typesconverter.oracle;

import java.io.IOException;
import java.io.Reader;
import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.math.BigDecimal;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import oracle.jdbc.OracleConnection;
import oracle.sql.ARRAY;
import oracle.sql.ArrayDescriptor;
import oracle.sql.CHAR;
import oracle.sql.CLOB;
import oracle.sql.DATE;
import oracle.sql.Datum;
import oracle.sql.NUMBER;
import oracle.sql.STRUCT;
import oracle.sql.StructDescriptor;
import br.com.andrelfreis.typesconverter.annotation.DBType;
import br.com.andrelfreis.typesconverter.annotation.NotInType;
import br.com.andrelfreis.typesconverter.annotation.SendNull;
import br.com.andrelfreis.typesconverter.oracle.printer.STRUCTPrinter;

/**
 * O objetivo desta classe &eacute; facilitar a passagem de parametros entre
 * java e procedures no Oracle, por meio da transforma&ccedil;&atilde;o de pojos
 * com a conven&ccedil;&atilde;o JavaBean (getter's and setter's) para objetos
 * {@link oracle.sql.STRUCT} ou para cole&ccedil;&otilde;es
 * {@link oracle.sql.ARRAY}, com o aux&iacute;lio das anota&ccedil;&otilde;es:
 * {@link DBType}, {@link NotInType} e {@link SendNull}.
 * 
 * <P>
 * Exemplo de um simples pojo java e seu respectivo objeto no Oracle:
 * <P>
 * <strong>A classe java abaixo:</strong>
 * 
 * <PRE>
 * <code>@DBType(&quot;TP_POJO_QUALQUER&quot;)</code>
 * public class UmPojoQualquer {
 * 
 *     private Long nrSq;
 *     private String nome;
 *     private Date dataRegistro;
 *     
 *     // metodos getters and setters suprimidos
 * }
 * </PRE>
 * 
 * <strong>Corresponde ao respectivo objeto no Oracle:</strong>
 * 
 * <PRE>
 * CREATE OR REPLACE TYPE TP_POJO_QUALQUER AS OBJECT (
 *     id        NUMBER,
 *     nome      VARCHAR2(150),
 *     dataReg   DATE
 * );
 * </PRE>
 * 
 * <P>
 * Para efeitos de convers&atilde;o &eacute; considerado apenas <u>a ordem</u>
 * dos atributos, que devem ser de tipos compat&iacute;veis. O nome dos
 * atributos n&atilde;o t&ecirc;m nenhum efeito na convers&atilde;o. Exemplo de
 * atributos compat&iacute;veis:
 * <table cellpadding="2px;" border="1">
 * <tr>
 * <td>Tipos JAVA</td>
 * <td>converte para</td>
 * <td>Tipos PL/SQL</td>
 * </tr>
 * <tr>
 * <td>{@link String}</td>
 * <td align="center">&lt;-&gt;</td>
 * <td><code>VARCHAR / VACHAR2</code> <strong>*</strong></td>
 * </tr>
 * <tr>
 * <td>{@link Long}, {@link Integer}, {@link Short}, {@link Byte}, {@link Float}, {@link Double} e {@link BigDecimal}</td>
 * <td align="center">&lt;-&gt;</td>
 * <td>{@link NUMBER} <strong>**</strong></td>
 * </tr>
 * <tr>
 * <td>{@link Date}, {@link Timestamp}</td>
 * <td align="center">&lt;-&gt;</td>
 * <td>{@link DATE}</td>
 * </tr>
 * <tr>
 * <td>{@link Boolean}</td>
 * <td align="center">&lt;-&gt;</td>
 * <td>{@link NUMBER}(1) <strong>***</strong></td>
 * </tr>
 * <tr>
 * <td>{@link String} anotada com {@link DBType}</td>
 * <td align="center">&lt;-&gt;</td>
 * <td>{@link CLOB}</td>
 * </tr>
 * </table>
 * 
 * <ul>
 * <li><strong>*</strong> Como no Oracle os tipos <code>VARCHAR</code> e
 * <code>VARCHAR2</code> possuem limite de caracteres e a <code>String</code> no
 * java &eacute; ilimitada, recomenda-se fazer a verifica&ccedil;&atilde;o antes
 * da convers&atilde;o, sob risco de receber uma exce&ccedil;&atilde;o
 * <code>ORA-06502:
 * PL/SQL: numeric or value error string</code>.</li>
 * <li><strong>**</strong> O tipo <code>NUMBER</code> tamb&eacute;m possui
 * limite de caracteres, ent&atilde;o fazer a verifica&ccedil;&atilde;o antes da
 * convers&atilde;o.</li>
 * <li><strong>***</strong> N&atilde;o existe suporte para o tipo
 * <code>Boolean</code> do PL/SQL, portanto o Boolean no java &eacute;
 * convertido para: <code>true = 1, false = 0</code> e vice-versa. Quando vindo
 * do banco, o valor 0 &eacute; convertido para <code>false</code>, e qualquer
 * outro valor diferente de zero &eacute; convertivo para <code>true</code>.</li>
 * </ul>
 * 
 * <P>
 * Por padr&atilde;o, todos atributos do pojo java ser&atilde;o considerados
 * para popular o objeto correspondente no oracle, exceto os anotados com
 * {@link NotInType}. J&aacute; os atributos anotados com {@link SendNull},
 * ser&atilde;o considerados, por&eacute;m o seu valor ser&aacute; sempre
 * enviado como <code>null</code>. Consulte as anota&ccedil;&otilde;es para mais
 * informa&ccedil;&otilde;es.
 * 
 * <P>
 * <strong>Exemplo de passagem de parametro POJO -> STRUCT para uma
 * procedure/function no oracle:</strong>
 * 
 * <PRE>
 * HibernateUtil.getInstance().getSession().doWork(new Work() {
 * 
 *    <code>@Override</code>
 *    public void execute(Connection connection) {
 *     
 *    OracleCallableStatement cs = null;
 *    try {
 *        OracleConnection <strong>oconn</strong> = OracleConnectionUnwrapper.unwrapOracleConnection(connection);
 *        cs = (OracleCallableStatement) oconn.prepareCall(&quot;{call PK_PACKAGE.PR_PROC_TESTE(?)}&quot;);
 *        cs.setSTRUCT(1, OracleTypesConverter.toSTRUCT(<strong>oconn</strong>, <strong>pojoJava</strong>));
 *        cs.execute();
 * 
 * // restante do c&oacute;digo suprimido
 * </PRE>
 * 
 * Onde:
 * <ul>
 * <li><strong><code>OracleConnectionUnwrapper</code></strong> &eacute;
 * a classe respons&aacute;vel por desencapsular a OracleConnection de dentro da
 * classe Wrapper do Hibernate e Jboss, por meio do seu m&eacute;todo est&aacute;tico:
 * <code>unwrapOracleConnection(Connection)</code>.</li>
 * </ul>
 * 
 * <P>
 * O m&eacute;todo {@link #toSTRUCT(OracleConnection, Object)}, &eacute;
 * respons&aacute;vel pela convers&atilde;o entre o pojo java e o type no
 * Oracle:<BR>
 * <ul>
 * <li>O par&acirc;metro <strong><code>pojoJava</code></strong> &eacute; uma
 * inst&acirc;ncia do pojo java, anotado com {@link DBType}.</li>
 * </ul>
 * 
 * <P>
 * <strong>Exemplo de passagem de uma cole&ccedil;&atilde;o de pojos -> ARRAY
 * para uma procedure/function no oracle:</strong>
 * 
 * <P>
 * Para parametros do tipo Collection, utilizar o m&eacute;todo
 * <code>{@link #toARRAY(OracleConnection, Collection, String)}</code>, passando
 * a inst&acirc;ncia da <code>{@link OracleConnection}</code>, uma collection
 * com pojos java e a String correspondente &agrave; cole&ccedil;&atilde;o de
 * objetos no oracle, como no exemplo:
 * 
 * <PRE>
 * cs.setARRAY(1, OracleTypesConverter.toARRAY(<strong>oconn</strong>, <b>listaPojoJava</b>, &quot;TAB_LISTA_IDS&quot;));
 * </PRE>
 * 
 * <BR>
 * ------------------------------------------------------------------------ <BR>
 * <strong>A id&eacute;ia para o desenvolvimento desta classe foi baseada na
 * documenta&ccedil;&atilde;o oficial Oracle.<BR>
 * Compat&iacute;vel com a versão 10g release 2 do driver jdbc ou superior:
 * (ojdbc14.jar)</strong><BR>
 * <P>
 * <strong>Oracle Database JDBC Developer's Guide and Reference:</strong><BR>
 * <a href=
 * "http://download.oracle.com/docs/cd/B19306_01/java.102/b14355/oraoot.htm"
 * target="_blank">1 - Working with Oracle Object Types</a> <BR>
 * <a href=
 * "http://download.oracle.com/docs/cd/B19306_01/appdev.102/b14261/collections.htm"
 * target="_blank" >5 - Using PL/SQL Collections and Records</a>
 * 
 * <P>
 * <strong>Oracle Database JPublisher User's Guide</strong><BR>
 * <a href=
 * "http://download.oracle.com/docs/cd/B19306_01/java.102/b14188/intro.htm"
 * target="_blank">1 - Introduction to JPublisher</a> <BR>
 * <a href=
 * "http://download.oracle.com/docs/cd/B19306_01/java.102/b14188/intro.htm#sthref50"
 * target="_blank" >1 - Introduction to JPublisher # JDBC Mapping</a>
 * 
 * <P>
 * <strong>Download Oracle Drivers</strong><BR>
 * <a href=
 * "http://www.oracle.com/technetwork/database/features/jdbc/index-091264.html"
 * target="_blank">JDBC, SQLJ, Oracle JPublisher and Universal Connection Pool
 * </a> <BR>
 * <a href=
 * "http://www.oracle.com/technetwork/database/enterprise-edition/jdbc-112010-090769.html"
 * target="_blank" >Oracle Database 11g Release 2 JDBC Drivers</a> <BR>
 * <a href=
 * "http://www.oracle.com/technetwork/database/enterprise-edition/jdbc-10201-088211.html"
 * target="_blank" >Oracle Database 10g Release 2 JDBC Drivers</a> <BR>
 * 
 * @author Andr&eacute; Reis : arghos@gmail.com
 * 
 * @see DBType
 * @see NotInType
 * @see SendNull
 * @see STRUCTPrinter
 * 
 */
public class OracleTypesConverter {

	private OracleTypesConverter() {
	}

	/*
	 * REFATORAR URGENTE ESTA CLASSE -> PRINCIPALMENTE TODOS OS SEUS TRY
	 * CACTH's] Criar tratamento para outros tipos java: double, float, char
	 * conforme necessario
	 */

	/**
	 * Converte um pojo java anotado com {@link DBType} para um
	 * {@link oracle.sql.STRUCT}, que ser&aacute; enviado como par&acirc;metro
	 * para uma procedure ou function oracle.
	 * 
	 * @param oconn
	 *            Uma inst&acirc;ncia de {@link OracleConnection} utilizada para
	 *            cria&ccedil;&atilde;o do STRUCT
	 * @param bean
	 *            Um pojo java anotado com {@link DBType}
	 * 
	 */
	public static STRUCT toSTRUCT(OracleConnection oconn, Object bean) {

		Class<?> klass = bean.getClass();
		StructDescriptor sd;
		try {
			sd = StructDescriptor.createDescriptor(getDBTypeAnnotation(klass),
					oconn);
		} catch (SQLException e) {
			throw new RuntimeException(e.getMessage(), e);
		}

		List<Field> fieldlist = getFieldsInType(klass);
		List<Object> listaAtributos = new ArrayList<Object>();

		for (Field field : fieldlist) {
			if (null != field.getAnnotation(SendNull.class)) {
				listaAtributos.add(null);
			} else {

				String campo = prepareFieldName(field);

				Method meth = null;
				try {
					meth = klass.getMethod("get" + campo, (Class[]) null);
				} catch (SecurityException e) {
					throw new RuntimeException(e.getMessage(), e);
				} catch (NoSuchMethodException em) {
					try {
						meth = klass.getMethod("is" + campo, (Class[]) null);
					} catch (SecurityException e) {
						throw new RuntimeException(e.getMessage(), e);
					} catch (NoSuchMethodException e) {
						throw new RuntimeException(e.getMessage(), e);
					}
				}

				Object result = null;
				try {
					result = meth.invoke(bean, (Object[]) null);
				} catch (IllegalAccessException e) {
					throw new RuntimeException(e.getMessage(), e);
				} catch (InvocationTargetException e) {
					throw new RuntimeException(e.getMessage(), e);
				}

				if (null == result) {
					listaAtributos.add(null);
				} 
				else if (result instanceof Byte) {
					listaAtributos.add(Integer.valueOf(Byte.toString((Byte) result)));
				} 
				else if (result instanceof Short) {
					listaAtributos.add(Integer.valueOf(Short.toString((Short) result)));
				} 
				else if (result instanceof Integer) {
					listaAtributos.add(result);					
				} 
				else if (result instanceof Long) {
					listaAtributos.add(result);
				}
				
				else if (result instanceof Float) {
					listaAtributos.add(result);					
				}
				else if (result instanceof Double) {
					listaAtributos.add(result);					
				}
				else if (result instanceof BigDecimal) {
					listaAtributos.add(result);					
				}
				
				else if (result instanceof Boolean) {
					listaAtributos.add((Boolean) result ? 1 : 0);
				}				
				else if (result instanceof String) {
					String str = ((String) result).trim();
					if (!"".equals(str)) {
						if (hasCLOBDbTypeAnnotation(field)) {
							try {
								listaAtributos.add(CLOB.getEmptyCLOB());
							} catch (SQLException e) {
								throw new RuntimeException(e.getMessage(), e);
							}
						} else if (hasTriStatesDbTypeAnnotation(field)) {
							if (!("0".equals(str)) && !("1".equals(str))) {
								listaAtributos.add(null);
							} else {
								listaAtributos.add(str);
							}
						} else {
							listaAtributos.add(str);
						}
					} else {
						listaAtributos.add(null);
					}
				}
				
				else if (result instanceof Date) {
					listaAtributos.add(new Timestamp(((Date) result).getTime()));
				}
				
				else if (result instanceof Collection<?>) {
					
					Annotation dbListTypeAnnotation = field.getAnnotation(DBType.class);
					
					if (null == dbListTypeAnnotation) {
						String msg = "O atributo: " + field.getName()
								+ " não possui a anotação: " + DBType.class
								+ " com o tipo equivalente do Banco.";
						throw new RuntimeException(msg);
					}
					
					ARRAY listaArray = null;

					String oracleTypeCOLLECTION = ((DBType) dbListTypeAnnotation)
							.value();
					ArrayDescriptor adListaArray;
					try {
						adListaArray = ArrayDescriptor.createDescriptor(oracleTypeCOLLECTION, oconn);
					} catch (SQLException e) {
						throw new RuntimeException(e.getMessage(), e);
					}

					List<Object> listaObject = new ArrayList<Object>();
					for (Object o : (Collection<?>) result) {
						if (null != o) {
							if (o instanceof Number || o instanceof String) {
								listaObject.add(o);
							} else {
								listaObject.add(OracleTypesConverter.toSTRUCT(oconn, o));
							}
						}
					}
					try {
						listaArray = new ARRAY(adListaArray, oconn,
								listaObject.toArray());
					} catch (SQLException e) {
						throw new RuntimeException(e.getMessage(), e);
					}
					listaAtributos.add(listaArray);
				} else {
					listaAtributos.add(OracleTypesConverter.toSTRUCT(oconn,
							result));
				}

			}
		}

		try {
			return new STRUCT(sd, oconn, listaAtributos.toArray());
		} catch (SQLException e) {
			throw new RuntimeException(e.getMessage(), e);
		}

	}

	/**
	 * Converte uma {@link java.util.Collection} de pojos anotados com
	 * {@link DBType} para um {@link oracle.sql.ARRAY}, que ser&aacute; enviado
	 * como parametro para uma procedure ou function no Oracle.
	 * 
	 * @param oconn
	 *            Uma inst&acirc;ncia de {@link OracleConnection} utilizada para
	 *            cria&ccedil;&atilde;o do ARRAY
	 * @param listaBean
	 *            A Collection de pojos anotados com {@link DBType}
	 * @param stringDbType
	 *            O tipo do objeto no Oracle
	 * @return Um ARRAY com os pojos da lista convertidos
	 */
	public static <T> ARRAY toARRAY(OracleConnection oconn,
			Collection<T> listaBean, String stringDbType) {

		ArrayDescriptor ad;
		try {
			ad = ArrayDescriptor.createDescriptor(stringDbType, oconn);
		} catch (SQLException e) {
			throw new RuntimeException(e.getMessage(), e);
		}
		List<Object> listaLocal = new ArrayList<Object>();
		if (null != listaBean) {
			for (T bean : listaBean) {
				listaLocal.add(OracleTypesConverter.toSTRUCT(oconn, bean));
			}
		}

		Object[] objs = listaLocal.toArray();
		try {
			return new ARRAY(ad, oconn, objs);
		} catch (SQLException e) {
			throw new RuntimeException(e.getMessage(), e);
		}
	}

	/**
	 * Converte um objeto {@link STRUCT} para um pojo java anotado com
	 * {@link DBType}
	 * 
	 * @param beanClass
	 *            A classe do pojo anotada com {@link DBType} correspondente ao
	 *            STRUCT passado como par&acirc;metro
	 * @param struct
	 *            O {@link STRUCT} retornado da procedure ou function no Oracle
	 * @return O pojo java convertido e preenchido
	 */
	// TODO: Tratar a falta de metodos javabean: set's -> NoSuchMethodException
	public static <T> T toBean(Class<T> beanClass, STRUCT struct) {

		T bean = null;

		if (null != struct) {
			// Cria uma instancia da Classe
			try {
				bean = beanClass.newInstance();
			} catch (InstantiationException e) {
				String msg = "Erro ao instanciar o objeto da classe "
						+ beanClass.getName()
						+ ".java: pode ser a falta de um construtor vazio.";
				// TODO: Incluir ou Detectar LOG4J e usar se possivel, tirar
				// syso.
				throw new RuntimeException(e.getMessage()
						+ "\n\tOracleTypesConverter: " + msg, e);
			} catch (IllegalAccessException e) {
				throw new RuntimeException(e.getMessage(), e);
			}

			Object[] attributes;
			try {
				attributes = struct.getOracleAttributes();
			} catch (SQLException e) {
				throw new RuntimeException(e.getMessage(), e);
			}

			List<Field> beanFieldlist = getFieldsInType(beanClass);

			int tamJava = beanFieldlist.size();
			int tamOra = attributes.length;

			if (tamJava != tamOra) {
				String nomeStruct;
				try {
					nomeStruct = struct.getSQLTypeName();
				} catch (SQLException e) {
					throw new RuntimeException(
							"Não foi possível obter o SQLTypeName do objeto Oracle, verifique a permissão GRANT EXECUTE. Mensagem da exceção: "
									+ e.getMessage(), e);
				}

				String msg = "A quantidade de atributos do objeto: "
						+ beanClass.getSimpleName() + " : " + tamJava
						+ " é diferente do type: " + nomeStruct + " : "
						+ tamOra + ".";
				// TODO: Detectar LOG4J e usar se possivel, tirar syso.
				System.out.println(msg);
			}

			for (int i = 0; i < beanFieldlist.size(); i++) {

				Field beanField = beanFieldlist.get(i);

				if (i < attributes.length) {

					if (null != attributes[i]) {

						String campo = prepareFieldName(beanField);

						Class<?> typeFieldClass = beanField.getType();
						Class<?>[] paramTypes = { typeFieldClass };
						Method meth;
						try {
							meth = beanClass.getMethod("set" + campo, paramTypes);
						} catch (SecurityException e1) {
							throw new RuntimeException(e1.getMessage(), e1);
						} catch (NoSuchMethodException e1) {
							throw new RuntimeException(e1.getMessage(), e1);
						}
						
						if (Byte.class == typeFieldClass || Byte.TYPE == typeFieldClass) {
							try {
								meth.invoke(bean, ((NUMBER) attributes[i]).byteValue());
							} catch (IllegalAccessException e) {
								throw new RuntimeException(e.getMessage(), e);
							} catch (InvocationTargetException e) {
								throw new RuntimeException(e.getMessage(), e);
							} catch (SQLException e) {
								throw new RuntimeException(e.getMessage(), e);
							}
							
						} else if (Short.class == typeFieldClass || Short.TYPE == typeFieldClass) {
							try {
								meth.invoke(bean, ((NUMBER) attributes[i]).shortValue());
							} catch (IllegalAccessException e) {
								throw new RuntimeException(e.getMessage(), e);
							} catch (InvocationTargetException e) {
								throw new RuntimeException(e.getMessage(), e);
							} catch (SQLException e) {
								throw new RuntimeException(e.getMessage(), e);
							}
						} 
						
						else if (Integer.class == typeFieldClass || Integer.TYPE == typeFieldClass) {
							try {
								meth.invoke(bean, ((NUMBER) attributes[i]).intValue());
							} catch (IllegalAccessException e) {
								throw new RuntimeException(e.getMessage(), e);
							} catch (InvocationTargetException e) {
								throw new RuntimeException(e.getMessage(), e);
							} catch (SQLException e) {
								throw new RuntimeException(e.getMessage(), e);
							}							
						}
						
						else if (Long.class == typeFieldClass || Long.TYPE == typeFieldClass) {
							try {
								meth.invoke(bean, ((NUMBER) attributes[i]).longValue());
							} catch (IllegalAccessException e) {
								throw new RuntimeException(e.getMessage(), e);
							} catch (InvocationTargetException e) {
								throw new RuntimeException(e.getMessage(), e);
							} catch (SQLException e) {
								throw new RuntimeException(e.getMessage(), e);
							}
						}
						
						
						else if (Float.class == typeFieldClass || Float.TYPE == typeFieldClass) {
							try {
								meth.invoke(bean, ((NUMBER) attributes[i]).floatValue());
							} 
							catch (IllegalAccessException e) {
								throw new RuntimeException(e.getMessage(), e);
							} 
							catch (InvocationTargetException e) {
								throw new RuntimeException(e.getMessage(), e);
							}
						}
						
						else if (Double.class == typeFieldClass || Double.TYPE == typeFieldClass) {
							try {
								meth.invoke(bean, ((NUMBER) attributes[i]).doubleValue());
							} 
							catch (IllegalAccessException e) {
								throw new RuntimeException(e.getMessage(), e);
							} 
							catch (InvocationTargetException e) {
								throw new RuntimeException(e.getMessage(), e);
							}
						}
						
						
						else if (BigDecimal.class == typeFieldClass) {
							try {
								meth.invoke(bean, ((NUMBER) attributes[i]).bigDecimalValue());								 
							} 
							catch (IllegalAccessException e) {
								throw new RuntimeException(e.getMessage(), e);
							} 
							catch (InvocationTargetException e) {
								throw new RuntimeException(e.getMessage(), e);
							} 
							catch (SQLException e) {								
								throw new RuntimeException(e.getMessage(), e);
							}
						}
						
						
						else if (String.class == typeFieldClass) {
							if (hasCLOBDbTypeAnnotation(beanField)) {
								CLOB cl = ((CLOB) attributes[i]);
								try {
									if (!cl.isEmptyLob()) {
										Reader r = cl.characterStreamValue();
										StringBuffer sb = new StringBuffer();
										int nchars = 0;
										char[] buffer = new char[10];
										while ((nchars = r.read(buffer)) != -1) {
											sb.append(buffer, 0, nchars);
										}
										r.close();
										meth.invoke(bean, sb.toString());
									}
								} catch (SQLException e) {
									throw new RuntimeException(e.getMessage(),
											e);
								} catch (IOException e) {
									throw new RuntimeException(e.getMessage(),
											e);
								} catch (IllegalAccessException e) {
									throw new RuntimeException(e.getMessage(),
											e);
								} catch (InvocationTargetException e) {
									throw new RuntimeException(e.getMessage(),
											e);
								}
							} else {
								try {
									meth.invoke(bean,
											((CHAR) attributes[i]).getString());
								} catch (IllegalAccessException e) {
									throw new RuntimeException(e.getMessage(),
											e);
								} catch (InvocationTargetException e) {
									throw new RuntimeException(e.getMessage(),
											e);
								} catch (SQLException e) {
									throw new RuntimeException(e.getMessage(),
											e);
								}
							}
						} else if (Date.class.isAssignableFrom(typeFieldClass)) {
							try {
								meth.invoke(bean,
										((DATE) attributes[i]).timestampValue());
							} catch (IllegalAccessException e) {
								throw new RuntimeException(e.getMessage(), e);
							} catch (InvocationTargetException e) {
								throw new RuntimeException(e.getMessage(), e);
							}
						} 
						else if (Boolean.class == typeFieldClass
								|| Boolean.TYPE == typeFieldClass) {
							try {
								//Convert an Oracle Number to a Java boolean. A zero value translates to false and non-zero values translate to true,
								meth.invoke(bean, ((NUMBER) attributes[i]).booleanValue());
							} 
							catch (IllegalAccessException e) {
								throw new RuntimeException(e.getMessage(), e);
							} 
							catch (InvocationTargetException e) {
								throw new RuntimeException(e.getMessage(), e);
							}
						} 
						else if (Collection.class
								.isAssignableFrom(typeFieldClass)) {

							Type genericFieldType = beanField.getGenericType();
							ParameterizedType aType = (ParameterizedType) genericFieldType;
							Type[] fieldArgTypes = aType
									.getActualTypeArguments();
							Class<?> fieldArgClass = (Class<?>) fieldArgTypes[0];
							try {
								meth.invoke(
										bean,
										createParameterizedCollection(
												typeFieldClass, fieldArgClass,
												attributes[i]));
							} catch (IllegalAccessException e) {
								throw new RuntimeException(e.getMessage(), e);
							} catch (InvocationTargetException e) {
								throw new RuntimeException(e.getMessage(), e);
							}
						} else {
							try {
								meth.invoke(
										bean,
										toBean(typeFieldClass,
												(STRUCT) attributes[i]));
							} catch (IllegalAccessException e) {
								throw new RuntimeException(e.getMessage(), e);
							} catch (InvocationTargetException e) {
								throw new RuntimeException(e.getMessage(), e);
							}
						}
					}
				}
			}
		}

		return bean;
	}

	private static boolean hasCLOBDbTypeAnnotation(Field beanField) {
		Annotation dbStringTypeAnnotation = beanField
				.getAnnotation(DBType.class);
		if (null != dbStringTypeAnnotation
				&& "CLOB".equals(((DBType) dbStringTypeAnnotation).value())) {
			return true;
		}
		return false;
	}

	private static boolean hasTriStatesDbTypeAnnotation(Field beanField) {
		Annotation dbStringTypeAnnotation = beanField
				.getAnnotation(DBType.class);
		if (null != dbStringTypeAnnotation
				&& "TRISTATES"
						.equals(((DBType) dbStringTypeAnnotation).value())) {
			return true;
		}
		return false;
	}

	/**
	 * Converte um {@link oracle.sql.ARRAY} em um {@link ArrayList} java de
	 * pojos anotados com {@link DBType}
	 * 
	 * @param c
	 *            A classe do pojo anotada com {@link DBType} correspondente aos
	 *            elementos do ARRAY passado como par&acirc;metro
	 * @param array
	 *            O {@link oracle.sql.ARRAY} retornado da procedure ou function
	 *            no Oracle
	 * @return Um {@link ArrayList} de pojos java populados
	 */
	public static <T> List<T> toList(Class<T> c, ARRAY array) {
		return (List<T>) createParameterizedCollection(List.class, c, array);
	}

	/**
	 * Converte um {@link oracle.sql.ARRAY} em um {@link HashSet} java de pojos
	 * anotados com {@link DBType}
	 * 
	 * @param c
	 *            A classe do pojo anotada com {@link DBType} correspondente aos
	 *            elementos do ARRAY passado como par&acirc;metro
	 * @param array
	 *            O {@link oracle.sql.ARRAY} retornado da procedure ou function
	 *            no Oracle
	 * @return Um {@link HashSet} de pojos java populados
	 */
	public static <T> Set<T> toSet(Class<T> c, ARRAY array) {
		return (Set<T>) createParameterizedCollection(Set.class, c, array);
	}

	/**
	 * Retorna o valor preenchido na anota&ccedil;&atilde;o {@link DBType} da
	 * classe passada como parametro.
	 * 
	 * @param klass
	 *            Uma classe anotada com {@link DBType}
	 * @return O valor preenchido na anota&ccedil;&atilde;o
	 *         <code>DBType(&quot;valor&quot;)</code>
	 */
	public static String getDBTypeAnnotation(Class<?> klass) {
		Annotation dbTypeAnnotation = klass.getAnnotation(DBType.class);

		if (null == dbTypeAnnotation) {
			String msg = "A classe do objeto: " + klass.getSimpleName()
					+ " não possui a anotação: " + DBType.class
					+ " com o tipo equivalente do Banco.";
			throw new RuntimeException(msg);
		}
		return ((DBType) dbTypeAnnotation).value();
	}

	private static List<Field> getFieldsInType(Class<?> klass) {
		Field[] fieldlist = klass.getDeclaredFields();
		List<Field> listaRetorno = new ArrayList<Field>();

		for (Field field : fieldlist) {
			if (null == field.getAnnotation(NotInType.class)) {
				listaRetorno.add(field);
			}
		}
		return listaRetorno;
	}

	private static String prepareFieldName(Field field) {
		int tam = field.getName().length();
		String p = field.getName().substring(0, 1).toUpperCase();
		String resto = field.getName().substring(1, tam);
		return p + resto;
	}

	@SuppressWarnings("unchecked")
	private static <T> Collection<T> createParameterizedCollection(
			Class<?> collectionClass, Class<T> parameterizedClass,
			Object oraArray) {
		Collection<T> result = null;

		if (null != oraArray) {
			if (List.class.isAssignableFrom(collectionClass)) {
				result = new ArrayList<T>();
			} else if (Set.class.isAssignableFrom(collectionClass)) {
				result = new HashSet<T>();
			} else {
				String msg = "Somente há suporte para atributos do tipo List(ArrayList) ou Set(HashSet)";
				throw new UnsupportedOperationException(msg);
			}

			ARRAY listaArray = (ARRAY) oraArray;

			if (String.class == parameterizedClass
					|| Number.class.isAssignableFrom(parameterizedClass)) {
				T[] lista;
				try {
					lista = (T[]) listaArray.getArray();
				} catch (SQLException e) {
					throw new RuntimeException(e.getMessage(), e);
				}
				for (T t : lista) {
					result.add(t);
				}
			} else {
				Datum[] datums;
				try {
					datums = listaArray.getOracleArray();
				} catch (SQLException e) {
					throw new RuntimeException(e.getMessage(), e);
				}
				for (int j = 0; j < datums.length; j++) {
					result.add(toBean(parameterizedClass, (STRUCT) datums[j]));
				}
			}
		}

		return result;
	}

}
