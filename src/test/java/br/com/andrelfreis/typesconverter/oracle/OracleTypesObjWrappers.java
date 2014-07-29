package br.com.andrelfreis.typesconverter.oracle;

import java.math.BigDecimal;
import java.text.SimpleDateFormat;
import java.util.Date;

import br.com.andrelfreis.typesconverter.annotation.DBType;
import br.com.andrelfreis.typesconverter.annotation.NotInType;
import br.com.andrelfreis.typesconverter.annotation.SendNull;

@DBType("TESTE_ORACLETYPES_OBJ")
public class OracleTypesObjWrappers {
	
	private Byte byte_j;
	private Short short_j;
	private Integer int_j;
	private Long long_j;
	private Float float_j;
	private Double double_j;
	private BigDecimal bigDecimal_j;
	private String string_j;
	private Date date_j;
	private Boolean boolean_j;
	@DBType("CLOB")
	private String clob_j;
	@NotInType
	private Integer atributoNaoTemNoTypeOracle;
	@SendNull
	private String sempreEnviaNullParaOracle = "bla bla bla sempre enviará null";
	
	
	public Byte getByte_j() {
		return byte_j;
	}
	public void setByte_j(Byte byte_j) {
		this.byte_j = byte_j;
	}
	public Short getShort_j() {
		return short_j;
	}
	public void setShort_j(Short short_j) {
		this.short_j = short_j;
	}
	public Integer getInt_j() {
		return int_j;
	}
	public void setInt_j(Integer int_j) {
		this.int_j = int_j;
	}
	public Long getLong_j() {
		return long_j;
	}
	public void setLong_j(Long long_j) {
		this.long_j = long_j;
	}
	public Float getFloat_j() {
		return float_j;
	}
	public void setFloat_j(Float float_j) {
		this.float_j = float_j;
	}
	public Double getDouble_j() {
		return double_j;
	}
	public void setDouble_j(Double double_j) {
		this.double_j = double_j;
	}
	public BigDecimal getBigDecimal_j() {
		return bigDecimal_j;
	}
	public void setBigDecimal_j(BigDecimal bigDecimal_j) {
		this.bigDecimal_j = bigDecimal_j;
	}
	public String getString_j() {
		return string_j;
	}
	public void setString_j(String string_j) {
		this.string_j = string_j;
	}
	public Date getDate_j() {
		return date_j;
	}
	public void setDate_j(Date date_j) {
		this.date_j = date_j;
	}
	public Boolean getBoolean_j() {
		return boolean_j;
	}
	public void setBoolean_j(Boolean boolean_j) {
		this.boolean_j = boolean_j;
	}
	public String getClob_j() {
		return clob_j;
	}
	public void setClob_j(String clob_j) {
		this.clob_j = clob_j;
	}
	public Integer getAtributoNaoTemNoTypeOracle() {
		return atributoNaoTemNoTypeOracle;
	}
	public void setAtributoNaoTemNoTypeOracle(Integer atributoNaoTemNoTypeOracle) {
		this.atributoNaoTemNoTypeOracle = atributoNaoTemNoTypeOracle;
	}
	public String getSempreEnviaNullParaOracle() {
		return sempreEnviaNullParaOracle;
	}
	public void setSempreEnviaNullParaOracle(String sempreEnviaNullParaOracle) {
		this.sempreEnviaNullParaOracle = sempreEnviaNullParaOracle;
	}   
	

	@Override
	public String toString() {
        SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss");
        String date_s = sdf.format(date_j);
        
		return "OracleTypesObjWrappers [byte_j=" + byte_j + ", short_j="
				+ short_j + ", int_j=" + int_j + ", long_j=" + long_j
				+ ", float_j=" + float_j + ", double_j=" + double_j
				+ ", bigDecimal_j=" + bigDecimal_j + ", string_j=" + string_j
				+ ", date_j=" + date_s + ", boolean_j=" + boolean_j
				+ ", clob_j=" + clob_j + ", sempreEnviaNullParaOracle="
				+ sempreEnviaNullParaOracle + "]";
	}
	
		
}
