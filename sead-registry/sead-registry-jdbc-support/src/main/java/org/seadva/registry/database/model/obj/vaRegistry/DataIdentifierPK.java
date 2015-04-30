package org.seadva.registry.database.model.obj.vaRegistry;


import java.io.Serializable;

import com.google.gson.annotations.Expose;
import org.seadva.registry.database.model.obj.vaRegistry.iface.IDataIdentifierPK;


/** 
 * Object mapping for hibernate-handled table: data_identifier.
 * @author autogenerated
 */


public class DataIdentifierPK implements  IDataIdentifierPK {

	/** Serial Version UID. */
	private static final long serialVersionUID = -559002646L;

	

	/** Field mapping. */
    @Expose

	private DataIdentifierType dataIdentifierType;

	/** Field mapping. */
	private BaseEntity entity;

 


 
	/** Return the type of this class. Useful for when dealing with proxies.
	* @return Defining class.
	*/

	public Class<?> getClassType() {
		return DataIdentifierPK.class;
	}
 

    /**
     * Return the value associated with the column: dataIdentifierType.
	 * @return A DataIdentifierType object (this.dataIdentifierType)
	 */
	public DataIdentifierType getDataIdentifierType() {
		return this.dataIdentifierType;
		
	}
	

  
    /**  
     * Set the value related to the column: dataIdentifierType.
	 * @param dataIdentifierType the dataIdentifierType value you wish to set
	 */
	public void setDataIdentifierType(final DataIdentifierType dataIdentifierType) {
		this.dataIdentifierType = dataIdentifierType;
	}

    /**
     * Return the value associated with the column: entity.
	 * @return A BaseEntity object (this.entity)
	 */
	public BaseEntity getEntity() {
		return this.entity;
		
	}
	

  
    /**  
     * Set the value related to the column: entity.
	 * @param entity the entity value you wish to set
	 */
	public void setEntity(final BaseEntity entity) {
		this.entity = entity;
	}


   /**
    * Deep copy.
	* @return cloned object
	* @throws CloneNotSupportedException on error
    */
    @Override
    public DataIdentifierPK clone() throws CloneNotSupportedException {
		
        final DataIdentifierPK copy = (DataIdentifierPK)super.clone();

		return copy;
	}
	


	/** Provides toString implementation.
	 * @see Object#toString()
	 * @return String representation of this class.
	 */
	@Override
	public String toString() {
		StringBuffer sb = new StringBuffer();
		
		return sb.toString();		
	}


	/** Equals implementation. 
	 * @see Object#equals(Object)
	 * @param aThat Object to compare with
	 * @return true/false
	 */
	@Override
	public boolean equals(final Object aThat) {
		Object proxyThat = aThat;
		
		if ( this == aThat ) {
			 return true;
		}

		if (aThat == null)  {
			 return false;
		}
		
		final DataIdentifierPK that; 
		try {
			that = (DataIdentifierPK) proxyThat;
			if ( !(that.getClassType().equals(this.getClassType()))){
				return false;
			}
		} catch (org.hibernate.ObjectNotFoundException e) {
				return false;
		} catch (ClassCastException e) {
				return false;
		}
		
		
		boolean result = true;
		result = result && (((getDataIdentifierType() == null) && (that.getDataIdentifierType() == null)) || (getDataIdentifierType() != null && getDataIdentifierType().getId().equals(that.getDataIdentifierType().getId())));	
		result = result && (((getEntity() == null) && (that.getEntity() == null)) || (getEntity() != null && getEntity().getId().equals(that.getEntity().getId())));	
		return result;
	}
	
	/** Calculate the hashcode.
	 * @see Object#hashCode()
	 * @return a calculated number
	 */
	@Override
	public int hashCode() {
	int hash = 0;
		hash = hash + getDataIdentifierType().hashCode();
//		hash = hash + getEntity().hashCode();
	return hash;
	}
	

	
}