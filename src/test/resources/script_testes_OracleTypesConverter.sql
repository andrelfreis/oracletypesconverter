/**
 * Criar os seguintes tipos e a package no Oracle, antes de rodar os testes unitários
 */

create or replace 
TYPE TESTE_ORACLETYPES_OBJ AS OBJECT (

  BYTE_J NUMBER(3) -- Byte ou byte no java, até 127
  , SHORT_J NUMBER(5) -- Short ou short no java, até 32767
  , INT_J NUMBER(10) -- Integer ou int no java, até 2147483647
  , LONG_J NUMBER(19) -- Long ou long no java, até 9223372036854775807l
  , FLOAT_J NUMBER(4,2) -- Float ou float no java
  , DOUBLE_J NUMBER(8,4) -- Double ou double no java
  , BIGDECIMAL_J NUMBER(17,2) -- BigDecimal no java, para dinheiro
  , TEXTO VARCHAR2(30) -- String no java
  , DATE_J DATE -- java.util.Date/Timestamp no java
  , BOOLEAN_J NUMBER(1) -- Boolean ou boolean no java
  , CLOB_J CLOB -- String no java
  , RECEBE_NULL VARCHAR2(55) -- String anotada com @SendNull no java  
  
);


create or replace 
TYPE TESTE_ORACLETYPES_OBJS AS TABLE OF TESTE_ORACLETYPES_OBJ;

-- Grant execute para o usuário que irá logar e testar
GRANT EXECUTE ON TESTE_ORACLETYPES_OBJ TO <USUARIO>;
GRANT EXECUTE ON TESTE_ORACLETYPES_OBJS TO <USUARIO>;

/**
 * PACKAGE SPEC 
 */
create or replace 
PACKAGE PK_TESTE_ORACLETYPESCONVERTER AS 

  FUNCTION FC_TESTAR_ENVIO_OBJ(OBJ_JAVA TESTE_ORACLETYPES_OBJ, EMPTY_CLOB_REF OUT CLOB)
  RETURN VARCHAR2;
  
  FUNCTION FC_TESTAR_RETORNO_OBJ
  RETURN TESTE_ORACLETYPES_OBJ;
  
  FUNCTION FC_TESTAR_ENVIO_LISTA(LISTA_JAVA TESTE_ORACLETYPES_OBJS)
  RETURN TESTE_ORACLETYPES_OBJS;
  

END PK_TESTE_ORACLETYPESCONVERTER;





/**
 * PACKAGE BODY 
 */
