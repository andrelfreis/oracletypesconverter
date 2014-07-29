package br.com.andrelfreis.typesconverter.oracle;

import java.math.BigDecimal;
import java.text.SimpleDateFormat;
import java.util.Date;

import br.com.andrelfreis.typesconverter.annotation.DBType;
import br.com.andrelfreis.typesconverter.annotation.NotInType;
import br.com.andrelfreis.typesconverter.annotation.SendNull;

@DBType("TESTE_ORACLETYPES_OBJ")
public class OracleTypesObjPrimitivos {
	
	private byte byte_j; // -128 a 127
	private short short_j; // -32.768 a 32.767
	private int int_j; // -2,147,483,648 a 2,147,483,647
	private long long_j; // -9,223,372,036,854,775,808 a 9,223,372,036,854,775,807
	private float float_j;
	private double double_j;
	private BigDecimal bigDecimal_j;
	private String string_j;
	private Date date_j;
	private boolean boolean_j;
	@DBType("CLOB")
	private String clob_j;
	@NotInType
	private int atributoNaoTemNoTypeOracle;
	@SendNull
	private String sempreEnviaNullParaOracle = "bla bla bla sempre enviará null";
	

	
	public byte getByte_j() {
		return byte_j;
	}
	public void setByte_j(byte byte_j) {
		this.byte_j = byte_j;
	}
	public short getShort_j() {
		return short_j;
	}
	public void setShort_j(short short_j) {
		this.short_j = short_j;
	}
	public int getInt_j() {
		return int_j;
	}
	public void setInt_j(int int_j) {
		this.int_j = int_j;
	}
	public long getLong_j() {
		return long_j;
	}
	public void setLong_j(long long_j) {
		this.long_j = long_j;
	}
	public float getFloat_j() {
		return float_j;
	}
	public void setFloat_j(float float_j) {
		this.float_j = float_j;
	}
	public double getDouble_j() {
		return double_j;
	}
	public void setDouble_j(double double_j) {
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
	public boolean isBoolean_j() {
		return boolean_j;
	}
	public void setBoolean_j(boolean boolean_j) {
		this.boolean_j = boolean_j;
	}
	public String getClob_j() {
		return clob_j;
	}
	public void setClob_j(String clob_j) {
		this.clob_j = clob_j;
	}
	public int getAtributoNaoTemNoTypeOracle() {
		return atributoNaoTemNoTypeOracle;
	}
	public void setAtributoNaoTemNoTypeOracle(int atributoNaoTemNoTypeOracle) {
		this.atributoNaoTemNoTypeOracle = atributoNaoTemNoTypeOracle;
	}
	public String getSempreEnviaNullParaOracle() {
		return sempreEnviaNullParaOracle;
	}
	public void setSempreEnviaNullParaOracle(String sempreEnviaNullParaOracle) {
		this.sempreEnviaNullParaOracle = sempreEnviaNullParaOracle;
	}   
	
	
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result
				+ ((bigDecimal_j == null) ? 0 : bigDecimal_j.hashCode());
		result = prime * result + (boolean_j ? 1231 : 1237);
		result = prime * result + byte_j;
		result = prime * result + ((clob_j == null) ? 0 : clob_j.hashCode());
		result = prime * result + ((date_j == null) ? 0 : date_j.hashCode());
		long temp;
		temp = Double.doubleToLongBits(double_j);
		result = prime * result + (int) (temp ^ (temp >>> 32));
		result = prime * result + Float.floatToIntBits(float_j);
		result = prime * result + int_j;
		result = prime * result + (int) (long_j ^ (long_j >>> 32));
		result = prime * result + short_j;
		result = prime * result
				+ ((string_j == null) ? 0 : string_j.hashCode());
		return result;
	}
	
	
	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		
		OracleTypesObjPrimitivos other = (OracleTypesObjPrimitivos) obj;
		
		if (bigDecimal_j == null) {
			if (other.getBigDecimal_j() != null)
				return false;
		} 
		else if (!bigDecimal_j.equals(other.getBigDecimal_j()))
			return false;
		
		if (boolean_j != other.isBoolean_j())
			return false;
		
		if (byte_j != other.getByte_j())
			return false;
		
		if (clob_j == null) {
			if (other.getClob_j() != null)
				return false;			
		} else if (!clob_j.equals(other.getClob_j()))
			return false;
		
		if (date_j == null) {
			if (other.getDate_j() != null)
				return false;
		} else if (!date_j.equals(other.getDate_j()))
			return false;
		
		if (Double.doubleToLongBits(double_j) != Double
				.doubleToLongBits(other.getDouble_j()))
			return false;
		
		if (Float.floatToIntBits(float_j) != Float
				.floatToIntBits(other.getFloat_j()))
			return false;
		
		if (int_j != other.getInt_j())
			return false;
		
		if (long_j != other.getLong_j())
			return false;
		
		if (short_j != other.getShort_j())
			return false;
		
		if (string_j == null) {
			if (other.getString_j() != null)
				return false;			
		} else if (!string_j.equals(other.getString_j()))
			return false;
		return true;
	}
	
	
	@Override
	public String toString() {
		SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss");
        String date_s = sdf.format(date_j);
		
		return "OracleTypesObjPrimitivos [byte_j=" + byte_j + ", short_j="
				+ short_j + ", int_j=" + int_j + ", long_j=" + long_j
				+ ", float_j=" + float_j + ", double_j=" + double_j
				+ ", bigDecimal_j=" + bigDecimal_j + ", string_j=" + string_j
				+ ", date_j=" + date_s + ", boolean_j=" + boolean_j
				+ ", clob_j=" + clob_j + ", sempreEnviaNullParaOracle="
				+ sempreEnviaNullParaOracle + "]";
	}
	
	
	
}
