package myWordCooccurence;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;

import org.apache.hadoop.io.WritableComparable;

public class TextStripe implements WritableComparable<Object> {

        /**
         * the first field
         */
		HashMap <String, Integer> map;

        public TextStripe() {
        	map  = new HashMap<String, Integer>();
        }

        public TextStripe(HashMap<String, Integer> t2) {
        	map = t2;
        }
        
        /**
         * set the first Text
         * @param t1
         */
        public void addText(String t1, int count) {
                if(map.containsKey(t1)) {
                	int val = map.get(t1);
                	map.put( t1, val+count);
                }
                else {
                	map.put(t1, count);
                }
        }
        
        public void write(DataOutput out) throws IOException 
        {
        		out.writeUTF(map.toString());
        }

        public void readFields(DataInput in) throws IOException {
        	map.clear();
        	String stripe		= in.readUTF();
        	List<String> tokens 	= MyTokenizer.ParseStripe(stripe);
        	int noOftokens 		= tokens.size();
        	for(int i=0 ; i<noOftokens ; i+=2) {
    			addText(tokens.get(i), Integer.parseInt(tokens.get(i+1)));
    		}  
        }

		@Override
		public int compareTo(Object object) {
			if(map.equals(object))
				return 0;
			else
				return 1;
		}
}