create or replace 
PACKAGE BODY PK_TESTE_ORACLETYPESCONVERTER AS 

  ---------------------------------------------------
  -- PROCEDURE QUE IMPRIME NA TELA E/OU NO ARQUIVO DE LOG
  ---------------------------------------------------     
	PROCEDURE PR_OUTPUT (   
	   WP_MSG VARCHAR2 DEFAULT '--------------------------- # # # ---------------------------',      
	   WP_DISPLAY BOOLEAN DEFAULT TRUE,
	   WP_HORARIO BOOLEAN DEFAULT FALSE
  ) 
  IS 
	   WL_LINHA VARCHAR2(1000); 
		 OUT_FILE_LOG UTL_FILE.FILE_TYPE;
     WL_NM_ARQUIVO_LOG VARCHAR2(50) := 'TESTE_ORACLETYPESCONVERTER_LOG.txt';
     LOG_DIRECTORY VARCHAR2(30) := 'LOG_LOCAL'; 
     
	BEGIN
	
	   IF WP_HORARIO THEN
		    WL_LINHA := SUBSTR(TO_CHAR(SYSDATE, 'DD/MM/YYYY HH24:MI:SS') || '| ' || WP_MSG, 1, 1000);
     ELSE
        WL_LINHA := SUBSTR(WP_MSG, 1, 1000);	
     END IF;	
	
     IF WP_DISPLAY THEN
	      DBMS_OUTPUT.PUT_LINE(SUBSTR(WP_MSG, 1, 250));	
     END IF;
	
  
		 BEGIN        
        IF NOT UTL_FILE.IS_OPEN(OUT_FILE_LOG) THEN
           OUT_FILE_LOG := UTL_FILE.FOPEN(LOG_DIRECTORY, WL_NM_ARQUIVO_LOG, 'a');
        END IF;   
           
        UTL_FILE.PUT_LINE(OUT_FILE_LOG, WL_LINHA);
        UTL_FILE.FFLUSH(OUT_FILE_LOG);
        UTL_FILE.FCLOSE(OUT_FILE_LOG);
    
		 EXCEPTION		
        WHEN UTL_FILE.INVALID_PATH THEN
            RAISE_APPLICATION_ERROR(-20001,
                'INVALID_PATH: Specified path does not exist or is not visible to Oracle.');
                
        WHEN UTL_FILE.INVALID_MODE THEN
            RAISE_APPLICATION_ERROR(-20002,
                'INVALID_MODE: The open_mode parameter in FOPEN was invalid.');
                
        WHEN UTL_FILE.INVALID_FILEHANDLE THEN        
            RAISE_APPLICATION_ERROR(-20003,
                'INVALID_FILEHANDLE: File handle does not exist.');
                
        WHEN UTL_FILE.INVALID_OPERATION THEN        
            RAISE_APPLICATION_ERROR(-20004,
                'INVALID_OPERATION: The file could not be opened or operated on as requested.');
                
        WHEN UTL_FILE.READ_ERROR THEN        
            raise_application_error(-20005,
                'READ_ERROR: An operating system error occurred during the read operation.');
                
        WHEN UTL_FILE.WRITE_ERROR THEN
            RAISE_APPLICATION_ERROR(-20006,
                'WRITE_ERROR: An operating system error occurred during the write operation.');
        
        WHEN UTL_FILE.INTERNAL_ERROR THEN
            raise_application_error(-20007,
                'INTERNAL_ERROR: An unspecified error in PL/SQL.');
     END;
	
   END PR_OUTPUT;


  /* **************************************************************
    TESTAR O ENVIO DE OBJETOS POPULADOS NO JAVA
  ************************************************************** */
  FUNCTION FC_TESTAR_ENVIO_OBJ(OBJ_JAVA IN TESTE_ORACLETYPES_OBJ, EMPTY_CLOB_REF OUT CLOB)
  RETURN VARCHAR2
  IS  
    DADOS_ENVIADOS VARCHAR2(4000);
    EMPTY_CLOB CLOB;
  BEGIN
  
    EMPTY_CLOB_REF := OBJ_JAVA.CLOB_J;
    
    PR_OUTPUT('----------- FC_TESTAR_ENVIO_OBJ -----------');
  
    PR_OUTPUT('BYTE_J = ' || OBJ_JAVA.BYTE_J
                      || ', SHORT_J = ' || OBJ_JAVA.SHORT_J
                      || ', INT_J = ' || OBJ_JAVA.INT_J
                      || ', LONG_J = ' || OBJ_JAVA.LONG_J
                      || ', FLOAT_J = ' || OBJ_JAVA.FLOAT_J
                      || ', DOUBLE_J = ' || OBJ_JAVA.DOUBLE_J
                      || ', BIGDECIMAL_J = ' || OBJ_JAVA.BIGDECIMAL_J
                      || ', TEXTO = ' || OBJ_JAVA.TEXTO
                      || ', DATE_J = ' || TO_CHAR(OBJ_JAVA.DATE_J, 'DD/MM/YYYY HH24:MI:SS')
                      || ', BOOLEAN_J = ' || OBJ_JAVA.BOOLEAN_J    
                      || ', CLOB_J = ' || OBJ_JAVA.CLOB_J    
                      || ', RECEBE_NULL = ' || OBJ_JAVA.RECEBE_NULL   
    );    
    PR_OUTPUT(CHR(13));
    
    
    DADOS_ENVIADOS := 'BYTE_J = ' || OBJ_JAVA.BYTE_J
                      || ', SHORT_J = ' || OBJ_JAVA.SHORT_J
                      || ', INT_J = ' || OBJ_JAVA.INT_J
                      || ', LONG_J = ' || OBJ_JAVA.LONG_J
                      || ', FLOAT_J = ' || OBJ_JAVA.FLOAT_J
                      || ', DOUBLE_J = ' || OBJ_JAVA.DOUBLE_J
                      || ', BIGDECIMAL_J = ' || OBJ_JAVA.BIGDECIMAL_J
                      || ', TEXTO = ' || OBJ_JAVA.TEXTO
                      || ', DATE_J = ' || TO_CHAR(OBJ_JAVA.DATE_J, 'DD/MM/YYYY HH24:MI:SS')
                      || ', BOOLEAN_J = ' || OBJ_JAVA.BOOLEAN_J
                      || ', CLOB_J = ' || OBJ_JAVA.CLOB_J    
                      || ', RECEBE_NULL = ' || OBJ_JAVA.RECEBE_NULL
                      ;
  
    RETURN DADOS_ENVIADOS;
    
  
  END FC_TESTAR_ENVIO_OBJ;
  
  
  
  /* **************************************************************
    TESTAR O RETORNO DE OBJETOS, POPULADOS NO ORACLE
  ************************************************************** */
  FUNCTION FC_TESTAR_RETORNO_OBJ
  RETURN TESTE_ORACLETYPES_OBJ
  AS
  
    OBJ_JAVA TESTE_ORACLETYPES_OBJ;
  
  BEGIN
  
    OBJ_JAVA := TESTE_ORACLETYPES_OBJ(NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL);
    
    OBJ_JAVA.BYTE_J := -128; -- Byte ou byte no java, -128 a 127
    OBJ_JAVA.SHORT_J := 32767; -- Short ou short no java, -32.768 a 32.767
    OBJ_JAVA.INT_J := -2147483648; -- Integer ou int no java, -2,147,483,648 a 2,147,483,647
    OBJ_JAVA.LONG_J := 9223372036854775807; -- Long ou long no java, -9,223,372,036,854,775,808 a 9,223,372,036,854,775,807
    OBJ_JAVA.FLOAT_J := -55.00;
    OBJ_JAVA.DOUBLE_J := 1.9999;
    OBJ_JAVA.BIGDECIMAL_J := -1798.72;
    OBJ_JAVA.TEXTO := 'TEXTO COM TRINTA CARACTERES!!!';
    OBJ_JAVA.DATE_J := TO_DATE('13/05/2013 14:09:18', 'DD/MM/YYYY HH24:MI:SS');
    OBJ_JAVA.BOOLEAN_J := 1;
    OBJ_JAVA.CLOB_J := 'TESTE'; 
    OBJ_JAVA.RECEBE_NULL := 'CAMPO POPULADO PELO ORACLE';
 
    RETURN OBJ_JAVA;
  
  END FC_TESTAR_RETORNO_OBJ;
  
  
  
  
  /* **************************************************************
    TESTAR O ENVIO E O RETORNO DE LISTA DE OBJETOS
  ************************************************************** */
  FUNCTION FC_TESTAR_ENVIO_LISTA(LISTA_JAVA TESTE_ORACLETYPES_OBJS)
  RETURN TESTE_ORACLETYPES_OBJS
  IS
    
    LISTA_RETORNO TESTE_ORACLETYPES_OBJS;
    
  BEGIN
  
    LISTA_RETORNO := TESTE_ORACLETYPES_OBJS();
  
    IF LISTA_JAVA IS NULL OR LISTA_JAVA.COUNT = 0 THEN
      RAISE_APPLICATION_ERROR(-20001, 'Lista null ou vazia!');
    END IF;
    
    PR_OUTPUT('----------- FC_TESTAR_ENVIO_LISTA -----------');
  
    FOR I IN 1 .. LISTA_JAVA.LAST LOOP
    
      PR_OUTPUT('Enviado: ' || I);
      PR_OUTPUT('BYTE_J = ' || LISTA_JAVA(I).BYTE_J
                      || ', SHORT_J = ' || LISTA_JAVA(I).SHORT_J
                      || ', INT_J = ' || LISTA_JAVA(I).INT_J
                      || ', LONG_J = ' || LISTA_JAVA(I).LONG_J
                      || ', FLOAT_J = ' || LISTA_JAVA(I).FLOAT_J
                      || ', DOUBLE_J = ' || LISTA_JAVA(I).DOUBLE_J
                      || ', BIGDECIMAL_J = ' || LISTA_JAVA(I).BIGDECIMAL_J
                      || ', TEXTO = ' || LISTA_JAVA(I).TEXTO
                      || ', DATE_J = ' || TO_CHAR(LISTA_JAVA(I).DATE_J, 'DD/MM/YYYY HH24:MI:SS')
                      || ', BOOLEAN_J = ' || LISTA_JAVA(I).BOOLEAN_J    
                      || ', CLOB_J = ' || LISTA_JAVA(I).CLOB_J    
                      || ', RECEBE_NULL = ' || LISTA_JAVA(I).RECEBE_NULL
      );    
      PR_OUTPUT(CHR(13));
    
    
      LISTA_RETORNO.EXTEND;
      LISTA_RETORNO(LISTA_RETORNO.LAST) := TESTE_ORACLETYPES_OBJ(NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL);
      LISTA_RETORNO(LISTA_RETORNO.LAST).BYTE_J := LISTA_JAVA(I).BYTE_J - I;
      LISTA_RETORNO(LISTA_RETORNO.LAST).SHORT_J := LISTA_JAVA(I).SHORT_J - I;
      LISTA_RETORNO(LISTA_RETORNO.LAST).INT_J := LISTA_JAVA(I).INT_J - I;
      LISTA_RETORNO(LISTA_RETORNO.LAST).LONG_J := LISTA_JAVA(I).LONG_J - I;
      LISTA_RETORNO(LISTA_RETORNO.LAST).FLOAT_J := LISTA_JAVA(I).FLOAT_J - I;
      LISTA_RETORNO(LISTA_RETORNO.LAST).DOUBLE_J := LISTA_JAVA(I).DOUBLE_J - I;
      LISTA_RETORNO(LISTA_RETORNO.LAST).BIGDECIMAL_J := LISTA_JAVA(I).BIGDECIMAL_J - I;
      LISTA_RETORNO(LISTA_RETORNO.LAST).TEXTO := LISTA_JAVA(I).TEXTO || I;
      LISTA_RETORNO(LISTA_RETORNO.LAST).DATE_J := LISTA_JAVA(I).DATE_J - I;
      LISTA_RETORNO(LISTA_RETORNO.LAST).BOOLEAN_J := LISTA_JAVA(I).BOOLEAN_J - I;
      LISTA_RETORNO(LISTA_RETORNO.LAST).CLOB_J := LISTA_JAVA(I).CLOB_J;    
      
      IF LISTA_JAVA(I).RECEBE_NULL IS NULL THEN
        LISTA_RETORNO(LISTA_RETORNO.LAST).RECEBE_NULL := 'POPULADO PELO ORACLE';
      END IF;
      
    
    END LOOP;    
    
    
    FOR J IN 1 .. LISTA_RETORNO.LAST LOOP
    
      PR_OUTPUT('Retornado: ' || J);
      PR_OUTPUT('BYTE_J = ' || LISTA_RETORNO(J).BYTE_J
                      || ', SHORT_J = ' || LISTA_RETORNO(J).SHORT_J
                      || ', INT_J = ' || LISTA_RETORNO(J).INT_J
                      || ', LONG_J = ' || LISTA_RETORNO(J).LONG_J
                      || ', FLOAT_J = ' || LISTA_RETORNO(J).FLOAT_J
                      || ', DOUBLE_J = ' || LISTA_RETORNO(J).DOUBLE_J
                      || ', BIGDECIMAL_J = ' || LISTA_RETORNO(J).BIGDECIMAL_J
                      || ', TEXTO = ' || LISTA_RETORNO(J).TEXTO
                      || ', DATE_J = ' || TO_CHAR(LISTA_RETORNO(J).DATE_J, 'DD/MM/YYYY HH24:MI:SS')
                      || ', BOOLEAN_J = ' || LISTA_RETORNO(J).BOOLEAN_J    
                      || ', CLOB_J = ' || LISTA_RETORNO(J).CLOB_J    
                      || ', RECEBE_NULL = ' || LISTA_RETORNO(J).RECEBE_NULL
      );    
      PR_OUTPUT(CHR(13));
    
    
    END LOOP;
    
    
    RETURN LISTA_RETORNO;
    
  
  END FC_TESTAR_ENVIO_LISTA;

  
  
  

END PK_TESTE_ORACLETYPESCONVERTER;




/**
 * ENTENDENDO NUMBER(PRECISION, SCALE) NO ORACLE:
 * http://docs.oracle.com/cd/B28359_01/server.111/b28318/datatype.htm#CNCPT1832
 * 
 * Para enviar a receber texto a partir da versão 11g do Oracle, verificar se é necessário
 * adicionar a biblioteca: orai18n, devido ao charset AL16UTF16. 
 * Num projeto java maven seria:
 * 
 * 	<dependency> 
 *		<groupId>com.oracle</groupId> 
 *		<artifactId>orai18n</artifactId>
 *		<version>11.2.0.3.0</version>
 *		<scope>test</scope>			 
 * 	</dependency> 
 * 
 */
