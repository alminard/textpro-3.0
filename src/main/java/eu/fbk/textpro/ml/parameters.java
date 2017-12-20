package eu.fbk.textpro.ml;

import java.io.IOException;
import java.io.InvalidObjectException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.ObjectStreamException;
import java.io.Serializable;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Objects;

public class parameters implements Serializable {
	
	private static final long serialVersionUID = 1L;
	public HashMap<String, Integer> indexm = new HashMap<String, Integer>();
	public HashMap<String, Integer> tags = new HashMap<String, Integer>();
	public HashMap<String, Integer> tags_predicted = new HashMap<String, Integer>();
	public LinkedList<feature_node> tags_predicted_features_list = new LinkedList<>();
	
	public int column_size=Integer.MIN_VALUE;
	public String feature;
	public parameters(){}
	
	public int id = 10;
	
	 @Override
	    public boolean equals(Object o) {

	        if (o == this) return true;
	        if ((o instanceof parameters)) {
	            return true;
	        }
	        return false;
	    }

	 @Override
	    public int hashCode() {
	        return Objects.hash(indexm,tags,column_size,feature,tags_predicted_features_list);

	    }
	 /**
	    * Serialize this instance.
	    * 
	    * @param out Target to which this instance is written.
	    * @throws IOException Thrown if exception occurs during serialization.
	    */
	   private void writeObject(final ObjectOutputStream out) throws IOException
	   {
	      out.writeObject(this.indexm);
	      out.writeObject(this.tags);
	      out.writeObject(this.tags_predicted);
	      out.writeObject(this.tags_predicted_features_list);
	      out.writeObject(this.column_size);
	      out.writeObject(this.feature);


	   }
	 
	   /**
	    * Deserialize this instance from input stream.
	    * 
	    * @param in Input Stream from which this instance is to be deserialized.
	    * @throws IOException Thrown if error occurs in deserialization.
	    * @throws ClassNotFoundException Thrown if expected class is not found.
	    */
	@SuppressWarnings("unchecked")
	private void readObject( ObjectInputStream in) throws IOException, ClassNotFoundException
	   {
	      this.indexm = (HashMap<String, Integer>) in.readObject();
	      this.tags = (HashMap<String, Integer>) in.readObject();
	      this.tags_predicted = (HashMap<String, Integer>) in.readObject();
	      this.tags_predicted_features_list = (LinkedList<feature_node>) in.readObject();
	      this.column_size = (int) in.readObject();
	      this.feature = (String) in.readObject();
	   }
	   private void readObjectNoData() throws ObjectStreamException
	   {
	      throw new InvalidObjectException("Stream data required");
	   }
}
