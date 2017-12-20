package eu.fbk.textpro.ml;

import java.io.IOException;
import java.io.InvalidObjectException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.ObjectStreamException;
import java.io.Serializable;
import java.util.LinkedList;
import java.util.Objects;

public class feature_node implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = 2603060341130442061L;
	LinkedList<Integer> tokenpos = new LinkedList<Integer>();
	 LinkedList<Integer> colpos = new LinkedList<Integer>();
	 boolean last_feature_not_known=false;
	 public feature_node(){}
	 @Override
	    public boolean equals(Object o) {

	        if (o == this) return true;
	        if ((o instanceof feature_node)) {
	            return true;
	        }
	        return false;
	    }

	 @Override
	    public int hashCode() {
	        return Objects.hash(tokenpos,colpos,last_feature_not_known);

	    }
	 /**
	    * Serialize this instance.
	    * 
	    * @param out Target to which this instance is written.
	    * @throws IOException Thrown if exception occurs during serialization.
	    */
//	   private void writeObject(final ObjectOutputStream out) throws IOException
//	   {
//	      out.writeObject(this.tokenpos);
//	      out.writeObject(this.colpos);
//	      out.writeObject(this.last_feature_not_known);
//	   }
	 
	   /**
	    * Deserialize this instance from input stream.
	    * 
	    * @param in Input Stream from which this instance is to be deserialized.
	    * @throws IOException Thrown if error occurs in deserialization.
	    * @throws ClassNotFoundException Thrown if expected class is not found.
	    */
//	   private void readObject(final ObjectInputStream in) throws IOException, ClassNotFoundException
//	   {
//	      this.tokenpos = (LinkedList<Integer>) in.readObject();
//	      this.colpos = (LinkedList<Integer>) in.readObject();
//	      this.last_feature_not_known = (boolean) in.readObject();
//
//	   }
//	   private void readObjectNoData() throws ObjectStreamException
//	   {
//	      throw new InvalidObjectException("Stream data required");
//	   }

}
