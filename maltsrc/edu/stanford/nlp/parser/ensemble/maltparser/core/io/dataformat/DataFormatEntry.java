package edu.stanford.nlp.parser.ensemble.maltparser.core.io.dataformat;
/**
 *  DataFormatEntry is used for storing information about one column in a data format. 
 *
 * @author Johan Hall
 * @since 1.0
**/
public class DataFormatEntry implements Comparable<DataFormatEntry>{
	/** Column position from 0 to n-1, where n is number of columns in the data format. */
	private int position;
	/** Column name */
	private String dataFormatEntryName;
	/** Column category (INPUT, HEAD or OUTPUT) */
	private String category;
	/** Column type (STRING, INTEGER, BOOLEAN, ECHO or IGNORE) */
	private String type;
	/** Default output for a ignore column */
	private String defaultOutput;
	/** Cache the hash code for the data format entry */
	private int cachedHash;
	/**
	 * Creates a data format entry
	 * 
	 * @param position	column position
	 * @param dataFormatEntryName column name
	 * @param category	column category
	 * @param type	column type
	 * @param defaultOutput	default output for a ignore column
	 */
	public DataFormatEntry(int position, String dataFormatEntryName, String category, String type, String defaultOutput) {
		setPosition(position);
		setDataFormatEntryName(dataFormatEntryName);
		setCategory(category);
		setType(type);
		setDefaultOutput(defaultOutput);
	}
	
	/**
	 * Returns the column position
	 * 
	 * @return the column position
	 */
	public int getPosition() {
		return position;
	}

	/**
	 * Sets the column position
	 * 
	 * @param position	column position
	 */
	public void setPosition(int position) {
		this.position = position;
	}

	/**
	 * Returns the column name
	 * @return	the column name
	 */
	public String getDataFormatEntryName() {
		return dataFormatEntryName;
	}

	/**
	 * Sets the column name
	 * @param dataFormatEntryName	column name
	 */
	public void setDataFormatEntryName(String dataFormatEntryName) {
		this.dataFormatEntryName = dataFormatEntryName.toUpperCase();
	}

	/**
	 * Returns the column category
	 * 
	 * @return	the column category
	 */
	public String getCategory() {
		return category;
	}

	/**
	 * Sets the column category
	 * 
	 * @param category	the column category
	 */
	public void setCategory(String category) {
		this.category = category.toUpperCase();
	}

	/**
	 * Return the column type
	 * 
	 * @return	the column type
	 */
	public String getType() {
		return type;
	}

	/**
	 * Sets the column type
	 * 
	 * @param type	the column type
	 */
	public void setType(String type) {
		this.type = type.toUpperCase();
	}

	/* (non-Javadoc)
	 * @see java.lang.Comparable#compareTo(java.lang.Object)
	 */
	public int compareTo(DataFormatEntry obj) {
		if (this.position == obj.position) {
			return 0;
		} else if (this.position < obj.position) {
			return -1;
		} else {
			return 1;
		}
	}

	/**
	 * Returns the default output of an ignore column
	 * 
	 * @return the default output of an ignore column
	 */
	public String getDefaultOutput() {
		return defaultOutput;
	}

	/**
	 * Sets the default output of an ignore column
	 * 
	 * @param defaultOutput  the default output of an ignore column
	 */
	public void setDefaultOutput(String defaultOutput) {
		this.defaultOutput = defaultOutput;
	}
	
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		DataFormatEntry objC = (DataFormatEntry)obj;
		return ((dataFormatEntryName == null) ? objC.dataFormatEntryName == null : dataFormatEntryName.equals(objC.dataFormatEntryName)) &&
				((type == null) ? objC.type == null : type.equals(objC.type)) &&
				((category == null) ? objC.category == null : category.equals(objC.category)) && 
				((defaultOutput == null) ? objC.defaultOutput == null : defaultOutput.equals(objC.defaultOutput));
	}

	public int hashCode() {
		if (cachedHash == 0) {
			int hash = 7;
			hash = 31 * hash + (null == dataFormatEntryName ? 0 : dataFormatEntryName.hashCode());
			hash = 31 * hash + (null == type ? 0 : type.hashCode());
			hash = 31 * hash + (null == category ? 0 : category.hashCode());
			hash = 31 * hash + (null == defaultOutput ? 0 : defaultOutput.hashCode());
			cachedHash = hash;
		}
		return cachedHash;
	}
	
	public String toString() {
		final StringBuilder sb = new StringBuilder();
		sb.append(position);
		sb.append("\t");
		sb.append(dataFormatEntryName);
		sb.append("\t");
		sb.append(category);
		sb.append("\t");
		sb.append(type);
		if (defaultOutput != null) {
			sb.append("\t");
			sb.append(defaultOutput);
		}
		return sb.toString();
	}
}
