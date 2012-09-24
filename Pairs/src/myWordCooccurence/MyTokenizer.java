package myWordCooccurence;

import java.util.ArrayList;
import java.util.List;

public class MyTokenizer {
	
	static String stopWords	= "a,able,about,across,after,all,almost,also,am,among,an,and," +
			  			  	  "any,are,as,at,be,because,been,but,by,can,cannot,could,dear," +
			  			  	  "did,do,does,either,else,ever,every,for,from,get,got,had,has," +
			  			  	  "have,he,her,hers,him,his,how,however,i,if,in,into,is,it,its," +
			  			  	  "just,least,let,like,likely,may,me,might,most,must,my,neither," +
			  			  	  "no,nor,not,of,off,often,on,only,or,other,our,own,rather,said," +
			  			  	  "say,says,she,should,since,so,some,than,that,the,their,them,then," +
			  			  	  "there,these,they,this,tis,to,too,twas,us,wants,was,we,were,what," +
			  			  	  "when,where,which,while,who,whom,why,will,with,would,yet,you,your";
	
	static List<String> GetValidTokens(String data) {
		data = data.toLowerCase();
		List<String> tokens 	= new ArrayList<String>();
		String [] strArr 		= data.split("[^a-zA-Z]");
		
		for(String str : strArr) {
			
			if(!str.equals("")) {
				if(stopWords.indexOf(str) == -1) {
					tokens.add(str);
				}
			}
		}
		
		return tokens;
	}
}
